package net.risesoft.service;

import org.springframework.data.domain.Page;

import net.risesoft.entity.Archives;
import net.risesoft.model.SearchPage;

public interface ArchivesService {

    Page<Archives> pageArchives(String categoryId, int page, int rows);

    SearchPage<Archives> listArchives(String categoryId, int page, int rows);

    SearchPage<Archives> listArchivesByColumnNameAndValues(String categoryId, String columnNameAndValues, int page,
        int rows);

    Archives save(Archives archives);

    Archives findByArchives_id(Long id);

    void delete(String categoryId, Long[] id);
}
