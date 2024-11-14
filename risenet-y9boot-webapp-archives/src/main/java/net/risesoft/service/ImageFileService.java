package net.risesoft.service;

import java.util.Map;

import net.risesoft.entity.ImageFile;

public interface ImageFileService {

    Map<String, Object> findByDetailId(String detailId);

    ImageFile save(ImageFile imageFile);

    ImageFile findById(Long id);
}
