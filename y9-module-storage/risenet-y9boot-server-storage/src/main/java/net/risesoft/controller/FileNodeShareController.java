package net.risesoft.controller;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.api.platform.org.OrgUnitApi;
import net.risesoft.controller.dto.FileNodeDTO;
import net.risesoft.controller.dto.FileNodeShareDTO;
import net.risesoft.entity.FileNode;
import net.risesoft.entity.FileNodeShare;
import net.risesoft.enums.platform.org.OrgTypeEnum;
import net.risesoft.log.annotation.RiseLog;
import net.risesoft.model.platform.org.OrgUnit;
import net.risesoft.model.user.UserInfo;
import net.risesoft.pojo.Y9Page;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.FileNodeService;
import net.risesoft.service.FileNodeShareService;
import net.risesoft.support.FileOptType;
import net.risesoft.y9.Y9LoginUserHolder;

/**
 * 文件共享接口
 *
 * @author yihong
 *
 */
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/vue/fileNodeShare")
public class FileNodeShareController {

    private final FileNodeShareService fileNodeShareService;
    private final FileNodeService fileNodeService;
    private final OrgUnitApi orgUnitApi;

    /**
     * 取消分享
     *
     * @param fileNodeIdList 文件节点ID列表
     * @return {@link Y9Result}
     */
    @RiseLog(operationName = "取消分享")
    @DeleteMapping
    public Y9Result<Object> cancelShare(@RequestParam(name = "fileNodeIds") List<String> fileNodeIdList) {
        if (fileNodeIdList == null || fileNodeIdList.isEmpty()) {
            return Y9Result.failure("参数错误：文件节点ID列表不能为空");
        }
        UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
        fileNodeService.cancelShare(fileNodeIdList);
        fileNodeShareService.cancelShare(userInfo.getPersonId(), fileNodeIdList);
        return Y9Result.success();
    }

    /**
     * 删除公开记录
     *
     * @param publicIdsList 公开记录ID列表
     * @return {@link Y9Result}
     */
    @RiseLog(operationName = "删除公开记录")
    @DeleteMapping(value = "/deletePublic")
    public Y9Result<Object> deletePublic(@RequestParam(name = "publicIds") List<String> publicIdsList) {
        fileNodeShareService.deleteByFileNodeIdList(publicIdsList);
        return Y9Result.success();
    }

    /**
     * 获取文件公开记录列表
     *
     * @param fileId 文件ID
     * @param page 页码
     * @param rows 每页条数
     * @return {@link Y9Page}
     */
    @RiseLog(operationName = "获取文件公开记录列表")
    @GetMapping(value = "/getFilePublicRecord")
    public Y9Page<Map<String, Object>> getFilePublicRecord(@RequestParam String fileId,
        @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int rows) {
        String tenantId = Y9LoginUserHolder.getTenantId();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Page<FileNodeShare> dlList =
            fileNodeShareService.getFilePublicRecord(fileId, FileOptType.PUBLIC.getValue(), page, rows);
        if (dlList.isEmpty()) {
            return Y9Page.success(page, 0, 0, new ArrayList<>());
        }
        // 批量收集所有需要查询的orgUnitId，减少远程调用
        List<String> orgUnitIds = dlList.stream()
            .map(FileNodeShare::getToOrgUnitId)
            .filter(StringUtils::isNotBlank)
            .distinct()
            .collect(Collectors.toList());
        // 逐个缓存orgUnit类型（避免N+1的逐条远程调用）
        Map<String, OrgUnit> orgUnitCache = new HashMap<>();
        for (String orgUnitId : orgUnitIds) {
            try {
                OrgUnit org = orgUnitApi.getOrgUnit(tenantId, orgUnitId).getData();
                if (org != null) {
                    orgUnitCache.put(orgUnitId, org);
                }
            } catch (Exception e) {
                LOGGER.warn("查询组织单元[{}]失败", orgUnitId, e);
            }
        }
        List<Map<String, Object>> items = new ArrayList<>();
        for (FileNodeShare share : dlList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", share.getId());
            map.put("fileNodeId", share.getFileNodeId());
            map.put("toOrgUnitId", share.getToOrgUnitId());
            map.put("toOrgUnitName", share.getToOrgUnitName());
            OrgUnit org = orgUnitCache.get(share.getToOrgUnitId());
            map.put("orgType", getOrgTypeLabel(org));
            if (share.getCreateTime() != null) {
                map.put("createTime",
                    dtf.format(share.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()));
            }
            items.add(map);
        }
        return Y9Page.success(page, dlList.getTotalPages(), dlList.getTotalElements(), items);
    }

