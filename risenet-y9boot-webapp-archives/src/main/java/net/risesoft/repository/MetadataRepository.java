package net.risesoft.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import net.risesoft.entity.Metadata;

public interface MetadataRepository extends JpaRepository<Metadata, String>, JpaSpecificationExecutor<Metadata> {

}
