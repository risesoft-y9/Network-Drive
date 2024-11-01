package net.risesoft.service.Impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import net.risesoft.consts.SqlConstants;
import net.risesoft.entity.CategoryTable;
import net.risesoft.entity.CategoryTableField;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.pojo.Y9Result;
import net.risesoft.repository.CategoryTableFieldRepository;
import net.risesoft.repository.CategoryTableRepository;
import net.risesoft.service.CategoryTableManagerService;
import net.risesoft.service.CategoryTableService;
import net.risesoft.service.MetadataConfigService;
import net.risesoft.util.Y9FormDbMetaDataUtil;
import net.risesoft.y9.sqlddl.pojo.DbColumn;

@Slf4j
@Service
@Transactional(value = "rsTenantTransactionManager", readOnly = true)
public class CategoryTableServiceImpl implements CategoryTableService {

    private final JdbcTemplate jdbcTemplate4Tenant;

    private final CategoryTableRepository categoryTableRepository;

    private final CategoryTableFieldRepository categoryTableFieldRepository;

    private final CategoryTableManagerService categoryTableManagerService;

    private final MetadataConfigService metadataConfigService;

    public CategoryTableServiceImpl(@Qualifier("jdbcTemplate4Tenant") JdbcTemplate jdbcTemplate4Tenant,
        CategoryTableRepository categoryTableRepository, CategoryTableFieldRepository categoryTableFieldRepository,
        CategoryTableManagerService categoryTableManagerService, MetadataConfigService metadataConfigService) {
        this.jdbcTemplate4Tenant = jdbcTemplate4Tenant;
        this.categoryTableRepository = categoryTableRepository;
        this.categoryTableFieldRepository = categoryTableFieldRepository;
        this.categoryTableManagerService = categoryTableManagerService;
        this.metadataConfigService = metadataConfigService;
    }

    @Override
    public CategoryTable checkCategoryIsExistTable(String categoryMark) {
        List<CategoryTable> tableList = categoryTableRepository.findByCategoryMark(categoryMark);
        CategoryTable table = null;
        if (!tableList.isEmpty() || tableList.size() != 0) {
            table = tableList.get(0);
        }
        return table;
    }

