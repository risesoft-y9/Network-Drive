package net.risesoft.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import net.risesoft.entity.DataApiTableEntity;
import net.risesoft.entity.DataSourceEntity;
import net.risesoft.entity.SubscribeEntity;
import net.risesoft.entity.TableForeignKeyEntity;
import net.risesoft.pojo.Y9Result;
import net.risesoft.repository.DataApiTableRepository;
import net.risesoft.repository.SubscribeRepository;
import net.risesoft.repository.TableForeignKeyRepository;
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
    private final DataApiTableRepository dataApiTableRepository;
    private final SubscribeRepository subscribeRepository;
    private final TableForeignKeyRepository tableForeignKeyRepository;

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

    @PostMapping(value = "/search/{dataSourceId}/{tableName}")
    public Y9Result<Map<String, Object>> search(@PathVariable @NotBlank String tableName, @PathVariable @NotBlank String dataSourceId) {
        String owner = Y9ApiThreadHoder.getAppName();
        DataApiTableEntity dataApiTableEntity = dataApiTableRepository.findByTableNameAndDataSourceIdAndOwner(tableName, dataSourceId, owner);
        if (dataApiTableEntity != null) {
            if(dataApiTableEntity.getIsDeleted()) {
                return Y9Result.failure("该接口申请已被禁用");
            }
            // 判断关联的订阅信息状态,如果不是通过状态不允许调用
            SubscribeEntity subscribeEntity = subscribeRepository.findById(dataApiTableEntity.getSubscribeId()).orElse(null);
            if(subscribeEntity == null || !subscribeEntity.getReviewStatus().equals("通过")) {
                return Y9Result.failure("该接口的订阅信息不存在或者未通过审核，不允许调用");
            }

            // 返回字段和查询字段都是List<String>的json数据需要转换成逗号分割的字符串
            String returnFields = Y9JsonUtil.readList(dataApiTableEntity.getReturnFields(), String.class).stream().collect(Collectors.joining(","));
            String queryFields = Y9JsonUtil.readList(dataApiTableEntity.getQueryFields(), String.class).stream().collect(Collectors.joining(","));
            String sql = "SELECT " + returnFields.toUpperCase() + " FROM " + dataApiTableEntity.getTableName().toUpperCase();

            // 接口参数
            List<Object> args = new ArrayList<Object>();
            Map<String, Object> body = Y9JsonUtil.readHashMap(Y9ApiThreadHoder.getBody());

            // 通过查询字段和固定查询参数拼接SQL语句,注意为空时where子句不拼接
            if (StringUtils.isNotBlank(queryFields)) {
                sql += " WHERE ";
                String[] queryFieldsArray = queryFields.split(",");
                for (String queryField : queryFieldsArray) {
                    // 如果参数值为空,则返回提示
                    if (StringUtils.isBlank(body.get(queryField).toString())) {
                        return Y9Result.failure("参数" + queryField + "不能为空");
                    }
                    sql += " " + queryField.toUpperCase() + " = ? AND ";
                    args.add(body.get(queryField));
                }
                // 去掉最后一个AND
                sql = sql.substring(0, sql.length() - 5);
            }
            if (StringUtils.isNotBlank(dataApiTableEntity.getQueryParams())) {
                sql += (StringUtils.isNotBlank(queryFields) ? " AND " : " WHERE ") + dataApiTableEntity.getQueryParams();
            }

            // 增量字段,使用INSTR函数判断是否包含增量字段值
            TableForeignKeyEntity tableForeignKeyEntity = tableForeignKeyRepository.findByTableNameAndDataSourceId(tableName, dataSourceId);
            if (tableForeignKeyEntity != null && StringUtils.isNotBlank(tableForeignKeyEntity.getIncrementField())) {
                // 如果增量字段值为空,则不拼接增量查询条件
                if (StringUtils.isNotBlank(body.get(tableForeignKeyEntity.getIncrementField()).toString())) {
                    sql += (sql.contains(" WHERE ") ? " AND " : " WHERE ") + " INSTR(" + tableForeignKeyEntity.getIncrementField().toUpperCase() + ", ?) > 0";
                    args.add(body.get(tableForeignKeyEntity.getIncrementField()));
                }
            }
            
            // 添加分页参数和排序参数
            // 加offset数据量大的时候查询会导致性能问题,采用存储上一次查询的最后一条数据的排序字段值作为分页查询的起始值
            // 验证sortField是否为合法的列名，防止SQL注入
            String sortField = body.get("sortField").toString();
            if (!isValidColumnName(sortField)) {
                return Y9Result.failure("无效的排序字段");
            }
            Object lastSortValue = body.get("lastSortValue");
            if (lastSortValue != null) {
                sql += (sql.contains(" WHERE ") ? " AND " : " WHERE ") + sortField.toUpperCase() + " > ?";
                args.add(lastSortValue);
            }
            sql += " ORDER BY " + sortField.toUpperCase() + " LIMIT " + body.get("size");

            DataSourceEntity dataSourceEntity =
                dataSourceService.getDataSourceById(dataApiTableEntity.getDataSourceId());
            if (dataSourceEntity != null) {
                List<Map<String, Object>> listMap =
                    Y9SqlUtil.executeQuery(sql, dataSourceEntity.getUrl(), dataSourceEntity.getUsername(),
                        dataSourceEntity.getPassword(), dataSourceEntity.getDriver(), args.toArray());
                // 构建返回结果，包含数据列表和下一页的lastSortValue
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("data", listMap);
                // 如果有数据，获取最后一条记录的sortField值作为下一页的lastSortValue
                if (!listMap.isEmpty()) {
                    Map<String, Object> lastRecord = listMap.get(listMap.size() - 1);
                    String nextLastSortValue = lastRecord.get(sortField.toUpperCase()) != null ? lastRecord.get(sortField.toUpperCase()).toString() : null;
                    resultMap.put("lastSortValue", nextLastSortValue);
                } else {
                    resultMap.put("lastSortValue", null);
                }
                return Y9Result.success(resultMap);
            } else {
                return Y9Result.failure("接口查不到数据源信息，请联系负责人处理");
            }
        } else {
            return Y9Result.failure("该账号接口申请不存在");
        }
    }

    /**
     * 验证列名是否合法，防止SQL注入
     * 
     * @param columnName 列名
     * @return 是否为合法列名
     */
    private boolean isValidColumnName(String columnName) {
        // 只允许字母、数字、下划线，并且以字母开头
        return columnName != null && columnName.matches("^[a-zA-Z][a-zA-Z0-9_]*$");
    }

}
