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

/**
 * @author yihong
 * @date 2024/10/15
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "Y9_ARCHIVES_DETAILS")
@org.hibernate.annotations.Table(comment = "档案详情表", appliesTo = "Y9_ARCHIVES_DETAILS")
public class Archives implements Serializable {

    private static final long serialVersionUID = 4774402268063407277L;

    @Id
    @Column(name = "ID", unique = true, nullable = false)
    @GeneratedValue(generator = "IDGENERATOR")
    @GenericGenerator(name = "IDGENERATOR", strategy = "native")
    @Comment("主键id")
    private Long id;

    @Column(name = "ARCHIVE_NAME", length = 30)
    @Comment("档案馆名称")
    private String archiveName;// 集中管理特定范围的专门机构的名称。用以标识本件电子档案所属的档案馆。

    @Column(name = "ARCHIVE_CODE", length = 6)
    @Comment("档案馆代码")
    private String archiveCode;// 依据《编制全国档案馆名称代码实施细则》，唯一标识综合档案馆的一组代码。

    @Column(name = "GENERAL_ARCHIVE_NAME", length = 100)
    @Comment("全宗名称")
    private String generalArchiveName;// 档案馆赋予档案全宗的标识，用以标识本件电子档案所属的全宗。

    @Column(name = "ESTABLISH_ARCHIVE_UNITNAME", length = 100)
    @Comment("立档单位名称")
    private String establishArchiveUnitname;// 构成档案全宗的国家机构、社会组织或个人的名称

    @Column(name = "ARCHIVE_ID", length = 50)
    @Comment("档号")
    private String archiveId; // 以字符形式赋予本件电子档案的、用以固定和反映本件电子档案排列顺序的一组代码。

    @Column(name = "GENERAL_ARCHIVE_ID", length = 100)
    @Comment("全宗号")
    private String generalArchiveId;// 本件电子档案所属的全宗的代码。

    @Column(name = "CATEGORY_CODE", length = 2)
    @Comment("类别代码")
    private String categoryCode;// 本件电子档案所属的类别代码。如WS(文书）、ZP(照片）、LY(录音)、LX(录像)等。

    @Comment("年度")
    @Column(name = "YEAR", length = 4)
    private String year;// 本件电子档案的形成年度。用4位阿拉伯数字表示,如“2012”。

    @Column(name = "STORAGE_PERIOD_CODE", length = 3)
    @Comment("保管期限代码")
    private String storagePeriodCode;// 对本件电子档案划定的存留年限。值：Y（永久）、D30（30年）、D10（10年）。

    @Column(name = "AGENCY_PROBlEM_CODE", length = 3)
    @Comment("机构或问题代码")
    private String agencyProblemCode;// 本件电子档案的形成承办部门的代码或问题的代码。是对电子档案进行分类整理时按部门或者问题分类的结果。用3位字母或阿拉伯数字标识。

    @Column(name = "PART_NUMBER", length = 4)
    @Comment("件号")
    private String partNumber;// 本件电子档案在所属“机构或问题代码”下排列的项序号。

    @Column(name = "AGENCY_PROBlEM", length = 100)
    @Comment("机构或问题")
    private String agencyProblem;// 本件电子档案的形成承办部门的名称或间题的名称。是对电子档案进行分类整理时按部门或者问题分类的结果。

    @Column(name = "TITLE", length = 600)
    @Comment("题名")
    private String title;// 又称标题、题目,是表达电子档案中心内容和形式特征的名称,用以描述电子文件的中心内容,提供检索点。

    @Column(name = "FILE_LEVEL", length = 4)
    @Comment("密级")
    private String level;// 本文件保密程度的等级。值：无、秘密、机密、绝密。

    @Column(name = "CONFIDENTIALITY_PERIOD", length = 20)
    @Comment("保密期限")
    private String confidentialityPeriod;// 对本文件密级时效的规定和说明。若本文件有保密期限则必须采用。

    @Column(name = "DECRYPTION_IDENTIFIER", length = 20)
    @Comment("解密标识")
    private String decryptionIdentifier;// 标识本文件是否已经解密。若本文件的密级标注了秘密/机密/绝密则必须采用。值：已解密、未解密。

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Temporal(TemporalType.DATE)
    @Column(name = "DECRYPTION_DATE", length = 10)
    @Comment("解密日期")
    private Date decryptionDate;// 本文件解密的日期。采用GB/T7408-2005 中5.2.1.1条规定的格式，例如“2021-02-24”。若本文件已解密则必须采用。

    @Column(name = "OPEN_STATUS", length = 4)
    @Comment("开放状态")
    private String openStatus;// 标识本件电子档案是否开放或控制使用。未经开放审核程序的电子档案，开放状态为原控。 值：开放.控制，原控。

    @Column(name = "COPYRIGHT_INFORMATION", length = 1200)
    @Comment("版权信息")
    private String copyrightInformation;// 描述本件电子档案版权归属的信息应著录的描述信息包括:本件电子档案的版权所有者的名称、版权注册时间、版权注册号、版权期限，
    // 版权所有者关于版权的声明及其他特殊约定等。国家机构、社会组织在履行法定职能过程中形成的电子档案无需著录本元数据。
    // 综合档案馆通过征集、获赠、购买、下载等方式获得的电子档案，
    // 应依据《中华人民共和国著作权法》等法律法规著录版权归属信息如“根据《中华人民共和国著作权法》规定，
    // 以及XX省档案馆与捐赠者YY签订的协议，YY依法享有该电子档案的著作权，XX省档案馆可以依法提供利用,或用于编研、展览、宣传等公益性活动。”

    @Column(name = "NOTE_APPENDED", length = 1200)
    @Comment("附注")
    private String noteAppended;// 对本件电子档案和其元数据所做的解释和补充说明。

    @Column(name = "REFERENCE_NUMBER", length = 600)
    @Comment("参见号")
    private String referenceNumber;// 与本件电子档案具有相同主题的不同记录形式和载体的各门类电子档案档号的组合，不同档号之间用“,”隔开。
    // 例如，本件电子档案与档号为“068-ZP-2017-￥-BGS-1201”的照片类电子档案及档号为“068-LX-2017-Y-BGS-0023”的录像类电子档案有关，
    // 则本件电子档案的参见号可写为“068-ZP-2017-Y-BGS-1201,068-LX-2017-Y-BGS0023”。
}
