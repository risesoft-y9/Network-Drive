package net.risesoft.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import net.risesoft.entity.TableForeignKeyEntity;

public interface TableForeignKeyRepository
    extends JpaRepository<TableForeignKeyEntity, String>, JpaSpecificationExecutor<TableForeignKeyEntity> {

    TableForeignKeyEntity findByTableNameAndForeignKeyColumnAndDataSourceId(String tableName, String foreignKeyColumn,
        String dataSourceId);

    TableForeignKeyEntity findByTableNameAndDataSourceId(String tableName, String dataSourceId);
}
