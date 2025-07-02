package net.risesoft.service.Impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.entity.DictionaryOption;
import net.risesoft.entity.DictionaryValue;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.pojo.Y9Result;
import net.risesoft.repository.DictionaryOptionRepository;
import net.risesoft.repository.DictionaryValueRepository;
import net.risesoft.service.DictionaryOptionService;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/20
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(value = "rsTenantTransactionManager", readOnly = true)
public class DictionaryOptionServiceImpl implements DictionaryOptionService {

    private final DictionaryOptionRepository dictionaryOptionRepository;

    private final DictionaryValueRepository dictionaryValueRepository;

    @Override
    @Transactional(readOnly = false)
    public Y9Result<String> delOptionClass(String type) {
        try {
            if (StringUtils.isNotBlank(type)) {
                dictionaryOptionRepository.deleteById(type);
                dictionaryValueRepository.deleteByType(type);
            }
            return Y9Result.successMsg("删除成功");
        } catch (Exception e) {
            LOGGER.error("删除失败", e);
            return Y9Result.failure("删除失败");
        }
    }

    @Override
    @Transactional(readOnly = false)
    public Y9Result<String> delOptionValue(String id) {
        try {
            if (StringUtils.isNotBlank(id)) {
                dictionaryValueRepository.deleteById(id);
            }
            return Y9Result.successMsg("删除成功");
        } catch (Exception e) {
            LOGGER.error("删除字典数据失败", e);
            return Y9Result.failure("删除失败");
        }
    }

    @Override
    public DictionaryValue findById(String id) {
        return dictionaryValueRepository.findById(id).orElse(null);
    }

    @Override
    public DictionaryOption findByType(String type) {
        return dictionaryOptionRepository.findByType(type);
    }

    @Override
    public List<DictionaryOption> listAllOptionClass() {
        return dictionaryOptionRepository.findAll();
    }

    @Override
    public List<DictionaryValue> listAllOptionValue() {
        return dictionaryValueRepository.findAll();
    }

    @Override
    public List<DictionaryOption> listByName(String name) {
        return dictionaryOptionRepository.findByNameContaining(StringUtils.isBlank(name) ? "" : name);
    }

    @Override
    public List<DictionaryValue> listByTypeOrderByTabIndexAsc(String type) {
        return dictionaryValueRepository.findByTypeOrderByTabIndexAsc(type);
    }

    @Transactional(readOnly = false)
    @Override
    public Y9Result<DictionaryOption> saveOptionClass(DictionaryOption optionClass) {
        try {
            DictionaryOption DictionaryOption = dictionaryOptionRepository.findByType(optionClass.getType());
            if (DictionaryOption == null) {
                DictionaryOption = new DictionaryOption();
                DictionaryOption.setType(optionClass.getType());
            }
            DictionaryOption.setName(optionClass.getName());
            DictionaryOption = dictionaryOptionRepository.save(DictionaryOption);

            return Y9Result.success(DictionaryOption, "保存成功");
        } catch (Exception e) {
            LOGGER.error("保存字典类型失败", e);
            return Y9Result.failure("保存失败");
        }
    }

    @Transactional(readOnly = false)
    @Override
    public Y9Result<DictionaryValue> saveOptionValue(DictionaryValue optionValue) {
        try {
            DictionaryValue DictionaryValue = null;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (StringUtils.isNotBlank(optionValue.getId())) {
                DictionaryValue = dictionaryValueRepository.findById(optionValue.getId()).orElse(null);
            }
            if (DictionaryValue == null || DictionaryValue.getId() == null) {
                DictionaryValue = new DictionaryValue();
                DictionaryValue.setId(StringUtils.isBlank(optionValue.getId()) ? Y9IdGenerator.genId(IdType.SNOWFLAKE)
                    : optionValue.getId());
                Integer tabIndex = dictionaryValueRepository.getMaxTabIndex(optionValue.getType());
                DictionaryValue.setTabIndex((tabIndex == null || tabIndex == 0) ? 1 : tabIndex + 1);
                if ((tabIndex == null || tabIndex == 0)) {
                    DictionaryValue.setDefaultSelected(1);
                }
            } else {
                DictionaryValue.setTabIndex(optionValue.getTabIndex());
            }
            DictionaryValue.setCode(optionValue.getCode());
            DictionaryValue.setName(optionValue.getName());
            DictionaryValue.setType(optionValue.getType());
            DictionaryValue.setUpdateTime(sdf.format(new Date()));
            DictionaryValue = dictionaryValueRepository.save(DictionaryValue);

            return Y9Result.success(DictionaryValue, "保存成功");
        } catch (Exception e) {
            LOGGER.error("保存字典数据失败", e);
            return Y9Result.failure("保存失败");
        }
    }

    @Override
    @Transactional(readOnly = false)
    public Y9Result<String> saveOrder(String ids) {
        try {
            String[] id = ids.split(",");
            for (String idTemp : id) {
                String guid = idTemp.split(":")[0];
                String tabIndex = idTemp.split(":")[1];
                DictionaryValue DictionaryValue = dictionaryValueRepository.findById(guid).orElse(null);
                if (DictionaryValue != null) {
                    DictionaryValue.setTabIndex(Integer.valueOf(tabIndex));
                    dictionaryValueRepository.save(DictionaryValue);
                }
            }
            return Y9Result.successMsg("保存成功");
        } catch (Exception e) {
            LOGGER.error("保存失败", e);
            return Y9Result.failure("保存失败");
        }
    }

    @Override
    @Transactional(readOnly = false)
    public Y9Result<String> updateOptionValue(String id) {
        try {
            DictionaryValue DictionaryValue = dictionaryValueRepository.findById(id).orElse(null);
            if (DictionaryValue != null) {
                List<DictionaryValue> list =
                    dictionaryValueRepository.findByTypeOrderByTabIndexAsc(DictionaryValue.getType());
                for (DictionaryValue optionValue : list) {
                    if (optionValue.getId().equals(id)) {
                        optionValue.setDefaultSelected(1);
                        dictionaryValueRepository.save(optionValue);
                    } else {
                        optionValue.setDefaultSelected(0);
                        dictionaryValueRepository.save(optionValue);
                    }
                }
            }
            return Y9Result.successMsg("设置成功");
        } catch (Exception e) {
            LOGGER.error("设置失败", e);
            return Y9Result.failure("设置失败");
        }
    }

	@Override
	public String findByCodeAndType(String code, String type) {
		DictionaryValue dictionaryValue = dictionaryValueRepository.findByCodeAndType(code, type);
		if(dictionaryValue != null) {
			return dictionaryValue.getName();
		}
		return "";
	}

}
