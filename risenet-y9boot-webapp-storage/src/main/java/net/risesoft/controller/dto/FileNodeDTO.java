package net.risesoft.controller.dto;

import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.NoArgsConstructor;

import net.risesoft.entity.FileNode;
import net.risesoft.support.FileNodeType;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.util.Y9ModelConvertUtil;

@Data
@NoArgsConstructor
public class FileNodeDTO {

    public static FileNodeDTO from(FileNode fileNode) {
        return Y9ModelConvertUtil.convert(fileNode, FileNodeDTO.class);
    }

    public static List<FileNodeDTO> from(List<FileNode> fileNodeList) {
        return Y9ModelConvertUtil.convert(fileNodeList, FileNodeDTO.class);
    }

    private String id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
    private String parentId;
    private String orgId;
    private String name;
    private String listType;
    private Integer fileType;
    private String fileSize;
    private String previewUrl;
    private String fileUrl;
    private String fileStoreId;
    private String fileSuffix;
    private boolean encryption;
    private String filePassword;
    private String linkPassword;
    private Integer tabIndex;
    private String userId;
    private String userName;
    private boolean collect;
    private boolean owner;
    private String collectRoute;

    private FileNodeDTO parentFileNode;

    public String getFileSuffix() {
        if (!FileNodeType.FOLDER.getValue().equals(fileType)) {
            return FilenameUtils.getExtension(name);
        }
        return null;
    }

    public void setFileSize(Long fileSize) {
        if (fileSize != null) {
            this.fileSize = FileUtils.byteCountToDisplaySize(fileSize);
        }
    }

    public void setUserId(String userId) {
        this.owner = Y9LoginUserHolder.getUserInfo().getPersonId().equals(userId);
        // this.owner = false;
    }
}
