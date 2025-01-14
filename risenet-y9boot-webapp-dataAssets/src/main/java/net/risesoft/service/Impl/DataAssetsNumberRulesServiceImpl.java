package net.risesoft.service.Impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import net.risesoft.entity.DataAssetsNumberRules;
import net.risesoft.repository.DataAssetsNumberRulesRepository;
import net.risesoft.service.DataAssetsNumberRulesService;

@Service
@RequiredArgsConstructor
public class DataAssetsNumberRulesServiceImpl implements DataAssetsNumberRulesService {

    private final DataAssetsNumberRulesRepository dataAssetsNumberRulesRepository;

    @Override
    public DataAssetsNumberRules save(DataAssetsNumberRules dataAssetsNumberRules) {
        return dataAssetsNumberRulesRepository.save(dataAssetsNumberRules);
    }

    @Override
    public List<DataAssetsNumberRules> findByCategoryMark(String categoryMark) {
        return dataAssetsNumberRulesRepository.findByCategoryMark(categoryMark);
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteByCategoryMark(String categoryMark) {
        List<DataAssetsNumberRules> rules = this.findByCategoryMark(categoryMark);
        if (null != rules && !rules.isEmpty()) {
            dataAssetsNumberRulesRepository.deleteAll(rules);
        }
    }
}
