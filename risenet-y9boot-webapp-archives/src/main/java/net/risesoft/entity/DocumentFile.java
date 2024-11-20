package net.risesoft.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Comment;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "Y9_ARCHIVES_DOCUMENT_FILE")
@org.hibernate.annotations.Table(comment = "档案文书类文件表", appliesTo = "Y9_ARCHIVES_DOCUMENT_FILE")
public class DocumentFile implements Serializable {

    private static final long serialVersionUID = 2194298990968114325L;

    @Id
    @Column(name = "ID", unique = true, nullable = false)
    @GeneratedValue(generator = "IDGENERATOR")
    @GenericGenerator(name = "IDGENERATOR", strategy = "native")
    @Comment("主键id")
    private Long id;

    @Column(name = "DETAILID", nullable = false)
    @Comment("详情关联id")
    private Long detailId;

    @Column(name = "ABREASTTITLE", length = 600)
    @Comment("并列题名")
    private String abreastTitle;// 用第二种语言文字书写的与题名对照并列的题名

    @Column(name = "SUBTITLE", length = 600)
    @Comment("副题名")
    private String subtitle;// 解释或从属于题名的另一题名，利于通过题名的解释文字或从属信息进步了解文件

    @Column(name = "KEYWORD", length = 100)
    @Comment("关键词")
    private String keyWord;// 用以表达本件电子档案的主题并具有检索意义的词或词组

    @Column(name = "PERSONNAME", length = 200)
    @Comment("人名")
    private String personName;// 本件电子档案中涉及的具有检索意义的人物姓名。

    @Column(name = "EXCERPT", length = 1200)
    @Comment("摘要")
    private String excerpt;// 对本件电子档案核心内容的简短描述

    @Column(name = "DOCUMENTNUMBER", length = 50)
    @Comment("文号")
    private String documentNumber;// 本文件在制发过程中由制发机关、团体或个人赋予该文件的顺序号。一般由发文机关代字、发文年号和发文顺序号三部分组成。若本文件有文号则必须采用。

    @Column(name = "RESPONSIBILITYPERSON", length = 100)
    @Comment("责任者")
    private String responsibilityPerson;// 对本文件的内容进行创造、负有责任的团体或个人。

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Temporal(TemporalType.TIME)
    @Column(name = "PHOTOGRAPHYTIME", length = 19)
    @Comment("成文日期")
    private Date photographyTime;// 本文件形成的日期，为文件发文时间、发表时间、签署时间或通过时间。采用GB/T 7408-2005中5.2.1.1条规定的格式,例如“2021-02-24”

    @Column(name = "DOCUMENTTYPE", length = 12)
    @Comment("文种")
    private String documentType;// 按性质和用途确定的文件种类的名称。 命令(令)，决定，公告通告,通知,通报,议案报告,请示,批复,意见函,纪要，指示,决议，公报,条例，规定，〔其他]

    @Column(name = "LANGUAGETYPE", length = 50)
    @Comment("语种")
    private String languageType;// 本文件正文所使用的语言的类别。默认为汉语。

    @Column(name = "URGENTLEVEL", length = 4)
    @Comment("紧急程度")
    private String urgentLevel;// 对本文件送达和办理时间要求的急缓等级。 特提、特急、加急、平急、急件、〔其他〕。

    @Column(name = "ACTIVESEND", length = 200)
    @Comment("主送")
    private String activeSend;// 本文件的主要受理者，说明文件的发送对象。

    @Column(name = "COPYSEND", length = 200)
    @Comment("抄送")
    private String copySend;// 除主送者以外需要执行或知晓本文件的其他受文者

    @Column(name = "PAGENUMBER")
    @Comment("页数")
    private Integer pageNumber = 0;// 本件电子档案包含的总页数。如33”

    @Column(name = "MICRONUMBER", length = 50)
    @Comment("缩微号")
    private String microNumber;// 与本件电子档案对应的缩微胶片的编号。若本件电子档案有对应的缩微胶片则必须采用。

    @Column(name = "DRAFTER", length = 50)
    @Comment("拟稿人")
    private String drafter;// 拟稿是由承办人员根据工作要求、领导意见等起草文件草稿的过程，是发文办理程序的首要环节，
    // 也是审核和签发等后续工作的基础,在发文办理中占据重要地位。拟稿人即完成本文件拟稿的工作人员。若发文则必须采用。

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Temporal(TemporalType.TIME)
    @Column(name = "DRAFTTIME", length = 19)
    @Comment("拟稿时间")
    private Date draftTime;// 起草本文件草稿的时间。采用GB/T7408-2005中5.4.1条的格式，例如“2021-02-24T10:23:14”。若发文则必须采用。

    @Column(name = "REVIEWER", length = 50)
    @Comment("核稿人")
    private String reviewer;// 审核是将拟写的文稿在送领导审批签发以前，对文件的内容、体式等进行必要修改，认真检查和全面审核把关的关键环节，
    // 是保证和提高文件质量的重要程序。核稿人即完成本文件核稿的工作人员。若发文则必须采用。

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Temporal(TemporalType.TIME)
    @Column(name = "REVIEWTIME", length = 19)
    @Comment("核稿时间")
    private Date reviewTime;// 审核本文件的时间。采用GB/T7408-2005中5.4.1条的格式，例如“2021-02-24T10:23:14”。若发文则必须采用

