package net.risesoft.service;

import java.util.Map;

import net.risesoft.entity.VideoFile;

public interface VideoFileService {

    Map<String, Object> findByDetailId(String detailId);

    VideoFile save(VideoFile videoFile);

    VideoFile findById(Long id);
}
