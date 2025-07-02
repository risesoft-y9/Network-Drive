package net.risesoft.service;

import java.util.Map;

import net.risesoft.entity.DocumentFile;

public interface DocumentFileService {

    Map<String, Object> findByDetailId(Long detailId);

    DocumentFile save(DocumentFile documentFile);

    DocumentFile findById(Long id);

}
