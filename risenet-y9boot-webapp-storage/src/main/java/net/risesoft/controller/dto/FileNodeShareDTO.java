package net.risesoft.controller.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FileNodeShareDTO {

    private FileNodeDTO fileNode;
    private String toOrgUnitNames;

}
