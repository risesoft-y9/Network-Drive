package net.risesoft.service.Impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import net.risesoft.api.platform.org.PositionApi;
import net.risesoft.entity.FileNode;
import net.risesoft.entity.FileNodeShare;
import net.risesoft.entity.StorageCapacity;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.model.platform.OrgUnit;
import net.risesoft.model.platform.Position;
import net.risesoft.model.user.UserInfo;
import net.risesoft.repository.FileNodeRepository;
import net.risesoft.repository.FileNodeShareRepository;
import net.risesoft.repository.spec.FileNodeSpecification;
import net.risesoft.service.FileNodeService;
import net.risesoft.service.StorageCapacityService;
import net.risesoft.support.FileListType;
import net.risesoft.support.FileNodeType;
import net.risesoft.support.OrderProp;
import net.risesoft.support.OrderRequest;
import net.risesoft.support.RootFolder;
import net.risesoft.support.comparator.CreateTimeComparator;
import net.risesoft.support.comparator.FileNameComparator;
import net.risesoft.support.comparator.FileSizeComparator;
import net.risesoft.support.comparator.UpdateTimeComparator;
import net.risesoft.util.FileNodeUtil;
import net.risesoft.y9.Y9Context;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9public.entity.Y9FileStore;
import net.risesoft.y9public.service.Y9FileStoreService;

import y9.client.rest.platform.org.OrgUnitApiClient;
import y9.client.rest.platform.org.PersonApiClient;

@Service
@RequiredArgsConstructor
public class FileNodeServiceImpl implements FileNodeService {

    private static final Map<String, Comparator<FileNode>> sortTypeComparatorMap = new HashMap<>();

    static {
        sortTypeComparatorMap.put(new OrderRequest(OrderProp.FILE_NAME, true).toString(), new FileNameComparator(true));
        sortTypeComparatorMap.put(new OrderRequest(OrderProp.FILE_NAME, false).toString(),
            new FileNameComparator(false));
        sortTypeComparatorMap.put(new OrderRequest(OrderProp.FILE_SIZE, true).toString(), new FileSizeComparator(true));
        sortTypeComparatorMap.put(new OrderRequest(OrderProp.FILE_SIZE, false).toString(),
            new FileSizeComparator(false));
        sortTypeComparatorMap.put(new OrderRequest(OrderProp.UPDATE_TIME, true).toString(),
            new UpdateTimeComparator(true));
        sortTypeComparatorMap.put(new OrderRequest(OrderProp.UPDATE_TIME, false).toString(),
            new UpdateTimeComparator(false));
        sortTypeComparatorMap.put(new OrderRequest(OrderProp.CREATE_TIME, true).toString(),
            new CreateTimeComparator(true));
        sortTypeComparatorMap.put(new OrderRequest(OrderProp.CREATE_TIME, false).toString(),
            new CreateTimeComparator(false));
    }

    private final FileNodeRepository fileNodeRepository;
    private final FileNodeShareRepository fileNodeShareRepository;
    private final PersonApiClient personManager;
    private final OrgUnitApiClient orgUnitApiClient;
    private final PositionApi positionApi;
    private final Y9FileStoreService y9FileStoreService;
    private final StorageCapacityService storageCapacityService;

    @Override
    public void cancelShare(List<String> fileNodeIdList) {
        for (String fileNodeId : fileNodeIdList) {
            cancelShare(fileNodeId);
        }
    }

    private void cancelShare(String fileNodeId) {
        FileNode fileNode = this.findById(fileNodeId);
        fileNode.setParentId(RootFolder.MY.getEnName());
        fileNode.setListType(RootFolder.MY.getEnName());
        this.save(fileNode);
    }

    @Override
    public List<FileNode> deletedList(String userId) {
        List<FileNode> fileNodeList = fileNodeRepository.findByUserIdAndDeletedTrue(userId);

        // 上面查找出了所有已删除节点 但是仅需要展示最顶层的节点
        List<FileNode> rootFileNodeList = new ArrayList<>();
        for (FileNode fileNode : fileNodeList) {
            findRoot(fileNode, fileNodeList, rootFileNodeList);
        }

        rootFileNodeList.sort(new UpdateTimeComparator(false));
        return rootFileNodeList;
    }

