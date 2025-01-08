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
@Table(name = "Y9_ARCHIVES_TESTINGITEM")
@Comment("档案检测项表")
public class TestingItem implements Serializable {

    private static final long serialVersionUID = 3308862552935705648L;

    @Id
    @Comment("主键")
    @Column(name = "ID", length = 38)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "assigned")
    private String id;

    @Comment("档案检测信息表id")
    @Column(name = "TESTINGINFOID", length = 50)
    private String testingInfoId;

    @Comment("检测编号")
    @Column(name = "TESTINGNO", length = 50)
    private String testingNo;

    @Comment("检测结果")
    @Column(name = "TESTINGRESULT", length = 200)
    private String testingResult;

    @Comment("未通过原因")
    @Column(name = "TESTINGREASON", length = 500)
    private String testingReason;

}
