package net.risesoft.controller;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.entity.Chunk;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.FileChunkService;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/vue/files")
public class DataAssetsFileController {

    private final FileChunkService chunkService;

    @PostMapping(value = "/chunk")
    public Y9Result<String> uploadChunk1(Chunk chunk) {
        MultipartFile file = chunk.getFile();
        LOGGER.debug("file originName: {}, chunkNumber: {}", file.getOriginalFilename(), chunk.getChunkNumber());
        return null;
    }

    @GetMapping(value = "/chunk")
    public Y9Result<Object> checkChunk(Chunk chunk, HttpServletResponse response) {
        LOGGER.debug("文件 {} 验证, uuid:{}", chunk.getFilename(), chunk.getIdentifier());
        if (chunkService.checkChunk(chunk.getIdentifier(), chunk.getChunkNumber())) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        }
        return Y9Result.success(chunk);
    }

}
