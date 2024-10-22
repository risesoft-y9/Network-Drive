package net.risesoft.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import net.risesoft.entity.MetadataField;

public interface MetadataFieldRepository
    extends JpaRepository<MetadataField, String>, JpaSpecificationExecutor<MetadataField> {

}
