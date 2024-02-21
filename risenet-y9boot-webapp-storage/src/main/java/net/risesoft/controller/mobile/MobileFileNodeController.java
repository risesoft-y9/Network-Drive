package net.risesoft.controller.mobile;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import net.risesoft.consts.UtilConsts;
import net.risesoft.controller.dto.FileNodeDTO;
import net.risesoft.controller.dto.FileNodeListDTO;
import net.risesoft.entity.FileDownLoadRecord;
import net.risesoft.entity.FileNode;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.model.platform.Person;
import net.risesoft.model.user.UserInfo;
import net.risesoft.service.FileDownLoadRecordService;
import net.risesoft.service.FileNodeCollectService;
import net.risesoft.service.FileNodeService;
import net.risesoft.service.FileNodeShareService;
import net.risesoft.support.FileListType;
import net.risesoft.support.FileNodeType;
import net.risesoft.support.OrderProp;
import net.risesoft.support.OrderRequest;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.json.Y9JsonUtil;
import net.risesoft.y9.util.Y9Util;
import net.risesoft.y9.util.mime.ContentDispositionUtil;
import net.risesoft.y9.util.signing.Y9MessageDigest;
import net.risesoft.y9public.service.Y9FileStoreService;

import y9.client.platform.org.PersonApiClient;

/**
 * 文件列表及文件操作接口
 *
 * @author yihong
 *
 */
@RestController
@RequestMapping("/mobile/fileNode")
public class MobileFileNodeController {

    protected Logger log = LoggerFactory.getLogger(MobileFileNodeController.class);

    @Autowired
    private FileNodeService fileNodeService;

    @Autowired
    private FileNodeShareService fileNodeShareService;

    @Autowired
    private FileNodeCollectService fileNodeCollectService;

    @Autowired
    private FileDownLoadRecordService fileDownLoadRecordService;

    @Autowired
    private Y9FileStoreService y9FileStoreService;

    @Autowired
    private PersonApiClient personApiClient;

