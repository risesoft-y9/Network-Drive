package net.risesoft.service;

import net.risesoft.entity.Chunk;

public interface ChunkService {

    void saveChunk(Chunk chunk);

    boolean checkChunk(String identifier, Integer chunkNumber);
}
