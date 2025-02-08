package net.risesoft.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import net.risesoft.entity.FileInfo;

public interface FileInfoRepository extends JpaRepository<FileInfo, String>, JpaSpecificationExecutor<FileInfo> {}
