package net.risesoft.controller;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

import net.risesoft.entity.AudioFile;
import net.risesoft.entity.CategoryTable;
import net.risesoft.entity.DataAssets;
import net.risesoft.entity.DataAssetsFile;
import net.risesoft.entity.DocumentFile;
import net.risesoft.entity.FileInfo;
import net.risesoft.entity.ImageFile;
import net.risesoft.entity.MetadataConfig;
import net.risesoft.entity.VideoFile;
import net.risesoft.enums.CategoryEnums;
import net.risesoft.log.LogLevelEnum;
import net.risesoft.log.OperationTypeEnum;
import net.risesoft.log.annotation.RiseLog;
import net.risesoft.model.AssetsModel;
import net.risesoft.model.SearchPage;
import net.risesoft.model.platform.resource.DataCatalog;
import net.risesoft.pojo.Y9Page;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.AudioFileService;
import net.risesoft.service.CategoryTableService;
import net.risesoft.service.DataAssetsFileService;
import net.risesoft.service.DataAssetsService;
import net.risesoft.service.DocumentFileService;
import net.risesoft.service.ImageFileService;
import net.risesoft.service.LabelService;
import net.risesoft.service.MetadataConfigService;
import net.risesoft.service.VideoFileService;
import net.risesoft.util.EntityOrTableUtils;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.json.Y9JsonUtil;
import net.risesoft.y9.util.Y9BeanUtil;

import y9.client.rest.platform.resource.DataCatalogApiClient;