    @Column(name = "LEGITIMACYREVIEWER", length = 50)
    @Comment("合法性审查人")
    private String legitimacyReviewer;// 对于一些机关发布行政规范性文件来讲，还须把好法制关，应由法制部门对文件进行合法性审查，
    // 主要包括制定主体的审查、是否逾越权限的审查、具体行政措施的审查、是否与上位法抵本触的审查等。合法性审查人即完成文件合法性审查的工作人员。若发文并进行了合法性审查则必须采用。

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Temporal(TemporalType.TIME)
    @Column(name = "LEGITIMACYREVIEWETIME", length = 19)
    @Comment("合法性审查时间")
    private Date legitimacyRevieWeTime;// 对本文件进行合法性审查的时间。采用GB/T7408-2005中5.4.1条的格式，例如“2021-02-24T10:23:14”。若发文并进行了合法性审查则必须采用。

    @Column(name = "SIGNER", length = 50)
    @Comment("签发人")
    private String signer;// 签发应由发文单位领导人对文稿审定后，进行最后审批，签注意见并进行署名，是单位领导人行使决策权力的表示,
    // 也是发文的法定程序,还是文件生效的法定行为。草稿经签发后即为定稿，即可生效。签发人即完成本文件签发的工作人员。若发文则必须采用

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Temporal(TemporalType.TIME)
    @Column(name = "SIGNTIME", length = 19)
    @Comment("签发时间")
    private Date signTime;// 领导签发本文件的时间。采用(;B/T7408-2005中5.4.1条的格式，例如“2021-02-24T10:23:14”。若发文则必须采用。

    @Column(name = "PROOFREADER", length = 50)
    @Comment("校对人")
    private String proofreader;// 校对是以签发的定稿为标准，对文件进行全面核对校正的工作，是对文件最后把关的重要程序，其目的是提高文件质量。校对人即完成本文件校对的工作人员。若发文则必须采用

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Temporal(TemporalType.TIME)
    @Column(name = "PROOFREADINGTIME", length = 19)
    @Comment("校对时间")
    private Date proofreadingTime;// 对本文件进行校对的时间。采用GB/T7408-2005 中5.4.1条的格式，例如“2021-02-24T10:23:14若发文则必须采用。

    @Column(name = "REGISTRANT", length = 50)
    @Comment("登记人")
    private String registrant;// 登记即对来文进行编号并记载来源时间、内容、去向等的过程，对于方便来文的管理、查找、统计、催办等具有重要作用。登记人即完成本文件登记的工作人员。若收文则必须采用

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Temporal(TemporalType.TIME)
    @Column(name = "REGISTRANTTIME", length = 19)
    @Comment("登记时间")
    private Date registrantTime;// 对本文件进行登记的时间。采用GB/T7408-2005中 5.4.1条的格式,例如“2021-02-24T10:23:14”若收文则必须采用。

    @Column(name = "PROPOSER", length = 50)
    @Comment("拟办人")
    private String proposer;// 拟办即对来文如何办理提出初步意灭和处理方案，是为领导批办进行决- 策提供参考、提高办文准确性和时效性的重要程序，
    // 对于发挥文秘部门的参谋助手作用和提高办文速度和质量具有重要作用。拟办人即完成本文件拟办的工作人员。若收文则必须采用。

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Temporal(TemporalType.TIME)
    @Column(name = "PROPOSERTIME", length = 19)
    @Comment("拟办时间")
    private Date proposerTime;// 拟办人员对本文件进行办理的时间采用GB/T7408-2005中5.4.1 条的格式，例如“2021-02-24T10:23:14”。若收文则必须采用。

    @Column(name = "APPROVER", length = 50)
    @Comment("批办人")
    private String approver;// 批办即领导人根据文件内容、拟办意见和部门职责，对来文做出的指示性意见，是来文办理过程中具有决策意义的环节，
    // 是行使领导职权的具体体现。批办人即完成本文件批办的工作人员。若收文则必须采用。

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Temporal(TemporalType.TIME)
    @Column(name = "APPROVERTIME", length = 19)
    @Comment("批办时间")
    private Date approverTime;// 领导人对本文件批示办理意见的时间。采用GB/T7408-2005中5.4.1条的格式，例如“2021-02-24T10:23:14”。若收文则必须采用。

    @Column(name = "UNDERTAKER", length = 50)
    @Comment("承办人")
    private String undertaker;// 承办即有关部门或人员根据领导的批办意见和有关规定，对来文进行具体办理的过程，
    // 包括文件的传达执行、办理落实、撰文答复等,是来文办理的核心环节。承办人即完成本文件承办的工作人员。若收文则必须采用。

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Temporal(TemporalType.TIME)
    @Column(name = "UNDERTAKERTIME", length = 19)
    @Comment("承办时间")
    private Date undertakerTime;// 本文件承办的时间。采用GB/T7408-2005中5.4.1条的格式，例如“2021-02-24T10:23:14”。若收文则必须采用。

    @Column(name = "COMPONENTNUMBER", length = 3)
    @Comment("组件号")
    private String componentNumber;// 本组件在该件电子档案内排列的顺序号。如“D01”

    @Column(name = "COMPONENTCATEGORY", length = 20)
    @Comment("组件类别")
    private String componentCategory;// 揭示本组件的类型、稿本等属性。 正本，文件处理单，定稿，重要修改稿，〔其他〕。

    @Column(name = "COMPONENTTITLE", length = 600)
    @Comment("组件题名")
    private String componentTitle;// 揭示本组件的中心主题的标题或名称 。

    @Column(name = "COMPONENTFORMAT", length = 10)
    @Comment("组件格式")
    private String componentFormat;// 本组件的计算机文件格式名称

    @Column(name = "COMPONENTSIZE")
    @Comment("组件大小")
    private Integer componentSize = 0;// 本组件的计算机文件大小。以字节为单位。如“69120”
}
