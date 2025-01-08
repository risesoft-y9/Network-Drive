package net.risesoft.controller;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import net.risesoft.entity.AudioFile;
import net.risesoft.entity.CategoryTable;
import net.risesoft.entity.DataAssets;
import net.risesoft.entity.DataAssetsFile;
import net.risesoft.entity.DocumentFile;
import net.risesoft.entity.ImageFile;
import net.risesoft.entity.MetadataConfig;
import net.risesoft.entity.VideoFile;
import net.risesoft.enums.CategoryEnums;
import net.risesoft.model.SearchPage;
import net.risesoft.model.platform.DataCatalog;
import net.risesoft.pojo.Y9Page;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.AudioFileService;
import net.risesoft.service.CategoryTableService;
import net.risesoft.service.DataAssetsFileService;
import net.risesoft.service.DataAssetsService;
import net.risesoft.service.DocumentFileService;
import net.risesoft.service.ImageFileService;
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
@RequestMapping(value = "/vue/dataAssets", produces = MediaType.APPLICATION_JSON_VALUE)
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

    /**
     * 获取档案著录数据列表
     *
     * @param page 页码
     * @param rows 条数
     * @return
     */
    @GetMapping(value = "/getArchivesList")
    public Y9Page<Map<String, Object>> getArchivesList(@RequestParam String categoryId,
        @RequestParam(required = false) String columnNameAndValues, @RequestParam Integer fileStatus,
        @RequestParam Integer page, @RequestParam Integer rows) {
        if (page < 1) {
            page = 1;
        }
        String tenantId = Y9LoginUserHolder.getTenantId();
        List<Map<String, Object>> list_map = new ArrayList<>();
        SearchPage<DataAssets> searchPage = null;
        if (StringUtils.isBlank(columnNameAndValues)) {
            searchPage = dataAssetsService.listArchives(categoryId, fileStatus, false, page, rows);
        } else {
            searchPage = dataAssetsService.listArchivesByColumnNameAndValues(categoryId, fileStatus, false,
                columnNameAndValues, page, rows);
        }
        DataCatalog dataCatalog = dataCatalogApiClient.getTreeRoot(tenantId, categoryId).getData();
        String customId = dataCatalog.getCustomId();
        for (DataAssets archives : searchPage.getRows()) {
            Map<String, Object> map = new HashMap<>();
            map = EntityOrTableUtils.convertToMap(archives);
            if (customId.equals(CategoryEnums.DOCUMENT.getEnName())) {
                map.putAll(documentFileService.findByDetailId(archives.getDataAssetsId()));
            } else if (customId.equals(CategoryEnums.IMAGE.getEnName())) {
                map.putAll(imageFileService.findByDetailId(archives.getDataAssetsId()));
            } else if (customId.equals(CategoryEnums.AUDIO.getEnName())) {
                map.putAll(audioFileService.findByDetailId(archives.getDataAssetsId()));
            } else if (customId.equals(CategoryEnums.VIDEO.getEnName())) {
                map.putAll(videoFileService.findByDetailId(archives.getDataAssetsId()));
            } else {
                CategoryTable categoryTable = categoryTableService.findByCategoryMark(customId);
                if (null != categoryTable) {
                    List<Map<String, Object>> list_categoryTable = categoryTableService
                        .getTableData(categoryTable.getTableName(), archives.getDataAssetsId().toString());
                    for (Map<String, Object> map_categoryTable : list_categoryTable) {
                        map.putAll(map_categoryTable);
                    }
                }
            }
            List<DataAssetsFile> archivesFiles = dataAssetsFileService.findByDetailId(archives.getDataAssetsId());
            map.put("hasFile", null != archivesFiles && !archivesFiles.isEmpty());
            list_map.add(map);
        }
        return Y9Page.success(page, searchPage.getTotalpages(), searchPage.getTotal(), list_map, "获取列表成功");
    }

    @GetMapping(value = "/getSelectArchivesList")
    public Y9Result<List<DataAssets>> getSelectArchivesList(@RequestParam Long[] archivesId) {
        List<DataAssets> list = dataAssetsService.findByDataAssetsIdIn(archivesId);
        return Y9Result.success(list, "获取列表成功");
    }

    /**
     * 保存档案著录数据
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
        DataAssets archives = new DataAssets();
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
                        if (metadataConfig.getFieldOrigin().equals("archives")) {
                            // 获取字段
                            Field field = DataAssets.class.getDeclaredField(fieldName);
                            // 设置字段可访问
                            field.setAccessible(true);
                            // 给字段赋值
                            field.set(archives, value);
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
            // if (fieldName.equals("archivesId") && null != value) {
            // archives.setArchivesId(Long.parseLong(value.toString()));
            // }
        }
        if (saveType.equals("add")) {
            archives.setCategoryCode(customId);
            archives.setCategoryId(categoryId);
            archives.setCreateTime(new Date());
            dataAssetsService.save(archives);
            if (customId.equals(CategoryEnums.DOCUMENT.getEnName())) {
                documentFile.setDetailId(archives.getDataAssetsId());
                documentFileService.save(documentFile);
            } else if (customId.equals(CategoryEnums.IMAGE.getEnName())) {
                imageFile.setDetailId(archives.getDataAssetsId());
                imageFileService.save(imageFile);
            } else if (customId.equals(CategoryEnums.AUDIO.getEnName())) {
                audioFile.setDetailId(archives.getDataAssetsId());
                audioFileService.save(audioFile);
            } else if (customId.equals(CategoryEnums.VIDEO.getEnName())) {
                videoFile.setDetailId(archives.getDataAssetsId());
                videoFileService.save(videoFile);
            } else {
                categoryTableService.saveTableData("add", customId, archives.getDataAssetsId().toString(), map);
            }
        } else {
            if (null != archives.getDataAssetsId()) {
                DataAssets oldArchives = dataAssetsService.findById(archives.getDataAssetsId());
                if (null != oldArchives) {
                    Y9BeanUtil.copyProperties(archives, oldArchives);
                    dataAssetsService.save(oldArchives);
                }
                if (customId.equals(CategoryEnums.DOCUMENT.getEnName())) {
                    if (null != documentFile.getId()) {
                        DocumentFile oldDocumentFile = documentFileService.findById(documentFile.getId());
                        Y9BeanUtil.copyProperties(documentFile, oldDocumentFile);
                        documentFileService.save(oldDocumentFile);
                    } else {
                        documentFile.setDetailId(archives.getDataAssetsId());
                        documentFileService.save(documentFile);
                    }
                } else if (customId.equals(CategoryEnums.IMAGE.getEnName())) {
                    if (null != imageFile.getId()) {
                        ImageFile oldImageFile = imageFileService.findById(imageFile.getId());
                        Y9BeanUtil.copyProperties(imageFile, oldImageFile);
                        imageFileService.save(oldImageFile);
                    } else {
                        imageFile.setDetailId(archives.getDataAssetsId());
                        imageFileService.save(imageFile);
                    }
                } else if (customId.equals(CategoryEnums.AUDIO.getEnName())) {
                    if (null != audioFile.getId()) {
                        AudioFile oldAudioFile = audioFileService.findById(audioFile.getId());
                        Y9BeanUtil.copyProperties(audioFile, oldAudioFile);
                        audioFileService.save(oldAudioFile);
                    } else {
                        audioFile.setDetailId(archives.getDataAssetsId());
                        audioFileService.save(audioFile);
                    }
                } else if (customId.equals(CategoryEnums.VIDEO.getEnName())) {
                    if (null != videoFile.getId()) {
                        VideoFile oldVideoFile = videoFileService.findById(videoFile.getId());
                        Y9BeanUtil.copyProperties(videoFile, oldVideoFile);
                        videoFileService.save(oldVideoFile);
                    } else {
                        videoFile.setDetailId(archives.getDataAssetsId());
                        videoFileService.save(videoFile);
                    }
                } else {
                    categoryTableService.saveTableData("edit", customId, archives.getDataAssetsId().toString(), map);
                }
            }
        }
        return Y9Result.successMsg("保存成功");
    }

    /**
     * 删除档案著录数据
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
     * 档案著录数据预归档
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
     * 档案数据生成档号
     *
     * @param ids
     * @return
     */
    @PostMapping(value = "/createArchivesNo")
    public Y9Result<String> createArchivesNo(@RequestParam String categoryId, @RequestParam Long[] ids) {
        return dataAssetsService.createAssetsNo(categoryId, ids);
    }

}