    @Override
    public void emptyRecycleBin() {
        List<FileNode> fileNodeList = fileNodeRepository.findByUserIdAndDeletedTrue(Y9LoginUserHolder.getPersonId());
        for (FileNode fileNode : fileNodeList) {
            permanentlyDelete(fileNode, false);
        }
    }

    @Override
    public FileNode findById(String id) {
        return fileNodeRepository.findById(id).orElse(null);
    }

    /**
     * 在 fileNodeList 中找到 fileNode 的最顶层节点放入 rootFileNodeList 中
     *
     * @param fileNode
     * @param fileNodeList
     * @param rootFileNodeList
     */
    private void findRoot(FileNode fileNode, List<FileNode> fileNodeList, List<FileNode> rootFileNodeList) {
        Optional<FileNode> fileNodeOptional =
            fileNodeList.stream().filter(fn -> fn.getId().equals(fileNode.getParentId())).findFirst();
        if (fileNodeOptional.isPresent()) {
            findRoot(fileNodeOptional.get(), fileNodeList, rootFileNodeList);
        } else {
            if (!rootFileNodeList.contains(fileNode)) {
                rootFileNodeList.add(fileNode);
            }
        }
    }

    @Override
    public FileNode getParent(String parentId) {
        if (RootFolder.MY.getEnName().equals(parentId)) {
            return new FileNode(RootFolder.MY.getEnName(), RootFolder.MY.getCnName());
        } else if (RootFolder.SHARED.getEnName().equals(parentId)) {
            return new FileNode(RootFolder.SHARED.getEnName(), RootFolder.SHARED.getCnName());
        } else if (RootFolder.PUBLIC.getEnName().equals(parentId)) {
            return new FileNode(RootFolder.PUBLIC.getEnName(), RootFolder.PUBLIC.getCnName());
        } else if (RootFolder.DEPT.getEnName().equals(parentId)) {
            return new FileNode(RootFolder.DEPT.getEnName(), RootFolder.DEPT.getCnName());
        } else if (StringUtils.isNotBlank(parentId)) {
            return this.findById(parentId);
        } else {
            return null;
        }
    }

    private boolean isFileNodeExists(String parentId, String fileName) {
        FileNodeSpecification specification =
            new FileNodeSpecification(Y9LoginUserHolder.getPersonId(), parentId, fileName, false);
        List<FileNode> fileNodeList = fileNodeRepository.findAll(specification);
        return !fileNodeList.isEmpty();
    }

    private void logicDelete(FileNode fileNode) {
        if (FileNodeType.FOLDER.getValue().equals(fileNode.getFileType())) {
            // 递归删除
            List<FileNode> fileNodeList =
                fileNodeRepository.findByUserIdAndParentId(Y9LoginUserHolder.getPersonId(), fileNode.getId());
            for (FileNode fn : fileNodeList) {
                logicDelete(fn);
            }
        }
        fileNode.setDeleted(true);
        fileNode.setUpdateTime(new Date());
        this.save(fileNode);
    }

    @Override
    public void logicDelete(List<String> idList) {
        for (String id : idList) {
            FileNode fileNode = this.findById(id);
            logicDelete(fileNode);
        }
    }

    @Override
    public void move(List<String> idList, String targetId) {
        for (String id : idList) {
            FileNode fileNode = this.findById(id);
            if (StringUtils.isNotBlank(targetId)) {
                fileNode.setParentId(targetId);
            } else {
                fileNode.setParentId(null);
            }
            this.save(fileNode);
        }
    }

    private void permanentlyDelete(FileNode fileNode, boolean recursive) {
        if (FileNodeType.FOLDER.getValue().equals(fileNode.getFileType())) {
            if (recursive) {
                List<FileNode> fileNodeList =
                    fileNodeRepository.findByUserIdAndParentId(Y9LoginUserHolder.getPersonId(), fileNode.getId());
                for (FileNode fn : fileNodeList) {
                    permanentlyDelete(fn, true);
                }
            }
        } else {
            y9FileStoreService.deleteFile(fileNode.getFileStoreId());
            StorageCapacity sc = storageCapacityService.findByCapacityOwnerId(Y9LoginUserHolder.getPersonId());
            if (sc != null && StringUtils.isNotBlank(sc.getId())) {
                sc.setRemainingLength(sc.getRemainingLength() + fileNode.getFileSize());
                storageCapacityService.save(sc);
            }
        }
        fileNodeRepository.delete(fileNode);
    }

