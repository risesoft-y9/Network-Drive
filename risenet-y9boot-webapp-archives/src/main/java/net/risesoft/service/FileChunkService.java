package net.risesoft.service;

import net.risesoft.entity.Chunk;

public interface FileChunkService {

    void saveChunk(Chunk chunk);

    boolean checkChunk(String identifier, Integer chunkNumber);
}
