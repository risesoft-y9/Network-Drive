package net.risesoft.service.Impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import net.risesoft.entity.ImageFile;
import net.risesoft.repository.ImageFileRepository;
import net.risesoft.service.ImageFileService;
import net.risesoft.util.EntityToMapConverter;

@Service
@RequiredArgsConstructor
public class ImageFileServiceImpl implements ImageFileService {

    private final ImageFileRepository imageFileRepository;

    @Override
    public Map<String, Object> findByDetailId(Long detailId) {
        Map<String, Object> map = new HashMap<>();
        ImageFile imageFile = imageFileRepository.findByDetailId(detailId);
        if (null != imageFile) {
            map.putAll(EntityToMapConverter.convertToMap(imageFile));
        }
        return map;
    }

    @Override
    public ImageFile save(ImageFile imageFile) {
        return imageFileRepository.save(imageFile);
    }

    @Override
    public ImageFile findById(Long id) {
        return imageFileRepository.findById(id).orElse(null);
    }

}
