package net.risesoft.controller.dto;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

import net.risesoft.entity.FileNode;

@Data
@NoArgsConstructor
public class FileNodeListDTO {

    private List<FileNodeDTO> subFileNodeList;
    private List<FileNodeDTO> recursiveToRootFileNodeList;

    public static FileNodeListDTO from(List<FileNode> subFileNodeList, List<FileNode> recursiveToRootFileNodeList) {
        FileNodeListDTO fileNodeListDTO = new FileNodeListDTO();
        fileNodeListDTO.setSubFileNodeList(FileNodeDTO.from(subFileNodeList));
        fileNodeListDTO.setRecursiveToRootFileNodeList(FileNodeDTO.from(recursiveToRootFileNodeList));
        return fileNodeListDTO;
    }

}