    /**
     * 获取文件列表
     * 
     * @param tenantId 租户id
     * @param userId 用户id
     * @param positionId 岗位id
     * @param folderId 文件夹id
     * @param fileNodeType 文件分类类型（文件夹：0，图片：1，文档：2，视频：3，音频：4，压缩包：5，其他：6）
     * @param searchName 文件名称查询
     * @param listType 列表类型（我的文件：my,共享文件：shared，公共文件：public,部门文件：dept，上报文件：report，上报文件管理：reportManage）
     * @param orderProp 排序字段（文件名称：FILE_NAME，文件大小：FILE_SIZE，更新时间：UPDATE_TIME，创建时间；CREATE_TIME）
     * @param orderAsc 排序是否正序（true|false）
     * @param response
     */
    @RequestMapping(value = "/getFileNodeList")
    public void getFileNodeList(@RequestHeader("auth-tenantId") String tenantId,
        @RequestHeader("auth-userId") String userId, @RequestHeader("auth-positionId") String positionId,
        @RequestParam String folderId, @RequestParam(required = false) String fileNodeType,
        @RequestParam(required = false) String searchName, @RequestParam String listType,
        @RequestParam(required = false) String orderProp, @RequestParam(required = false) boolean orderAsc,
        HttpServletResponse response) {
        Map<String, Object> map = new HashMap<String, Object>(16);
        try {
            Y9LoginUserHolder.setTenantId(tenantId);
            Y9LoginUserHolder.setPersonId(userId);
            Person person = personApiClient.getPerson(tenantId, userId).getData();
            Y9LoginUserHolder.setUserInfo(person.toUserInfo());
            OrderRequest orderRequest = new OrderRequest(OrderProp.valueOf(orderProp), orderAsc);
            FileNodeType fileType = null;
            if (StringUtils.isNotBlank(fileNodeType)) {
                fileType = FileNodeType.getByValue(Integer.valueOf(fileNodeType));
            }
            List<FileNode> subFileList =
                fileNodeService.subList(positionId, folderId, fileType, searchName, listType, orderRequest);
            List<FileNodeDTO> fileNodeDTOList = FileNodeDTO.from(subFileList);

            for (FileNodeDTO fileNodeDTO : fileNodeDTOList) {
                boolean isCollect = fileNodeCollectService.findByCollectUserIdAndFileIdAndListName(userId,
                    fileNodeDTO.getId(), fileNodeDTO.getListType());
                fileNodeDTO.setCollect(isCollect);
            }
            List<FileNode> recursiveToRootFileNodeList = fileNodeService.recursiveToRoot(folderId);
            FileNodeListDTO fileNodeListDTO = new FileNodeListDTO();
            fileNodeListDTO.setSubFileNodeList(fileNodeDTOList);
            fileNodeListDTO.setRecursiveToRootFileNodeList(FileNodeDTO.from(recursiveToRootFileNodeList));
            map.put("fileNodeList", fileNodeListDTO);
            map.put(UtilConsts.SUCCESS, true);
            map.put("msg", "获取文件列表成功");
        } catch (Exception e) {
            map.put(UtilConsts.SUCCESS, false);
            map.put("msg", "获取文件列表失败");
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

    /**
     * 获取公共文件列表
     * 
     * @param tenantId 租户id
     * @param userId 用户id
     * @param folderId 文件夹id
     * @param fileNodeType 文件分类类型（文件夹：0，图片：1，文档：2，视频：3，音频：4，压缩包：5，其他：6）
     * @param searchName 文件名称查询条件
     * @param startTime 开始时间查询条件
     * @param endTime 结束时间查询条件
     * @param listType 列表类型（我的文件：my,共享文件：shared，公共文件：public,部门文件：dept，上报文件：report，上报文件管理：reportManage）
     * @param orderProp 排序字段（文件名称：FILE_NAME，文件大小：FILE_SIZE，更新时间：UPDATE_TIME，创建时间；CREATE_TIME）
     * @param orderAsc 排序是否正序（true|false）
     * @param response
     */
    @RequestMapping(value = "/getPublicFileNodeList")
    public void getPublicFileNodeList(@RequestHeader("auth-tenantId") String tenantId,
        @RequestHeader("auth-userId") String userId, @RequestParam String folderId,
        @RequestParam(required = false) String fileNodeType, @RequestParam(required = false) String searchName,
        @RequestParam(required = false) String startTime, @RequestParam(required = false) String endTime,
        @RequestParam String listType, @RequestParam(required = false) String orderProp,
        @RequestParam(required = false) boolean orderAsc, HttpServletResponse response) {
        Map<String, Object> map = new HashMap<String, Object>(16);
        try {
            Y9LoginUserHolder.setTenantId(tenantId);
            Y9LoginUserHolder.setPersonId(userId);
            Person person = personApiClient.getPerson(tenantId, userId).getData();
            Y9LoginUserHolder.setUserInfo(person.toUserInfo());
            OrderRequest orderRequest = new OrderRequest(OrderProp.valueOf(orderProp), orderAsc);
            FileNodeType fileType = null;
            if (StringUtils.isNotBlank(fileNodeType)) {
                fileType = FileNodeType.getByValue(Integer.valueOf(fileNodeType));
            }
            List<FileNode> subFileList = fileNodeService.subPublicList(folderId, fileType, searchName, startTime,
                endTime, listType, orderRequest);
            List<FileNodeDTO> fileNodeDTOList = FileNodeDTO.from(subFileList);

            for (FileNodeDTO fileNodeDTO : fileNodeDTOList) {
                boolean isCollect = fileNodeCollectService.findByCollectUserIdAndFileIdAndListName(userId,
                    fileNodeDTO.getId(), fileNodeDTO.getListType());
                fileNodeDTO.setCollect(isCollect);
            }
            List<FileNode> recursiveToRootFileNodeList = fileNodeService.recursiveToRoot(folderId);
            FileNodeListDTO fileNodeListDTO = new FileNodeListDTO();
            fileNodeListDTO.setSubFileNodeList(fileNodeDTOList);
            fileNodeListDTO.setRecursiveToRootFileNodeList(FileNodeDTO.from(recursiveToRootFileNodeList));
            map.put("fileNodeList", fileNodeListDTO);
            map.put(UtilConsts.SUCCESS, true);
            map.put("msg", "获取公共文件列表成功");
        } catch (Exception e) {
            map.put(UtilConsts.SUCCESS, false);
            map.put("msg", "获取公共文件列表失败");
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

    /**
     * 获取公共管理文件列表
     * 
     * @param tenantId 租户id
     * @param userId 用户id
     * @param folderId 文件夹id
     * @param fileNodeType 文件分类类型（文件夹：0，图片：1，文档：2，视频：3，音频：4，压缩包：5，其他：6）
     * @param searchName 文件名称查询条件
     * @param startTime 开始时间查询条件
     * @param endTime 结束时间查询条件
     * @param listType 列表类型（我的文件：my,共享文件：shared，公共文件：public,部门文件：dept，上报文件：report，上报文件管理：reportManage）
     * @param orderProp 排序字段（文件名称：FILE_NAME，文件大小：FILE_SIZE，更新时间：UPDATE_TIME，创建时间；CREATE_TIME）
     * @param orderAsc 排序是否正序（true|false）
     * @param response
     */
    @RequestMapping(value = "/getPublicManageFileList")
    public void getPublicManageFileList(@RequestHeader("auth-tenantId") String tenantId,
        @RequestHeader("auth-userId") String userId, @RequestParam String folderId,
        @RequestParam(required = false) String fileNodeType, @RequestParam(required = false) String searchName,
        @RequestParam(required = false) String startTime, @RequestParam(required = false) String endTime,
        @RequestParam String listType, @RequestParam(required = false) String orderProp,
        @RequestParam(required = false) boolean orderAsc, HttpServletResponse response) {
        Map<String, Object> map = new HashMap<String, Object>(16);
        try {
            Y9LoginUserHolder.setTenantId(tenantId);
            Y9LoginUserHolder.setPersonId(userId);
            Person person = personApiClient.getPerson(tenantId, userId).getData();
            Y9LoginUserHolder.setUserInfo(person.toUserInfo());
            OrderRequest orderRequest = new OrderRequest(OrderProp.valueOf(orderProp), orderAsc);
            FileNodeType fileType = null;
            if (StringUtils.isNotBlank(fileNodeType)) {
                fileType = FileNodeType.getByValue(Integer.valueOf(fileNodeType));
            }
            List<FileNode> subFileList = fileNodeService.subManageList(folderId, fileType, searchName, startTime,
                endTime, listType, orderRequest);
            List<FileNodeDTO> fileNodeDTOList = FileNodeDTO.from(subFileList);

            for (FileNodeDTO fileNodeDTO : fileNodeDTOList) {
                boolean isCollect = fileNodeCollectService.findByCollectUserIdAndFileIdAndListName(userId,
                    fileNodeDTO.getId(), fileNodeDTO.getListType());
                fileNodeDTO.setCollect(isCollect);
                FileNode parentFileNode = fileNodeService.getParent(fileNodeDTO.getParentId());
                fileNodeDTO.setParentFileNode(FileNodeDTO.from(parentFileNode));
            }
            List<FileNode> recursiveToRootFileNodeList = fileNodeService.recursiveToRoot(folderId);
            FileNodeListDTO fileNodeListDTO = new FileNodeListDTO();
            fileNodeListDTO.setSubFileNodeList(fileNodeDTOList);
            fileNodeListDTO.setRecursiveToRootFileNodeList(FileNodeDTO.from(recursiveToRootFileNodeList));
            map.put("fileNodeList", fileNodeListDTO);
            map.put(UtilConsts.SUCCESS, true);
            map.put("msg", "获取公共文件列表成功");
        } catch (Exception e) {
            map.put(UtilConsts.SUCCESS, false);
            map.put("msg", "获取公共文件列表失败");
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

    /**
     * 获取我的收藏列表
     * 
     * @param tenantId 租户id
     * @param userId 用户id
     * @param folderId 文件夹id
     * @param searchName 文件名称查询条件
     * @param listType 列表类型（我的文件：my,共享文件：shared，公共文件：public,部门文件：dept，上报文件：report，上报文件管理：reportManage）
     * @param orderProp 排序字段（文件名称：FILE_NAME，文件大小：FILE_SIZE，更新时间：UPDATE_TIME，创建时间；CREATE_TIME）
     * @param orderAsc 排序是否正序（true|false）
     * @param response
     */
    @RequestMapping(value = "/getCollectFileNodeList")
    public void getCollectFileNodeList(@RequestHeader("auth-tenantId") String tenantId,
        @RequestHeader("auth-userId") String userId, @RequestParam String folderId,
        @RequestParam(required = false) String searchName, @RequestParam String listType,
        @RequestParam(required = false) String orderProp, @RequestParam(required = false) boolean orderAsc,
        HttpServletResponse response) {
        Map<String, Object> map = new HashMap<String, Object>(16);
        try {
            Y9LoginUserHolder.setTenantId(tenantId);
            Y9LoginUserHolder.setPersonId(userId);
            Person person = personApiClient.getPerson(tenantId, userId).getData();
            Y9LoginUserHolder.setUserInfo(person.toUserInfo());
            List<String> listNames = new ArrayList<String>();
            listNames.add(FileListType.MY.getValue());
            listNames.add(FileListType.DEPT.getValue());
            listNames.add(FileListType.PUBLIC.getValue());
            listNames.add(FileListType.SHARED.getValue());
            List<String> collectList = new ArrayList<String>();
            List<FileNode> subFileList = new ArrayList<FileNode>();
            OrderRequest orderRequest = new OrderRequest(OrderProp.valueOf(orderProp), orderAsc);
            if (StringUtils.isNotBlank(folderId) && !listNames.contains(folderId)) {
                subFileList = fileNodeService.subCollectList(folderId, searchName, listType, orderRequest);
            } else {
                collectList = fileNodeCollectService.getCollectList(userId, listNames);
                subFileList = fileNodeService.subCollectList(collectList, searchName, orderRequest);
            }

            List<FileNodeDTO> fileNodeDTOList = FileNodeDTO.from(subFileList);
            for (FileNodeDTO fileNodeDTO : fileNodeDTOList) {
                boolean isCollect = fileNodeCollectService.findByCollectUserIdAndFileIdAndListName(userId,
                    fileNodeDTO.getId(), fileNodeDTO.getListType());
                fileNodeDTO.setCollect(isCollect);
                List<FileNode> rootList = fileNodeService.recursiveToRoot(fileNodeDTO.getId());
                List<String> list = rootList.stream().map(FileNode::getName).collect(Collectors.toList());
                fileNodeDTO.setCollectRoute(Y9Util.join(list, "/"));
            }
            List<FileNode> recursiveToRootFileNodeList = fileNodeService.recursiveToRoot(folderId);
            FileNodeListDTO fileNodeListDTO = new FileNodeListDTO();
            fileNodeListDTO.setSubFileNodeList(fileNodeDTOList);
            fileNodeListDTO.setRecursiveToRootFileNodeList(FileNodeDTO.from(recursiveToRootFileNodeList));
            map.put("fileNodeList", fileNodeListDTO);
            map.put(UtilConsts.SUCCESS, true);
            map.put("msg", "获取公共文件列表成功");
        } catch (Exception e) {
            map.put(UtilConsts.SUCCESS, false);
            map.put("msg", "获取公共文件列表失败");
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

    /**
     * 获取回收站列表
     * 
     * @param tenantId 租户id
     * @param userId 用户id
     * @param response
     */
    @RequestMapping(value = "/getRecycleList")
    public void getRecycleList(@RequestHeader("auth-tenantId") String tenantId,
        @RequestHeader("auth-userId") String userId, HttpServletResponse response) {
        Map<String, Object> map = new HashMap<String, Object>();
        Y9LoginUserHolder.setTenantId(tenantId);
        Y9LoginUserHolder.setPersonId(userId);
        try {
            List<FileNode> fileNodeList = fileNodeService.deletedList(userId);
            map.put("fileNodeList", fileNodeList);
            map.put(UtilConsts.SUCCESS, true);
            map.put("msg", "获取回收站列表成功");
        } catch (Exception e) {
            map.put(UtilConsts.SUCCESS, false);
            map.put("msg", "获取回收站列表失败");
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

    /**
     * 清空回收站
     * 
     * @param tenantId
     * @param userId
     * @param response
     */
    @RequestMapping(value = "/clearRecycle")
    public void clearRecycle(@RequestHeader("auth-tenantId") String tenantId,
        @RequestHeader("auth-userId") String userId, HttpServletResponse response) {
        Map<String, Object> map = new HashMap<String, Object>();
        Y9LoginUserHolder.setTenantId(tenantId);
        Y9LoginUserHolder.setPersonId(userId);
        try {
            fileNodeService.emptyRecycleBin();
            map.put(UtilConsts.SUCCESS, true);
            map.put("msg", "清空回收站成功");
        } catch (Exception e) {
            map.put(UtilConsts.SUCCESS, false);
            map.put("msg", "清空回收站失败");
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

    /**
     * 删除回收站文件（彻底删除）
     * 
     * @param tenantId
     * @param userId
     * @param ids
     * @param response
     */
    @RequestMapping(value = "/deleteRecycleFile")
    public void deleteRecycleFile(@RequestHeader("auth-tenantId") String tenantId,
        @RequestHeader("auth-userId") String userId, @RequestParam String ids, HttpServletResponse response) {
        Map<String, Object> map = new HashMap<String, Object>();
        Y9LoginUserHolder.setTenantId(tenantId);
        Y9LoginUserHolder.setPersonId(userId);
        try {
            List<String> idsList = Y9Util.stringToList(ids, ",");
            fileNodeShareService.deleteByFileNodeIdList(Y9LoginUserHolder.getUserInfo().getPersonId(), idsList);
            fileNodeService.permanentlyDelete(idsList);
            map.put(UtilConsts.SUCCESS, true);
            map.put("msg", "彻底删除回收站文件成功");
        } catch (Exception e) {
            map.put(UtilConsts.SUCCESS, false);
            map.put("msg", "彻底删除回收站文件失败");
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

    /**
     * 还原回收站文件
     * 
     * @param tenantId
     * @param userId
     * @param ids
     * @param response
     */
    @RequestMapping(value = "/restoreRecycleFile")
    public void restoreRecycleFile(@RequestHeader("auth-tenantId") String tenantId,
        @RequestHeader("auth-userId") String userId, @RequestParam String ids, HttpServletResponse response) {
        Map<String, Object> map = new HashMap<String, Object>();
        Y9LoginUserHolder.setTenantId(tenantId);
        Y9LoginUserHolder.setPersonId(userId);
        try {
            List<String> idsList = Y9Util.stringToList(ids, ",");
            fileNodeService.restore(idsList);
            map.put(UtilConsts.SUCCESS, true);
            map.put("msg", "还原回收站文件成功");
        } catch (Exception e) {
            map.put(UtilConsts.SUCCESS, false);
            map.put("msg", "还原回收站文件失败");
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

    /**
     * 上传文件
     * 
     * @param tenantId 租户id
     * @param userId 用户id
     * @param positionId 岗位id
     * @param file 文件对象
     * @param parentId 父文件id（未进入列表文件夹时传listType的值，否则传点击文件夹的id）
     * @param listType 列表类型（我的文件：my,共享文件：shared，公共文件：public,部门文件：dept，上报文件：report，上报文件管理：reportManage）
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/uploadFile")
    public void uploadFile(@RequestHeader("auth-tenantId") String tenantId, @RequestHeader("auth-userId") String userId,
        @RequestHeader("auth-positionId") String positionId, @RequestParam(required = false) MultipartFile file,
        @RequestParam String parentId, @RequestParam String listType, HttpServletResponse response) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>(16);
        try {
            Y9LoginUserHolder.setTenantId(tenantId);
            Y9LoginUserHolder.setPersonId(userId);
            Y9LoginUserHolder.setPositionId(positionId);
            Person person = personApiClient.getPerson(tenantId, userId).getData();
            Y9LoginUserHolder.setUserInfo(person.toUserInfo());
            map = fileNodeService.saveUploadFile(file, parentId, listType);
        } catch (Exception e) {
            map.put(UtilConsts.SUCCESS, false);
            map.put("msg", "文件上传失败");
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

    /**
     * 新建文件夹
     * 
     * @param tenantId 租户id
     * @param userId 用户id
     * @param positionId 岗位id
     * @param name 文件名称
     * @param parentId 当前所在父目录id
     * @param listType 列表类型（我的文件：my,共享文件：shared，公共文件：public,部门文件：dept，上报文件：report，上报文件管理：reportManage）
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/addFolder")
    public void addFolder(@RequestHeader("auth-tenantId") String tenantId, @RequestHeader("auth-userId") String userId,
        @RequestHeader("auth-positionId") String positionId, @RequestParam String name, @RequestParam String parentId,
        @RequestParam String listType, HttpServletResponse response) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>(16);
        try {
            Y9LoginUserHolder.setTenantId(tenantId);
            Y9LoginUserHolder.setPersonId(userId);
            Y9LoginUserHolder.setPositionId(positionId);
            Person person = personApiClient.getPerson(tenantId, userId).getData();
            Y9LoginUserHolder.setUserInfo(person.toUserInfo());
            FileNode folder = new FileNode();
            folder.setName(name);
            folder.setParentId(parentId);
            folder.setListType(listType);
            fileNodeService.saveFolder(folder);
            map.put(UtilConsts.SUCCESS, true);
            map.put("msg", "文件夹新建成功");
        } catch (Exception e) {
            map.put(UtilConsts.SUCCESS, false);
            map.put("msg", "文件夹新建失败");
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

    /**
     * 单个或者多个文件下载
     * 
     * @param tenantId 租户id
     * @param userId 用户id
     * @param positionId 岗位id
     * @param ids 文件id字符串，多个id以“,”隔开
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/downloadFile")
    public void downloadFile(@RequestHeader("auth-tenantId") String tenantId,
        @RequestHeader("auth-userId") String userId, @RequestHeader("auth-positionId") String positionId,
        @RequestParam String ids, HttpServletResponse response) throws Exception {
        Y9LoginUserHolder.setTenantId(tenantId);
        Y9LoginUserHolder.setPersonId(userId);
        Y9LoginUserHolder.setPositionId(positionId);
        Person person = personApiClient.getPerson(tenantId, userId).getData();
        Y9LoginUserHolder.setUserInfo(person.toUserInfo());
        List<String> idList = Y9Util.stringToList(ids, ",");
        if (idList.size() > 1) {
            downloadMultiFile(positionId, idList, response);
        } else {
            downloadSingleFile(positionId, idList, response);
        }
    }

    public void downloadMultiFile(String positionId, List<String> idList, HttpServletResponse response) {
        ZipOutputStream zipos = null;
        long fileSize = 0;
        UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
        try {
            String fileName = "【批量下载】.zip";
            response.setContentType("text/html;charset=UTF-8");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-disposition", ContentDispositionUtil.standardizeAttachment(fileName));

            // 设置压缩流：直接写入response，实现边压缩边下载
            zipos = new ZipOutputStream(new BufferedOutputStream(response.getOutputStream()));
            zipos.setMethod(ZipOutputStream.DEFLATED);// 设置压缩方法

            for (String str : idList) {
                FileNode folder = fileNodeService.findById(str);
                compress(zipos, folder, folder.getName(), folder.getListType(), positionId);
                FileDownLoadRecord fdr = new FileDownLoadRecord();
                fdr.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
                fdr.setFileId(idList.get(0));
                fdr.setDownLoadTime(new Date());
                fdr.setDownLoadUserId(userInfo.getPersonId());
                fdr.setDownLoadUserName(userInfo.getName());
                fdr.setDownLoadMode("网盘");
                fileDownLoadRecordService.save(fdr);
                fileSize = fileSize + folder.getFileSize();
            }
            // response.setHeader("Content-Length", fileSize + "");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                zipos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void compress(ZipOutputStream out, FileNode fileNode, String base, String listType, String positionId)
        throws Exception {
        // 如果路径为目录（文件夹）
        if (FileNodeType.FOLDER.getValue().equals(fileNode.getFileType())) {
            // 取出文件夹中的文件（或子文件夹）
            List<FileNode> fileNodeList =
                fileNodeService.subList(positionId, fileNode.getId(), null, null, listType, new OrderRequest());

            if (fileNodeList.size() == 0) {// 如果文件夹为空，则只需在目的地zip文件中写入一个目录进入点
                out.putNextEntry(new ZipEntry(base + File.separator));
            } else {// 如果文件夹不为空，则递归调用compress,文件夹中的每一个文件（或文件夹）进行压缩
                for (FileNode fileNode1 : fileNodeList) {
                    compress(out, fileNode1, base + File.separator + fileNode1.getName(), fileNode1.getListType(),
                        positionId);
                }
            }
        } else {
            byte[] be = y9FileStoreService.downloadFileToBytes(fileNode.getFileStoreId());
            try {
                // 添加ZipEntry，并ZipEntry中写入文件流
                // 这里，加上i是防止要下载的文件有重名的导致下载失败
                out.putNextEntry(new ZipEntry(base));
                InputStream is = new ByteArrayInputStream(be);
                byte[] b = new byte[100];
                int length = 0;
                while ((length = is.read(b)) != -1) {
                    out.write(b, 0, length);
                }
                is.close();
                out.closeEntry();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void downloadSingleFile(String positionId, List<String> idList, HttpServletResponse response) {
        ServletOutputStream os = null;
        try {
            FileNode fileNode = fileNodeService.findById(idList.get(0));
            if (FileNodeType.FOLDER.getValue().equals(fileNode.getFileType())) {
                downloadMultiFile(positionId, idList, response);
            } else {
                UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
                String fileName = fileNode.getName();
                String y9FileStoreId = fileNode.getFileStoreId();
                response.setContentType("text/html;charset=UTF-8");
                response.setContentType("application/octet-stream");
                response.setHeader("Content-disposition", ContentDispositionUtil.standardizeAttachment(fileName));
                // response.setHeader("Content-length", fileNode.getFileSize().toString());
                os = response.getOutputStream();
                y9FileStoreService.downloadFileToOutputStream(y9FileStoreId, os);
                FileDownLoadRecord fdr = new FileDownLoadRecord();
                fdr.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
                fdr.setFileId(idList.get(0));
                fdr.setDownLoadTime(new Date());
                fdr.setDownLoadUserId(userInfo.getPersonId());
                fdr.setDownLoadUserName(userInfo.getName());
                fdr.setDownLoadMode("网盘");
                fileDownLoadRecordService.save(fdr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 删除单个或者多个文件（标记删除）
     * 
     * @param tenantId 租户id
     * @param userId 用户id
     * @param positionId 岗位id
     * @param ids 文件id字符串，多个id以“,”隔开
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/deleteFile")
    public void deleteFile(@RequestHeader("auth-tenantId") String tenantId, @RequestHeader("auth-userId") String userId,
        @RequestHeader("auth-positionId") String positionId, @RequestParam String ids, HttpServletResponse response)
        throws Exception {
        Y9LoginUserHolder.setTenantId(tenantId);
        Y9LoginUserHolder.setPersonId(userId);
        Y9LoginUserHolder.setPositionId(positionId);
        List<String> idList = Y9Util.stringToList(ids, ",");
        Map<String, Object> map = new HashMap<String, Object>(16);
        try {
            fileNodeService.logicDelete(idList);
            map.put(UtilConsts.SUCCESS, true);
            map.put("msg", "删除成功");
        } catch (Exception e) {
            map.put(UtilConsts.SUCCESS, false);
            map.put("msg", "删除失败");
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

    /**
     * 移动单个或者多个文件
     * 
     * @param tenantId 租户id
     * @param userId 用户id
     * @param ids 需要移动文件id字符串，多个id以“,”隔开
     * @param targetId 移动到目标文件夹的id
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/moveFile")
    public void moveFile(@RequestHeader("auth-tenantId") String tenantId, @RequestHeader("auth-userId") String userId,
        @RequestParam String ids, @RequestParam String targetId, HttpServletResponse response) throws Exception {
        Y9LoginUserHolder.setTenantId(tenantId);
        Y9LoginUserHolder.setPersonId(userId);
        List<String> idList = Y9Util.stringToList(ids, ",");
        Map<String, Object> map = new HashMap<String, Object>(16);
        try {
            fileNodeService.move(idList, targetId);
            map.put(UtilConsts.SUCCESS, true);
            map.put("msg", "移动成功");
        } catch (Exception e) {
            map.put(UtilConsts.SUCCESS, false);
            map.put("msg", "移动失败");
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

    /**
     * 重命名文件或文件夹
     * 
     * @param tenantId 租户id
     * @param userId 用户id
     * @param positionId 岗位id
     * @param id 文件id
     * @param name 文件名称
     * @param parentId 当前所在父目录id
     * @param listType 列表类型（我的文件：my,共享文件：shared，公共文件：public,部门文件：dept，上报文件：report，上报文件管理：reportManage）
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/rename")
    public void rename(@RequestHeader("auth-tenantId") String tenantId, @RequestHeader("auth-userId") String userId,
        @RequestHeader("auth-positionId") String positionId, @RequestParam String id, @RequestParam String name,
        @RequestParam String parentId, @RequestParam String listType, HttpServletResponse response) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>(16);
        try {
            Y9LoginUserHolder.setTenantId(tenantId);
            Y9LoginUserHolder.setPersonId(userId);
            Y9LoginUserHolder.setPositionId(positionId);
            Person person = personApiClient.getPerson(tenantId, userId).getData();
            Y9LoginUserHolder.setUserInfo(person.toUserInfo());
            FileNode folder = new FileNode();
            folder.setId(id);
            folder.setName(name);
            folder.setParentId(parentId);
            folder.setListType(listType);
            fileNodeService.saveFolder(folder);
            map.put(UtilConsts.SUCCESS, true);
            map.put("msg", "重命名成功");
        } catch (Exception e) {
            map.put(UtilConsts.SUCCESS, false);
            map.put("msg", "重命名失败");
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

    /**
     * 获取公共文件的文件公开记录列表
     * 
     * @param tenantId 租户id
     * @param userId 用户id
     * @param fileId 文件id
     * @param page 页数
     * @param rows 总行数
     * @param response
     */
    @RequestMapping(value = "/getDownloadRecord")
    public void getDownloadRecord(@RequestHeader("auth-tenantId") String tenantId,
        @RequestHeader("auth-userId") String userId, @RequestParam String fileId, @RequestParam Integer page,
        @RequestParam Integer rows, HttpServletResponse response) throws Exception {
        Y9LoginUserHolder.setTenantId(tenantId);
        Y9LoginUserHolder.setPersonId(userId);
        Person person = personApiClient.getPerson(tenantId, userId).getData();
        Y9LoginUserHolder.setUserInfo(person.toUserInfo());
        List<Map<String, Object>> items = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Map<String, Object> ret_map = new HashMap<String, Object>(16);
        try {
            if (page < 1) {
                page = 1;
            }
            Page<FileDownLoadRecord> dlList = fileDownLoadRecordService.getFileDownLoadRecord(fileId, page, rows);
            for (FileDownLoadRecord ddr : dlList) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", ddr.getId());
                map.put("fileId", ddr.getFileId());
                map.put("downLoadUserName", ddr.getDownLoadUserName());
                map.put("downLoadTime", sdf.format(ddr.getDownLoadTime()));
                items.add(map);
            }
            ret_map.put("rows", items);
            ret_map.put("currPage", page);
            ret_map.put("totalPages", dlList.getTotalPages());
            ret_map.put("total", dlList.getTotalElements());
            ret_map.put(UtilConsts.SUCCESS, true);
            ret_map.put("msg", "获取公共文件下载记录列表成功");
        } catch (Exception e) {
            ret_map.put(UtilConsts.SUCCESS, false);
            ret_map.put("msg", "获取公共文件下载记录列表失败");
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(ret_map));
        return;
    }

    /**
     * 文件夹加密方法
     * 
     * @param tenantId 租户id
     * @param userId 用户id
     * @param folderId 文件夹id
     * @param password 文件夹密码
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/setFolderPassword")
    public void setFolderPassword(@RequestHeader("auth-tenantId") String tenantId,
        @RequestHeader("auth-userId") String userId, @RequestParam String folderId, @RequestParam String password,
        HttpServletResponse response) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>(16);
        try {
            Y9LoginUserHolder.setTenantId(tenantId);
            Y9LoginUserHolder.setPersonId(userId);
            FileNode node = fileNodeService.findById(folderId);
            node.setFilePassword(Y9MessageDigest.MD5(password));
            node.setUpdateTime(new Date());
            fileNodeService.saveNode(node);
            map.put(UtilConsts.SUCCESS, true);
            map.put("msg", "文件夹加密成功");
        } catch (Exception e) {
            map.put(UtilConsts.SUCCESS, false);
            map.put("msg", "文件夹加密失败");
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

    /**
     * 取消文件夹密码方法
     * 
     * @param tenantId 租户id
     * @param userId 用户id
     * @param folderId 文件夹id
     * @param password 文件夹密码
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/cancelFolderPassword")
    public void cancelFolderPassword(@RequestHeader("auth-tenantId") String tenantId,
        @RequestHeader("auth-userId") String userId, @RequestParam String folderId, @RequestParam String password,
        HttpServletResponse response) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>(16);
        map.put(UtilConsts.SUCCESS, false);
        map.put("msg", "文件夹密码取消失败");
        try {
            Y9LoginUserHolder.setTenantId(tenantId);
            Y9LoginUserHolder.setPersonId(userId);
            FileNode node = fileNodeService.findById(folderId);
            if (null != node) {
                if (StringUtils.isNotBlank(node.getFilePassword())) {
                    String inputPwd = Y9MessageDigest.MD5(password);
                    if (inputPwd.equals(node.getFilePassword())) {
                        node.setFilePassword("");
                        node.setUpdateTime(new Date());
                        fileNodeService.saveNode(node);
                        map.put(UtilConsts.SUCCESS, true);
                        map.put("msg", "文件夹密码取消成功");
                    } else {
                        map.put(UtilConsts.SUCCESS, true);
                        map.put("msg", "文件夹原始密码错误");
                    }
                } else {
                    map.put(UtilConsts.SUCCESS, false);
                    map.put("msg", "该文件夹未设置密码，请先设置密码");
                }
            } else {
                map.put(UtilConsts.SUCCESS, false);
                map.put("msg", "文件夹密码取消失败！未找到文件");
            }
        } catch (Exception e) {
            map.put(UtilConsts.SUCCESS, false);
            map.put("msg", "文件夹密码取消失败");
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

    /**
     * 重置文件夹密码方法
     * 
     * @param tenantId 租户id
     * @param userId 用户id
     * @param folderId 文件夹id
     * @param password 文件夹密码
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/resetFolderPassword")
    public void resetFolderPassword(@RequestHeader("auth-tenantId") String tenantId,
        @RequestHeader("auth-userId") String userId, @RequestParam String folderId, @RequestParam String password,
        HttpServletResponse response) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>(16);
        map.put(UtilConsts.SUCCESS, false);
        map.put("msg", "文件夹密码重置失败");
        try {
            Y9LoginUserHolder.setTenantId(tenantId);
            Y9LoginUserHolder.setPersonId(userId);
            FileNode node = fileNodeService.findById(folderId);
            if (null != node) {
                String newPassword = Y9MessageDigest.MD5(password);
                node.setFilePassword(newPassword);
                node.setUpdateTime(new Date());
                fileNodeService.saveNode(node);
                map.put(UtilConsts.SUCCESS, true);
                map.put("msg", "文件夹密码重置成功");
            } else {
                map.put(UtilConsts.SUCCESS, false);
                map.put("msg", "文件夹密码重置失败！未找到文件!");
            }
        } catch (Exception e) {
            map.put(UtilConsts.SUCCESS, false);
            map.put("msg", "文件夹密码重置失败");
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

    /**
     * 验证文件夹密码方法
     * 
     * @param tenantId 租户id
     * @param userId 用户id
     * @param folderId 文件夹id
     * @param password 文件夹密码
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/checkFolderPassword")
    public void checkFolderPassword(@RequestHeader("auth-tenantId") String tenantId,
        @RequestHeader("auth-userId") String userId, @RequestParam String folderId, @RequestParam String password,
        HttpServletResponse response) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>(16);
        map.put(UtilConsts.SUCCESS, false);
        map.put("msg", "文件夹密码验证失败");
        try {
            Y9LoginUserHolder.setTenantId(tenantId);
            Y9LoginUserHolder.setPersonId(userId);
            FileNode node = fileNodeService.findById(folderId);
            if (null != node) {
                if (StringUtils.isNotBlank(node.getFilePassword())) {
                    String inputPwd = Y9MessageDigest.MD5(password);
                    if (inputPwd.equals(node.getFilePassword())) {
                        map.put(UtilConsts.SUCCESS, true);
                        map.put("msg", "文件夹密码验证成功");
                    } else {
                        map.put(UtilConsts.SUCCESS, false);
                        map.put("msg", "原始密码输入错误，请重新输入");
                    }
                } else {
                    map.put(UtilConsts.SUCCESS, false);
                    map.put("msg", "该文件夹未设置密码，请先设置密码");
                }
            } else {
                map.put(UtilConsts.SUCCESS, false);
                map.put("msg", "文件夹密码验证失败，未找到文件");
            }
        } catch (Exception e) {
            map.put(UtilConsts.SUCCESS, false);
            map.put("msg", "文件夹密码验证失败");
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

    /**
     * 文件夹解密方法
     * 
     * @param tenantId 租户id
     * @param userId 用户id
     * @param folderId 文件夹id
     * @param password 文件夹密码
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/decryptPassword")
    public void decryptPassword(@RequestHeader("auth-tenantId") String tenantId,
        @RequestHeader("auth-userId") String userId, @RequestParam String folderId, @RequestParam String password,
        HttpServletResponse response) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>(16);
        map.put(UtilConsts.SUCCESS, false);
        map.put("msg", "文件夹解密失败");
        try {
            Y9LoginUserHolder.setTenantId(tenantId);
            Y9LoginUserHolder.setPersonId(userId);
            FileNode node = fileNodeService.findById(folderId);
            if (null != node) {
                if (StringUtils.isNotBlank(node.getFilePassword())) {
                    String inputPwd = Y9MessageDigest.MD5(password);
                    if (inputPwd.equals(node.getFilePassword())) {
                        map.put(UtilConsts.SUCCESS, true);
                        map.put("msg", "文件夹解密成功");
                    } else {
                        map.put(UtilConsts.SUCCESS, false);
                        map.put("msg", "密码输入错误，请重新输入");
                    }
                }
            } else {
                map.put(UtilConsts.SUCCESS, false);
                map.put("msg", "文件夹解密失败，未找到文件");
            }
        } catch (Exception e) {
            map.put(UtilConsts.SUCCESS, false);
            map.put("msg", "文件夹解密失败");
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

    /**
     * 收藏文件或者文件夹
     * 
     * @param tenantId 租户id
     * @param userId 用户id
     * @param fileNodeId 文件夹或者文件id
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/setCollect")
    public void setCollect(@RequestHeader("auth-tenantId") String tenantId, @RequestHeader("auth-userId") String userId,
        @RequestParam String fileNodeId, HttpServletResponse response) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>(16);
        map.put(UtilConsts.SUCCESS, false);
        map.put("msg", "文件夹解密失败");
        try {
            Y9LoginUserHolder.setTenantId(tenantId);
            Y9LoginUserHolder.setPersonId(userId);
            fileNodeCollectService.save(fileNodeId, "");
            map.put(UtilConsts.SUCCESS, true);
            map.put("msg", "收藏成功");
        } catch (Exception e) {
            map.put(UtilConsts.SUCCESS, false);
            map.put("msg", "收藏失败");
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

    /**
     * 取消收藏
     * 
     * @param tenantId 租户id
     * @param userId 用户id
     * @param fileNodeId 文件夹或者文件id
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/cancelCollect")
    public void cancelCollect(@RequestHeader("auth-tenantId") String tenantId,
        @RequestHeader("auth-userId") String userId, @RequestParam String fileNodeId, HttpServletResponse response)
        throws Exception {
        Map<String, Object> map = new HashMap<String, Object>(16);
        map.put(UtilConsts.SUCCESS, false);
        map.put("msg", "文件夹解密失败");
        try {
            Y9LoginUserHolder.setTenantId(tenantId);
            Y9LoginUserHolder.setPersonId(userId);
            fileNodeCollectService.cancelCollect(fileNodeId);
            map.put(UtilConsts.SUCCESS, true);
            map.put("msg", "取消收藏成功");
        } catch (Exception e) {
            map.put(UtilConsts.SUCCESS, false);
            map.put("msg", "取消收藏失败");
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }
}
