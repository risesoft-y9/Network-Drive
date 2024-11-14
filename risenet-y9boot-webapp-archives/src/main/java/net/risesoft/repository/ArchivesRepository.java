package net.risesoft.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import net.risesoft.entity.Archives;

public interface ArchivesRepository extends JpaRepository<Archives, String>, JpaSpecificationExecutor<Archives> {

    Page<Archives> findByCategoryId(String categoryId, Pageable pageable);

    @Query(value = "SELECT * FROM Y9_ARCHIVES_DETAILS a WHERE a.archives_id = ?1", nativeQuery = true)
    Archives findByArchives_id(Long archivesId);
}
