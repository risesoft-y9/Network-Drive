package net.risesoft.service;

import org.springframework.data.domain.Page;

import net.risesoft.entity.Archives;

public interface ArchivesService {

    Page<Archives> pageArchives(String categoryId, int page, int size);

    Archives save(Archives archives);

    Archives findByArchives_id(Long id);
}
