package net.risesoft.service.Impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import net.risesoft.entity.Archives;
import net.risesoft.enums.CategoryEnums;
import net.risesoft.model.platform.DataCatalog;
import net.risesoft.repository.ArchivesRepository;
import net.risesoft.repository.AudioFileRepository;
import net.risesoft.repository.DocumentFileRepository;
import net.risesoft.repository.ImageFileRepository;
import net.risesoft.repository.VideoFileRepository;
import net.risesoft.service.ArchivesService;
import net.risesoft.service.CategoryTableService;
import net.risesoft.y9.Y9LoginUserHolder;

import y9.client.rest.platform.resource.DataCatalogApiClient;

@Service("archivesService")
@RequiredArgsConstructor
public class ArchivesServiceImpl implements ArchivesService {

    private final ArchivesRepository archivesRepository;
    private final DataCatalogApiClient dataCatalogApiClient;
    private final DocumentFileRepository documentFileRepository;
    private final AudioFileRepository audioFileRepository;
    private final ImageFileRepository imageFileRepository;
    private final VideoFileRepository videoFileRepository;
    private final CategoryTableService categoryTableService;

    @Override
    public Page<Archives> pageArchives(String categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        return archivesRepository.findByCategoryId(categoryId, pageable);
    }

    @Override
    public Archives save(Archives archives) {
        return archivesRepository.save(archives);
    }

    @Override
    public Archives findByArchives_id(Long id) {
        return archivesRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(String categoryId, Long[] ids) {
        String tenantId = Y9LoginUserHolder.getTenantId();
        DataCatalog dataCatalog = dataCatalogApiClient.getTreeRoot(tenantId, categoryId).getData();
        for (Long id : ids) {
            archivesRepository.deleteById(id);
            if (dataCatalog.getCustomId().equals(CategoryEnums.DOCUMENT.getEnName())) {
                documentFileRepository.deleteByDetailId(id);
            } else if (dataCatalog.getCustomId().equals(CategoryEnums.IMAGE.getEnName())) {
                imageFileRepository.deleteByDetailId(id);
            } else if (dataCatalog.getCustomId().equals(CategoryEnums.AUDIO.getEnName())) {
                audioFileRepository.deleteByDetailId(id);
            } else if (dataCatalog.getCustomId().equals(CategoryEnums.VIDEO.getEnName())) {
                videoFileRepository.deleteByDetailId(id);
            } else {
                categoryTableService.deleteTableData(dataCatalog.getCustomId(), id.toString());
            }
        }
    }

}
