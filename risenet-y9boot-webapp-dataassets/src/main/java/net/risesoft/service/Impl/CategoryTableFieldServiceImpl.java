package net.risesoft.service.Impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import net.risesoft.entity.CategoryTable;
import net.risesoft.entity.CategoryTableField;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.repository.CategoryTableFieldRepository;
import net.risesoft.repository.CategoryTableRepository;
import net.risesoft.service.CategoryTableFieldService;
import net.risesoft.service.CategoryTableManagerService;

@Service
@RequiredArgsConstructor
@Transactional(value = "rsTenantTransactionManager", readOnly = true)
public class CategoryTableFieldServiceImpl implements CategoryTableFieldService {

    private final CategoryTableFieldRepository categoryTableFieldRepository;

    private final CategoryTableRepository categoryTableRepository;

    private final CategoryTableManagerService tableManagerService;

    @Override
    @Transactional(readOnly = false)
    public void delete(String id) {
        categoryTableFieldRepository.deleteById(id);
    }

    @Override
    public CategoryTableField findById(String id) {
        return categoryTableFieldRepository.findById(id).orElse(null);
    }

    @Override
    public List<CategoryTableField> listByTableId(String tableId) {
        List<CategoryTableField> list = categoryTableFieldRepository.findByTableIdOrderByDisplayOrderAsc(tableId);
        Map<String, Object> map = tableManagerService.getExistTableFields(tableId);
        for (CategoryTableField field : list) {
            /*
             * 指定表中是否存在该字段，0为否，1为是
             */
            field.setState(0);
            if (map != null && !map.isEmpty()) {
                if (map.get(field.getFieldName().toLowerCase()) != null) {
                    field.setState(1);
                }
            }
        }
        return list;
    }

    @Override
    @Transactional(readOnly = false)
    public CategoryTableField saveOrUpdate(CategoryTableField field) {
        if (StringUtils.isBlank(field.getId())) {
            field.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
        }
        if (field.getDisplayOrder() == null) {
            Integer order = categoryTableFieldRepository.getMaxDisplayOrder(field.getTableId());
            field.setDisplayOrder(order == null ? 1 : order + 1);
        }
        CategoryTable y9Table = categoryTableRepository.findById(field.getTableId()).orElse(null);
        assert y9Table != null;
        field.setTableName(y9Table.getTableName());
        boolean b = field.getFieldType().contains("(");
        if (!b) {
            field.setFieldType(field.getFieldType() + "(" + field.getFieldLength() + ")");
        }
        return categoryTableFieldRepository.save(field);
    }

    @Override
    @Transactional
    public void updateState(String tableId) {
        categoryTableFieldRepository.updateState(tableId);
    }

}
