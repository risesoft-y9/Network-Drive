package net.risesoft.service;

import net.risesoft.entity.Archives;
import net.risesoft.model.SearchPage;

public interface ArchivesService {

    SearchPage<Archives> listArchives(String categoryId, Integer fileStatus, int page, int rows);

    SearchPage<Archives> listArchivesByColumnNameAndValues(String categoryId, Integer fileStatus,
        String columnNameAndValues, int page, int rows);

    Archives save(Archives archives);

    Archives findByArchives_id(Long id);

    void delete(String categoryId, Long[] id);

    void signDelete(String categoryId, Long[] id);

    void recordArchiving(Long[] ids);

    void createArchivesNo(Long[] ids);
}
