package net.risesoft.service;

import java.util.Map;

import net.risesoft.entity.AudioFile;

public interface AudioFileService {

    Map<String, Object> findByDetailId(Long detailId);

    AudioFile save(AudioFile audioFile);

    AudioFile findById(Long id);

}
