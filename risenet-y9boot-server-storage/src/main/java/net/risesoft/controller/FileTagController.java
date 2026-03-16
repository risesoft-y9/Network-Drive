package net.risesoft.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import net.risesoft.entity.FileTag;
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
    @GetMapping("/getAllTag")
    public Y9Result<List<FileTag>> getAllTag() {
        return Y9Result.success(fileTagService.listAll(), "获取标签列表成功");
    }

    /**
     * 标签管理保存标签
     */
    @RiseLog(operationName = "新增文件标签", operationType = OperationTypeEnum.ADD)
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
    @RiseLog(operationName = "删除文件标签", operationType = OperationTypeEnum.DELETE)
    @RequestMapping(value = "/deleteFileTag")
    public Y9Result<Object> deleteFileTag(@RequestParam(name = "ids") List<String> idList) {
        fileTagService.deleteFileTag(idList);
        return Y9Result.success(null, "删除成功");
    }

    /**
     * 为多个文件添加多个标签
     * 
     * @param fileNodeIdList
     * @param tagIdsList
     * @return
     */
    @RiseLog(operationName = "对多个文件增加多个标签", operationType = OperationTypeEnum.ADD)
    @PostMapping("/addFileTagToFile")
    public Y9Result<Object> addFileTagToFile(@RequestParam(name = "fileNodeIds") List<String> fileNodeIdList,
        @RequestParam(name = "tagIds") List<String> tagIdsList) {
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
    @RiseLog(operationName = "单文件添加文件标签", operationType = OperationTypeEnum.ADD)
    @PostMapping("/simpleFileToTag")
    public Y9Result<Object> simpleFileToTag(String tagId, String fileId) {
        return fileTagRelationService.simpleFileToTag(tagId, fileId);
    }

    /**
     * 单文件删除标签
     *
     * @param tagId
     * @param fileId
     * @return
     */
    @RiseLog(operationName = "删除文件标签", operationType = OperationTypeEnum.DELETE)
    @DeleteMapping("/removeTagFromFile")
    public Y9Result<Object> removeTagFromFile(@RequestParam String fileId, @RequestParam String tagId) {
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
    @RiseLog(operationName = "新增自定义文件标签", operationType = OperationTypeEnum.ADD)
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
    @RiseLog(operationName = "编辑自定义文件标签", operationType = OperationTypeEnum.MODIFY)
    @PostMapping("/updateCustomTag")
    public Y9Result<Object> updateFileTag(FileTag fileTag) {
        return fileTagService.updateFileTag(fileTag);
    }

}
