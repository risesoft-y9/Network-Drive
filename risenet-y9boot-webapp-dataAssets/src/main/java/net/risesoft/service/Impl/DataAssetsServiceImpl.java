package net.risesoft.service.Impl;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import net.risesoft.entity.DataAssetsTestingInfo;
import net.risesoft.entity.MetadataConfig;
import net.risesoft.enums.CategoryEnums;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
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
import net.risesoft.util.ArchiveDetection;
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
            dataAssetsRepository.findByCategoryIdAndFileStatusAndIsDeleted(categoryId, fileStatus, isDeleted, pageable);
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
            joinSql = "LEFT JOIN Y9_ARCHIVES_DOCUMENT_FILE C ON T.ARCHIVES_ID = C.DETAIL_ID";
        } else if (customId.equals(CategoryEnums.IMAGE.getEnName())) {
            joinSql = "LEFT JOIN Y9_ARCHIVES_IMAGE_FILE C ON T.ARCHIVES_ID = C.DETAIL_ID";
        } else if (customId.equals(CategoryEnums.AUDIO.getEnName())) {
            joinSql = "LEFT JOIN Y9_ARCHIVES_AUDIO_FILE C ON T.ARCHIVES_ID = C.DETAIL_ID";
        } else if (customId.equals(CategoryEnums.VIDEO.getEnName())) {
            joinSql = "LEFT JOIN Y9_ARCHIVES_VIDEO_FILE C ON T.ARCHIVES_ID = C.DETAIL_ID";
        } else {
            CategoryTable categoryTable = categoryTableService.findByCategoryMark(customId);
            if (null != categoryTable) {
                joinSql = "LEFT JOIN " + categoryTable.getTableName() + " C ON T.ARCHIVES_ID = C.DETAIL_ID";
            }
        }
        String sql = "SELECT T.* FROM Y9_ARCHIVES_DETAILS T " + joinSql
            + " WHERE T.CATEGORY_ID = ? AND T.FILE_STATUS = ? AND T.IS_DELETED = ? AND " + conditionSql
            + " ORDER BY T.CREATE_TIME DESC";
        String countSql = "SELECT COUNT(T.ARCHIVES_ID) FROM Y9_ARCHIVES_DETAILS T " + joinSql
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
    public DataAssets findByArchives_id(Long id) {
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
            DataAssets archives = this.findByArchives_id(id);
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
            DataAssets archives = this.findByArchives_id(id);
            if (null != archives) {
                archives.setFileStatus(1);
                this.save(archives);
            }
        }
    }

    @Override
    @Transactional(readOnly = false)
    public Y9Result<String> createArchivesNo(String categoryId, Long[] ids) {
        String tenantId = Y9LoginUserHolder.getTenantId();
        DataCatalog dataCatalog = dataCatalogApiClient.getTreeRoot(tenantId, categoryId).getData();
        for (Long id : ids) {
            DataAssets archives = this.findByArchives_id(id);
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
                System.out.println("档案号：" + archiveNo);
                archives.setArchiveNo(archiveNo);
            } else {
                return Y9Result.failure("未找到档案号规则");
            }
            this.save(archives);
        }
        return Y9Result.success("创建档案号成功");
    }

    @Override
    public List<DataAssets> findByArchivesIdIn(Long[] ids) {
        return dataAssetsRepository.findByArchivesIdIn(ids);
    }

    @Override
    public Y9Result<Map<String, Object>> checkArchives(String processName, Long[] archivesId) {
        String tenantId = Y9LoginUserHolder.getTenantId();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Map<String, Object> map = new HashMap<>();
        for (Long id : archivesId) {
            DataAssets archives = this.findByArchives_id(id);
            if (null != archives) {
                // 完整性检查（1、档案是否包含完整的文档、图片、音频、视频等附件，2、档案必填字段是否完整，）
                // 真实性检查（验证档案文件上传时的哈希值和数据库中记录的哈希值是否一致，验证档案文件是否损坏，验证档案是否存在）
                // 可用性检查（验证档案文件是否存在，文件正常）
                // 【GD-1-1】档案文件上传人是否存在
                DataAssetsTestingInfo testingInfo = new DataAssetsTestingInfo();
                String testingInfoId = Y9IdGenerator.genId(IdType.SNOWFLAKE);
                testingInfo.setId(testingInfoId);
                testingInfo.setArchivesId(id);
                testingInfo.setTestingStep(processName);
                testingInfo.setTestingTime(sdf.format(new Date()));
                /*** 关于档案文件 start ******/
                ArchiveDetection.checkArchivesFile(testingInfoId, archives);
                /*** 关于档案文件 end ******/
                /*** 关于档案元数据 start ******/
                // 1、根据档案检测必填配置，检测数据是否完整
                ArchiveDetection.checkRequiredMetadata(testingInfoId, archives);
                // 2、验证数据库表数据的长度是否大于初始建立表的配置长度（防止有人为在数据库修改表字段长度后，数据变长跟初始配置如字段长度和字段类型不一致的情况）
                // 3、检查元数据是否存在乱码
                ArchiveDetection.checkDataTable(testingInfoId, archives);

                /*** 关于档案元数据 end ******/
                // 【GD-1-10、GD-1-14\GD-2-2】查询档案文件的哈希值
                // 【GD-1-11-1、GD-2-1、】档案是否包含文件
                // 【GD-1-2、GD-1-3】验证数据库表数据的长度是否大于初始建立表的配置长度（防止在数据库修改表字段长度后，数据变长跟初始配置如字段长度和字段类型不一致的情况）
                // 【GD-1-4、GD-1-5、GD-1-6、GD-2-11、GD-2-3、GD-2-4、GD-2-6】查询档案元数据的必填值
                // 【GD-2-5】档案过程信息是否完整（暂时不用）
                // 【GD-3-1】检查元数据是否存在乱码
                // 【GD-3-4】验证档案文件下载是否有数据、文件是否有后缀

            }
        }
        return Y9Result.success(map, "检测成功");
    }

}
