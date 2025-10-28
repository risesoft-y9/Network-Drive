package net.risesoft.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotBlank;

import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import net.risesoft.api.auth.util.Y9ApiThreadHoder;
import net.risesoft.api.auth.util.Y9SqlUtil;
import net.risesoft.entity.DataSourceEntity;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.ApiDataService;
import net.risesoft.service.DataSourceService;
import net.risesoft.y9.json.Y9JsonUtil;
import net.risesoft.y9public.entity.ApiServiceEntity;

@Validated
@RestController
@RequestMapping(value = "/services/rest", produces = "application/json")
@RequiredArgsConstructor
public class ApiController {

    private final ApiDataService apiDataService;
    private final DataSourceService dataSourceService;

    @GetMapping(value = "/get/{method}/{id}")
    public Y9Result<List<Map<String, Object>>> get(@PathVariable @NotBlank String method,
        @PathVariable @NotBlank String id) {
        try {
            String apiIds = Y9ApiThreadHoder.getApiIds();
            ApiServiceEntity apiServiceEntity = apiDataService.findById(id);
            if (apiServiceEntity != null) {
                if (apiServiceEntity.getApiType() == 1 && (StringUtils.isBlank(apiIds) || !apiIds.contains(id))) {
                    return Y9Result.failure("没有权限");
                }
                // 获取SQL语句
                String sql = apiServiceEntity.getSqlData();
                // 接口参数
                List<Object> args = new ArrayList<Object>();
                Map<String, String> params = Y9ApiThreadHoder.getParams();
                List<Map<String, Object>> paramsMap = Y9JsonUtil.readListOfMap(apiServiceEntity.getParams());
                for (Map<String, Object> map : paramsMap) {
                    // 获取参数名称
                    String name = map.get("name").toString();
                    // 用参数名称获取值
                    String value = params.get(name);
                    args.add(value);
                }
                DataSourceEntity dataSourceEntity =
                    dataSourceService.getDataSourceById(apiServiceEntity.getDataSourceId());
                if (dataSourceEntity != null) {
                    List<Map<String, Object>> listMap =
                        Y9SqlUtil.executeQuery(sql, dataSourceEntity.getUrl(), dataSourceEntity.getUsername(),
                            dataSourceEntity.getPassword(), dataSourceEntity.getDriver(), args.toArray());
                    return Y9Result.success(listMap);
                } else {
                    return Y9Result.failure("接口查不到数据源信息，请联系负责人处理");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Y9Result.failure("程序出错：" + e.getMessage());
        }
        return Y9Result.failure("接口不存在");
    }

    @GetMapping(value = "/getPage/{method}/{id}")
    public Y9Result<List<Map<String, Object>>> getPage(@PathVariable @NotBlank String method,
        @PathVariable @NotBlank String id) {
        try {
            String apiIds = Y9ApiThreadHoder.getApiIds();
            ApiServiceEntity apiServiceEntity = apiDataService.findById(id);
            if (apiServiceEntity != null) {
                int page = 1, size = 10;
                if (apiServiceEntity.getApiType() == 1 && (StringUtils.isBlank(apiIds) || !apiIds.contains(id))) {
                    return Y9Result.failure("没有权限");
                }
                // 获取SQL语句
                String sql = apiServiceEntity.getSqlData();
                // 接口参数
                List<Object> args = new ArrayList<Object>();
                Map<String, String> params = Y9ApiThreadHoder.getParams();
                List<Map<String, Object>> paramsMap = Y9JsonUtil.readListOfMap(apiServiceEntity.getParams());
                for (Map<String, Object> map : paramsMap) {
                    // 获取参数名称
                    String name = map.get("name").toString();
                    // 用参数名称获取值
                    String value = params.get(name);

                    if (name.equals("page")) {
                        page = Integer.parseInt(value);
                    } else if (name.equals("size")) {
                        size = Integer.parseInt(value);
                    } else {
                        args.add(value);
                    }
                }
                args.add(size);
                DataSourceEntity dataSourceEntity =
                    dataSourceService.getDataSourceById(apiServiceEntity.getDataSourceId());
                if (dataSourceEntity != null) {
                    int offset = (page - 1) * size;
                    args.add(offset);
                    List<Map<String, Object>> listMap =
                        Y9SqlUtil.executeQuery(sql, dataSourceEntity.getUrl(), dataSourceEntity.getUsername(),
                            dataSourceEntity.getPassword(), dataSourceEntity.getDriver(), args.toArray());
                    return Y9Result.success(listMap);
                } else {
                    return Y9Result.failure("接口查不到数据源信息，请联系负责人处理");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Y9Result.failure("程序出错：" + e.getMessage());
        }
        return Y9Result.failure("接口不存在");
    }

    @PostMapping(value = "/post/{method}/{id}")
    public Y9Result<Integer> post(@PathVariable @NotBlank String method, @PathVariable @NotBlank String id) {
        try {
            String apiIds = Y9ApiThreadHoder.getApiIds();
            ApiServiceEntity apiServiceEntity = apiDataService.findById(id);
            if (apiServiceEntity != null) {
                if (apiServiceEntity.getApiType() == 1 && (StringUtils.isBlank(apiIds) || !apiIds.contains(id))) {
                    return Y9Result.failure("没有权限");
                }
                // 获取SQL语句
                String sql = apiServiceEntity.getSqlData();
                // 接口参数
                List<Object> args = new ArrayList<Object>();
                Map<String, Object> body = Y9JsonUtil.readHashMap(Y9ApiThreadHoder.getBody());
                List<Map<String, Object>> paramsMap = Y9JsonUtil.readListOfMap(apiServiceEntity.getParams());
                for (Map<String, Object> map : paramsMap) {
                    // 获取参数名称
                    String name = map.get("name").toString();
                    // 用参数名称获取值
                    String value = body.get(name).toString();
                    args.add(value);
                }
                DataSourceEntity dataSourceEntity =
                    dataSourceService.getDataSourceById(apiServiceEntity.getDataSourceId());
                if (dataSourceEntity != null) {
                    int count = Y9SqlUtil.executeUpdate(sql, dataSourceEntity.getUrl(), dataSourceEntity.getUsername(),
                        dataSourceEntity.getPassword(), dataSourceEntity.getDriver(), args.toArray());
                    return Y9Result.successMsg("操作成功，影响行数：" + count);
                } else {
                    return Y9Result.failure("接口查不到数据源信息，请联系负责人处理");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Y9Result.failure("程序出错：" + e.getMessage());
        }
        return Y9Result.failure("接口不存在");
    }

}
