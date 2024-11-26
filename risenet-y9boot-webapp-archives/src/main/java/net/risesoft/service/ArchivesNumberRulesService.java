package net.risesoft.service;

import java.util.List;

import net.risesoft.entity.ArchivesNumberRules;

public interface ArchivesNumberRulesService {

    ArchivesNumberRules save(ArchivesNumberRules archivesNumberRules);

    List<ArchivesNumberRules> findByCategoryMark(String categoryMark);

    void deleteByCategoryMark(String categoryMark);
}