    @Override
    public void permanentlyDelete(List<String> idList) {
        for (String id : idList) {
            FileNode fileNode = this.findById(id);
            permanentlyDelete(fileNode, true);
        }
    }

    @Override
    public List<FileNode> recursiveToRoot(String parentId) {
        List<FileNode> fileNodeList = new ArrayList<>();
        if (StringUtils.isNotBlank(parentId)) {
            recursiveUp(fileNodeList, parentId);
        }
        Collections.reverse(fileNodeList);
        return fileNodeList;
    }

    private void recursiveUp(List<FileNode> fileNodeList, String parentId) {
        FileNode fileNode = this.getParent(parentId);
        if (fileNode != null) {
            fileNodeList.add(fileNode);
            recursiveUp(fileNodeList, fileNode.getParentId());
        }
    }

    @Override
    public void restore(List<String> idList) {
        for (String id : idList) {
            restore(id);
        }
    }

    private void restore(String id) {
        FileNode fileNode = this.findById(id);
        if (FileNodeType.FOLDER.getValue().equals(fileNode.getFileType())) {
            restoreChildren(fileNode.getId());
        }
        fileNode.setDeleted(false);
        fileNode.setUpdateTime(new Date());
        this.save(fileNode);
    }

    public void restoreChildren(String id) {
        List<FileNode> fileNodeList = fileNodeRepository.findByUserIdAndParentId(Y9LoginUserHolder.getPersonId(), id);
        for (FileNode folder : fileNodeList) {
            if (FileNodeType.FOLDER.getValue().equals(folder.getFileType())) {
                restoreChildren(folder.getId());
            }
            folder.setDeleted(false);
            this.save(folder);
        }
    }

    private FileNode save(FileNode fileNode) {
        return fileNodeRepository.save(fileNode);
    }

    @Override
    public FileNode saveFolder(FileNode fileNode) {
        if (StringUtils.isBlank(fileNode.getId())) {
            UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
            fileNode.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
            fileNode.setCreateTime(new Date());
            fileNode.setUpdateTime(new Date());
            fileNode.setFileType(FileNodeType.FOLDER.getValue());
            if (fileNode.getListType().equals(FileListType.DEPT.getValue())) {
                String positionId = Y9LoginUserHolder.getPositionId();
                String tenantId = Y9LoginUserHolder.getTenantId();
                OrgUnit orgUnit = orgUnitApiClient.getParent(tenantId, positionId).getData();
                fileNode.setOrgId(orgUnit.getId());
            } else {
                fileNode.setUserId(userInfo.getPersonId());
                fileNode.setUserName(userInfo.getName());
            }
            fileNode.setParentId(StringUtils.isNotBlank(fileNode.getParentId()) ? fileNode.getParentId() : null);
            return fileNodeRepository.save(fileNode);
        } else {
            FileNode oldFileNode = fileNodeRepository.findById(fileNode.getId()).orElse(null);
            oldFileNode.setName(fileNode.getName());
            oldFileNode.setUpdateTime(new Date());
            return fileNodeRepository.save(oldFileNode);
        }
    }

    public String saveOtherFolderOrFile(FileNode folder, String userId, String guid) {
        String tenantId = Y9LoginUserHolder.getTenantId();
        // UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
        String guid1 = Y9IdGenerator.genId(IdType.SNOWFLAKE);
        FileNode fold = new FileNode();
        fold.setCreateTime(new Date());
        fold.setUpdateTime(new Date());
        fold.setFileStoreId(folder.getFileStoreId());
        fold.setFileType(folder.getFileType());
        fold.setFileSuffix(folder.getFileSuffix());
        fold.setId(guid1);
        fold.setName(folder.getName());
        fold.setParentId(guid);
        fold.setUserId(userId);
        fold.setUserName(personManager.get(tenantId, userId).getData().getName());
        fileNodeRepository.save(fold);

        return guid1;
    }

