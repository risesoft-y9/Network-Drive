package net.risesoft.service.Impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import net.risesoft.entity.Archives;
import net.risesoft.entity.CategoryTable;
import net.risesoft.entity.MetadataConfig;
import net.risesoft.enums.CategoryEnums;
import net.risesoft.model.SearchPage;
import net.risesoft.model.platform.DataCatalog;
import net.risesoft.repository.ArchivesRepository;
import net.risesoft.repository.AudioFileRepository;
import net.risesoft.repository.DocumentFileRepository;
import net.risesoft.repository.ImageFileRepository;
import net.risesoft.repository.MetadataConfigRepository;
import net.risesoft.repository.VideoFileRepository;
import net.risesoft.service.ArchivesService;
import net.risesoft.service.CategoryTableService;
import net.risesoft.util.PageUtil;
import net.risesoft.y9.Y9LoginUserHolder;

import y9.client.rest.platform.resource.DataCatalogApiClient;

@Service("archivesService")
@RequiredArgsConstructor
public class ArchivesServiceImpl implements ArchivesService {

    private final ArchivesRepository archivesRepository;
    private final DataCatalogApiClient dataCatalogApiClient;
    private final DocumentFileRepository documentFileRepository;
    private final AudioFileRepository audioFileRepository;
    private final ImageFileRepository imageFileRepository;
    private final VideoFileRepository videoFileRepository;
    private final CategoryTableService categoryTableService;
    private final PageUtil pageUtil;
    private final MetadataConfigRepository metadataConfigRepository;

    @Override
    public Page<Archives> pageArchives(String categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        return archivesRepository.findByCategoryId(categoryId, pageable);
    }

    @Override
    public SearchPage<Archives> listArchives(String categoryId, int page, int rows) {
        Pageable pageable = PageRequest.of(page - 1, rows, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Archives> pageList = archivesRepository.findByCategoryId(categoryId, pageable);
        List<Archives> list = pageList.getContent();
        SearchPage<Archives> searchPage = SearchPage.<Archives>builder().rows(list).currpage(page).size(rows)
            .totalpages(pageList.getTotalPages()).total(pageList.getTotalElements()).build();
        return searchPage;
    }

    @Override
    public SearchPage<Archives> listArchivesByColumnNameAndValues(String categoryId, String columnNameAndValues,
        int page, int rows) {
        String tenantId = Y9LoginUserHolder.getTenantId();
        DataCatalog dataCatalog = dataCatalogApiClient.getTreeRoot(tenantId, categoryId).getData();
        String customId = dataCatalog.getCustomId();
        String[] arr = columnNameAndValues.split(";");
        String joinSql = "";
        String conditionSql = "";
        for (int i = 0; i < arr.length; i++) {
            String[] arrTemp = arr[i].split(":");
            if (arrTemp.length == 2) {
                String columnNameStr = arrTemp[0];
                String value = arrTemp[1];
                MetadataConfig metadataConfig =
                    metadataConfigRepository.findByViewTypeAndColumnName(customId, columnNameStr);
                String sign = "";
                if (null != metadataConfig) {
                    if (metadataConfig.getFieldOrigin().equals("archives")) {
                        sign = "T";
                    } else {
                        sign = "C";
                    }
                }
                String columnName = arrTemp[0].toUpperCase();
                if (StringUtils.isBlank(conditionSql)) {
                    conditionSql += "INSTR(" + sign + "." + columnName + ",'" + value + "') > 0 ";
                } else {
                    conditionSql += " AND INSTR(" + sign + "." + columnName + ",'" + value + "') > 0 ";
                }
            }
        }
        if (customId.equals(CategoryEnums.DOCUMENT.getEnName())) {
            joinSql = "LEFT JOIN Y9_ARCHIVES_DOCUMENT_FILE C ON T.ARCHIVESID = C.DETAILID";
        } else if (customId.equals(CategoryEnums.IMAGE.getEnName())) {
            joinSql = "LEFT JOIN Y9_ARCHIVES_IMAGE_FILE C ON T.ARCHIVESID = C.DETAILID";
        } else if (customId.equals(CategoryEnums.AUDIO.getEnName())) {
            joinSql = "LEFT JOIN Y9_ARCHIVES_AUDIO_FILE C ON T.ARCHIVESID = C.DETAILID";
        } else if (customId.equals(CategoryEnums.VIDEO.getEnName())) {
            joinSql = "LEFT JOIN Y9_ARCHIVES_VIDEO_FILE C ON T.ARCHIVESID = C.DETAILID";
        } else {
            CategoryTable categoryTable = categoryTableService.findByCategoryMark(customId);
            if (null != categoryTable) {
                joinSql = "LEFT JOIN " + categoryTable.getTableName() + " C ON T.ARCHIVESID = C.DETAILID";
            }
        }
        String sql = "SELECT T.* FROM Y9_ARCHIVES_DETAILS T " + joinSql + " WHERE T.CATEGORYID = ? AND " + conditionSql
            + " ORDER BY T.CREATETIME DESC";
        String countSql = "SELECT COUNT(T.ARCHIVESID) FROM Y9_ARCHIVES_DETAILS T " + joinSql
            + " WHERE T.CATEGORYID = ? AND " + conditionSql;
        System.out.println(sql);
        System.out.println(countSql);
        Object[] args = new Object[1];
        args[0] = categoryId;
        SearchPage<Archives> searchPage =
            pageUtil.page(sql, args, new BeanPropertyRowMapper<>(Archives.class), countSql, args, page, rows);
        return searchPage;
    }

    @Override
    public Archives save(Archives archives) {
        return archivesRepository.save(archives);
    }

    @Override
    public Archives findByArchives_id(Long id) {
        return archivesRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(String categoryId, Long[] ids) {
        String tenantId = Y9LoginUserHolder.getTenantId();
        DataCatalog dataCatalog = dataCatalogApiClient.getTreeRoot(tenantId, categoryId).getData();
        for (Long id : ids) {
            archivesRepository.deleteById(id);
            if (dataCatalog.getCustomId().equals(CategoryEnums.DOCUMENT.getEnName())) {
                documentFileRepository.deleteByDetailId(id);
            } else if (dataCatalog.getCustomId().equals(CategoryEnums.IMAGE.getEnName())) {
                imageFileRepository.deleteByDetailId(id);
            } else if (dataCatalog.getCustomId().equals(CategoryEnums.AUDIO.getEnName())) {
                audioFileRepository.deleteByDetailId(id);
            } else if (dataCatalog.getCustomId().equals(CategoryEnums.VIDEO.getEnName())) {
                videoFileRepository.deleteByDetailId(id);
            } else {
                categoryTableService.deleteTableData(dataCatalog.getCustomId(), id.toString());
            }
        }
    }

}
