package net.risesoft.service.Impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import net.risesoft.entity.DocumentFile;
import net.risesoft.repository.DocumentFileRepository;
import net.risesoft.service.DocumentFileService;
import net.risesoft.util.EntityToMapConverter;

@Service
@RequiredArgsConstructor
public class DocumentFileServiceImpl implements DocumentFileService {

    private final DocumentFileRepository documentFileRepository;

    @Override
    public Map<String, Object> findByDetailId(Long detailId) {
        Map<String, Object> map = new HashMap<>();
        DocumentFile documentFile = documentFileRepository.findByDetailId(detailId);
        if (null != documentFile && null != documentFile.getId()) {
            map.putAll(EntityToMapConverter.convertToMap(documentFile));
        }
        return map;
    }

    @Override
    public DocumentFile save(DocumentFile documentFile) {
        return documentFileRepository.save(documentFile);
    }

    @Override
    public DocumentFile findById(Long id) {
        return documentFileRepository.findById(id).orElse(null);
    }

}
