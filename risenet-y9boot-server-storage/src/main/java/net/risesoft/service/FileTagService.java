package net.risesoft.service;

import java.util.List;

import org.springframework.web.bind.annotation.RequestParam;

import net.risesoft.entity.FileTag;
import net.risesoft.pojo.Y9Page;
import net.risesoft.pojo.Y9Result;

public interface FileTagService {

    Y9Page<FileTag> list(Integer page, Integer rows, String tagName);

    List<FileTag> listAll();

    Y9Result<Object> save(FileTag fileTag);

    void deleteFileTag(List<String> idList);

    List<FileTag> getTagsByFileId(String fileId);

    Y9Result<Object> updateFileTag(FileTag fileTag);

    Y9Result<Object> saveCustomTag(FileTag fileTag, @RequestParam String fileId);

    void removeTagFromFile(String fileId, String tagId, String operatorId);

}
