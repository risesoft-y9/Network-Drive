package net.risesoft.service.Impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import net.risesoft.entity.Archives;
import net.risesoft.repository.ArchivesRepository;
import net.risesoft.service.ArchivesService;

@Service("archivesService")
@RequiredArgsConstructor
public class ArchivesServiceImpl implements ArchivesService {

    private final ArchivesRepository archivesRepository;

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
        return archivesRepository.findByArchives_id(id);
    }

}
