package net.risesoft.controller;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.entity.MetadataConfig;
import net.risesoft.pojo.Y9Page;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.MetadataConfigService;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/vue/metadata/config")
public class MetadataConfigController {

    private final MetadataConfigService metadataConfigService;

    @RequestMapping(value = "/getMetadataList", method = RequestMethod.GET, produces = "application/json")
    public Y9Page<MetadataConfig> getMetadataList(String viewType, int page, int rows) {
        if (page < 1) {
            page = 1;
        }
        Page<MetadataConfig> configList = metadataConfigService.listByViewType(viewType, page, rows);
        return Y9Page.success(page, configList.getTotalPages(), configList.getTotalElements(), configList.getContent());
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
            metadataConfigService.initMetadataConfigByViewType(viewType);
            return Y9Result.success("重置配置成功");
        } catch (Exception e) {
            LOGGER.error("重置配置失败", e);
            return Y9Result.failure("重置配置失败");
        }
    }
}
