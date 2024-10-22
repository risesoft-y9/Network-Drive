package net.risesoft.service.Impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import net.risesoft.entity.MetadataField;
import net.risesoft.repository.MetadataFieldRepository;
import net.risesoft.service.MetadataFieldService;

@Service
@RequiredArgsConstructor
@Transactional(value = "rsTenantTransactionManager", readOnly = true)
public class MetadataFieldServiceImpl implements MetadataFieldService {

    private final MetadataFieldRepository metadataFieldRepository;

    @Override
    @Transactional(readOnly = false)
    public void delete(String id) {
        metadataFieldRepository.deleteById(id);
    }

    @Override
    public MetadataField findById(String id) {
        return metadataFieldRepository.findById(id).orElse(null);
    }

    @Override
    public List<MetadataField> listByTableId(String tableId) {
        // List<MetadataField> list = metadataFieldRepository.findByTableIdOrderByDisplayOrderAsc(tableId);
        // Map<String, Object> map = tableManagerService.getExistTableFields(tableId);
        // for (MetadataField field : list) {
        // /*
        // * 指定表中是否存在该字段，0为否，1为是
        // */
        // field.setState(0);
        // if (map != null && !map.isEmpty()) {
        // if (map.get(field.getFieldName().toLowerCase()) != null) {
        // field.setState(1);
        // }
        // }
        // }
        // return list;
        return null;
    }

    @Override
    public List<MetadataField> listByTableIdAndState(String tableId, Integer state) {
        // return metadataFieldRepository.findByTableIdAndStateOrderByDisplayOrderAsc(tableId, state);
        return null;
    }

    @Override
    public List<MetadataField> listByTableIdOrderByDisplay(String tableId) {
        // return metadataFieldRepository.findByTableIdOrderByDisplayOrderAsc(tableId);
        return null;
    }

    @Override
    public List<MetadataField> listSystemFieldByTableId(String tableId) {
        // return metadataFieldRepository.findByTableIdAndIsSystemFieldOrderByDisplayOrderAsc(tableId, 1);
        return null;
    }

    @Override
    @Transactional(readOnly = false)
    public MetadataField saveOrUpdate(MetadataField field) {
        // if (StringUtils.isBlank(field.getId())) {
        // field.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
        // }
        // if (field.getDisplayOrder() == null) {
        // Integer order = metadataFieldRepository.getMaxDisplayOrder(field.getTableId());
        // field.setDisplayOrder(order == null ? 1 : order + 1);
        // }
        // Y9Table y9Table = y9TableRepository.findById(field.getTableId()).orElse(null);
        // assert y9Table != null;
        // field.setTableName(y9Table.getTableName());
        // boolean b = field.getFieldType().contains("(");
        // if (!b) {
        // field.setFieldType(field.getFieldType() + "(" + field.getFieldLength() + ")");
        // }
        // return metadataFieldRepository.save(field);
        return null;
    }

    @Override
    @Transactional
    public void updateState(String tableId) {
        // metadataFieldRepository.updateState(tableId);
    }

}
