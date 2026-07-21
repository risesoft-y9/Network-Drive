package net.risesoft.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.entity.FileTag;
import net.risesoft.entity.FileTagRelation;
import net.risesoft.log.OperationTypeEnum;
import net.risesoft.log.annotation.RiseLog;
import net.risesoft.pojo.Y9Page;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.FileTagRelationService;
import net.risesoft.service.FileTagService;
import net.risesoft.y9.Y9LoginUserHolder;

@Slf4j
@RestController
@RequestMapping("/vue/fileTag")
@RequiredArgsConstructor
public class FileTagController {

    private final FileTagService fileTagService;
    private final FileTagRelationService fileTagRelationService;

    /**
     * 获取标签分页列表
     *
     * @param page 页码
     * @param rows 每页条数
     * @param tagName 标签名称（模糊搜索）
     * @return {@link Y9Page}&lt;{@link FileTag}&gt;
     */
    @GetMapping("/getTagList")
    public Y9Page<FileTag> getTagList(@RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "20") Integer rows, @RequestParam(required = false) String tagName) {
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
     * @param tagIdList 标签ID列表
     * @return {@link Y9Result}
     */
    @PostMapping("/checkIsUsed")
    public Y9Result<Object> checkIsUsed(@RequestParam(name = "tagIds") List<String> tagIdList) {
        if (tagIdList == null || tagIdList.isEmpty()) {
            return Y9Result.failure("参数错误：标签ID列表不能为空");
        }
        List<FileTagRelation> fileTagRelationList = fileTagRelationService.findByTagIds(tagIdList);
        if (fileTagRelationList == null || fileTagRelationList.isEmpty()) {
            return Y9Result.success(null, "标签未被使用");
        }
        // 去重后批量查询标签，避免N+1
        Set<String> uniqueTagIds = new HashSet<>();
        for (FileTagRelation fileTagRelation : fileTagRelationList) {
            uniqueTagIds.add(fileTagRelation.getTagId());
        }
        List<FileTag> fileTagList = fileTagService.findAllById(uniqueTagIds);
        if (fileTagList.isEmpty()) {
            return Y9Result.success(null, "标签未被使用");
        }
        return Y9Result.success(fileTagList, "标签被使用");
    }

    /**
     * 标签管理保存标签
     */
    @RiseLog(operationName = "新增文件标签", operationType = OperationTypeEnum.ADD, saveParams = true)
    @PostMapping("/saveFileTag")
    public Y9Result<Object> saveFileTag(FileTag fileTag) {
        if (fileTag == null || StringUtils.isBlank(fileTag.getTagName())) {
            return Y9Result.failure("参数错误：标签名称不能为空");
        }
        fileTag.setTagType("systemTag");
        return fileTagService.save(fileTag);
    }

    /**
     * 删除标签
     *
     * @param idList 标签ID列表
     * @param listType 列表类型(作为审计日志区分的参数，不可删除)
     * @return {@link Y9Result}
     */
    @RiseLog(operationName = "删除文件标签", operationType = OperationTypeEnum.DELETE, saveParams = true)
    @DeleteMapping("/deleteFileTag")
    public Y9Result<Object> deleteFileTag(@RequestParam(name = "ids") List<String> idList,
        @RequestParam(required = false) String listType) {
        if (idList == null || idList.isEmpty()) {
            return Y9Result.failure("参数错误：标签ID列表不能为空");
        }
        fileTagService.deleteFileTag(idList);
        return Y9Result.success(null, "删除成功");
    }

    /**
     * 删除标签和文件关联
     *
     * @param idList 标签ID列表
     * @param listType 列表类型(作为审计日志区分的参数，不可删除)
     * @return {@link Y9Result}
     */
    @RiseLog(operationName = "删除标签和文件关联", operationType = OperationTypeEnum.DELETE, saveParams = true)
    @DeleteMapping("/deleteTagAndRelation")
    public Y9Result<Object> deleteTagAndRelation(@RequestParam(name = "ids") List<String> idList,
        @RequestParam(required = false) String listType) {
        if (idList == null || idList.isEmpty()) {
            return Y9Result.failure("参数错误：标签ID列表不能为空");
        }
        fileTagService.deleteFileTag(idList);
        List<FileTagRelation> fileTagRelationList = fileTagRelationService.findByTagIds(idList);
        if (fileTagRelationList != null && !fileTagRelationList.isEmpty()) {
            fileTagRelationService.deleteAll(fileTagRelationList);
        }
        return Y9Result.success(null, "删除标签和文件关联成功");
    }

    /**
     * 为多个文件添加多个标签
     *
     * @param fileNodeIdList 文件节点ID列表
     * @param tagIdsList 标签ID列表
     * @param listType 列表类型(作为审计日志区分的参数，不可删除)
     * @return {@link Y9Result}
     */
    @RiseLog(operationName = "对多个文件增加多个标签", operationType = OperationTypeEnum.ADD, saveParams = true)
    @PostMapping("/addFileTagToFile")
    public Y9Result<Object> addFileTagToFile(@RequestParam(name = "fileNodeIds") List<String> fileNodeIdList,
        @RequestParam(name = "tagIds") List<String> tagIdsList, @RequestParam(required = false) String listType) {
        if (fileNodeIdList == null || fileNodeIdList.isEmpty()) {
            return Y9Result.failure("参数错误：文件节点ID列表不能为空");
        }
        if (tagIdsList == null || tagIdsList.isEmpty()) {
            return Y9Result.failure("参数错误：标签ID列表不能为空");
        }
        fileTagRelationService.addFileTagToFile(fileNodeIdList, tagIdsList);
        return Y9Result.success(null, "文件添加标签成功");
    }

    /**
     * 单文件添加标签
     *
     * @param tagId 标签ID
     * @param fileId 文件ID
     * @param listType 列表类型(作为审计日志区分的参数，不可删除)
     * @return {@link Y9Result}
     */
    @RiseLog(operationName = "单文件添加文件标签", operationType = OperationTypeEnum.ADD, saveParams = true)
    @PostMapping("/simpleFileToTag")
    public Y9Result<Object> simpleFileToTag(@RequestParam String tagId, @RequestParam String fileId,
        @RequestParam(required = false) String listType) {
        if (StringUtils.isBlank(tagId) || StringUtils.isBlank(fileId)) {
            return Y9Result.failure("参数错误：标签ID和文件ID不能为空");
        }
        return fileTagRelationService.simpleFileToTag(tagId, fileId);
    }

    /**
     * 单文件删除标签
     *
     * @param tagId 标签ID
     * @param fileId 文件ID
     * @param listType 列表类型(作为审计日志区分的参数，不可删除)
     * @return {@link Y9Result}
     */
    @RiseLog(operationName = "删除文件标签", operationType = OperationTypeEnum.DELETE, saveParams = true)
    @DeleteMapping("/removeTagFromFile")
    public Y9Result<Object> removeTagFromFile(@RequestParam String fileId, @RequestParam String tagId,
        @RequestParam(required = false) String listType) {
        if (StringUtils.isBlank(fileId) || StringUtils.isBlank(tagId)) {
            return Y9Result.failure("参数错误：文件ID和标签ID不能为空");
        }
        fileTagService.removeTagFromFile(fileId, tagId, Y9LoginUserHolder.getUserInfo().getPersonId());
        return Y9Result.success(null, "标签移除成功");
    }

    /**
     * 新增自定义标签
     *
     * @param fileTag 标签实体
     * @param fileId 文件ID
     * @param listType 列表类型(作为审计日志区分的参数，不可删除)
     * @return {@link Y9Result}
     */
    @RiseLog(operationName = "新增自定义文件标签", operationType = OperationTypeEnum.ADD, saveParams = true)
    @PostMapping("/saveCustomTag")
    public Y9Result<Object> saveCustomTag(FileTag fileTag, @RequestParam String fileId,
        @RequestParam(required = false) String listType) {
        if (fileTag == null || StringUtils.isBlank(fileTag.getTagName())) {
            return Y9Result.failure("参数错误：标签名称不能为空");
        }
        if (StringUtils.isBlank(fileId)) {
            return Y9Result.failure("参数错误：文件ID不能为空");
        }
        return fileTagService.saveCustomTag(fileTag, fileId);
    }

    /**
     * 编辑自定义标签
     *
     * @param fileTag 标签实体
     * @param fileId 文件ID
     * @param listType 列表类型(作为审计日志区分的参数，不可删除)
     * @return {@link Y9Result}
     */
    @RiseLog(operationName = "编辑自定义文件标签", operationType = OperationTypeEnum.MODIFY, saveParams = true)
    @PostMapping("/updateCustomTag")
    public Y9Result<Object> updateCustomTag(FileTag fileTag, @RequestParam String fileId,
        @RequestParam(required = false) String listType) {
        if (fileTag == null || StringUtils.isBlank(fileTag.getTagName())) {
            return Y9Result.failure("参数错误：标签名称不能为空");
        }
        if (StringUtils.isBlank(fileId)) {
            return Y9Result.failure("参数错误：文件ID不能为空");
        }
        return fileTagService.updateCustomTag(fileTag, fileId);
    }

}
