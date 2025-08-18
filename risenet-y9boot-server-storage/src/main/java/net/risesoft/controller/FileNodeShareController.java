package net.risesoft.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import net.risesoft.model.platform.org.OrgUnit;
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
     * @param fileNodeIdList
     * @return
     */
    @DeleteMapping
    public Y9Result<Object> cancelShare(@RequestParam(name = "fileNodeIds") List<String> fileNodeIdList) {
        fileNodeService.cancelShare(fileNodeIdList);
        fileNodeShareService.cancelShare(Y9LoginUserHolder.getUserInfo().getPersonId(), fileNodeIdList);
        return Y9Result.success();
    }

    /**
     * 删除公开人员
     *
     * @param publicIdsList
     * @return
     */
    @DeleteMapping(value = "/deletePublic")
    public Y9Result<Object> deletePublic(@RequestParam(name = "publicIds") List<String> publicIdsList) {
        fileNodeShareService.deleteByFileNodeIdList(publicIdsList);
        return Y9Result.success(null, "删除公开人员成功");
    }

    /**
     * 获取文件公开记录列表
     * 
     * @param fileId
     * @param page
     * @param rows
     * @return
     */
    @GetMapping(value = "/getFilePublicRecord")
    public Y9Page<Map<String, Object>> getFilePublicRecord(String fileId, int page, int rows) {
        String tenantId = Y9LoginUserHolder.getTenantId();
        List<Map<String, Object>> items = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (page < 1) {
            page = 1;
        }
        Page<FileNodeShare> dlList =
            fileNodeShareService.getFilePublicRecord(fileId, FileOptType.PUBLIC.getValue(), page, rows);
        for (FileNodeShare share : dlList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", share.getId());
            map.put("fileNodeId", share.getFileNodeId());
            OrgUnit org = orgUnitApi.getOrgUnit(tenantId, share.getToOrgUnitId()).getData();
            map.put("toOrgUnitId", share.getToOrgUnitId());
            map.put("toOrgUnitName", share.getToOrgUnitName());
            map.put("orgType", org.getOrgType().equals(OrgTypeEnum.PERSON) ? "人员" : "部门");
            map.put("createTime", sdf.format(share.getCreateTime()));
            items.add(map);
        }
        return Y9Page.success(page, dlList.getTotalPages(), dlList.getTotalElements(), items);
    }

    /**
     * 获取我的分享列表
     * 
     * @return
     */
    @GetMapping("/myList")
    public Y9Result<List<FileNodeShareDTO>> myShareList() {
        List<FileNodeShareDTO> fileNodeShareDTOList = new ArrayList<>();
        List<FileNodeShare> myFileNodeShareList =
            fileNodeShareService.list(Y9LoginUserHolder.getUserInfo().getPersonId(), FileOptType.SHARE.getValue());
        Map<String, List<FileNodeShare>> fileNodeIdAndListMap =
            myFileNodeShareList.stream().collect(Collectors.groupingBy(FileNodeShare::getFileNodeId));
        for (String fileNodeId : fileNodeIdAndListMap.keySet()) {
            FileNode fileNode = fileNodeService.findById(fileNodeId);
            if (!fileNode.isDeleted()) {
                FileNodeShareDTO fileNodeShareDTO = new FileNodeShareDTO();
                fileNodeShareDTO.setFileNode(FileNodeDTO.from(fileNode));

                List<FileNodeShare> fileNodeShareList = fileNodeIdAndListMap.get(fileNodeId);
                String toOrgUnitNames =
                    fileNodeShareList.stream().map(FileNodeShare::getToOrgUnitName).collect(Collectors.joining("，"));
                fileNodeShareDTO.setToOrgUnitNames(toOrgUnitNames);

                fileNodeShareDTOList.add(fileNodeShareDTO);
            }
        }
        return Y9Result.success(fileNodeShareDTOList);
    }

    /**
     * 公开文件
     * 
     * @param fileNodeIdList
     * @param orgUnitIdList
     * @return
     */
    @PostMapping("/publicTo")
    public Y9Result<Object> publicTo(@RequestParam(name = "fileNodeIds") List<String> fileNodeIdList,
        @RequestParam(name = "orgUnitIds") List<String> orgUnitIdList) {
        fileNodeShareService.publicTo(fileNodeIdList, orgUnitIdList);
        return Y9Result.success();
    }

    /**
     * 分享文件
     * 
     * @param fileNodeIdList
     * @param orgUnitIdList
     * @return
     */
    @PostMapping("/share")
    public Y9Result<Object> share(@RequestParam(name = "fileNodeIds") List<String> fileNodeIdList,
        @RequestParam(name = "orgUnitIds") List<String> orgUnitIdList) {
        fileNodeShareService.share(fileNodeIdList, orgUnitIdList);
        fileNodeService.share(fileNodeIdList);
        return Y9Result.success();
    }
}
