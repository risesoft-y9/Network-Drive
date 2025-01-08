package net.risesoft.service.Impl;

import java.lang.reflect.Field;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import net.risesoft.entity.CategoryTable;
import net.risesoft.entity.DataAssets;
import net.risesoft.entity.DataAssetsNumberRules;
import net.risesoft.entity.MetadataConfig;
import net.risesoft.enums.CategoryEnums;
import net.risesoft.model.SearchPage;
import net.risesoft.model.platform.DataCatalog;
import net.risesoft.pojo.Y9Result;
import net.risesoft.repository.AudioFileRepository;
import net.risesoft.repository.DataAssetsNumberRulesRepository;
import net.risesoft.repository.DataAssetsRepository;
import net.risesoft.repository.DocumentFileRepository;
import net.risesoft.repository.ImageFileRepository;
import net.risesoft.repository.MetadataConfigRepository;
import net.risesoft.repository.VideoFileRepository;
import net.risesoft.service.CategoryTableService;
import net.risesoft.service.DataAssetsService;
import net.risesoft.util.PageUtil;
import net.risesoft.y9.Y9LoginUserHolder;

import y9.client.rest.platform.resource.DataCatalogApiClient;

@Service("archivesService")
@RequiredArgsConstructor
public class DataAssetsServiceImpl implements DataAssetsService {

    private final DataAssetsRepository dataAssetsRepository;

    private final DataCatalogApiClient dataCatalogApiClient;
    private final DocumentFileRepository documentFileRepository;
    private final AudioFileRepository audioFileRepository;
    private final ImageFileRepository imageFileRepository;
    private final VideoFileRepository videoFileRepository;
    private final CategoryTableService categoryTableService;
    private final PageUtil pageUtil;
    private final MetadataConfigRepository metadataConfigRepository;
    private final DataAssetsNumberRulesRepository dataAssetsNumberRulesRepository;

    public static Object getFieldValue(Object entity, String fieldName) {
        try {
            Field field = entity.getClass().getDeclaredField(fieldName); // 获取字段
            field.setAccessible(true); // 设置字段可访问
            return field.get(entity); // 获取字段值
        } catch (NoSuchFieldException e) {
            e.printStackTrace(); // 字段不存在
        } catch (IllegalAccessException e) {
            e.printStackTrace(); // 访问字段时异常
        }
        return null; // 返回null表示未找到或无法访问
    }

