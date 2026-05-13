package net.risesoft.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import net.risesoft.entity.FileTag;

public interface FileTagRepository extends JpaRepository<FileTag, String>, JpaSpecificationExecutor<FileTag> {

    Page<FileTag> findByTagNameLike(String tagName, Pageable pageable);

    List<FileTag> findByTagName(String tagName);

    List<FileTag> findByTagNameLike(String tagName);

    @Query("SELECT MAX(ft.tabIndex) FROM FileTag ft")
    Integer getMaxTabIndex();
}
