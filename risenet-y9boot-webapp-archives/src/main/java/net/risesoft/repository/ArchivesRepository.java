package net.risesoft.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import net.risesoft.entity.Archives;

public interface ArchivesRepository extends JpaRepository<Archives, String>, JpaSpecificationExecutor<Archives> {

}