    @Override
    @Transactional(readOnly = false)
    public Map<String, Object> saveUploadFile(MultipartFile file, String parentId, String listType) {
        Map<String, Object> map = new HashMap<>();
        UserInfo userInfo = Y9LoginUserHolder.getUserInfo();
        String tenantId = Y9LoginUserHolder.getTenantId(), userId = userInfo.getPersonId(),
            userName = userInfo.getName();
        String fileName = FilenameUtils.getName(file.getOriginalFilename());
        String fileExtension = FilenameUtils.getExtension(fileName).toLowerCase();
        String defaultStorageCapacity = Y9Context.getProperty("y9.app.storage.defaultStorageCapacity");
        try {
            long size = Long.parseLong(Y9Context.getProperty("y9.app.storage.singleUploadLimit"));
            if (file.getSize() > size) {
                map.put("msg", "上传文件的大小超过单次上传限制120M?");
                map.put("success", false);
            } else {
                Long fileSize = file.getSize();
                if (listType.equals(FileListType.MY.getValue())) {
                    StorageCapacity capacity = storageCapacityService.findByCapacityOwnerId(userId);
                    if (null == capacity) {
                        StorageCapacity sc = new StorageCapacity();
                        sc.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
                        sc.setCapacityOwnerId(userId);
                        sc.setCapacityOwnerName(userName);
                        sc.setCapacitySize(Long.valueOf(defaultStorageCapacity));
                        sc.setRemainingLength(Long.valueOf(defaultStorageCapacity));
                        sc.setCreateTime(new Date());
                        storageCapacityService.save(sc);
                    } else {
                        if (capacity.getRemainingLength() > fileSize) {
                            capacity.setRemainingLength(capacity.getRemainingLength() - fileSize);
                            storageCapacityService.save(capacity);
                        }
                        if (capacity.getRemainingLength() < fileSize) {
                            map.put("msg", "存储空间不够，无法上传");
                            map.put("success", false);
                        }
                    }
                }
                String fullPath = Y9FileStore.buildPath(Y9Context.getSystemName(), Y9LoginUserHolder.getTenantId(),
                    userInfo.getPersonId(), parentId);
                Y9FileStore y9FileStore = y9FileStoreService.uploadFile(file, fullPath, fileName);
                Integer type = FileNodeUtil.fileTypeConvert(fileExtension);
                boolean fileNodeExists = isFileNodeExists(parentId, fileName);

                FileNode fileNode = new FileNode();
                fileNode.setFileSuffix(fileExtension);
                fileNode.setFileSize(fileSize);
                fileNode.setCreateTime(new Date());
                fileNode.setUpdateTime(new Date());
                fileNode.setFileStoreId(y9FileStore.getId());
                fileNode.setListType(listType);
                if (listType.equals(FileListType.DEPT.getValue())) {
                    String positionId = Y9LoginUserHolder.getPositionId();
                    OrgUnit orgUnit = orgUnitApiClient.getParent(tenantId, positionId).getData();
                    Position position = positionApi.get(tenantId, positionId).getData();
                    fileNode.setOrgId(orgUnit.getId());
                    fileNode.setUserId(position.getId());
                    fileNode.setUserName(position.getName());
                } else {
                    fileNode.setUserId(userInfo.getPersonId());
                    fileNode.setUserName(userInfo.getName());
                }
                fileNode.setFileType(type);
                fileNode.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
                if (StringUtils.isNotBlank(parentId)) {
                    fileNode.setParentId(parentId);
                }
                if (fileNodeExists) {
                    SimpleDateFormat sdf = new SimpleDateFormat("_yyyyMMdd_HHmmss");
                    fileName = FilenameUtils.getBaseName(fileName) + sdf.format(new Date()) + "." + fileExtension;
                }
                fileNode.setName(fileName);
                this.save(fileNode);
                map.put("msg", "文件上传成功");
                map.put("success", true);
            }

        } catch (Exception e) {
            e.printStackTrace();
            map.put("msg", "文件上传失败");
            map.put("success", false);
        }
        return map;
    }

    @Override
    public void share(List<String> fileNodeIdList) {
        for (String fileNodeId : fileNodeIdList) {
            share(fileNodeId);
        }
    }

