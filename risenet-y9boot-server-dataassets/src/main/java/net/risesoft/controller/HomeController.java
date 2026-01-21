package net.risesoft.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import net.risesoft.entity.DataRecentEntity;
import net.risesoft.entity.DataSourceEntity;
import net.risesoft.log.LogLevelEnum;
import net.risesoft.log.annotation.RiseLog;
import net.risesoft.pojo.Y9Page;
import net.risesoft.pojo.Y9Result;
import net.risesoft.repository.DataAssetsRepository;
import net.risesoft.repository.DataSourceRepository;
import net.risesoft.repository.FileInfoRepository;
import net.risesoft.repository.SubscribeRepository;
import net.risesoft.service.ApiDataService;
import net.risesoft.service.DataApiOnlineService;
import net.risesoft.service.DataRecentService;
import net.risesoft.service.DataSourceService;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/vue/home", produces = MediaType.APPLICATION_JSON_VALUE)
public class HomeController {

    private final DataRecentService dataRecentService;
    private final DataSourceService dataSourceService;
    private final DataApiOnlineService dataApiOnlineService;
    private final SubscribeRepository subscribeRepository;
    private final ApiDataService apiDataService;
    private final FileInfoRepository fileInfoRepository;
    private final DataSourceRepository dataSourceRepository;
    private final DataAssetsRepository dataAssetsRepository;

    @RiseLog(operationName = "分页获取最近操作数据列表", logLevel = LogLevelEnum.RSLOG)
    @GetMapping("/findDataRecent")
    public Y9Page<DataRecentEntity> findAll(Integer page, Integer size) {
        Page<DataRecentEntity> pageList = dataRecentService.findAll(page, size);
        return Y9Page.success(page, pageList.getTotalPages(), pageList.getTotalElements(), pageList.getContent(),
            "获取数据成功");
    }

    @RiseLog(operationName = "获取首页统计数据", logLevel = LogLevelEnum.RSLOG)
    @GetMapping("/findHomeStatistics")
    public Y9Result<Map<String, Object>> findHomeStatistics() {
        Map<String, Object> map = new HashMap<>();
        // 获取资产总数量
        map.put("totalAssetCount", dataAssetsRepository.count());
        // 获取数据源数据量和表数量
        List<DataSourceEntity> dataSourceList = dataSourceRepository.findAll();
        if (dataSourceList != null && !dataSourceList.isEmpty()) {
            map.put("totalDataSourceCount", dataSourceList.size());
            int num = 0;
            for (DataSourceEntity dataSource : dataSourceList) {
                num += dataSourceService.getTablePage(dataSource.getId(), "").size();
            }
            map.put("totalTableCount", num);
        } else {
            map.put("totalDataSourceCount", 0);
            map.put("totalTableCount", 0);
        }
        // 获取接口数量
        map.put("totalApiCount", dataApiOnlineService.getApiCount());
        // 获取订阅数量
        map.put("totalSubscribeCount", subscribeRepository.countAll());
        // 获取近7天接口调用次数
        map.put("dailyApiCallCount", apiDataService.getDailyApiCallCount());

        List<Map<String, Object>> dataTypeCount = new ArrayList<>();
        Map<String, Object> dataType1 = new HashMap<>();
        dataType1.put("name", "结构化数据");
        dataType1.put("value", fileInfoRepository.countByFileType("数据库") + fileInfoRepository.countByFileType("数据表"));
        dataTypeCount.add(dataType1);

        Map<String, Object> dataType2 = new HashMap<>();
        dataType2.put("name", "文件类数据");
        dataType2.put("value", fileInfoRepository.countByFileType("文件"));
        dataTypeCount.add(dataType2);

        Map<String, Object> dataType3 = new HashMap<>();
        dataType3.put("name", "接口类数据");
        dataType3.put("value", fileInfoRepository.countByFileType("接口"));
        dataTypeCount.add(dataType3);

        map.put("dataTypeCount", dataTypeCount);
        return Y9Result.success(map);
    }
}