    private String getOrgTypeLabel(OrgUnit org) {
        if (org == null) {
            return "部门";
        }
        return OrgTypeEnum.PERSON == org.getOrgType() ? "人员" : "部门";
    }

    /**
     * 获取我的分享列表
     *
     * @return {@link Y9Result}&lt;{@link List}&lt;{@link FileNodeShareDTO}&gt;&gt;
     */
    @RiseLog(operationName = "获取我的分享列表")
    @GetMapping("/myList")
    public Y9Result<List<FileNodeShareDTO>> myShareList() {
        UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
        List<FileNodeShareDTO> fileNodeShareDTOList = new ArrayList<>();
        List<FileNodeShare> myFileNodeShareList =
            fileNodeShareService.list(userInfo.getPersonId(), FileOptType.SHARE.getValue());
        if (myFileNodeShareList.isEmpty()) {
            return Y9Result.success(fileNodeShareDTOList);
        }
        Map<String, List<FileNodeShare>> fileNodeIdAndListMap =
            myFileNodeShareList.stream().collect(Collectors.groupingBy(FileNodeShare::getFileNodeId));
        for (Map.Entry<String, List<FileNodeShare>> entry : fileNodeIdAndListMap.entrySet()) {
            FileNode fileNode = fileNodeService.findById(entry.getKey());
            if (fileNode != null && !fileNode.isDeleted()) {
                FileNodeShareDTO fileNodeShareDTO = new FileNodeShareDTO();
                fileNodeShareDTO.setFileNode(FileNodeDTO.from(fileNode));

                String toOrgUnitNames =
                    entry.getValue().stream().map(FileNodeShare::getToOrgUnitName).collect(Collectors.joining("，"));
                fileNodeShareDTO.setToOrgUnitNames(toOrgUnitNames);

                fileNodeShareDTOList.add(fileNodeShareDTO);
            }
        }
        return Y9Result.success(fileNodeShareDTOList);
    }

    /**
     * 公开文件
     *
     * @param fileNodeIdList 文件节点ID列表
     * @param orgUnitIdList 组织单元ID列表
     * @return {@link Y9Result}
     */
    @RiseLog(operationName = "公开文件")
    @PostMapping("/publicTo")
    public Y9Result<Object> publicTo(@RequestParam(name = "fileNodeIds") List<String> fileNodeIdList,
        @RequestParam(name = "orgUnitIds") List<String> orgUnitIdList) {
        if (fileNodeIdList == null || fileNodeIdList.isEmpty()) {
            return Y9Result.failure("参数错误：文件节点ID不能为空");
        }
        if (orgUnitIdList == null || orgUnitIdList.isEmpty()) {
            return Y9Result.failure("参数错误：组织单元ID不能为空");
        }
        fileNodeShareService.publicTo(fileNodeIdList, orgUnitIdList);
        return Y9Result.success();
    }

    /**
     * 分享文件
     *
     * @param fileNodeIdList 文件节点ID列表
     * @param orgUnitIdList 组织单元ID列表
     * @return {@link Y9Result}
     */
    @RiseLog(operationName = "分享文件")
    @PostMapping("/share")
    public Y9Result<Object> share(@RequestParam(name = "fileNodeIds") List<String> fileNodeIdList,
        @RequestParam(name = "orgUnitIds") List<String> orgUnitIdList) {
        if (fileNodeIdList == null || fileNodeIdList.isEmpty()) {
            return Y9Result.failure("参数错误：文件节点ID不能为空");
        }
        if (orgUnitIdList == null || orgUnitIdList.isEmpty()) {
            return Y9Result.failure("参数错误：组织单元ID不能为空");
        }
        fileNodeShareService.share(fileNodeIdList, orgUnitIdList);
        fileNodeService.share(fileNodeIdList);
        return Y9Result.success();
    }
}
