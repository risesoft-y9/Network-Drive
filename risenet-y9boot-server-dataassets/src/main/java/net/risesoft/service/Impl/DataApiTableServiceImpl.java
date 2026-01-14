package net.risesoft.service.Impl;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import net.risesoft.entity.DataApiTableEntity;
import net.risesoft.entity.TableForeignKeyEntity;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.repository.DataApiTableRepository;
import net.risesoft.repository.TableForeignKeyRepository;
import net.risesoft.service.DataApiTableService;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.json.Y9JsonUtil;
import y9.client.rest.platform.permission.cache.PersonRoleApiClient;

@Service
@RequiredArgsConstructor
public class DataApiTableServiceImpl implements DataApiTableService {

    private final DataApiTableRepository dataApiTableRepository;
    private final PersonRoleApiClient personRoleApiClient;
    private final TableForeignKeyRepository tableForeignKeyRepository;

    @Override
    public Page<DataApiTableEntity> findByTableNameAndSubscribeId(String tableName, String subscribeId, Pageable pageable) {
        Page<DataApiTableEntity> pageList = null;
        if(StringUtils.isBlank(tableName)) {
            pageList = dataApiTableRepository.findByTenantIdAndCreatorIdAndSubscribeId(Y9LoginUserHolder.getTenantId(),
            Y9LoginUserHolder.getPersonId(), subscribeId, pageable);
        } else {
            pageList = dataApiTableRepository.findByTableNameContainingAndTenantIdAndCreatorIdAndSubscribeId(tableName, Y9LoginUserHolder.getTenantId(), 
            Y9LoginUserHolder.getPersonId(), subscribeId, pageable);
        }
        // 查询出增量字段信息添加进去返回
        List<DataApiTableEntity> content = pageList.getContent();
        if(content != null && !content.isEmpty()) {
            for(DataApiTableEntity entity : content) {
                TableForeignKeyEntity foreignKeys = tableForeignKeyRepository.findByTableNameAndDataSourceId(entity.getTableName(), entity.getDataSourceId());
                entity.setIncrementField(foreignKeys != null ? foreignKeys.getIncrementField() : "");
            }
        }
        return pageList;
    }

    @Override
    public String save(DataApiTableEntity dataApiTableEntity) {
        if (dataApiTableEntity.getId() == null) {
            // 判断是否已经申请过该表，禁用的除外
            DataApiTableEntity existingEntity = dataApiTableRepository.findByTableNameAndDataSourceIdAndCreatorIdAndIsDeleted(
            dataApiTableEntity.getTableName(), dataApiTableEntity.getDataSourceId(), Y9LoginUserHolder.getPersonId(), false);
            if (existingEntity != null) {
                throw new IllegalArgumentException("您已申请过该表，无需重复申请");
            }

            dataApiTableEntity.setCreatorId(Y9LoginUserHolder.getPersonId());
            dataApiTableEntity.setCreator(Y9LoginUserHolder.getUserInfo().getName());
        } else {
            DataApiTableEntity oldEntity = dataApiTableRepository.findById(dataApiTableEntity.getId()).orElse(null);
            if (oldEntity == null) {
                throw new IllegalArgumentException("数据接口表不存在");
            }
            dataApiTableEntity.setCreatorId(oldEntity.getCreatorId());
            dataApiTableEntity.setCreator(oldEntity.getCreator());

            dataApiTableEntity.setUpdateUserId(Y9LoginUserHolder.getPersonId());
            dataApiTableEntity.setUpdateUser(Y9LoginUserHolder.getUserInfo().getName());
        }
        dataApiTableEntity.setTenantId(Y9LoginUserHolder.getTenantId());

        // 将表的查询字段存为外键信息
        if (StringUtils.isNotBlank(dataApiTableEntity.getQueryFields())) {
            // 检查是否已存在相同的外键
            TableForeignKeyEntity existingForeignKey = tableForeignKeyRepository.
            findByTableNameAndForeignKeyColumnAndDataSourceId(dataApiTableEntity.getTableName(), dataApiTableEntity.getQueryFields(),
            dataApiTableEntity.getDataSourceId());
            if (existingForeignKey == null) {
                TableForeignKeyEntity tableForeignKeyEntity = new TableForeignKeyEntity();
                tableForeignKeyEntity.setId(Y9IdGenerator.genId());
                tableForeignKeyEntity.setTableName(dataApiTableEntity.getTableName());
                tableForeignKeyEntity.setForeignKeyColumn(dataApiTableEntity.getQueryFields());
                tableForeignKeyEntity.setDataSourceId(dataApiTableEntity.getDataSourceId());
                tableForeignKeyRepository.save(tableForeignKeyEntity);
            }
        }
        // 保存增量字段
        if (StringUtils.isNotBlank(dataApiTableEntity.getIncrementField())) {
            TableForeignKeyEntity tableForeignKeyEntity = tableForeignKeyRepository.
            findByTableNameAndDataSourceId(dataApiTableEntity.getTableName(), dataApiTableEntity.getDataSourceId());
            if (tableForeignKeyEntity != null) {
                tableForeignKeyEntity.setIncrementField(dataApiTableEntity.getIncrementField());
                tableForeignKeyRepository.save(tableForeignKeyEntity);
            } else {
                tableForeignKeyEntity = new TableForeignKeyEntity();
                tableForeignKeyEntity.setId(Y9IdGenerator.genId());
                tableForeignKeyEntity.setTableName(dataApiTableEntity.getTableName());
                tableForeignKeyEntity.setIncrementField(dataApiTableEntity.getIncrementField());
                tableForeignKeyEntity.setDataSourceId(dataApiTableEntity.getDataSourceId());
                tableForeignKeyRepository.save(tableForeignKeyEntity);
            }
        }
        dataApiTableRepository.save(dataApiTableEntity);
        return "保存成功";
    }

