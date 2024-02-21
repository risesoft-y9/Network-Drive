package net.risesoft.service.Impl;

import net.risesoft.entity.FileNodeShare;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.model.platform.OrgUnit;
import net.risesoft.model.user.UserInfo;
import net.risesoft.repository.FileNodeShareRepository;
import net.risesoft.service.FileNodeShareService;
import net.risesoft.support.FileOptType;
import net.risesoft.y9.Y9LoginUserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import y9.client.rest.platform.org.OrgUnitApiClient;

import java.util.Date;
import java.util.List;

@Service
public class FileNodeShareServiceImpl implements FileNodeShareService {

    @Autowired
    private FileNodeShareRepository fileNodeShareRepository;

    @Autowired
    private OrgUnitApiClient orgUnitManager;

    @Override
    public void cancelShare(String personId, List<String> fileNodeIdList) {
        for (String fileNodeId : fileNodeIdList) {
            cancelShare(personId, fileNodeId);
        }
    }

    private void cancelShare(String personId, String fileNodeId) {
        List<FileNodeShare> fileNodeShareList =
            fileNodeShareRepository.findByPersonIdAndFileNodeId(personId, fileNodeId);
        delete(fileNodeShareList);
    }

    private void delete(FileNodeShare fileNodeShare) {
        fileNodeShareRepository.delete(fileNodeShare);
    }

    private void delete(List<FileNodeShare> fileNodeShareList) {
        for (FileNodeShare fileNodeShare : fileNodeShareList) {
            delete(fileNodeShare);
        }
    }

    @Override
    public void deleteByFileNodeIdList(String personId, List<String> idList) {
        for (String fileNodeId : idList) {
            List<FileNodeShare> fileNodeShareList =
                fileNodeShareRepository.findByPersonIdAndFileNodeId(personId, fileNodeId);
            this.delete(fileNodeShareList);
        }
    }

    @Override
    public void deleteByFileNodeIdList(List<String> idList) {
        for (String fileNodeId : idList) {
            fileNodeShareRepository.deleteById(fileNodeId);
        }
    }

    @Override
    public List<FileNodeShare> list(String personId, String optType) {
        return fileNodeShareRepository.findByPersonIdAndFileOptType(personId, optType);
    }

    private FileNodeShare save(FileNodeShare fileNodeShare) {
        return fileNodeShareRepository.save(fileNodeShare);
    }

    @Override
    public void share(List<String> fileNodeIdList, List<String> orgUnitIdList) {
        orgUnitIdList.add(Y9LoginUserHolder.getUserInfo().getPersonId());

        for (String fileNodeId : fileNodeIdList) {
            for (String orgUnitId : orgUnitIdList) {
                share(fileNodeId, orgUnitId);
            }
        }
    }

    private void share(String fileNodeId, String orgUnitId) {
        FileNodeShare fileNodeShare = fileNodeShareRepository.findByFileNodeIdAndToOrgUnitId(fileNodeId, orgUnitId);
        if (fileNodeShare == null) {
            OrgUnit orgUnit = orgUnitManager.getOrgUnit(Y9LoginUserHolder.getTenantId(), orgUnitId).getData();
            UserInfo userInfo = Y9LoginUserHolder.getUserInfo();

            fileNodeShare = new FileNodeShare();
            fileNodeShare.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
            fileNodeShare.setCreateTime(new Date());
            fileNodeShare.setFileNodeId(fileNodeId);
            fileNodeShare.setFileOptType(FileOptType.SHARE.getValue());
            fileNodeShare.setToOrgUnitId(orgUnit.getId());
            fileNodeShare.setToOrgUnitName(orgUnit.getName());
            fileNodeShare.setPersonId(userInfo.getPersonId());
            fileNodeShare.setPersonName(userInfo.getName());
            save(fileNodeShare);
        }
    }

    @Override
    public void publicTo(List<String> fileNodeIdList, List<String> orgUnitIdList) {
        // orgUnitIdList.add(Y9LoginUserHolder.getUserInfo().getPersonId());

        for (String fileNodeId : fileNodeIdList) {
            for (String orgUnitId : orgUnitIdList) {
                publicTo(fileNodeId, orgUnitId);
            }
        }
    }

    private void publicTo(String fileNodeId, String orgUnitId) {
        FileNodeShare fileNodeShare = fileNodeShareRepository.findByFileNodeIdAndToOrgUnitId(fileNodeId, orgUnitId);
        if (fileNodeShare == null) {
            OrgUnit orgUnit = orgUnitManager.getOrgUnit(Y9LoginUserHolder.getTenantId(), orgUnitId).getData();
            UserInfo userInfo = Y9LoginUserHolder.getUserInfo();

            fileNodeShare = new FileNodeShare();
            fileNodeShare.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
            fileNodeShare.setCreateTime(new Date());
            fileNodeShare.setFileNodeId(fileNodeId);
            fileNodeShare.setFileOptType(FileOptType.PUBLIC.getValue());
            fileNodeShare.setToOrgUnitId(orgUnit.getId());
            fileNodeShare.setToOrgUnitName(orgUnit.getName());
            fileNodeShare.setPersonId(userInfo.getPersonId());
            fileNodeShare.setPersonName(userInfo.getName());
            save(fileNodeShare);
        }
    }

    @Override
    public Page<FileNodeShare> getFilePublicRecord(String fileNodeId, String optType, int page, int rows) {
        Pageable pageable = PageRequest.of(page - 1, rows, Direction.ASC, "createTime");
        return fileNodeShareRepository.findByFileNodeIdAndFileOptType(fileNodeId, optType, pageable);
    }
}
