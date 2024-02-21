package net.risesoft.service;

import java.util.List;

import org.springframework.data.domain.Page;

import net.risesoft.entity.FileNodeShare;

public interface FileNodeShareService {

    void cancelShare(String personId, List<String> fileNodeIdList);

    void deleteByFileNodeIdList(String personId, List<String> idList);

    void deleteByFileNodeIdList(List<String> idList);

    List<FileNodeShare> list(String personId, String optType);

    Page<FileNodeShare> getFilePublicRecord(String fileNodeId, String optType, int page, int rows);

    void share(List<String> fileNodeIdList, List<String> orgUnitIdList);

    void publicTo(List<String> fileNodeIdList, List<String> orgUnitIdList);
}
