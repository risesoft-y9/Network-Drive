package net.risesoft.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import net.risesoft.entity.AssetsLabelEntity;

public interface AssetsLabelRepository
    extends JpaRepository<AssetsLabelEntity, String>, JpaSpecificationExecutor<AssetsLabelEntity> {

    AssetsLabelEntity findByAssetsId(Long assetsId);
}