/**
 * 资产管理
 *
 * @author yihong
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/vue/detail", produces = MediaType.APPLICATION_JSON_VALUE)
public class DataAssetsController {

    private final DataAssetsService dataAssetsService;
    private final DocumentFileService documentFileService;
    private final ImageFileService imageFileService;
    private final AudioFileService audioFileService;
    private final VideoFileService videoFileService;
    private final MetadataConfigService metadataConfigService;
    private final CategoryTableService categoryTableService;
    private final DataAssetsFileService dataAssetsFileService;
    private final DataCatalogApiClient dataCatalogApiClient;
    private final LabelService labelService;

    /**
     * 获取资产数据列表
     *
     * @param page 页码
     * @param rows 条数
     * @return
     */
    @GetMapping(value = "/getDataAssetsList")
    public Y9Page<Map<String, Object>> getDataAssetsList(@RequestParam String categoryId,
        @RequestParam(required = false) String columnNameAndValues, @RequestParam Integer page,
        @RequestParam Integer rows) {
        if (page < 1) {
            page = 1;
        }
        String tenantId = Y9LoginUserHolder.getTenantId();
        List<Map<String, Object>> list_map = new ArrayList<>();
        SearchPage<DataAssets> searchPage = null;
        if (StringUtils.isBlank(columnNameAndValues)) {
            searchPage = dataAssetsService.list(categoryId, false, page, rows);
        } else {
            searchPage =
                dataAssetsService.listByColumnNameAndValues(categoryId, false, columnNameAndValues, page, rows);
        }
        DataCatalog dataCatalog = dataCatalogApiClient.getTreeRoot(tenantId, categoryId).getData();
        String customId = dataCatalog.getCustomId();
        for (DataAssets dataAssets : searchPage.getRows()) {
            Map<String, Object> map = new HashMap<>();
            map = EntityOrTableUtils.convertToMap(dataAssets);
            if (customId.equals(CategoryEnums.DOCUMENT.getEnName())) {
                map.putAll(documentFileService.findByDetailId(dataAssets.getId()));
            } else if (customId.equals(CategoryEnums.IMAGE.getEnName())) {
                map.putAll(imageFileService.findByDetailId(dataAssets.getId()));
            } else if (customId.equals(CategoryEnums.AUDIO.getEnName())) {
                map.putAll(audioFileService.findByDetailId(dataAssets.getId()));
            } else if (customId.equals(CategoryEnums.VIDEO.getEnName())) {
                map.putAll(videoFileService.findByDetailId(dataAssets.getId()));
            } else {
                CategoryTable categoryTable = categoryTableService.findByCategoryMark(customId);
                if (null != categoryTable) {
                    List<Map<String, Object>> list_categoryTable =
                        categoryTableService.getTableData(categoryTable.getTableName(), dataAssets.getId().toString());
                    for (Map<String, Object> map_categoryTable : list_categoryTable) {
                        map.putAll(map_categoryTable);
                    }
                }
            }
            List<DataAssetsFile> files = dataAssetsFileService.findByDetailId(dataAssets.getId());
            map.put("hasFile", null != files && !files.isEmpty());
            list_map.add(map);
        }
        return Y9Page.success(page, searchPage.getTotalpages(), searchPage.getTotal(), list_map, "获取列表成功");
    }

    /**
     * 保存资产数据
     *
     * @param formDataJson 表单数据
     * @return
     */
    @PostMapping(value = "/saveOrUpdate")
    public Y9Result<String> saveOrUpdate(@RequestParam String saveType, @RequestParam String categoryId,
        @RequestParam String formDataJson) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Map<String, Object> map = Y9JsonUtil.readHashMap(formDataJson, String.class, Object.class);
        String tenantId = Y9LoginUserHolder.getTenantId();
        DataAssets dataAssets = new DataAssets();
        DocumentFile documentFile = new DocumentFile();
        ImageFile imageFile = new ImageFile();
        AudioFile audioFile = new AudioFile();
        VideoFile videoFile = new VideoFile();
        DataCatalog dataCatalog = dataCatalogApiClient.getTreeRoot(tenantId, categoryId).getData();
        String customId = dataCatalog.getCustomId();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String fieldName = entry.getKey();
            Object value = entry.getValue();
            MetadataConfig metadataConfig =
                metadataConfigService.findByViewTypeAndColumnName(dataCatalog.getCustomId(), fieldName);
            if (null != metadataConfig) {
                if (null != value && !value.equals("")) {
                    if (metadataConfig.getDataType().equals("Integer") || metadataConfig.getDataType().equals("int")) {
                        value = Integer.parseInt(value.toString());
                    } else if (metadataConfig.getDataType().equals("Long")
                        || metadataConfig.getDataType().equals("long")
                        || metadataConfig.getDataType().equals("bigint")) {
                        value = Long.parseLong(value.toString());
                    } else if (metadataConfig.getDataType().equals("Date")
                        || metadataConfig.getDataType().equals("date")
                        || metadataConfig.getDataType().equals("datetime")
                        || metadataConfig.getDataType().equals("timestamp")
                        || metadataConfig.getDataType().equals("time")) {
                        value = sdf.parse(value.toString());
                    }
                    try {
                        if (metadataConfig.getFieldOrigin().equals("baseInfo")) {
                            // 获取字段
                            Field field = DataAssets.class.getDeclaredField(fieldName);
                            // 设置字段可访问
                            field.setAccessible(true);
                            // 给字段赋值
                            field.set(dataAssets, value);
                        } else if (metadataConfig.getFieldOrigin().equals(CategoryEnums.DOCUMENT.getEnName())) {
                            Field field = DocumentFile.class.getDeclaredField(fieldName);
                            field.setAccessible(true);
                            field.set(documentFile, value);
                        } else if (metadataConfig.getFieldOrigin().equals(CategoryEnums.IMAGE.getEnName())) {
                            Field field = ImageFile.class.getDeclaredField(fieldName);
                            field.setAccessible(true);
                            field.set(imageFile, value);
                        } else if (metadataConfig.getFieldOrigin().equals(CategoryEnums.AUDIO.getEnName())) {
                            Field field = AudioFile.class.getDeclaredField(fieldName);
                            field.setAccessible(true);
                            field.set(audioFile, value);
                        } else if (metadataConfig.getFieldOrigin().equals(CategoryEnums.VIDEO.getEnName())) {
                            Field field = VideoFile.class.getDeclaredField(fieldName);
                            field.setAccessible(true);
                            field.set(videoFile, value);
                        }

                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (saveType.equals("add")) {
            dataAssets.setCategoryId(categoryId);
            dataAssetsService.save(dataAssets);
            if (customId.equals(CategoryEnums.DOCUMENT.getEnName())) {
                documentFile.setDetailId(dataAssets.getId());
                documentFileService.save(documentFile);
            } else if (customId.equals(CategoryEnums.IMAGE.getEnName())) {
                imageFile.setDetailId(dataAssets.getId());
                imageFileService.save(imageFile);
            } else if (customId.equals(CategoryEnums.AUDIO.getEnName())) {
                audioFile.setDetailId(dataAssets.getId());
                audioFileService.save(audioFile);
            } else if (customId.equals(CategoryEnums.VIDEO.getEnName())) {
                videoFile.setDetailId(dataAssets.getId());
                videoFileService.save(videoFile);
            } else {
                categoryTableService.saveTableData("add", customId, dataAssets.getId().toString(), map);
            }
        } else {
            if (null != dataAssets.getId()) {
                DataAssets oldDataAssets = dataAssetsService.findById(dataAssets.getId());
                if (null != oldDataAssets) {
                    Y9BeanUtil.copyProperties(dataAssets, oldDataAssets);
                    dataAssetsService.save(oldDataAssets);
                }
                if (customId.equals(CategoryEnums.DOCUMENT.getEnName())) {
                    if (null != documentFile.getId()) {
                        DocumentFile oldDocumentFile = documentFileService.findById(documentFile.getId());
                        Y9BeanUtil.copyProperties(documentFile, oldDocumentFile);
                        documentFileService.save(oldDocumentFile);
                    } else {
                        documentFile.setDetailId(dataAssets.getId());
                        documentFileService.save(documentFile);
                    }
                } else if (customId.equals(CategoryEnums.IMAGE.getEnName())) {
                    if (null != imageFile.getId()) {
                        ImageFile oldImageFile = imageFileService.findById(imageFile.getId());
                        Y9BeanUtil.copyProperties(imageFile, oldImageFile);
                        imageFileService.save(oldImageFile);
                    } else {
                        imageFile.setDetailId(dataAssets.getId());
                        imageFileService.save(imageFile);
                    }
                } else if (customId.equals(CategoryEnums.AUDIO.getEnName())) {
                    if (null != audioFile.getId()) {
                        AudioFile oldAudioFile = audioFileService.findById(audioFile.getId());
                        Y9BeanUtil.copyProperties(audioFile, oldAudioFile);
                        audioFileService.save(oldAudioFile);
                    } else {
                        audioFile.setDetailId(dataAssets.getId());
                        audioFileService.save(audioFile);
                    }
                } else if (customId.equals(CategoryEnums.VIDEO.getEnName())) {
                    if (null != videoFile.getId()) {
                        VideoFile oldVideoFile = videoFileService.findById(videoFile.getId());
                        Y9BeanUtil.copyProperties(videoFile, oldVideoFile);
                        videoFileService.save(oldVideoFile);
                    } else {
                        videoFile.setDetailId(dataAssets.getId());
                        videoFileService.save(videoFile);
                    }
                } else {
                    categoryTableService.saveTableData("edit", customId, dataAssets.getId().toString(), map);
                }
            }
        }
        return Y9Result.successMsg("保存成功");
    }

    /**
     * 删除资产著录数据
     *
     * @param ids
     * @return
     */
    @PostMapping(value = "/delete")
    public Y9Result<String> delete(String categoryId, @RequestParam Long[] ids) {
        dataAssetsService.signDelete(categoryId, ids);
        return Y9Result.successMsg("删除成功");
    }

    /**
     * 资产著录数据预归档
     *
     * @param ids
     * @return
     */
    @PostMapping(value = "/recordArchiving")
    public Y9Result<String> recordArchiving(@RequestParam Long[] ids) {
        dataAssetsService.recordArchiving(ids);
        return Y9Result.successMsg("归档成功");
    }

    /**
     * 资产数据生成档号
     *
     * @param ids
     * @return
     */
    @PostMapping(value = "/createAssetsNo")
    public Y9Result<String> createAssetsNo(@RequestParam String categoryId, @RequestParam Long[] ids) {
        return dataAssetsService.createAssetsNo(categoryId, ids);
    }

    @RiseLog(operationType = OperationTypeEnum.ADD, operationName = "保存数据资产信息", logLevel = LogLevelEnum.RSLOG)
    @PostMapping(value = "/saveDataAssets")
    public Y9Result<String> saveDataAssets(DataAssets dataAssets) {
        return dataAssetsService.saveDataAssets(dataAssets);
    }

    @RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "分页获取资产数据列表", logLevel = LogLevelEnum.RSLOG,
        enable = false)
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

    @RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "分页获取资产数据挂接文件列表", logLevel = LogLevelEnum.RSLOG,
        enable = false)
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

    @RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "可信数据空间-分页获取数据列表", logLevel = LogLevelEnum.RSLOG,
        enable = false)
    @GetMapping("/getDataPage")
    public Y9Page<Map<String, Object>> getDataPage(String searchText, String appScenarios, String dataZone,
        String productType, Integer sort, Integer page, Integer size) {
        return dataAssetsService.searchPage2(searchText, appScenarios, dataZone, productType, sort, page, size);
    }

    @RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "可信数据空间-获取数据信息", logLevel = LogLevelEnum.RSLOG,
        enable = false)
    @GetMapping("/getDataById")
    public Y9Result<AssetsModel> getDataById(Long id) {
        return dataAssetsService.getDataById(id);
    }

    @RiseLog(operationType = OperationTypeEnum.BROWSE, operationName = "可信数据空间-获取过滤条件", logLevel = LogLevelEnum.RSLOG,
        enable = false)
    @GetMapping("/getDataFilter")
    public Y9Result<List<Map<String, Object>>> getDataFilter() {
        return dataAssetsService.getDataFilter();
    }

}