    private void share(String fileNodeId) {
        FileNode fileNode = this.findById(fileNodeId);
        fileNode.setParentId(RootFolder.SHARED.getEnName());
        fileNode.setListType(FileListType.SHARED.getValue());
        this.save(fileNode);
    }

    @Override
    public List<FileNode> subFolderList(String id) {
        FileNodeSpecification spec =
            new FileNodeSpecification(Y9LoginUserHolder.getPersonId(), id, FileNodeType.FOLDER, false);
        List<FileNode> fileNodeList = fileNodeRepository.findAll(spec);
        fileNodeList.sort(new UpdateTimeComparator(false));
        return fileNodeList;
    }

    @Override
    public List<FileNode> subPublicFolderList(String id) {
        FileNodeSpecification spec = new FileNodeSpecification(id, FileNodeType.FOLDER, false);
        List<FileNode> fileNodeList = fileNodeRepository.findAll(spec);
        fileNodeList.sort(new UpdateTimeComparator(false));
        return fileNodeList;
    }

    @Override
    public List<FileNode> subList(String positionId, String id, FileNodeType fileType, String searchName,
        String listType, OrderRequest orderRequest) {
        List<FileNode> fileNodeList = new ArrayList<FileNode>();
        String personId = Y9LoginUserHolder.getPersonId();
        if (RootFolder.SHARED.getEnName().equals(id)) {
            String guidPath = Y9LoginUserHolder.getUserInfo().getGuidPath();
            String[] guidArray = StringUtils.split(guidPath, ",");
            List<String> guidList = Arrays.asList(guidArray);

            List<FileNodeShare> fileNodeShareList = fileNodeShareRepository.findByToOrgUnitIdIn(guidList);
            List<String> fileNodeIdList =
                fileNodeShareList.stream().map(FileNodeShare::getFileNodeId).collect(Collectors.toList());
            fileNodeList =
                fileNodeRepository.findByIdInAndParentIdAndListTypeAndDeletedFalse(fileNodeIdList, id, listType);
        } else {
            String tenantId = Y9LoginUserHolder.getTenantId();
            if (listType.equals(FileListType.DEPT.getValue())) {
                OrgUnit orgUnit = orgUnitApiClient.getParent(tenantId, positionId).getData();

                FileNodeSpecification spec =
                    new FileNodeSpecification(id, fileType, listType, searchName, orgUnit.getId(), false);
                fileNodeList = fileNodeRepository.findAll(spec);
            } else if (listType.equals(FileListType.SHARED.getValue())) {
                // FileNodeShare share = fileNodeShareRepository.findByFileNodeIdAndToOrgUnitId(id,personId);
                // if(null != share && StringUtils.isNotBlank(share.getId())){
                FileNodeSpecification spec = new FileNodeSpecification(false, id, searchName);
                fileNodeList = fileNodeRepository.findAll(spec);
                // }
            } else if (listType.equals(FileListType.REPORT.getValue())) {
                if (id.equals(FileListType.REPORT.getValue())) {
                    FileNodeSpecification spec =
                        new FileNodeSpecification(id, FileNodeType.FOLDER, listType, searchName, false);
                    fileNodeList = fileNodeRepository.findAll(spec);
                } else {
                    FileNodeSpecification spec = new FileNodeSpecification(false, id, listType, searchName);
                    fileNodeList = fileNodeRepository.findAll(spec);
                }
            } else if (listType.equals(FileListType.REPORTMANAGE.getValue())) {
                FileNodeSpecification spec = new FileNodeSpecification(false, id, searchName);
                fileNodeList = fileNodeRepository.findAll(spec);
            } else {
                FileNodeSpecification spec =
                    new FileNodeSpecification(personId, id, fileType, listType, searchName, false);
                fileNodeList = fileNodeRepository.findAll(spec);
            }
        }
        Comparator<FileNode> fileNodeComparator = sortTypeComparatorMap.get(orderRequest.toString());
        fileNodeList.sort(fileNodeComparator);
        return fileNodeList;
    }