    @Override
    public void deleteById(Long id) {
        DataApiTableEntity dataApiTableEntity = findById(id);
        if (dataApiTableEntity == null) {
            throw new IllegalArgumentException("数据不存在");
        }
        // 判断是否系统管理员
        boolean isAdmin = personRoleApiClient.hasRole(Y9LoginUserHolder.getTenantId(), "dataAssets", 
        null, "系统管理员", Y9LoginUserHolder.getPersonId()).getData();
        // 已禁用设为未禁用，未禁用设为已禁用
        if (isAdmin && StringUtils.isNotBlank(dataApiTableEntity.getOwner())) {
            // 判断该表在禁用状态下有没有新的在用申请，有的话不能解禁
            if(dataApiTableEntity.getIsDeleted()) {
                DataApiTableEntity existingEntity = dataApiTableRepository.findByTableNameAndDataSourceIdAndCreatorIdAndIsDeleted(
                dataApiTableEntity.getTableName(), dataApiTableEntity.getDataSourceId(), dataApiTableEntity.getCreatorId(), false);
                if (existingEntity != null) {
                    throw new IllegalArgumentException("该表已存在新的申请，无法解禁");
                }
            }
            dataApiTableEntity.setIsDeleted(!dataApiTableEntity.getIsDeleted());
            dataApiTableEntity.setUpdateUserId(Y9LoginUserHolder.getPersonId());
            dataApiTableEntity.setUpdateUser(Y9LoginUserHolder.getUserInfo().getName());
            dataApiTableRepository.save(dataApiTableEntity);
        } else {
            if(StringUtils.isNotBlank(dataApiTableEntity.getOwner())) {
                throw new IllegalArgumentException("没有操作权限，刷新下页面");
            }
            // 未审核的申请可以直接删除
            dataApiTableRepository.deleteById(id);
        }
    }

    @Override
    public DataApiTableEntity findById(Long id) {
        DataApiTableEntity dataApiTableEntity = dataApiTableRepository.findById(id).orElse(null);
        if (dataApiTableEntity != null) {
            // 增量字段
            TableForeignKeyEntity tableForeignKeyEntity = tableForeignKeyRepository.
            findByTableNameAndDataSourceId(dataApiTableEntity.getTableName(), dataApiTableEntity.getDataSourceId());
            if (tableForeignKeyEntity != null && StringUtils.isNotBlank(tableForeignKeyEntity.getIncrementField())) {
                dataApiTableEntity.setIncrementField(tableForeignKeyEntity.getIncrementField());
            }
        }
        return dataApiTableEntity;
    }

    @Override
    public List<String> findForeignKeysByTableName(String tableName, String dataSourceId) {
        // 检查是否存在外键
        TableForeignKeyEntity tableForeignKeyEntity = tableForeignKeyRepository.findByTableNameAndDataSourceId(tableName, dataSourceId);
        if (tableForeignKeyEntity == null) {
            return Collections.emptyList();
        }
        String column = tableForeignKeyEntity.getForeignKeyColumn();
        return Y9JsonUtil.readList(column, String.class);
    }
}