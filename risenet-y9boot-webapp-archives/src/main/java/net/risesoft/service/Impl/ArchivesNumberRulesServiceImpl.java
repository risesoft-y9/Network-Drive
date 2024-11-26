package net.risesoft.service.Impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import net.risesoft.entity.ArchivesNumberRules;
import net.risesoft.repository.ArchivesNumberRulesRepository;
import net.risesoft.service.ArchivesNumberRulesService;

@Service("archivesNumberRulesService")
@RequiredArgsConstructor
public class ArchivesNumberRulesServiceImpl implements ArchivesNumberRulesService {

    private final ArchivesNumberRulesRepository archivesNumberRulesRepository;

    @Override
    public ArchivesNumberRules save(ArchivesNumberRules archivesNumberRules) {
        return archivesNumberRulesRepository.save(archivesNumberRules);
    }

    @Override
    public List<ArchivesNumberRules> findByCategoryMark(String categoryMark) {
        return archivesNumberRulesRepository.findByCategoryMark(categoryMark);
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteByCategoryMark(String categoryMark) {
        List<ArchivesNumberRules> rules = this.findByCategoryMark(categoryMark);
        if (null != rules && !rules.isEmpty()) {
            archivesNumberRulesRepository.deleteAll(rules);
        }
    }
}
