package net.risesoft.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssetsModel implements Serializable {

    private static final long serialVersionUID = -2502536327396226990L;

    private Long id;

    private String name;

    private String code;

    private String picture;

    private String qrcode;

    private String codeGlobal;

    private String remark;

    private String shareTypeName;

    private String dataType;

    private String dataOriginSystem;

    private String dataProvider;

    private String dataOwner;

    private String dataManager;

    private String dataPath;

    private String dataSize;

    private String dataFrequency;

    private String dataSensitivity;

    private String dataRetain;

    private String dataPurpose;

    private String dataQualityScore;

    private String keyField;

    private String dataCompliance;

    private String dataSecurityLevel;

    private String dataValuation;

    private String dataProductUrl;

    private String dataServiceUrl;

    private String mountType;

    private String appScenarios;

    private String dataZone;

    private String productType;
    
    private String provideType;
}
