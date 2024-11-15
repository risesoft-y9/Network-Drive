package net.risesoft.service.Impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import net.risesoft.entity.AudioFile;
import net.risesoft.repository.AudioFileRepository;
import net.risesoft.service.AudioFileService;
import net.risesoft.util.EntityToMapConverter;

@Service
@RequiredArgsConstructor
public class AudioFileServiceImpl implements AudioFileService {

    private final AudioFileRepository audioFileRepository;

    @Override
    public Map<String, Object> findByDetailId(Long detailId) {
        Map<String, Object> map = new HashMap<>();
        AudioFile audioFile = audioFileRepository.findByDetailId(detailId);
        if (null != audioFile) {
            map.putAll(EntityToMapConverter.convertToMap(audioFile));
        }
        return map;
    }

    @Override
    public AudioFile save(AudioFile audioFile) {
        return audioFileRepository.save(audioFile);
    }

    @Override
    public AudioFile findById(Long id) {
        return audioFileRepository.findById(id).orElse(null);
    }

}
