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
@Table(name = "Y9_ARCHIVES_IMAGE_FILE")
@org.hibernate.annotations.Table(comment = "档案照片类文件表", appliesTo = "Y9_ARCHIVES_IMAGE_FILE")
public class ImageFile implements Serializable {

    private static final long serialVersionUID = 8868410000634135098L;

    @Id
    @Column(name = "ID", unique = true, nullable = false)
    @GeneratedValue(generator = "IDGENERATOR")
    @GenericGenerator(name = "IDGENERATOR", strategy = "native")
    @Comment("主键id")
    private Long id;

    @Column(name = "DETAIL_ID", nullable = false)
    @Comment("详情关联id")
    private String detailId;

    @Column(name = "GROUP_NUMBER", length = 4)
    @Comment("组号")
    private String groupNumber;// 本件电子档案所属的组在“机构或问题代码”下排列的顺序号。

    @Column(name = "GROUP_TITLE", length = 600)
    @Comment("组题名")
    private String groupTitle;// 揭示本组电子档案共同反映的主要内容

    @Column(name = "PHOTOGRAPHER", length = 100)
    @Comment("摄影者")
    private String photographer;// 本件电子档案的拍摄者。应著录摄影者姓名及其工作单位名称，姓名与单位名称之间用“，"隔开。多个摄影者信息之间用“;”隔开。摄影者无法考证时以“□□□”代替。

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Temporal(TemporalType.TIME)
    @Column(name = "PHOTOGRAPHY_TIME", length = 19)
    @Comment("摄影时间")
    private Date photographyTime;// 本件电子档案的拍摄时间。采用GB/T7408-2005中5.4.1 条的格式，例如“2021-02-24T10:23:14”

    @Column(name = "RECORDER", length = 100)
    @Comment("著录者")
    private String recorder;// 对本件电子档案进行著录的责任人姓名及其工作单位。姓名与单位名称之间用“,”隔开。多个著录者信息之间用“;”隔开。

    @Column(name = "PLACE", length = 200)
    @Comment("地点")
    private String place;// 本件电子档案的拍摄地点。

    @Column(name = "FIGURE", length = 600)
    @Comment("人物")
    private String figure;// 本件电子档案记录的主要人物信息著录人物的姓名、职务及其在照片中所处的位置，多个人物描述信息之间用”;”隔开。

    @Column(name = "BACKDROP", length = 200)
    @Comment("背景")
    private String backdrop;// 本件电子档案记录的具有检索或参照作用的实物背景信息，如建筑物、纪念碑、文 物等。

    @Column(name = "LOCATE_INFORMATION", length = 1200)
    @Comment("定位信息")
    private String locateInformation;// 本件电子档案拍摄地点的一组全球定位信息。包括全球定位系统版本，纬度基准，纬度，经度基准，经度，海拔基准，海拔，方向基准，镜头方向

    @Column(name = "HORIZONTAL_RESOLUTION")
    @Comment("水平分辨率")
    private Integer horizontalResolution = 0;// 静态图像水平方向每英寸像素数量与垂直分辨率共同构成图像分辨率如“500”

    @Column(name = "VERTICAL_RESOLUTION")
    @Comment("垂直分辨率")
    private Integer verticalResolution = 0;// 静态图像垂直方向每英寸像素数量与水平分辨率共同构成图像分辨率如“500”。

    @Column(name = "IMAGE_WIDTH")
    @Comment("图像宽度")
    private Integer imageWidth = 0;// 静态图像水平方向的像素数量。如“1024”。

    @Column(name = "IMAGE_HEIGHT")
    @Comment("图像高度")
    private Integer imageHeight = 0;// 静态图像垂直方向的像素数量。如“768”

    @Column(name = "COLOR_SPACE", length = 20)
    @Comment("色彩空间")
    private String colorSpace;// 表示静态图像颜色集合的抽象数学模型。sRGB,AdobeRGB,ProPhoto RGB,〔其他)

    @Column(name = "CAPTURE_DEVICE_MAkER", length = 100)
    @Comment("捕获设备制造商")
    private String captureDeviceMaker;// 创建并形成本件电子档案的硬件设备制造商名称。

    @Column(name = "CAPTURE_DEVICE_MODEL", length = 100)
    @Comment("捕获设备型号")
    private String captureDeviceModel;// 创建并形成本件电子档案的硬件设备的型号。

    @Column(name = "CAPTURE_DEVICE_SENSOR", length = 100)
    @Comment("捕获设备感光器")
    private String captureDeviceSensor;// 创建并形成本件电子档案的设备感光器的类型和参数 Not defined,One-chip color area sensor，Two-chip color
                                       // area sensor,Three-chip colorarea sensor,〔其他]

    @Column(name = "FILE_FORMAT", length = 10)
    @Comment("文件格式")
    private String fileFormat;// 本件电子档案的计算机文件格式名称。 JPEG,TIFF,〔其他】

    @Column(name = "FILE_SIZE")
    @Comment("文件大小")
    private Integer fileSize = 0;// 本件电子档案的字节数。如“3269120”。

    @Column(name = "MICRO_NUMBER", length = 50)
    @Comment("缩微号")
    private String microNumber;// 与本件电子档案对应的缩微胶片的编号。若本件电子档案有对应的缩微胶片则必须采用。

}
