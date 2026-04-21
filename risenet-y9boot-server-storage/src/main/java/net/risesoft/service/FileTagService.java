package net.risesoft.service;

import java.util.List;

import net.risesoft.entity.FileTag;
import net.risesoft.pojo.Y9Page;
import net.risesoft.pojo.Y9Result;

public interface FileTagService {

    Y9Page<FileTag> list(Integer page, Integer rows, String tagName);

    List<FileTag> listByTagName(String tagName);

    List<FileTag> listAll();

    Y9Result<Object> save(FileTag fileTag);

    void deleteFileTag(List<String> idList);

    List<FileTag> getTagsByFileId(String fileId);

    FileTag findById(String id);

    Y9Result<Object> updateFileTag(FileTag fileTag, String fileId);

    Y9Result<Object> saveCustomTag(FileTag fileTag, String fileId);

    void removeTagFromFile(String fileId, String tagId, String operatorId);

}
