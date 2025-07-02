package net.risesoft.service.Impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import net.risesoft.entity.VideoFile;
import net.risesoft.repository.VideoFileRepository;
import net.risesoft.service.VideoFileService;
import net.risesoft.util.EntityOrTableUtils;

@Service
@RequiredArgsConstructor
public class VideoFileServiceImpl implements VideoFileService {

    private final VideoFileRepository videoFileRepository;

    @Override
    public Map<String, Object> findByDetailId(Long detailId) {
        Map<String, Object> map = new HashMap<>();
        VideoFile videoFile = videoFileRepository.findByDetailId(detailId);
        if (null != videoFile) {
            map.putAll(EntityOrTableUtils.convertToMap(videoFile));
        }
        return map;
    }

    @Override
    public VideoFile save(VideoFile videoFile) {
        return videoFileRepository.save(videoFile);
    }

    @Override
    public VideoFile findById(Long id) {
        return videoFileRepository.findById(id).orElse(null);
    }

}
