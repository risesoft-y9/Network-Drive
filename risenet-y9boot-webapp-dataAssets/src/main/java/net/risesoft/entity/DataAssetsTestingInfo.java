package net.risesoft.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.Comment;
import org.hibernate.annotations.GenericGenerator;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Entity
@Table(name = "Y9_DATAASSETS_TESTINGINFO")
@Comment("检测信息表")
public class DataAssetsTestingInfo implements Serializable {

    private static final long serialVersionUID = -1543721396273150472L;

    @Id
    @Comment("主键")
    @Column(name = "ID", length = 38)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "assigned")
    private String id;

    @Comment("档案关联id")
    @Column(name = "ARCHIVESID", length = 50)
    private Long archivesId;

    @Comment("检测环节")
    @Column(name = "TESTINGSTEP", length = 100)
    private String testingStep;

    @Comment("检测数量")
    @Column(name = "TESTINGCOUNT")
    private Integer testingCount;

    @Comment("检测时间")
    @Column(name = "TESTINGTIME", length = 20)
    private String testingTime;

    @Comment("最终检测结果")
    @Column(name = "TESTINGRESULTFINAL", length = 500)
    private String testingResultFinal;

    @Comment("检测耗时")
    @Column(name = "TESTINGTIMECOST", length = 50)
    private String testingTimeCost;

    @Comment("检测人员Id")
    @Column(name = "PERSONID", length = 50)
    private String personId;

}
