package net.risesoft.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.service.MetadataService;
import net.risesoft.y9public.service.Y9FileStoreService;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/vue/fileNode")
public class MetadataController {

    private final Y9FileStoreService y9FileStoreService;
    private final MetadataService fileNodeService;

}
