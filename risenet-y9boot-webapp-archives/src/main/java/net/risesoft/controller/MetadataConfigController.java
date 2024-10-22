package net.risesoft.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.pojo.Y9Result;
import net.risesoft.service.ArchivesService;
import net.risesoft.y9public.service.Y9FileStoreService;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/vue/metadata/config")
public class MetadataConfigController {

    private final Y9FileStoreService y9FileStoreService;
    private final ArchivesService archivesService;

    @RequestMapping(value = "/getMetadataList", method = RequestMethod.GET, produces = "application/json")
    public Y9Result<Map<String, Object>> getMetadataList(String category) {
        Map<String, Object> map = archivesService.getArchivesFileList();
        return Y9Result.success(map);
    }
}
