package net.risesoft.controller;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import net.risesoft.entity.Archives;
import net.risesoft.entity.AudioFile;
import net.risesoft.entity.CategoryTable;
import net.risesoft.entity.DocumentFile;
import net.risesoft.entity.ImageFile;
import net.risesoft.entity.MetadataConfig;
import net.risesoft.entity.VideoFile;
import net.risesoft.enums.CategoryEnums;
import net.risesoft.model.platform.DataCatalog;
import net.risesoft.pojo.Y9Page;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.ArchivesService;
import net.risesoft.service.AudioFileService;
import net.risesoft.service.CategoryTableService;
import net.risesoft.service.DocumentFileService;
import net.risesoft.service.ImageFileService;
import net.risesoft.service.MetadataConfigService;
import net.risesoft.service.VideoFileService;
import net.risesoft.util.EntityToMapConverter;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.json.Y9JsonUtil;
import net.risesoft.y9.util.Y9BeanUtil;

import y9.client.rest.platform.resource.DataCatalogApiClient;

/**
 * 档案著录管理
 *
 * @author yihong
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/vue/record", produces = MediaType.APPLICATION_JSON_VALUE)
public class RecordController {

    private final ArchivesService archivesService;

    private final DocumentFileService documentFileService;

    private final ImageFileService imageFileService;

    private final AudioFileService audioFileService;

    private final VideoFileService videoFileService;

    private final MetadataConfigService metadataConfigService;

    private final CategoryTableService categoryTableService;

    private final DataCatalogApiClient dataCatalogApiClient;

    /**
     * 获取档案著录数据列表
     *
     * @param page 页码
     * @param rows 条数
     * @return
     */
    @GetMapping(value = "/getArchivesRecordList")
    public Y9Page<Map<String, Object>> getArchivesRecordList(String categoryId, @RequestParam Integer page,
        @RequestParam Integer rows) {
        if (page < 1) {
            page = 1;
        }
        List<Map<String, Object>> list_map = new ArrayList<>();
        String tenantId = Y9LoginUserHolder.getTenantId();
        DataCatalog dataCatalog = dataCatalogApiClient.getTreeRoot(tenantId, categoryId).getData();
        String customId = dataCatalog.getCustomId();
        Page<Archives> pageList = archivesService.pageArchives(categoryId, page, rows);
        List<Archives> list = pageList.getContent();
        for (Archives archives : list) {
            Map<String, Object> map = new HashMap<>();
            map = EntityToMapConverter.convertToMap(archives);
            if (customId.equals(CategoryEnums.DOCUMENT.getEnName())) {
                map.putAll(documentFileService.findByDetailId(archives.getArchives_id().toString()));
            } else if (customId.equals(CategoryEnums.IMAGE.getEnName())) {
                map.putAll(imageFileService.findByDetailId(archives.getArchives_id().toString()));
            } else if (customId.equals(CategoryEnums.AUDIO.getEnName())) {
                map.putAll(audioFileService.findByDetailId(archives.getArchives_id().toString()));
            } else if (customId.equals(CategoryEnums.VIDEO.getEnName())) {
                map.putAll(videoFileService.findByDetailId(archives.getArchives_id().toString()));
            } else {
                CategoryTable categoryTable = categoryTableService.findByCategoryMark(customId);
                if (null != categoryTable) {
                    List<Map<String, Object>> list_categoryTable = categoryTableService
                        .getTableData(categoryTable.getTableName(), archives.getArchives_id().toString());
                    for (Map<String, Object> map_categoryTable : list_categoryTable) {
                        map.putAll(map_categoryTable);
                    }
                }
            }
            list_map.add(map);
        }
        return Y9Page.success(page, pageList.getTotalPages(), pageList.getTotalElements(), list_map, "获取列表成功");
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
        Archives archives = new Archives();
        DocumentFile documentFile = new DocumentFile();
        ImageFile imageFile = new ImageFile();
        AudioFile audioFile = new AudioFile();
        VideoFile videoFile = new VideoFile();
        DataCatalog dataCatalog = dataCatalogApiClient.getTreeRoot(tenantId, categoryId).getData();
        String customId = dataCatalog.getCustomId();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            MetadataConfig metadataConfig =
                metadataConfigService.findByViewTypeAndColumnName(dataCatalog.getCustomId(), entry.getKey());
            if (null != metadataConfig) {
                String fieldName = entry.getKey();
                Object value = entry.getValue();

                Date date = null;
                if (null != value && !value.equals("")) {
                    if (metadataConfig.getDataType().equals("Integer")) {
                        value = Integer.parseInt(value.toString());
                    } else if (metadataConfig.getDataType().equals("Long")) {
                        value = Long.parseLong(value.toString());
                    } else if (metadataConfig.getDataType().equals("Date")) {
                        value = sdf.parse(value.toString());
                    }
                    try {
                        if (metadataConfig.getFieldOrigin().equals("archives")) {
                            // 获取字段
                            Field field = Archives.class.getDeclaredField(fieldName);
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
        }
        if (saveType.equals("add")) {
            archives.setCategoryCode(customId);
            archives.setCategoryId(categoryId);
            archives.setCreateTime(new Date());
            archivesService.save(archives);
            if (customId.equals(CategoryEnums.DOCUMENT.getEnName())) {
                documentFile.setDetailId(archives.getArchives_id().toString());
                documentFileService.save(documentFile);
            } else if (customId.equals(CategoryEnums.IMAGE.getEnName())) {
                imageFile.setDetailId(archives.getArchives_id().toString());
                imageFileService.save(imageFile);
            } else if (customId.equals(CategoryEnums.AUDIO.getEnName())) {
                audioFile.setDetailId(archives.getArchives_id().toString());
                audioFileService.save(audioFile);
            } else if (customId.equals(CategoryEnums.VIDEO.getEnName())) {
                videoFile.setDetailId(archives.getArchives_id().toString());
                videoFileService.save(videoFile);
            } else {
                categoryTableService.saveTableData("add", customId, archives.getArchives_id().toString(), map);
            }
        } else {
            if (null != archives.getArchives_id()) {
                Archives oldArchives = archivesService.findByArchives_id(archives.getArchives_id());
                if (null != oldArchives) {
                    Y9BeanUtil.copyProperties(archives, oldArchives);
                    archivesService.save(oldArchives);
                }
                if (customId.equals(CategoryEnums.DOCUMENT.getEnName())) {
                    DocumentFile oldDocumentFile = documentFileService.findById(documentFile.getId());
                    Y9BeanUtil.copyProperties(documentFile, oldDocumentFile);
                    documentFileService.save(oldDocumentFile);
                } else if (customId.equals(CategoryEnums.IMAGE.getEnName())) {
                    ImageFile oldImageFile = imageFileService.findById(imageFile.getId());
                    Y9BeanUtil.copyProperties(imageFile, oldImageFile);
                    imageFileService.save(oldImageFile);
                } else if (customId.equals(CategoryEnums.AUDIO.getEnName())) {
                    AudioFile oldAudioFile = audioFileService.findById(audioFile.getId());
                    Y9BeanUtil.copyProperties(audioFile, oldAudioFile);
                    audioFileService.save(oldAudioFile);
                } else if (customId.equals(CategoryEnums.VIDEO.getEnName())) {
                    VideoFile oldVideoFile = videoFileService.findById(videoFile.getId());
                    Y9BeanUtil.copyProperties(videoFile, oldVideoFile);
                    videoFileService.save(oldVideoFile);
                } else {
                    categoryTableService.saveTableData("edit", customId, archives.getArchives_id().toString(), map);
                }
            }
        }
        return Y9Result.successMsg("保存成功");
    }
}
