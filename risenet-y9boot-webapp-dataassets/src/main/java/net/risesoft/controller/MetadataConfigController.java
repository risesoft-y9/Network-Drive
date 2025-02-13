package net.risesoft.controller;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.entity.MetadataConfig;
import net.risesoft.model.platform.DataCatalog;
import net.risesoft.pojo.Y9Page;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.MetadataConfigService;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.json.Y9JsonUtil;

import y9.client.rest.platform.resource.DataCatalogApiClient;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/vue/metadata/config")
public class MetadataConfigController {

    private final MetadataConfigService metadataConfigService;
    private final DataCatalogApiClient dataCatalogApiClient;

    @RequestMapping(value = "/getMetadataList", method = RequestMethod.GET, produces = "application/json")
    public Y9Page<MetadataConfig> getMetadataList(String viewType, int page, int rows) {
        if (page < 1) {
            page = 1;
        }
        Page<MetadataConfig> configList = metadataConfigService.listByViewType(viewType, page, rows);
        return Y9Page.success(page, configList.getTotalPages(), configList.getTotalElements(), configList.getContent());
    }

    @RequestMapping(value = "/getMetadataOrderList", method = RequestMethod.GET, produces = "application/json")
    public Y9Result<List<MetadataConfig>> getMetadataOrderList(String viewType) {
        List<MetadataConfig> configList = metadataConfigService.listByViewType(viewType);
        return Y9Result.success(configList, "获取元数据配置列表成功");
    }

    @RequestMapping(value = "/saveMetadataConfig", method = RequestMethod.POST, produces = "application/json")
    public Y9Result<String> saveMetadataConfig(MetadataConfig metadataConfig) {
        try {
            metadataConfigService.saveMetadataConfig(metadataConfig);
            return Y9Result.success("保存元数据配置成功");
        } catch (Exception e) {
            LOGGER.error("保存元数据配置失败", e);
            return Y9Result.failure("保存元数据配置失败");
        }
    }

    @RequestMapping(value = "/saveOrder", method = RequestMethod.POST, produces = "application/json")
    public Y9Result<String> saveOrder(@RequestParam String[] idAndTabIndexs) {
        try {
            metadataConfigService.saveOrder(idAndTabIndexs);
            return Y9Result.success("保存顺序成功");
        } catch (Exception e) {
            LOGGER.error("保存顺序失败", e);
            return Y9Result.failure("保存顺序失败");
        }
    }

    @RequestMapping(value = "/resetConfig", method = RequestMethod.POST, produces = "application/json")
    public Y9Result<String> resetConfig(String viewType) {
        try {
            // if (viewType.equals(CategoryEnums.DOCUMENT.getEnName()) ||
            // viewType.equals(CategoryEnums.AUDIO.getEnName())
            // || viewType.equals(CategoryEnums.VIDEO.getEnName())
            // || viewType.equals(CategoryEnums.IMAGE.getEnName())) {
            metadataConfigService.initMetadataConfigByViewType(viewType);
            // } else {
            // metadataConfigService.initCustomMetadataConfigByViewType(viewType);
            // }
            return Y9Result.success("重置配置成功");
        } catch (Exception e) {
            LOGGER.error("重置配置失败", e);
            return Y9Result.failure("重置配置失败");
        }
    }

    @RequestMapping(value = "/getMetadataFieldList", method = RequestMethod.GET, produces = "application/json")
    public Y9Result<List<MetadataConfig>> getMetadataFieldList(String categoryId) {
        String tenantId = Y9LoginUserHolder.getTenantId();
        DataCatalog dataCatalog = dataCatalogApiClient.getTreeRoot(tenantId, categoryId).getData();
        List<MetadataConfig> metadataFieldList = metadataConfigService.listByViewType(dataCatalog.getCustomId());
        return Y9Result.success(metadataFieldList, "获取元数据字段列表成功");
    }

    /**
     * 获取元数据著录必填字段配置列表
     * 
     * @param viewType
     * @return
     */
    @RequestMapping(value = "/getMetadataRecordConfigList", method = RequestMethod.GET, produces = "application/json")
    public Y9Result<List<MetadataConfig>> getMetadataRecordConfigList(String viewType) {
        List<MetadataConfig> configList = metadataConfigService.getMetadataRecordConfigList(viewType);
        return Y9Result.success(configList, "获取元数据著录必填字段配置列表成功");
    }

    @RequestMapping(value = "/saveListFiledShow", method = RequestMethod.POST, produces = "application/json")
    public Y9Result<String> saveListFiledShow(@RequestParam String idAndIsShowJson) {
        try {
            List<Map<String, Object>> listMap = Y9JsonUtil.readListOfMap(idAndIsShowJson, String.class, Object.class);
            for (Map<String, Object> map : listMap) {
                MetadataConfig config = metadataConfigService.findById((String)map.get("id"));
                config.setIsListShow(Integer.parseInt(map.get("isShow").toString()));
                metadataConfigService.save(config);
            }
            return Y9Result.success("修改列表字段显示或隐藏成功");
        } catch (Exception e) {
            LOGGER.error("修改列表字段显示或隐藏失败", e);
            return Y9Result.failure("修改列表字段显示或隐藏失败");
        }
    }
}
