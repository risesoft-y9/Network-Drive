package net.risesoft.service.Impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import net.risesoft.entity.Archives;
import net.risesoft.service.ArchivesService;
import net.risesoft.service.MetadataConfigService;

@Service("archivesService")
@RequiredArgsConstructor
public class ArchivesServiceImpl implements ArchivesService {

    private final MetadataConfigService metadataConfigService;

    @Override
    public List<Map<String, Object>> getArchivesFileList() {
        return metadataConfigService.getEntityFieldList(Archives.class);
    }
}
