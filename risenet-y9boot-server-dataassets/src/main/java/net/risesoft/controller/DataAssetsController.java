package net.risesoft.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import net.risesoft.converter.util.Y9Encrytor;
import net.risesoft.entity.DataAssets;
import net.risesoft.entity.FileInfo;
import net.risesoft.entity.SubscribeBaseEntity;
import net.risesoft.entity.SubscribeEntity;
import net.risesoft.log.LogLevelEnum;
import net.risesoft.log.OperationTypeEnum;
import net.risesoft.log.annotation.RiseLog;
import net.risesoft.model.AssetsModel;
import net.risesoft.pojo.Y9Page;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.DataAssetsService;
import net.risesoft.service.LabelService;
import net.risesoft.util.DataConstant;
import net.risesoft.y9.Y9Context;
import net.risesoft.y9.Y9LoginUserHolder;

/**
 * 资产管理
 *
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/vue/detail", produces = MediaType.APPLICATION_JSON_VALUE)
public class DataAssetsController {

    private final DataAssetsService dataAssetsService;
    private final LabelService labelService;

    @RiseLog(operationType = OperationTypeEnum.ADD, operationName = "保存数据资产信息", logLevel = LogLevelEnum.RSLOG)
    @PostMapping(value = "/saveDataAssets")
    public Y9Result<String> saveDataAssets(DataAssets dataAssets) {
        return dataAssetsService.saveDataAssets(dataAssets);
    }

    @RiseLog(operationName = "分页获取资产数据列表", logLevel = LogLevelEnum.RSLOG)
    @GetMapping("/searchPage")
    public Y9Page<DataAssets> searchPage(String name, String code, String categoryId, Integer status, String dataState,
        Integer page, Integer size) {
        Page<DataAssets> pageList = dataAssetsService.searchPage(categoryId, name, code, status, dataState, page, size);
        pageList.stream().map((item) -> {
            item.setLabelData(labelService.getLabelData(item.getId()));
            return item;
        }).collect(Collectors.toList());
        return Y9Page.success(page, pageList.getTotalPages(), pageList.getTotalElements(), pageList.getContent(),
            "获取数据成功");
    }

    @RiseLog(operationType = OperationTypeEnum.DELETE, operationName = "删除数据资产信息", logLevel = LogLevelEnum.RSLOG)
    @PostMapping(value = "/deleteDataAssets")
    public Y9Result<String> deleteDataAssets(Long id) {
        return dataAssetsService.deleteDataAssets(id);
    }

    @RiseLog(operationType = OperationTypeEnum.ADD, operationName = "上传文件", logLevel = LogLevelEnum.RSLOG)
    @PostMapping("/fileUpload")
    public Y9Result<String> fileUpload(@RequestParam MultipartFile file, @RequestParam Long assetsId) {
        return dataAssetsService.fileUpload(file, assetsId);
    }

    @RiseLog(operationType = OperationTypeEnum.MODIFY, operationName = "上下架数据资产", logLevel = LogLevelEnum.RSLOG)
    @PostMapping(value = "/updownData")
    public Y9Result<String> updownData(Long id) {
        return dataAssetsService.updownData(id);
    }

    @RiseLog(operationType = OperationTypeEnum.ADD, operationName = "数据资产赋码", logLevel = LogLevelEnum.RSLOG)
    @PostMapping(value = "/genQr")
    public Y9Result<String> genQr(Long id) {
        return dataAssetsService.genQr(id);
    }

    @RiseLog(operationType = OperationTypeEnum.ADD, operationName = "生成数据资产编码", logLevel = LogLevelEnum.RSLOG)
    @GetMapping(value = "/genCode")
    public Y9Result<String> genCode(String categoryId, String pCode) {
        return dataAssetsService.genCode(categoryId, pCode);
    }

    @RiseLog(operationName = "分页获取资产数据挂接文件列表", logLevel = LogLevelEnum.RSLOG)
    @GetMapping("/getFilePage")
    public Y9Page<FileInfo> getFilePage(String name, Long id, Integer page, Integer size) {
        Page<FileInfo> pageList = dataAssetsService.getFilePage(id, name, page, size);
        return Y9Page.success(page, pageList.getTotalPages(), pageList.getTotalElements(), pageList.getContent(),
            "获取数据成功");
    }

    @RiseLog(operationType = OperationTypeEnum.SEND, operationName = "下载文件", logLevel = LogLevelEnum.RSLOG)
    @GetMapping("/fileDownload")
    public void fileDownload(Long id, HttpServletResponse response, HttpServletRequest request) {
        dataAssetsService.fileDownload(id, response, request);
    }

    @RiseLog(operationType = OperationTypeEnum.ADD, operationName = "上传资产图片", logLevel = LogLevelEnum.RSLOG)
    @PostMapping("/uploadPicture")
    public Y9Result<DataAssets> uploadPicture(@RequestParam MultipartFile file, @RequestParam Long assetsId) {
        return dataAssetsService.uploadPicture(file, assetsId);
    }

    @RiseLog(operationType = OperationTypeEnum.ADD, operationName = "资产挂接接口", logLevel = LogLevelEnum.RSLOG)
    @PostMapping("/saveAssetsInterface")
    public Y9Result<String> saveAssetsInterface(String ids, Long assetsId) {
        return dataAssetsService.saveAssetsInterface(ids, assetsId);
    }

    @RiseLog(operationType = OperationTypeEnum.ADD, operationName = "资产挂接数据", logLevel = LogLevelEnum.RSLOG)
    @PostMapping("/saveAssetsTable")
    public Y9Result<String> saveAssetsTable(String ids, Long assetsId) {
        return dataAssetsService.saveAssetsTable(ids, assetsId);
    }

    @RiseLog(operationType = OperationTypeEnum.MODIFY, operationName = "资产入库出库", logLevel = LogLevelEnum.RSLOG)
    @PostMapping(value = "/examineData")
    public Y9Result<String> examineData(Long id, String dataState) {
        return dataAssetsService.examineData(id, dataState);
    }

    @RiseLog(operationType = OperationTypeEnum.DELETE, operationName = "删除资产挂接数据", logLevel = LogLevelEnum.RSLOG)
    @PostMapping(value = "/deleteMountData")
    public Y9Result<String> deleteMountData(Long id) {
        return dataAssetsService.deleteMountData(id);
    }

    @RiseLog(operationName = "可信数据空间-分页获取数据列表", logLevel = LogLevelEnum.RSLOG)
    @GetMapping("/getDataPage")
    public Y9Page<Map<String, Object>> getDataPage(String searchText, String appScenarios, String dataZone,
        String productType, Integer sort, Integer page, Integer size) {
        return dataAssetsService.searchPage2(searchText, "", appScenarios, dataZone, productType, sort, page, size);
    }

    @RiseLog(operationName = "可信数据空间-获取数据信息", logLevel = LogLevelEnum.RSLOG)
    @GetMapping("/getDataById")
    public Y9Result<AssetsModel> getDataById(Long id) {
        return dataAssetsService.getDataById(id);
    }

    @RiseLog(operationName = "可信数据空间-获取过滤条件", logLevel = LogLevelEnum.RSLOG)
    @GetMapping("/getDataFilter")
    public Y9Result<List<Map<String, Object>>> getDataFilter() {
        return dataAssetsService.getDataFilter();
    }

    @RiseLog(operationName = "数据订阅-分页获取数据列表", logLevel = LogLevelEnum.RSLOG)
    @GetMapping("/searchPage2")
    public Y9Page<Map<String, Object>> searchPage2(String searchText, String categoryId, Integer page, Integer size) {
        return dataAssetsService.searchPage2(searchText, categoryId, "", "", "", null, page, size);
    }

    @RiseLog(operationType = OperationTypeEnum.ADD, operationName = "保存订阅信息", logLevel = LogLevelEnum.RSLOG)
    @PostMapping(value = "/saveSubscribe")
    public Y9Result<String> saveSubscribe(SubscribeEntity subscribeEntity) {
        return dataAssetsService.saveSubscribe(subscribeEntity);
    }

    @RiseLog(operationName = "数据订阅-我的订阅列表", logLevel = LogLevelEnum.RSLOG)
    @GetMapping("/searchSubscribePage")
    public Y9Page<Map<String, Object>> searchSubscribePage(String type, String name, Integer page, Integer size) {
        return dataAssetsService.searchSubscribePage(type, name, page, size);
    }

    @RiseLog(operationType = OperationTypeEnum.CHECK, operationName = "审核订阅信息", logLevel = LogLevelEnum.RSLOG)
    @PostMapping(value = "/review")
    public Y9Result<String> review(@RequestParam String id, @RequestParam String reviewStatus, String reason) {
        return dataAssetsService.review(id, reviewStatus, reason);
    }

    @RiseLog(operationName = "订阅与审核信息", logLevel = LogLevelEnum.RSLOG)
    @GetMapping("/getSubscribeById")
    public Y9Result<Map<String, Object>> getSubscribeById(String id) {
        return Y9Result.success(dataAssetsService.getSubscribeById(id));
    }

    @RiseLog(operationType = OperationTypeEnum.ADD, operationName = "保存订阅-库表推送信息", logLevel = LogLevelEnum.RSLOG)
    @PostMapping(value = "/saveSubscribeBase")
    public Y9Result<String> saveSubscribeBase(SubscribeBaseEntity subscribeBaseEntity) {
        return dataAssetsService.saveSubscribeBase(subscribeBaseEntity);
    }

    @RiseLog(operationName = "根据id获取订阅-库表推送信息", logLevel = LogLevelEnum.RSLOG)
    @GetMapping("/getSubscribeBaseById")
    public Y9Result<SubscribeBaseEntity> getSubscribeBaseById(String id) {
        return Y9Result.success(dataAssetsService.getSubscribeBaseById(id));
    }

    @PostMapping("/checkSubscribeBase")
    public Y9Result<String> checkSubscribeBase(String subscribeId) {
        SubscribeBaseEntity subscribeBaseEntity = dataAssetsService.getSubscribeBaseById(subscribeId);
        if (subscribeBaseEntity == null) {
            return Y9Result.failure("订阅-库表推送信息不存在");
        }
        SubscribeEntity subscribeEntity = dataAssetsService.findById(subscribeId);
        if (subscribeEntity == null) {
            return Y9Result.failure("订阅不存在");
        }

        // 获取数据流引擎数据库配置
        String driverClassName = Y9Context.getProperty("dataflow.driverClassName");
        String jdbcUrl = Y9Context.getProperty("dataflow.jdbcUrl");
        String username = Y9Context.getProperty("dataflow.username");
        String password = Y9Context.getProperty("dataflow.password");
        // 加载数据库驱动
        try {
            Class.forName(driverClassName);
        } catch (ClassNotFoundException e) {
            return Y9Result.failure("数据库驱动加载失败");
        }

        // 连接数据库
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            // 根据推送库的连接url：jdbc:mysql://localhost:3306/y9_data获取数据库的类型、驱动信息
            String url = subscribeBaseEntity.getUrl();
            // 从url中提取数据库类型
            String databaseProductName = url.substring(url.indexOf(":") + 1, url.indexOf("://"));
            // 获取数据库的驱动信息
            String driverName = DataConstant.getDirver(databaseProductName);
            if (driverClassName.isEmpty()) {
                return Y9Result.failure("不支持的数据库类型");
            }

            // 将库表推送信息保存到数据流引擎库Y9_DATASERVICE_DATASOURCE表里
            // 根据subscribeBaseEntity的id判断数据是否存在，存在返回查询到的名称
            String checkSql = "SELECT BASENAME FROM Y9_DATASERVICE_DATASOURCE WHERE ID = ?";
            try (PreparedStatement checkStatement = connection.prepareStatement(checkSql)) {
                checkStatement.setString(1, subscribeBaseEntity.getId());
                try (ResultSet resultSet = checkStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return Y9Result.success(resultSet.getString(1));
                    }
                }
            }
            String name = "资产订阅推送库";
            DataAssets dataAssetsEntity = dataAssetsService.findById(subscribeEntity.getAssetsId());
            if (dataAssetsEntity != null) {
                name = name + "-" + dataAssetsEntity.getName();
            }
            name = name + "-" + subscribeEntity.getUserName();

            Y9Encrytor y9Encrytor = new Y9Encrytor();
            String encryptPassword = y9Encrytor.Encrytor(subscribeBaseEntity.getPassword());
            String sql = "INSERT INTO Y9_DATASERVICE_DATASOURCE (ID,CREATE_TIME,UPDATE_TIME,BASENAME,BASESCHEMA,BASETYPE,DIRECTORY,DRIVER,INITIALSIZE,ISLOOK,MAXACTIVE,MINIDLE,PASSWORD,REMARK,RUNTYPE,`TYPE`,URL,USERNAME,TENANTID,USERID,EXTERNALID,SYSTEMNAME) " +
            "VALUES ('"+subscribeBaseEntity.getId()+"',null,null,'"+name+"','','"+databaseProductName+"','','"+driverName+"',1,0,20,1,'"+encryptPassword+"','"+subscribeBaseEntity.getRemark()+"',NULL,0,'"+subscribeBaseEntity.getUrl()+"','"+subscribeBaseEntity.getUsername()+"','"+Y9LoginUserHolder.getTenantId()+"','"+Y9LoginUserHolder.getPersonId()+"','"+subscribeId+"','dataassets')";
            // 执行SQL语句
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    return Y9Result.success(name);
                }
            }
            return Y9Result.failure("跳转库表推送信息保存失败");
        } catch (Exception e) {
            return Y9Result.failure("程序出错："+e.getMessage());
        }
    }

}
