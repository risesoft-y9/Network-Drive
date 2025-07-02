package net.risesoft.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import net.risesoft.entity.Chunk;

public interface ChunkRepository extends JpaRepository<Chunk, String>, JpaSpecificationExecutor<Chunk> {}
