package net.risesoft.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;

import net.risesoft.entity.DataRecentEntity;
import net.risesoft.entity.DataSourceEntity;
import net.risesoft.entity.DataStatisticsEntity;
import net.risesoft.entity.DownloadLogEntity;
import net.risesoft.log.LogLevelEnum;
import net.risesoft.log.annotation.RiseLog;
import net.risesoft.pojo.Y9Page;
import net.risesoft.pojo.Y9Result;
import net.risesoft.repository.DataAssetsRepository;
import net.risesoft.repository.DataSourceRepository;
import net.risesoft.repository.DataStatisticsRepository;
import net.risesoft.repository.DownloadLogRepository;
import net.risesoft.repository.FileInfoRepository;
import net.risesoft.repository.SubscribeRepository;
import net.risesoft.scheduler.DataStatisticsScheduler;
import net.risesoft.service.ApiDataService;
import net.risesoft.service.DataApiOnlineService;
import net.risesoft.service.DataRecentService;
import net.risesoft.service.DataSourceService;
import net.risesoft.util.DataConstant;
import net.risesoft.y9.Y9Context;
import net.risesoft.y9.Y9LoginUserHolder;

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
    private final DownloadLogRepository downloadLogRepository;
    private final DataStatisticsScheduler dataStatisticsScheduler;
    private final DataStatisticsRepository dataStatisticsRepository;
    private final RestTemplate restTemplate;

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

        List<Map<String, Object>> assetsTypeCount = new ArrayList<>();
        // 获取已登记的资产数量
        Map<String, Object> assetsType1 = new HashMap<>();
        assetsType1.put("name", "已登记资产");
        assetsType1.put("value", dataAssetsRepository.countByIsDeletedFalse());
        assetsTypeCount.add(assetsType1);
        // 获取已上架的资产数量
        Map<String, Object> assetsType2 = new HashMap<>();
        assetsType2.put("name", "已上架资产");
        assetsType2.put("value", dataAssetsRepository.countByIsUpTrueAndIsDeletedFalse());
        assetsTypeCount.add(assetsType2);
        // 获取已入库的资产数量
        Map<String, Object> assetsType3 = new HashMap<>();
        assetsType3.put("name", "已入库资产");
        assetsType3.put("value", dataAssetsRepository.countByIsUpFalseAndIsDeletedFalse());
        assetsTypeCount.add(assetsType3);

        map.put("assetsTypeCount", assetsTypeCount);
        return Y9Result.success(map);
    }

    @RiseLog(operationName = "获取接口近7天每天调用成功和失败次数", logLevel = LogLevelEnum.RSLOG)
    @GetMapping("/findApiCallCount")
    public Y9Result<List<Map<String, Object>>> findApiCallCount() {
        return Y9Result.success(apiDataService.getDailyApiCallCount());
    }

    @RiseLog(operationName = "分页获取资产下载日志列表", logLevel = LogLevelEnum.RSLOG)
    @GetMapping("/findDownloadLog")
    public Y9Page<Map<String, Object>> findDownloadLog(Integer page, Integer size) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<DownloadLogEntity> pageList = downloadLogRepository.findAll(pageable);
        // 对返回的接口数据进行赋值与处理
        List<Map<String, Object>> downloadLogsMap = new ArrayList<>();
        for (DownloadLogEntity downloadLog : pageList.getContent()) {
            Map<String, Object> map = new HashMap<>();
            map.put("createTime", sdf.format(downloadLog.getCreateTime()));
            map.put("downloader", downloadLog.getDownloader());
            fileInfoRepository.findById(downloadLog.getFileId()).ifPresent(fileInfo -> {
                map.put("fileName", fileInfo.getName());
            });
            dataAssetsRepository.findById(downloadLog.getAssetsId()).ifPresent(dataAssets -> {
                map.put("assetsName", dataAssets.getName());
            });
            map.put("result", downloadLog.getResult());
            downloadLogsMap.add(map);
        }
        return Y9Page.success(page, pageList.getTotalPages(), pageList.getTotalElements(), downloadLogsMap,
            "获取数据成功");
    }

    @RiseLog(operationName = "获取数据源数据量", logLevel = LogLevelEnum.RSLOG)
    @GetMapping("/findDataSourceDataCount")
    public Y9Result<Map<String, Object>> findDataSourceDataCount() {
        Map<String, Object> dataSourceDataMap = new HashMap<>();
        List<String> sourceNames = new ArrayList<>();// 数据库名称
        List<Integer> dataCounts = new ArrayList<>();// 数据库总量
        List<Integer> todayDataCounts = new ArrayList<>();// 今日增量
        // 获取数据库列表
        List<DataSourceEntity> dataSourceList = dataSourceRepository.findByTenantIdAndType(Y9LoginUserHolder.getTenantId(), 0);
        if (dataSourceList != null && !dataSourceList.isEmpty()) {
            for (DataSourceEntity dataSource : dataSourceList) {
                sourceNames.add(dataSource.getName());
                // 获取当前数据库的数据量
                long dataCount = dataStatisticsScheduler.statisticsData(dataSource);
                dataCounts.add((int) dataCount);
                // 查询昨天的数据量
                DataStatisticsEntity dataStatistics = dataStatisticsRepository.findBySourceIdAndDataTime(dataSource.getId(), DataConstant.getYesterday());
                if (dataStatistics != null) {
                    todayDataCounts.add((int) (dataCount - dataStatistics.getDataCount()));
                } else {
                    todayDataCounts.add((int) dataCount);
                }
            }
        }
        dataSourceDataMap.put("sourceNames", sourceNames);
        dataSourceDataMap.put("dataCounts", dataCounts);
        dataSourceDataMap.put("todayDataCounts", todayDataCounts);
        return Y9Result.success(dataSourceDataMap);
    }

    @RiseLog(operationName = "获取数据推送记录", logLevel = LogLevelEnum.RSLOG)
    @GetMapping("/getDataFlowLog")
    public Y9Page<Map<String, Object>> getDataFlowLog(Integer page, Integer size) {
        String url = Y9Context.getProperty("y9.common.dataflowBaseUrl") + "/services/rest/getDataFlowLog?page=" + page 
        + "&size=" + size + "&systemName=dataassets&tenantId=" + Y9LoginUserHolder.getTenantId();
        try {
            // 调用外部接口
            Y9Page<Map<String, Object>> response = restTemplate.getForObject(url, Y9Page.class);
            
            // 处理返回结果
            List<Map<String, Object>> listMap = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (response != null && response.getSuccess()) {
                List<Map<String, Object>> records = response.getRows();
                for (Map<String, Object> record : records) {
                    Map<String, Object> map = new HashMap<>();
                    // 转换时间戳为可读时间格式
                    Object dispatchTime = record.get("DISPATCH_TIME");
                    Object endTime = record.get("END_TIME");
                    
                    if (dispatchTime != null) {
                        try {
                            if (dispatchTime instanceof Number) {
                                map.put("startTime", sdf.format(new Date(((Number) dispatchTime).longValue())));
                            } else if (dispatchTime instanceof String) {
                                map.put("startTime", dispatchTime.toString());
                            } else {
                                map.put("startTime", "");
                            }
                        } catch (Exception e) {
                            map.put("startTime", dispatchTime.toString());
                        }
                    } else {
                        map.put("startTime", "");
                    }
                    
                    if (endTime != null) {
                        try {
                            if (endTime instanceof Number) {
                                map.put("endTime", sdf.format(new Date(((Number) endTime).longValue())));
                            } else if (endTime instanceof String) {
                                map.put("endTime", endTime.toString());
                            } else {
                                map.put("endTime", "");
                            }
                        } catch (Exception e) {
                            map.put("endTime", endTime.toString());
                        }
                    } else {
                        map.put("endTime", "");
                    }
                    
                    map.put("status", record.get("STATUS"));
                    map.put("message", record.get("LOG_CONSOLE"));
                    // 获取订阅id
                    String subscribeId = record.get("EXTERNALID").toString();
                    subscribeRepository.findById(subscribeId).ifPresent(subscribe -> {
                        map.put("subscribeName", subscribe.getUserName());
                        // 获取资产信息
                        dataAssetsRepository.findById(subscribe.getAssetsId()).ifPresent(dataAssets -> {
                            map.put("assetsName", dataAssets.getName());
                        });
                    });
                    listMap.add(map);
                }
                return Y9Page.success(page, response.getTotalPages(), response.getTotal(), listMap, "获取数据成功");
            }
            return Y9Page.success(page, 0, 0, new ArrayList<>(), "获取数据失败：接口返回数据为空");
        } catch (Exception e) {
            // 异常处理
            return Y9Page.success(page, 0, 0, new ArrayList<>(), "获取数据失败：" + e.getMessage());
        }
    }

}
