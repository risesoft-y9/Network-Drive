package net.risesoft.controller;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.controller.dto.FileNodeDTO;
import net.risesoft.controller.dto.FileNodeListDTO;
import net.risesoft.entity.FileDownLoadRecord;
import net.risesoft.entity.FileNode;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.log.annotation.RiseLog;
import net.risesoft.model.user.UserInfo;
import net.risesoft.pojo.Y9Page;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.FileDownLoadRecordService;
import net.risesoft.service.FileNodeCollectService;
import net.risesoft.service.FileNodeService;
import net.risesoft.service.FileNodeShareService;
import net.risesoft.support.FileListType;
import net.risesoft.support.FileNodeType;
import net.risesoft.support.OrderRequest;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.util.Y9Util;
import net.risesoft.y9.util.mime.ContentDispositionUtil;
import net.risesoft.y9.util.signing.Y9MessageDigestUtil;
import net.risesoft.y9public.entity.Y9FileStore;
import net.risesoft.y9public.service.Y9FileStoreService;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/vue/fileNode")
public class FileNodeController {

    private final Y9FileStoreService y9FileStoreService;
    private final FileNodeService fileNodeService;
    private final FileDownLoadRecordService fileDownLoadRecordService;
    private final FileNodeShareService fileNodeShareService;
    private final FileNodeCollectService fileNodeCollectService;

    /**
     * 取消文件夹密码
     * 
     * @param folder
     * @return
     */
    @RiseLog(operationName = "取消文件夹密码")
    @RequestMapping(value = "/cancelFolderPassword")
    public Y9Result<String> cancelFolderPassword(FileNode folder) {
        try {
            FileNode node = fileNodeService.findById(folder.getId());
            if (null != node) {
                if (StringUtils.isNotBlank(node.getFilePassword())) {
                    String inputPwd = Y9MessageDigestUtil.md5(folder.getFilePassword());
                    if (inputPwd.equals(node.getFilePassword())) {
                        node.setFilePassword("");
                        node.setUpdateTime(new Date());
                        fileNodeService.saveNode(node);
                        return Y9Result.success("文件夹密码取消成功！");
                    } else {
                        return Y9Result.failure("文件夹原始密码错误！");
                    }
                } else {
                    return Y9Result.failure("该文件夹未设置密码，请先设置密码！");
                }
            }
            return Y9Result.failure("文件夹密码取消失败！未找到文件！");
        } catch (Exception e) {
            LOGGER.error("取消文件夹密码失败！", e);
            return Y9Result.failure("文件夹密码取消失败！");
        }
    }