    @Override
    public List<FileNode> subCollectList(String id, String searchName, String listType, OrderRequest orderRequest) {
        List<FileNode> fileNodeList = new ArrayList<FileNode>();
        FileNodeSpecification spec = new FileNodeSpecification(false, id, listType, searchName);
        fileNodeList = fileNodeRepository.findAll(spec);
        Comparator<FileNode> fileNodeComparator = sortTypeComparatorMap.get(orderRequest.toString());
        fileNodeList.sort(fileNodeComparator);
        return fileNodeList;
    }

    @Override
    public List<FileNode> subCollectList(List<String> ids, String serachName, OrderRequest orderRequest) {
        List<FileNode> fileNodeList =
            fileNodeRepository.findByIdInAndNameLikeAndDeletedFalse(ids, "%" + serachName + "%");
        Comparator<FileNode> fileNodeComparator = sortTypeComparatorMap.get(orderRequest.toString());
        fileNodeList.sort(fileNodeComparator);
        return fileNodeList;
    }

    @Override
    public List<FileNode> subPublicList(String id, FileNodeType fileType, String searchName, String startTime,
        String endTime, String listType, OrderRequest orderRequest) {
        List<FileNode> newFileNodeList = new ArrayList<FileNode>();
        String guidPath = Y9LoginUserHolder.getUserInfo().getGuidPath();
        String[] guidArray = StringUtils.split(guidPath, ",");
        List<String> guidList = Arrays.asList(guidArray);

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date startDate = sdf.parse("2001-01-01 00:00:00");
            Date endDate = new Date();
            if (StringUtils.isNotBlank(startTime)) {
                startTime = startTime + " 00:00:00";
                startDate = sdf.parse(startTime);
            }
            if (StringUtils.isNotBlank(endTime)) {
                endTime = endTime + " 23:59:59";
                endDate = sdf.parse(endTime);
            }
            FileNodeSpecification spec =
                new FileNodeSpecification(id, fileType, listType, searchName, startDate, endDate, false);
            List<FileNode> fileNodeList = fileNodeRepository.findAll(spec);
            for (FileNode fileNode : fileNodeList) {
                List<FileNodeShare> shareList = fileNodeShareRepository.findByFileNodeId(fileNode.getId());
                if (null != shareList && !shareList.isEmpty()) {
                    List<FileNodeShare> fileNodeShareList =
                        fileNodeShareRepository.findByFileNodeIdAndToOrgUnitIdIn(fileNode.getId(), guidList);
                    if (null != fileNodeShareList && !fileNodeShareList.isEmpty()) {
                        List<String> fileNodeIdList =
                            fileNodeShareList.stream().map(FileNodeShare::getFileNodeId).collect(Collectors.toList());
                        List<FileNode> nodeList = fileNodeRepository
                            .findByIdInAndParentIdAndListTypeAndDeletedFalse(fileNodeIdList, id, listType);
                        newFileNodeList.addAll(nodeList);
                    } else {
                        continue;
                    }
                } else {
                    newFileNodeList.add(fileNode);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Comparator<FileNode> fileNodeComparator = sortTypeComparatorMap.get(orderRequest.toString());
        newFileNodeList.sort(fileNodeComparator);
        return newFileNodeList;
    }

    @Override
    public List<FileNode> subManageList(String id, FileNodeType fileType, String searchName, String startTime,
        String endTime, String listType, OrderRequest orderRequest) {
        List<FileNode> fileNodeList = new ArrayList<FileNode>();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date startDate = sdf.parse("2001-01-01 00:00:00");
            Date endDate = new Date();
            if (StringUtils.isNotBlank(startTime)) {
                startTime = startTime + " 00:00:00";
                startDate = sdf.parse(startTime);
            }
            if (StringUtils.isNotBlank(endTime)) {
                endTime = endTime + " 23:59:59";
                endDate = sdf.parse(endTime);
            }
            FileNodeSpecification spec =
                new FileNodeSpecification(id, fileType, listType, searchName, startDate, endDate, false);
            fileNodeList = fileNodeRepository.findAll(spec);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Comparator<FileNode> fileNodeComparator = sortTypeComparatorMap.get(orderRequest.toString());
        fileNodeList.sort(fileNodeComparator);
        return fileNodeList;
    }

    @Override
    public FileNode saveNode(FileNode fileNode) {
        return this.save(fileNode);
    }

}
