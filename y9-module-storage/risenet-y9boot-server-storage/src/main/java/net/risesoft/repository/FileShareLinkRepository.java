package net.risesoft.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import net.risesoft.entity.FileShareLink;

public interface FileShareLinkRepository
    extends JpaRepository<FileShareLink, String>, JpaSpecificationExecutor<FileShareLink> {

    FileShareLink findByLinkKey(String linkKey);
}
