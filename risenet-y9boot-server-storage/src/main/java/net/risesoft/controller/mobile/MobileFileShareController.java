package net.risesoft.controller.mobile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.api.platform.org.OrgUnitApi;
import net.risesoft.api.platform.user.UserApi;
import net.risesoft.consts.UtilConsts;
import net.risesoft.controller.dto.FileNodeDTO;
import net.risesoft.controller.dto.FileNodeShareDTO;
import net.risesoft.entity.FileNode;
import net.risesoft.entity.FileNodeShare;
import net.risesoft.enums.platform.org.OrgTypeEnum;
import net.risesoft.log.annotation.RiseLog;
import net.risesoft.model.platform.org.OrgUnit;
import net.risesoft.model.user.UserInfo;
import net.risesoft.service.FileNodeService;
import net.risesoft.service.FileNodeShareService;
import net.risesoft.support.FileOptType;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.json.Y9JsonUtil;
import net.risesoft.y9.util.Y9Util;

/**
 * 文件共享处理接口
 *
 * @author yihong
 *
 */
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/mobile/fileNodeShare")
public class MobileFileShareController {

    private final FileNodeService fileNodeService;
    private final FileNodeShareService fileNodeShareService;

    private final OrgUnitApi orgUnitApi;
    private final UserApi userApi;