    @Override
    public SearchPage<DataAssets> listArchives(String categoryId, Integer fileStatus, Boolean isDeleted, int page,
        int rows) {
        Pageable pageable = PageRequest.of(page - 1, rows, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<DataAssets> pageList =
            dataAssetsRepository.findByCategoryIdAndStatusAndIsDeleted(categoryId, fileStatus, isDeleted, pageable);
        List<DataAssets> list = pageList.getContent();
        SearchPage<DataAssets> searchPage = SearchPage.<DataAssets>builder().rows(list).currpage(page).size(rows)
            .totalpages(pageList.getTotalPages()).total(pageList.getTotalElements()).build();
        return searchPage;
    }

    @Override
    public SearchPage<DataAssets> listArchivesByColumnNameAndValues(String categoryId, Integer fileStatus,
        Boolean isDeleted, String columnNameAndValues, int page, int rows) {
        String tenantId = Y9LoginUserHolder.getTenantId();
        DataCatalog dataCatalog = dataCatalogApiClient.getTreeRoot(tenantId, categoryId).getData();
        String customId = dataCatalog.getCustomId();
        String[] arr = columnNameAndValues.split(";");
        String joinSql = "";
        String conditionSql = "";
        for (int i = 0; i < arr.length; i++) {
            String[] arrTemp = arr[i].split(":");
            if (arrTemp.length == 2) {
                String columnNameStr = arrTemp[0];
                String value = arrTemp[1];
                MetadataConfig metadataConfig =
                    metadataConfigRepository.findByViewTypeAndColumnName(customId, columnNameStr);
                String sign = "";
                if (null != metadataConfig) {
                    if (metadataConfig.getFieldOrigin().equals("archives")) {
                        sign = "T";
                    } else {
                        sign = "C";
                    }
                }
                String columnName = arrTemp[0].toUpperCase();
                if (StringUtils.isBlank(conditionSql)) {
                    conditionSql += "INSTR(" + sign + "." + columnName + ",'" + value + "') > 0 ";
                } else {
                    conditionSql += " AND INSTR(" + sign + "." + columnName + ",'" + value + "') > 0 ";
                }
            }
        }
        if (customId.equals(CategoryEnums.DOCUMENT.getEnName())) {
            joinSql = "LEFT JOIN Y9_ARCHIVES_DOCUMENT_FILE C ON T.DATAASSETS_ID = C.DETAIL_ID";
        } else if (customId.equals(CategoryEnums.IMAGE.getEnName())) {
            joinSql = "LEFT JOIN Y9_ARCHIVES_IMAGE_FILE C ON T.DATAASSETS_ID = C.DETAIL_ID";
        } else if (customId.equals(CategoryEnums.AUDIO.getEnName())) {
            joinSql = "LEFT JOIN Y9_ARCHIVES_AUDIO_FILE C ON T.DATAASSETS_ID = C.DETAIL_ID";
        } else if (customId.equals(CategoryEnums.VIDEO.getEnName())) {
            joinSql = "LEFT JOIN Y9_ARCHIVES_VIDEO_FILE C ON T.DATAASSETS_ID = C.DETAIL_ID";
        } else {
            CategoryTable categoryTable = categoryTableService.findByCategoryMark(customId);
            if (null != categoryTable) {
                joinSql = "LEFT JOIN " + categoryTable.getTableName() + " C ON T.DATAASSETS_ID = C.DETAIL_ID";
            }
        }
        String sql = "SELECT T.* FROM Y9_ARCHIVES_DETAILS T " + joinSql
            + " WHERE T.CATEGORY_ID = ? AND T.FILE_STATUS = ? AND T.IS_DELETED = ? AND " + conditionSql
            + " ORDER BY T.CREATE_TIME DESC";
        String countSql = "SELECT COUNT(T.DATAASSETS_ID) FROM Y9_ARCHIVES_DETAILS T " + joinSql
            + " WHERE T.CATEGORY_ID = ? AND T.FILE_STATUS = ? AND T.IS_DELETED = ? AND " + conditionSql;
        System.out.println(sql);
        System.out.println(countSql);
        Object[] args = new Object[3];
        args[0] = categoryId;
        args[1] = fileStatus;
        args[2] = isDeleted;
        SearchPage<DataAssets> searchPage =
            pageUtil.page(sql, args, new BeanPropertyRowMapper<>(DataAssets.class), countSql, args, page, rows);
        return searchPage;
    }

    @Override
    public DataAssets save(DataAssets archives) {
        return dataAssetsRepository.save(archives);
    }

    @Override
    public DataAssets findById(Long id) {
        return dataAssetsRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(String categoryId, Long[] ids) {
        String tenantId = Y9LoginUserHolder.getTenantId();
        DataCatalog dataCatalog = dataCatalogApiClient.getTreeRoot(tenantId, categoryId).getData();
        for (Long id : ids) {
            dataAssetsRepository.deleteById(id);
            if (dataCatalog.getCustomId().equals(CategoryEnums.DOCUMENT.getEnName())) {
                documentFileRepository.deleteByDetailId(id);
            } else if (dataCatalog.getCustomId().equals(CategoryEnums.IMAGE.getEnName())) {
                imageFileRepository.deleteByDetailId(id);
            } else if (dataCatalog.getCustomId().equals(CategoryEnums.AUDIO.getEnName())) {
                audioFileRepository.deleteByDetailId(id);
            } else if (dataCatalog.getCustomId().equals(CategoryEnums.VIDEO.getEnName())) {
                videoFileRepository.deleteByDetailId(id);
            } else {
                categoryTableService.deleteTableData(dataCatalog.getCustomId(), id.toString());
            }
        }
    }

    @Override
    @Transactional(readOnly = false)
    public void signDelete(String categoryId, Long[] ids) {
        for (Long id : ids) {
            DataAssets archives = this.findById(id);
            if (null != archives) {
                archives.setIsDeleted(true);
                this.save(archives);
            }
        }
    }

    @Override
    @Transactional(readOnly = false)
    public void recordArchiving(Long[] ids) {
        for (Long id : ids) {
            DataAssets archives = this.findById(id);
            if (null != archives) {
                archives.setAssetsStatus(1);
                this.save(archives);
            }
        }
    }

    @Override
    @Transactional(readOnly = false)
    public Y9Result<String> createAssetsNo(String categoryId, Long[] ids) {
        String tenantId = Y9LoginUserHolder.getTenantId();
        DataCatalog dataCatalog = dataCatalogApiClient.getTreeRoot(tenantId, categoryId).getData();
        for (Long id : ids) {
            DataAssets archives = this.findById(id);
            List<DataAssetsNumberRules> rulesList =
                dataAssetsNumberRulesRepository.findByCategoryMark(dataCatalog.getCustomId());
            if (null != rulesList && rulesList.size() > 0) {
                String archiveNo = "";
                for (DataAssetsNumberRules rules : rulesList) {
                    // TODO 根据规则生成档案号
                    Object fieldValue = getFieldValue(archives, rules.getFieldName()).toString();
                    if (null != fieldValue) {
                        if (StringUtils.isNotBlank(archiveNo) && StringUtils.isNotBlank(rules.getConnectorSymbol())) {
                            archiveNo += rules.getConnectorSymbol() + fieldValue;
                        } else {
                            archiveNo += fieldValue.toString();
                        }
                    }
                }
                System.out.println("资产编号：" + archiveNo);
                archives.setAssetsNo(archiveNo);
            } else {
                return Y9Result.failure("未找到档案号规则");
            }
            this.save(archives);
        }
        return Y9Result.success("创建档案号成功");
    }

    @Override
    public List<DataAssets> findByDataAssetsIdIn(Long[] ids) {
        return dataAssetsRepository.findByDataAssetsIdIn(ids);
    }

}