    @Override
    @Transactional(readOnly = false)
    public Y9Result<Object> addDataBaseTable(String tableName, String systemName, String systemCnName) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            CategoryTable y9Table = new CategoryTable();
            y9Table.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
            y9Table.setCreateTime(sdf.format(new Date()));
            y9Table.setOldTableName("");
            y9Table.setTableCnName(tableName);
            y9Table.setCategoryMark(systemName);
            y9Table.setCategoryName(systemCnName);
            y9Table.setTableName(tableName);
            categoryTableRepository.save(y9Table);
            List<DbColumn> list = Y9FormDbMetaDataUtil
                .listAllColumns(Objects.requireNonNull(jdbcTemplate4Tenant.getDataSource()), tableName, "%");
            int order = 1;
            for (DbColumn dbColumn : list) {
                CategoryTableField y9TableField = new CategoryTableField();
                y9TableField.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
                y9TableField.setFieldCnName(
                    StringUtils.isNotBlank(dbColumn.getComment()) ? dbColumn.getComment() : dbColumn.getColumnName());
                y9TableField.setFieldLength(dbColumn.getDataLength());
                y9TableField.setFieldType(dbColumn.getTypeName() + "(" + dbColumn.getDataLength() + ")");
                y9TableField.setIsMayNull(Boolean.TRUE.equals(dbColumn.getNullable()) ? 1 : 0);
                y9TableField.setState(1);
                y9TableField.setTableId(y9Table.getId());
                y9TableField.setTableName(tableName);
                y9TableField.setIsSystemField(Boolean.TRUE.equals(dbColumn.getPrimaryKey()) ? 1 : 0);
                y9TableField.setDisplayOrder(order);
                y9TableField.setFieldName(dbColumn.getColumnName());
                order += 1;
                categoryTableFieldRepository.save(y9TableField);
            }
            return Y9Result.successMsg("添加数据表成功");
        } catch (Exception e) {
            LOGGER.error("添加数据表失败", e);
            return Y9Result.failure("添加数据表失败");
        }
    }

    @Override
    @Transactional(readOnly = false)
    public Y9Result<Object> buildTable(CategoryTable table, List<Map<String, Object>> listMap) {
        try {
            boolean tableExist = true;
            if (StringUtils.isEmpty(table.getId())) {
                tableExist = false;
            } else {
                CategoryTable oldTable = this.findById(table.getId());
                if (null == oldTable) {
                    tableExist = false;
                }
            }
            CategoryTable tableTemp = this.saveOrUpdate(table);
            if (tableTemp != null && tableTemp.getId() != null) {
                String tableId = tableTemp.getId();
                if (!listMap.isEmpty()) {
                    List<String> ids = new ArrayList<>();
                    List<DbColumn> dbcs;
                    if (tableExist) {
                        List<CategoryTableField> list =
                            categoryTableFieldRepository.findByTableIdOrderByDisplayOrderAsc(tableId);
                        dbcs = saveField(tableTemp.getCategoryMark(), tableId, tableTemp.getTableName(), listMap, ids);
                        for (CategoryTableField categoryTableField : list) {
                            if (!ids.contains(categoryTableField.getId())) {
                                categoryTableFieldRepository.delete(categoryTableField);
                            }
                        }
                    } else {
                        dbcs = saveField(tableTemp.getCategoryMark(), tableId, tableTemp.getTableName(), listMap, ids);
                    }
                    return categoryTableManagerService.buildTable(tableTemp, dbcs);
                }
            }
            return Y9Result.successMsg("创建数据表成功");
        } catch (Exception e) {
            LOGGER.error("创建数据表失败", e);
            return Y9Result.failure("创建数据表失败");
        }
    }

    @Override
    @Transactional
    public Y9Result<Object> delete(String ids) {
        try {
            String[] id = ids.split(",");
            for (String idTemp : id) {
                categoryTableRepository.deleteById(idTemp);
                categoryTableFieldRepository.deleteByTableId(idTemp);
            }
            return Y9Result.successMsg("删除数据表成功");
        } catch (Exception e) {
            LOGGER.error("删除数据表失败", e);
            return Y9Result.failure("删除数据表失败");
        }
    }

    @Override
    public CategoryTable findById(String id) {
        return categoryTableRepository.findById(id).orElse(null);
    }

    @Override
    public String getAlltableName() {
        StringBuilder tableNames = new StringBuilder();
        try {
            List<CategoryTable> list = categoryTableRepository.findAll();
            for (CategoryTable y9Table : list) {
                if (tableNames.length() != 0) {
                    tableNames.append(",").append(y9Table.getTableName());
                } else {
                    tableNames.append(y9Table.getTableName());
                }
            }
        } catch (Exception e) {
            LOGGER.error("获取所有表名失败", e);
        }
        return tableNames.toString();
    }

    @Override
    public List<Map<String, String>> getAllTables(String name) {
        try {
            DataSource dataSource = Objects.requireNonNull(jdbcTemplate4Tenant.getDataSource());
            List<Map<String, String>> list = new ArrayList<>();
            if (StringUtils.isNotBlank(name)) {
                List<Map<String, String>> list1 = Y9FormDbMetaDataUtil.listAllTables(dataSource, "%" + name + "%");
                for (Map<String, String> m : list1) {
                    if (m.get("name").startsWith("y9_form_") || m.get("name").startsWith("Y9_FORM_")) {
                        list.add(m);
                    }
                }
            } else {
                list = Y9FormDbMetaDataUtil.listAllTables(dataSource, "y9_form_%");
                String dialect = Y9FormDbMetaDataUtil.getDatabaseDialectName(dataSource);
                if (SqlConstants.DBTYPE_ORACLE.equals(dialect)) {
                    List<Map<String, String>> list1 = Y9FormDbMetaDataUtil.listAllTables(dataSource, "Y9_ARCHIVES_%");
                    list.addAll(list1);
                } else if (SqlConstants.DBTYPE_DM.equals(dialect)) {
                    List<Map<String, String>> list1 = Y9FormDbMetaDataUtil.listAllTables(dataSource, "Y9_ARCHIVES_%");
                    list.addAll(list1);
                }
            }
            return list;
        } catch (Exception e) {
            LOGGER.error("获取所有表失败", e);
        }
        return Collections.emptyList();
    }

    /**
     * 解析字段类型，
     *
     * @param type
     * @return
     */
    private final String getFieldType(String type) {
        type = type.substring(0, type.lastIndexOf("("));
        return type;
    }

    /**
     * 保存字段信息
     *
     * @param tableId
     * @param tableName
     * @param listMap
     * @param ids
     * @return
     */
    @Transactional
    public List<DbColumn> saveField(String viewType, String tableId, String tableName,
        List<Map<String, Object>> listMap, List<String> ids) {
        List<DbColumn> dbcs = new ArrayList<>();
        int order = 1;
        String optType = "add";
        CategoryTableField fieldTemp = null;
        for (Map<String, Object> m : listMap) {
            String id = (String)m.get("id");
            Integer isSystemField = (Integer)m.get("isSystemField");
            String fieldCnName = (String)m.get("fieldCnName");
            String fieldName = (String)m.get("fieldName");
            Integer fieldLength = (Integer)m.get("fieldLength");
            String fieldType = (String)m.get("fieldType");
            Integer isMayNull = (Integer)m.get("isMayNull");
            String oldFieldName = (String)m.get("oldFieldName");
            if (StringUtils.isEmpty(id)) {
                fieldTemp = new CategoryTableField();
                fieldTemp.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
            } else {
                ids.add(id);
                fieldTemp = categoryTableFieldRepository.findById(id).orElse(null);
                if (null == fieldTemp) {
                    fieldTemp = new CategoryTableField();
                    fieldTemp.setId(id);
                } else {
                    optType = "edit";
                }
            }
            fieldTemp.setFieldCnName(fieldCnName);
            fieldTemp.setFieldLength(fieldLength);
            fieldTemp.setFieldName(fieldName);
            fieldTemp.setFieldType(fieldType);
            fieldTemp.setIsMayNull(isMayNull);
            fieldTemp.setIsSystemField(isSystemField);
            fieldTemp.setOldFieldName(StringUtils.isBlank(oldFieldName) ? "" : oldFieldName);
            fieldTemp.setDisplayOrder(order);
            fieldTemp.setTableId(tableId);
            fieldTemp.setTableName(tableName);
            order += 1;

            DbColumn dbColumn = new DbColumn();
            dbColumn.setColumnName(fieldTemp.getFieldName());
            dbColumn.setIsPrimaryKey(fieldTemp.getIsSystemField());
            dbColumn.setPrimaryKey(fieldTemp.getIsSystemField() == 1);
            dbColumn.setNullable(fieldTemp.getIsMayNull() == 1);
            dbColumn.setTypeName(getFieldType(fieldTemp.getFieldType()));
            dbColumn.setDataLength(fieldTemp.getFieldLength());
            dbColumn.setComment(fieldTemp.getFieldCnName());
            dbColumn.setColumnNameOld(fieldTemp.getOldFieldName());
            dbColumn.setDataPrecision(0);
            dbColumn.setDataScale(0);
            dbColumn.setDataType(0);
            dbColumn.setIsNull(fieldTemp.getIsMayNull());
            dbColumn.setTableName(tableName);
            dbcs.add(dbColumn);
            categoryTableFieldRepository.save(fieldTemp);
            metadataConfigService.save(optType, viewType, fieldTemp.getId(), fieldTemp.getFieldName(),
                fieldTemp.getFieldCnName(), fieldTemp.getFieldType(), "库表添加");
        }
        // 元数据基础数据
        if ("add".equals(optType)) {
            metadataConfigService.initBaseMetadataConfig(viewType);
        }
        return dbcs;
    }

    @Override
    @Transactional
    public CategoryTable saveOrUpdate(CategoryTable table) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            if (StringUtils.isBlank(table.getId())) {
                table.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
            }
            DataSource dataSource = Objects.requireNonNull(jdbcTemplate4Tenant.getDataSource());
            String dialect = Y9FormDbMetaDataUtil.getDatabaseDialectName(dataSource);
            if (SqlConstants.DBTYPE_MYSQL.equals(dialect)) {
                table.setTableName(table.getTableName().toLowerCase());
            }
            table.setCreateTime(sdf.format(new Date()));
            return categoryTableRepository.save(table);
        } catch (Exception e) {
            LOGGER.error("保存失败", e);
            throw new Exception("CategoryTableServiceImpl saveOrUpdate error");
        }
    }

    @Override
    @Transactional(readOnly = false)
    public Y9Result<Object> updateTable(CategoryTable table, List<Map<String, Object>> listMap, String type) {
        try {
            CategoryTable savetable = this.saveOrUpdate(table);
            if (savetable != null && savetable.getId() != null) {
                String tableId = savetable.getId();
                String tableName = savetable.getTableName();
                if (!listMap.isEmpty()) {
                    List<String> ids = new ArrayList<>();
                    List<CategoryTableField> list =
                        categoryTableFieldRepository.findByTableIdOrderByDisplayOrderAsc(tableId);
                    List<DbColumn> dbcs = new ArrayList<>();
                    dbcs = saveField(savetable.getCategoryMark(), tableId, tableName, listMap, ids);
                    /**
                     * 删除页面上已删除的字段
                     */
                    for (CategoryTableField y9TableField : list) {
                        if (!ids.contains(y9TableField.getId())) {
                            categoryTableFieldRepository.delete(y9TableField);
                        }
                    }
                    /**
                     * 修改表结构
                     */
                    boolean isSave = "save".equals(type);
                    if (!isSave) {
                        return categoryTableManagerService.addFieldToTable(savetable, dbcs);
                    }
                }
            }
            return Y9Result.successMsg("操作成功");
        } catch (Exception e) {
            LOGGER.error("操作失败", e);
            return Y9Result.failure("操作失败");
        }
    }
}