    /**
     * 获取文件共享记录列表
     * 
     * @param tenantId 租户id
     * @param userId 用户id
     * @param response
     */
    @RiseLog(operationName = "获取文件共享记录列表")
    @RequestMapping(value = "/getFileNodeShareRecordList")
    public void getFileNodeShareRecordList(@RequestHeader("auth-tenantId") String tenantId,
        @RequestHeader("auth-userId") String userId, HttpServletResponse response) {
        Map<String, Object> map = new HashMap<String, Object>(16);
        try {
            Y9LoginUserHolder.setTenantId(tenantId);
            Y9LoginUserHolder.setPersonId(userId);
            List<FileNodeShareDTO> fileNodeShareDTOList = new ArrayList<>();
            List<FileNodeShare> myFileNodeShareList = fileNodeShareService.list(userId, FileOptType.SHARE.getValue());
            Map<String, List<FileNodeShare>> fileNodeIdAndListMap =
                myFileNodeShareList.stream().collect(Collectors.groupingBy(FileNodeShare::getFileNodeId));
            for (String fileNodeId : fileNodeIdAndListMap.keySet()) {
                FileNode fileNode = fileNodeService.findById(fileNodeId);
                if (!fileNode.isDeleted()) {
                    FileNodeShareDTO fileNodeShareDTO = new FileNodeShareDTO();
                    fileNodeShareDTO.setFileNode(FileNodeDTO.from(fileNode));

                    List<FileNodeShare> fileNodeShareList = fileNodeIdAndListMap.get(fileNodeId);
                    String toOrgUnitNames = fileNodeShareList.stream()
                        .map(FileNodeShare::getToOrgUnitName)
                        .collect(Collectors.joining("，"));
                    fileNodeShareDTO.setToOrgUnitNames(toOrgUnitNames);
                    fileNodeShareDTOList.add(fileNodeShareDTO);
                }
            }
            map.put("fileNodeShareList", fileNodeShareDTOList);
            map.put(UtilConsts.SUCCESS, true);
            map.put("msg", "获取文件列表成功");
        } catch (Exception e) {
            map.put(UtilConsts.SUCCESS, false);
            map.put("msg", "获取文件列表失败");
            LOGGER.error("获取文件列表失败", e);
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

    /**
     * 共享文件
     * 
     * @param tenantId 租户id
     * @param userId 用户id
     * @param fileNodeIds 需要共享的文件id字符串，以“,”相隔
     * @param orgUnitIds 共享的人员id字符串，以“,”相隔
     * @param response
     * @throws Exception
     */
    @RiseLog(operationName = "共享文件")
    @RequestMapping(value = "/shareFile")
    public void shareFile(@RequestHeader("auth-tenantId") String tenantId, @RequestHeader("auth-userId") String userId,
        @RequestParam String fileNodeIds, @RequestParam String orgUnitIds, HttpServletResponse response)
        throws Exception {
        Y9LoginUserHolder.setTenantId(tenantId);
        UserInfo userInfo = userApi.get(tenantId, userId).getData();
        Y9LoginUserHolder.setUserInfo(userInfo);

        List<String> fileNodeIdList = Y9Util.stringToList(fileNodeIds, ",");
        List<String> orgUnitIdList = Y9Util.stringToList(orgUnitIds, ",");
        Map<String, Object> map = new HashMap<String, Object>(16);
        try {
            fileNodeShareService.share(fileNodeIdList, orgUnitIdList);
            fileNodeService.share(fileNodeIdList);
            map.put(UtilConsts.SUCCESS, true);
            map.put("msg", "共享成功");
        } catch (Exception e) {
            map.put(UtilConsts.SUCCESS, false);
            map.put("msg", "共享失败");
            LOGGER.error("共享失败", e);
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

    /**
     * 公共文件公开
     * 
     * @param tenantId 租户id
     * @param userId 用户id
     * @param fileNodeIds 需要公开的文件id字符串，以“,”相隔
     * @param orgUnitIds 公开的人员id字符串，以“,”相隔
     * @param response
     * @throws Exception
     */
    @RiseLog(operationName = "公共文件公开")
    @RequestMapping(value = "/publicFile")
    public void publicFile(@RequestHeader("auth-tenantId") String tenantId, @RequestHeader("auth-userId") String userId,
        @RequestParam String fileNodeIds, @RequestParam String orgUnitIds, HttpServletResponse response)
        throws Exception {
        Y9LoginUserHolder.setTenantId(tenantId);
        UserInfo userInfo = userApi.get(tenantId, userId).getData();
        Y9LoginUserHolder.setUserInfo(userInfo);

        List<String> fileNodeIdList = Y9Util.stringToList(fileNodeIds, ",");
        List<String> orgUnitIdList = Y9Util.stringToList(orgUnitIds, ",");
        Map<String, Object> map = new HashMap<String, Object>(16);
        try {
            fileNodeShareService.publicTo(fileNodeIdList, orgUnitIdList);
            map.put(UtilConsts.SUCCESS, true);
            map.put("msg", "公开成功");
        } catch (Exception e) {
            map.put(UtilConsts.SUCCESS, false);
            map.put("msg", "公开失败");
            LOGGER.error("公开失败", e);
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

    /**
     * 取消共享文件
     * 
     * @param tenantId 租户id
     * @param userId 用户id
     * @param fileNodeIds 需要取消的文件id字符串，以“,”相隔
     * @param response
     * @throws Exception
     */
    @RiseLog(operationName = "取消共享文件")
    @RequestMapping(value = "/cancelShare")
    public void cancelShare(@RequestHeader("auth-tenantId") String tenantId,
        @RequestHeader("auth-userId") String userId, @RequestParam String fileNodeIds, HttpServletResponse response)
        throws Exception {
        Y9LoginUserHolder.setTenantId(tenantId);
        UserInfo userInfo = userApi.get(tenantId, userId).getData();
        Y9LoginUserHolder.setUserInfo(userInfo);

        List<String> fileNodeIdList = Y9Util.stringToList(fileNodeIds, ",");
        Map<String, Object> map = new HashMap<String, Object>(16);
        try {
            fileNodeService.cancelShare(fileNodeIdList);
            fileNodeShareService.cancelShare(userId, fileNodeIdList);
            map.put(UtilConsts.SUCCESS, true);
            map.put("msg", "取消共享成功");
        } catch (Exception e) {
            map.put(UtilConsts.SUCCESS, false);
            map.put("msg", "取消共享失败");
            LOGGER.error("取消共享失败", e);
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

    /**
     * 获取公共文件公开记录列表
     * 
     * @param tenantId 租户id
     * @param userId 用户id
     * @param fileId 文件id
     * @param page 页数
     * @param rows 总行数
     * @param response
     */
    @RiseLog(operationName = "获取公共文件公开记录列表")
    @RequestMapping(value = "/getFilePublicRecord")
    public void getFilePublicRecord(@RequestHeader("auth-tenantId") String tenantId,
        @RequestHeader("auth-userId") String userId, @RequestParam String fileId, @RequestParam Integer page,
        @RequestParam Integer rows, HttpServletResponse response) throws Exception {
        Y9LoginUserHolder.setTenantId(tenantId);
        UserInfo userInfo = userApi.get(tenantId, userId).getData();
        Y9LoginUserHolder.setUserInfo(userInfo);

        List<Map<String, Object>> items = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Map<String, Object> ret_map = new HashMap<String, Object>(16);
        try {
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
            ret_map.put("rows", items);
            ret_map.put("currPage", page);
            ret_map.put("totalPages", dlList.getTotalPages());
            ret_map.put("total", dlList.getTotalElements());
            ret_map.put(UtilConsts.SUCCESS, true);
            ret_map.put("msg", "获取文件公开记录列表成功");
        } catch (Exception e) {
            ret_map.put(UtilConsts.SUCCESS, false);
            ret_map.put("msg", "获取文件公开记录列表失败");
            LOGGER.error("获取文件公开记录列表失败", e);
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(ret_map));
        return;
    }

    /**
     * 删除文件权限的公开人员
     * 
     * @param tenantId 租户id
     * @param userId 用户id
     * @param publicRecordIds 需要删除的公开记录id字符串（来源于getFilePublicRecord方法返回的id），以“,”相隔
     * @param response
     * @throws Exception
     */
    @RiseLog(operationName = "删除文件权限的公开人员")
    @RequestMapping(value = "/deletePublicPerson")
    public void deletePublicPerson(@RequestHeader("auth-tenantId") String tenantId,
        @RequestHeader("auth-userId") String userId, @RequestParam String publicRecordIds, HttpServletResponse response)
        throws Exception {
        Y9LoginUserHolder.setTenantId(tenantId);
        UserInfo userInfo = userApi.get(tenantId, userId).getData();
        Y9LoginUserHolder.setUserInfo(userInfo);

        List<String> publicRecordIdList = Y9Util.stringToList(publicRecordIds, ",");
        Map<String, Object> map = new HashMap<String, Object>(16);
        try {
            fileNodeShareService.deleteByFileNodeIdList(publicRecordIdList);
            map.put(UtilConsts.SUCCESS, true);
            map.put("msg", "删除公开人员成功");
        } catch (Exception e) {
            map.put(UtilConsts.SUCCESS, false);
            map.put("msg", "删除公开人员失败");
            LOGGER.error("删除公开人员失败", e);
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

}
