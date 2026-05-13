package net.risesoft.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import net.risesoft.entity.FileTag;
import net.risesoft.entity.FileTagRelation;
import net.risesoft.log.OperationTypeEnum;
import net.risesoft.log.annotation.RiseLog;
import net.risesoft.model.user.UserInfo;
import net.risesoft.pojo.Y9Page;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.FileTagRelationService;
import net.risesoft.service.FileTagService;
import net.risesoft.y9.Y9LoginUserHolder;

@RestController
@RequestMapping("/vue/fileTag")
@RequiredArgsConstructor
public class FileTagController {

    private final FileTagService fileTagService;
    private final FileTagRelationService fileTagRelationService;

    /**
     * 获取所有标签
     */
    @GetMapping("/getTagList")
    public Y9Page<FileTag> getTagList(Integer page, Integer rows, @RequestParam(required = false) String tagName) {
        return fileTagService.list(page, rows, tagName);
    }

    /**
     * 获取所有标签
     *
     * @return
     */
    @GetMapping("/getAllTagList")
    public Y9Result<List<FileTag>> getAllTagList(@RequestParam(required = false) String tagName) {
        List<FileTag> fileTagList = fileTagService.listByTagName(tagName);
        return Y9Result.success(fileTagList, "获取标签列表成功");
    }

    /**
     * 获取所有标签
     * 
     * @return
     */
    @GetMapping("/getAllTag")
    public Y9Result<List<FileTag>> getAllTag() {
        return Y9Result.success(fileTagService.listAll(), "获取标签列表成功");
    }

    /**
     * 检查标签是否被使用
     * 
     * @param tagIdList
     * @return
     */
    @PostMapping("/checkIsUsed")
    public Y9Result<Object> checkIsUsed(@RequestParam(name = "tagIds") List<String> tagIdList) {
        List<FileTagRelation> fileTagRelationList = fileTagRelationService.findByTagIds(tagIdList);
        List<FileTag> fileTagList = new ArrayList<>();
        if (null != fileTagRelationList && !fileTagRelationList.isEmpty()) {
            Set<String> uniqueTagIds = new HashSet<>();
            for (FileTagRelation fileTagRelation : fileTagRelationList) {
                String tagId = fileTagRelation.getTagId();
                if (uniqueTagIds.add(tagId)) {
                    FileTag fileTag = fileTagService.findById(tagId);
                    if (fileTag != null) {
                        fileTagList.add(fileTag);
                    }
                }
            }
            return Y9Result.success(fileTagList, "标签被使用");
        }
        return Y9Result.success(null, "标签未被使用");
    }

    /**
     * 标签管理保存标签
     */
    @RiseLog(operationName = "新增文件标签", operationType = OperationTypeEnum.ADD, saveParams = true)
    @PostMapping("/saveFileTag")
    public Y9Result<Object> saveFileTag(FileTag fileTag) {
        fileTag.setTagType("systemTag");
        return fileTagService.save(fileTag);
    }

    /**
     * 删除标签
     * 
     * @param idList
     * @return
     */
    @RiseLog(operationName = "删除文件标签", operationType = OperationTypeEnum.DELETE, saveParams = true)
    @RequestMapping(value = "/deleteFileTag")
    public Y9Result<Object> deleteFileTag(@RequestParam(name = "ids") List<String> idList, String listType) {
        fileTagService.deleteFileTag(idList);
        return Y9Result.success(null, "删除成功");
    }

    /**
     * 删除标签和文件关联
     *
     * @param idList
     * @return
     */
    @RiseLog(operationName = "删除标签和文件关联", operationType = OperationTypeEnum.DELETE, saveParams = true)
    @RequestMapping(value = "/deleteTagAndRelation")
    public Y9Result<Object> deleteTagAndRelation(@RequestParam(name = "ids") List<String> idList, String listType) {
        fileTagService.deleteFileTag(idList);
        List<FileTagRelation> fileTagRelationList = fileTagRelationService.findByTagIds(idList);
        if (null != fileTagRelationList && !fileTagRelationList.isEmpty()) {
            for (FileTagRelation fileTagRelation : fileTagRelationList) {
                fileTagRelationService.delete(fileTagRelation);
            }
        }
        return Y9Result.success(null, "删除标签和文件关联成功");
    }

    /**
     * 为多个文件添加多个标签
     * 
     * @param fileNodeIdList
     * @param tagIdsList
     * @return
     */
    @RiseLog(operationName = "对多个文件增加多个标签", operationType = OperationTypeEnum.ADD, saveParams = true)
    @PostMapping("/addFileTagToFile")
    public Y9Result<Object> addFileTagToFile(@RequestParam(name = "fileNodeIds") List<String> fileNodeIdList,
        @RequestParam(name = "tagIds") List<String> tagIdsList, String listType) {
        fileTagRelationService.addFileTagToFile(fileNodeIdList, tagIdsList);
        return Y9Result.success(null, "文件添加标签成功");
    }

    /**
     * 单文件添加标签
     *
     * @param tagId
     * @param fileId
     * @return
     */
    @RiseLog(operationName = "单文件添加文件标签", operationType = OperationTypeEnum.ADD, saveParams = true)
    @PostMapping("/simpleFileToTag")
    public Y9Result<Object> simpleFileToTag(String tagId, String fileId, String listType) {
        return fileTagRelationService.simpleFileToTag(tagId, fileId);
    }

    /**
     * 单文件删除标签
     *
     * @param tagId
     * @param fileId
     * @return
     */
    @RiseLog(operationName = "删除文件标签", operationType = OperationTypeEnum.DELETE, saveParams = true)
    @DeleteMapping("/removeTagFromFile")
    public Y9Result<Object> removeTagFromFile(@RequestParam String fileId, @RequestParam String tagId,
        String listType) {
        UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
        fileTagService.removeTagFromFile(fileId, tagId, userInfo.getPersonId());
        return Y9Result.success(null, "标签移除成功");
    }

    /**
     * 新增自定义标签
     * 
     * @param fileTag
     * @param fileId
     * @return
     */
    @RiseLog(operationName = "新增自定义文件标签", operationType = OperationTypeEnum.ADD, saveParams = true)
    @PostMapping("/saveCustomTag")
    public Y9Result<Object> saveCustomTag(FileTag fileTag, @RequestParam String fileId) {
        return fileTagService.saveCustomTag(fileTag, fileId);
    }

    /**
     * 编辑自定义标签
     *
     * @param fileTag
     * @return
     */
    @RiseLog(operationName = "编辑自定义文件标签", operationType = OperationTypeEnum.MODIFY, saveParams = true)
    @PostMapping("/updateCustomTag")
    public Y9Result<Object> updateCustomTag(FileTag fileTag, @RequestParam String fileId) {
        return fileTagService.updateCustomTag(fileTag, fileId);
    }

}
