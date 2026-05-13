package net.risesoft.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import net.risesoft.entity.FileNodeShare;

public interface FileNodeShareRepository
    extends JpaRepository<FileNodeShare, String>, JpaSpecificationExecutor<FileNodeShare> {

    FileNodeShare findByFileNodeIdAndToOrgUnitId(String fileNodeId, String orgUnitId);

    List<FileNodeShare> findByPersonIdAndFileOptType(String personId, String optType);

    List<FileNodeShare> findByPersonIdAndFileNodeId(String personId, String fileNodeId);

    List<FileNodeShare> findByToOrgUnitIdIn(List<String> guidArray);

    List<FileNodeShare> findByFileNodeIdAndToOrgUnitIdIn(String fileNodeId, List<String> guidArray);

    List<FileNodeShare> findByFileNodeIdAndToOrgUnitIdLike(String fileNodeId, String guidArray);

    List<FileNodeShare> findByFileNodeId(String fileNodeId);

    Page<FileNodeShare> findByFileNodeIdAndFileOptType(String fileNodeId, String fileOptType, Pageable pageable);

}