    /**
     * 验证文件夹密码
     *
     * @param folder
     * @return
     */
    @RiseLog(operationName = "验证文件夹密码")
    @RequestMapping(value = "/checkFolderPassword")
    public Y9Result<String> checkFolderPassword(FileNode folder) {
        try {
            FileNode node = fileNodeService.findById(folder.getId());
            if (null != node) {
                if (StringUtils.isNotBlank(node.getFilePassword())) {
                    String inputPwd = Y9MessageDigestUtil.md5(folder.getFilePassword());
                    if (inputPwd.equals(node.getFilePassword())) {
                        return Y9Result.success("文件夹密码验证成功！");
                    } else {
                        return Y9Result.failure("原始密码输入错误，请重新输入！");
                    }
                } else {
                    return Y9Result.failure("该文件夹未设置密码，请先设置密码！");
                }
            }
            return Y9Result.failure("文件夹密码验证失败，未找到文件！");
        } catch (Exception e) {
            LOGGER.error("验证文件夹密码失败！", e);
            return Y9Result.failure("文件夹密码验证失败！");
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
                LOGGER.error("压缩文件失败！", e);
            }
        }
    }

    /**
     * 文件夹解密
     *
     * @param folder
     * @return
     */
    @RiseLog(operationName = "解密文件夹")
    @RequestMapping(value = "/decryptPassword")
    public Y9Result<String> decryptPassword(FileNode folder) {
        try {
            FileNode node = fileNodeService.findById(folder.getId());
            if (null != node) {
                if (StringUtils.isNotBlank(node.getFilePassword())) {
                    String inputPwd = Y9MessageDigestUtil.md5(folder.getFilePassword());
                    if (inputPwd.equals(node.getFilePassword())) {
                        return Y9Result.success("文件夹解密成功！");
                    } else {
                        return Y9Result.failure("密码输入错误，请重新输入！");
                    }
                }
            }
            return Y9Result.failure("文件夹解密失败！未找到文件!");
        } catch (Exception e) {
            LOGGER.error("文件夹解密失败！", e);
            return Y9Result.failure("文件夹解密失败！");
        }
    }

    /**
     * 下载单个或者多个文件
     * 
     * @param positionId
     * @param idList
     * @param response
     */
    @RiseLog(operationName = "下载文件")
    @RequestMapping(value = "/downloadFile")
    public void downloadFile(@RequestParam(required = false) String positionId,
        @RequestParam(name = "ids") List<String> idList, HttpServletResponse response) {
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
            }
        } catch (Exception e) {
            LOGGER.error("批量下载文件失败！", e);
        } finally {
            try {
                zipos.close();
            } catch (IOException e) {
                LOGGER.error("关闭压缩流失败！", e);
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
            LOGGER.error("下载文件失败！", e);
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                LOGGER.error("关闭输出流失败！", e);
            }
        }
    }

    /**
     * 清空回收站
     *
     * @return
     */
    @RiseLog(operationName = "清空回收站")
    @DeleteMapping(value = "/emptyRecycleBin")
    public Y9Result<Object> emptyRecycleBin() {
        fileNodeService.emptyRecycleBin();
        return Y9Result.success(null, "成功清空回收站");
    }

    /**
     * 获取收藏列表
     * 
     * @param positionId
     * @param id
     * @param searchName
     * @param listType
     * @param orderRequest
     * @return
     */
    @RiseLog(operationName = "获取收藏列表")
    @GetMapping(value = "/getCollectList")
    public Y9Result<FileNodeListDTO> getCollectList(@RequestHeader("positionId") String positionId,
        @RequestParam(required = false) String id, @RequestParam(required = false) String searchName,
        @RequestParam(required = false) String listType, OrderRequest orderRequest) {
        UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
        List<String> listNames = new ArrayList<String>();
        listNames.add(FileListType.MY.getValue());
        listNames.add(FileListType.DEPT.getValue());
        listNames.add(FileListType.PUBLIC.getValue());
        listNames.add(FileListType.SHARED.getValue());
        List<String> collectList = new ArrayList<String>();
        List<FileNode> subFileList = new ArrayList<FileNode>();
        if (StringUtils.isNotBlank(id) && !listNames.contains(id)) {
            subFileList = fileNodeService.subCollectList(id, searchName, listType, orderRequest);
        } else {
            collectList = fileNodeCollectService.getCollectList(userInfo.getPersonId(), listNames);
            subFileList = fileNodeService.subCollectList(collectList, searchName, orderRequest);
        }

        List<FileNodeDTO> fileNodeDTOList = FileNodeDTO.from(subFileList);
        for (FileNodeDTO fileNodeDTO : fileNodeDTOList) {
            boolean isCollect =
                fileNodeCollectService.findByCollectUserIdAndFileId(userInfo.getPersonId(), fileNodeDTO.getId());
            fileNodeDTO.setCollect(isCollect);
            List<FileNode> rootList = fileNodeService.recursiveToRoot(fileNodeDTO.getId());
            List<String> list = rootList.stream().map(FileNode::getName).collect(Collectors.toList());
            fileNodeDTO.setCollectRoute(Y9Util.join(list, "/"));
        }
        List<FileNode> recursiveToRootFileNodeList = fileNodeService.recursiveToRoot(id);
        FileNodeListDTO fileNodeListDTO = new FileNodeListDTO();
        fileNodeListDTO.setSubFileNodeList(fileNodeDTOList);
        fileNodeListDTO.setRecursiveToRootFileNodeList(FileNodeDTO.from(recursiveToRootFileNodeList));
        return Y9Result.success(fileNodeListDTO);
    }

    /**
     * 获取公共文件下载记录
     *
     * @param fileId
     * @param page
     * @param rows
     * @return
     */
    @SuppressWarnings("deprecation")
    @RiseLog(operationName = "获取公共文件下载记录")
    @GetMapping(value = "/getDownloadRecord")
    public Y9Page<Map<String, Object>> getDownloadRecord(String fileId, int page, int rows) {
        List<Map<String, Object>> items = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
        return Y9Page.success(page, dlList.getTotalPages(), dlList.getTotalElements(), items);
    }

    /**
     * 获取txt文件的内容
     * 
     * @param fileStoreId
     * @return
     */
    @RiseLog(operationName = "获取txt文件的内容")
    @RequestMapping(value = "/getFileText")
    public Y9Result<Object> getFileText(String fileStoreId) {
        try {
            String info = y9FileStoreService.downloadFileToString(fileStoreId);
            return Y9Result.success(info, "获取文件内容失败");
        } catch (Exception e) {
            LOGGER.error("获取文件内容失败！", e);
        }
        return Y9Result.success(null, "获取文件内容失败");
    }

    /**
     * 获取我的回收站的文件列表
     *
     * @return
     */
    @RiseLog(operationName = "获取我的回收站的文件列表")
    @RequestMapping(value = "/deletedList")
    public Y9Result<List<FileNodeDTO>> deletedList() {
        List<FileNode> fileNodeList = fileNodeService.deletedList(Y9LoginUserHolder.getUserInfo().getPersonId());
        return Y9Result.success(FileNodeDTO.from(fileNodeList));
    }

    /**
     * 获取当前节点的父节点
     *
     * @param id
     * @return
     */
    @RiseLog(operationName = "获取当前节点的父节点")
    @GetMapping(value = "/getNetParentId")
    public Y9Result<FileNode> getNetParentId(String id) {
        FileNode node = fileNodeService.findById(id);
        return Y9Result.success(node);
    }

    /**
     * 根据文件类型和列表类型获取文件列表
     *
     * @param id
     * @param orderRequest
     * @return
     */
    @RiseLog(operationName = "根据文件类型和列表类型获取文件列表")
    @GetMapping(value = "/list")
    public Y9Result<FileNodeListDTO> list(@RequestHeader("positionId") String positionId,
        @RequestParam(required = false) String id, @RequestParam(required = false) FileNodeType fileNodeType,
        @RequestParam(required = false) String searchName, @RequestParam(required = false) String listType,
        OrderRequest orderRequest) {
        UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
        List<FileNode> subFileList =
            fileNodeService.subList(positionId, id, fileNodeType, searchName, listType, orderRequest);
        List<FileNodeDTO> fileNodeDTOList = FileNodeDTO.from(subFileList);

        for (FileNodeDTO fileNodeDTO : fileNodeDTOList) {
            boolean isCollect = fileNodeCollectService.findByCollectUserIdAndFileIdAndListName(userInfo.getPersonId(),
                fileNodeDTO.getId(), fileNodeDTO.getListType());
            fileNodeDTO.setCollect(isCollect);
            if (StringUtils.isNotBlank(searchName)) {
                FileNode parentFileNode = fileNodeService.getParent(fileNodeDTO.getParentId());
                fileNodeDTO.setParentFileNode(FileNodeDTO.from(parentFileNode));
            }
        }

        List<FileNode> recursiveToRootFileNodeList = fileNodeService.recursiveToRoot(id);
        FileNodeListDTO fileNodeListDTO = new FileNodeListDTO();
        fileNodeListDTO.setSubFileNodeList(fileNodeDTOList);
        fileNodeListDTO.setRecursiveToRootFileNodeList(FileNodeDTO.from(recursiveToRootFileNodeList));
        return Y9Result.success(fileNodeListDTO);
    }

    /**
     * 加载所有文件夹
     *
     * @param id
     * @return
     */
    @RiseLog(operationName = "加载所有文件夹")
    @GetMapping(value = "/listFolder")
    public Y9Result<List<FileNodeDTO>> listFolder(@RequestParam(required = false) String id) {
        List<FileNode> subFileList = fileNodeService.subFolderList(id);
        return Y9Result.success(FileNodeDTO.from(subFileList));
    }

    /**
     * 加载公共文件下所有文件夹
     *
     * @param id
     * @return
     */
    @RiseLog(operationName = "加载公共文件下所有文件夹")
    @GetMapping(value = "/listPublicFolder")
    public Y9Result<List<FileNodeDTO>> listPublicFolder(@RequestParam(required = false) String id) {
        List<FileNode> subFileList = fileNodeService.subPublicFolderList(id);
        return Y9Result.success(FileNodeDTO.from(subFileList));
    }

    /**
     * 删除文件（标记删除）
     *
     * @param idList
     * @return
     */
    @RiseLog(operationName = "删除文件")
    @DeleteMapping
    public Y9Result<Object> logicDelete(@RequestParam(name = "ids") List<String> idList) {
        fileNodeService.logicDelete(idList);
        return Y9Result.success(null, "删除成功");
    }

    /**
     * 加载所有公共管理文件
     *
     * @param id
     * @param orderRequest
     * @return
     */
    @RiseLog(operationName = "加载所有公共管理文件")
    @GetMapping(value = "/manageList")
    public Y9Result<FileNodeListDTO> manageList(@RequestParam(required = false) String id,
        @RequestParam(required = false) FileNodeType fileNodeType, @RequestParam(required = false) String searchName,
        @RequestParam(required = false) String startTime, @RequestParam(required = false) String endTime,
        @RequestParam(required = false) String listType, OrderRequest orderRequest) {

        List<FileNode> subFileList =
            fileNodeService.subManageList(id, fileNodeType, searchName, startTime, endTime, listType, orderRequest);
        List<FileNodeDTO> fileNodeDTOList = FileNodeDTO.from(subFileList);

        if (StringUtils.isNotBlank(searchName)) {
            for (FileNodeDTO fileNodeDTO : fileNodeDTOList) {
                FileNode parentFileNode = fileNodeService.getParent(fileNodeDTO.getParentId());
                fileNodeDTO.setParentFileNode(FileNodeDTO.from(parentFileNode));
            }
        }

        List<FileNode> recursiveToRootFileNodeList = fileNodeService.recursiveToRoot(id);

        FileNodeListDTO fileNodeListDTO = new FileNodeListDTO();
        fileNodeListDTO.setSubFileNodeList(fileNodeDTOList);
        fileNodeListDTO.setRecursiveToRootFileNodeList(FileNodeDTO.from(recursiveToRootFileNodeList));
        return Y9Result.success(fileNodeListDTO);
    }

    /**
     * 移动文件
     * 
     * @param idList
     * @param targetId
     * @return
     */
    @RiseLog(operationName = "移动文件")
    @PostMapping(value = "/move")
    public Y9Result<Object> moveTo(@RequestParam(name = "ids") List<String> idList, String targetId) {
        fileNodeService.move(idList, targetId);
        return Y9Result.success(null, "移动成功！");
    }

    /**
     * 打开文件
     * 
     * @param fileStoreId
     * @param request
     * @param response
     */
    @RiseLog(operationName = "打开文件")
    @RequestMapping(value = "/openFile")
    public void openFile(String fileStoreId, HttpServletRequest request, HttpServletResponse response) {
        ServletOutputStream os = null;
        try {
            Y9FileStore f = y9FileStoreService.getById(fileStoreId);
            if (f != null) {
                String fileName = f.getFileName();
                String y9FileStoreId = f.getId();
                String userAgent = request.getHeader("User-Agent");
                if (userAgent.contains("MSIE") || userAgent.contains("Trident")) {
                    fileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
                    fileName = fileName.replaceAll("\\+", "%20");
                } else {
                    fileName = new String(fileName.getBytes(StandardCharsets.UTF_8), "ISO8859-1");
                }

                response.reset();
                response.setContentType("text/html;charset=UTF-8");
                response.setContentType("application/octet-stream");
                response.setHeader("Content-disposition", ContentDispositionUtil.standardizeAttachment(fileName));

                os = response.getOutputStream();
                y9FileStoreService.downloadFileToOutputStream(y9FileStoreId, os);
            }
        } catch (Exception e) {
            LOGGER.error("下载文件失败！", e);
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                LOGGER.error("关闭输出流失败！", e);
            }
        }
    }

    /**
     * 彻底删除文件
     *
     * @param idList
     * @return
     */
    @RiseLog(operationName = "彻底删除文件")
    @RequestMapping(value = "/permanently")
    public Y9Result<Object> permanentlyDelete(@RequestParam(name = "ids") List<String> idList) {
        fileNodeShareService.deleteByFileNodeIdList(Y9LoginUserHolder.getUserInfo().getPersonId(), idList);
        fileNodeService.permanentlyDelete(idList);
        return Y9Result.success(null, "删除成功");
    }

    /**
     * 加载所有公共文件
     *
     * @param id
     * @param orderRequest
     * @return
     */
    @RiseLog(operationName = "加载所有公共文件")
    @GetMapping(value = "/publicList")
    public Y9Result<FileNodeListDTO> publicList(@RequestParam(required = false) String id,
        @RequestParam(required = false) FileNodeType fileNodeType, @RequestParam(required = false) String searchName,
        @RequestParam(required = false) String startTime, @RequestParam(required = false) String endTime,
        @RequestParam(required = false) String listType, OrderRequest orderRequest) {
        UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
        List<FileNode> subFileList =
            fileNodeService.subPublicList(id, fileNodeType, searchName, startTime, endTime, listType, orderRequest);
        List<FileNodeDTO> fileNodeDTOList = FileNodeDTO.from(subFileList);

        // if (StringUtils.isNotBlank(searchName)) {
        for (FileNodeDTO fileNodeDTO : fileNodeDTOList) {
            // FileNode parentFileNode = fileNodeService.getParent(fileNodeDTO.getParentId());
            // fileNodeDTO.setParentFileNode(FileNodeDTO.from(parentFileNode));

            boolean isCollect = fileNodeCollectService.findByCollectUserIdAndFileIdAndListName(userInfo.getPersonId(),
                fileNodeDTO.getId(), fileNodeDTO.getListType());
            fileNodeDTO.setCollect(isCollect);
        }
        // }

        List<FileNode> recursiveToRootFileNodeList = fileNodeService.recursiveToRoot(id);

        FileNodeListDTO fileNodeListDTO = new FileNodeListDTO();
        fileNodeListDTO.setSubFileNodeList(fileNodeDTOList);
        fileNodeListDTO.setRecursiveToRootFileNodeList(FileNodeDTO.from(recursiveToRootFileNodeList));
        return Y9Result.success(fileNodeListDTO);
    }

    /**
     * 保存重置的密码
     *
     * @param folder
     * @return
     */
    @RiseLog(operationName = "重置文件夹密码")
    @RequestMapping(value = "/resetFolderPassword")
    public Y9Result<Object> resetFolderPassword(FileNode folder) {
        try {
            FileNode node = fileNodeService.findById(folder.getId());
            if (null != node) {
                String newPassword = Y9MessageDigestUtil.md5(folder.getFilePassword());
                // if(newPassword.equals(node.getFilePassword())){
                // y9Result.setMsg("新密码与原始密码一致，请重新设置密码！");
                // y9Result.setSuccess(false);
                // } else {
                node.setFilePassword(newPassword);
                node.setUpdateTime(new Date());
                fileNodeService.saveNode(node);
                // }
                return Y9Result.success("文件夹密码重置成功！");
            }
            return Y9Result.failure("文件夹密码重置失败！未找到文件!");
        } catch (Exception e) {
            LOGGER.error("文件夹密码重置失败！", e);
            return Y9Result.failure("文件夹密码重置失败！");
        }

    }

    /**
     * 还原文件
     *
     * @param idList
     * @return
     */
    @RiseLog(operationName = "还原文件")
    @RequestMapping(value = "/restore")
    public Y9Result<Object> restore(@RequestParam(name = "ids") List<String> idList) {
        fileNodeService.restore(idList);
        return Y9Result.success(null, "还原成功！");
    }

    /**
     * 保存文件夹信息
     *
     * @param positionId
     * @param folder
     * @return
     */
    @RiseLog(operationName = "保存文件夹")
    @RequestMapping(value = "/saveFolder")
    public Y9Result<Object> saveFolder(@RequestHeader("positionId") String positionId, FileNode folder) {
        Y9LoginUserHolder.setPositionId(positionId);
        fileNodeService.saveFolder(folder);

        return Y9Result.success(null, "文件夹创建成功");
    }

    /**
     * 上传文件
     *
     * @param file
     * @param parentId
     * @return
     */
    @RiseLog(operationName = "上传文件")
    @RequestMapping(value = "/uploadFile")
    public Y9Result<Map<String, Object>> saveOrUpdate(@RequestHeader("positionId") String positionId,
        MultipartFile file, String parentId, String listType) {
        Y9LoginUserHolder.setPositionId(positionId);
        Map<String, Object> map = fileNodeService.saveUploadFile(file, parentId, listType);
        return Y9Result.success(map);
    }

    /**
     * 保存设置的文件夹密码
     *
     * @param folder
     * @return
     */
    @RiseLog(operationName = "保存设置文件夹密码")
    @RequestMapping(value = "/setFolderPassword")
    public Y9Result<Object> setFolderPassword(FileNode folder) {
        try {
            FileNode node = fileNodeService.findById(folder.getId());
            node.setFilePassword(Y9MessageDigestUtil.md5(folder.getFilePassword()));
            node.setUpdateTime(new Date());
            fileNodeService.saveNode(node);
            return Y9Result.success("文件夹密码设置成功！");
        } catch (Exception e) {
            e.printStackTrace();
            return Y9Result.failure("文件夹密码设置失败！");
        }
    }

    /**
     * 设置直链文件密码
     *
     * @param id
     * @return
     */
    @RiseLog(operationName = "设置直链文件密码")
    @RequestMapping(value = "/setLinkPwd")
    public Y9Result<Object> setLinkPwd(String id, boolean encryption, String linkPassword) {
        try {
            FileNode fileNode = fileNodeService.findById(id);
            if (null != fileNode) {
                fileNode.setEncryption(encryption);
                fileNode.setLinkPassword(encryption ? linkPassword : "");
                fileNodeService.saveNode(fileNode);
            }
        } catch (Exception e) {
            LOGGER.error("设置文件直链密码失败！", e);
        }
        return Y9Result.success(null, "设置文件直链密码成功");
    }

    /**
     * 获取默认顶节点
     *
     * @return
     */
    @RiseLog(operationName = "获取默认顶节点")
    @GetMapping(value = "/topFolder")
    public Y9Result<List<FileNodeDTO>> topFolder(String parentId) {
        List<FileNode> subFileList = new ArrayList<>();
        FileNode node = new FileNode();
        node.setId(parentId);
        node.setName("全部文件");
        node.setFileType(0);
        node.setParentId("");
        subFileList.add(node);
        return Y9Result.success(FileNodeDTO.from(subFileList));
    }

}
