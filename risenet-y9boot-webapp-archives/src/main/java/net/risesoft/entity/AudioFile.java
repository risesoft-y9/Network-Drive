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
@Table(name = "Y9_ARCHIVES_AUDIO_FILE")
@org.hibernate.annotations.Table(comment = "档案录音类文件表", appliesTo = "Y9_ARCHIVES_AUDIO_FILE")
public class AudioFile implements Serializable {

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

    @Column(name = "RESPONSIBILITY_PERSON", length = 100)
    @Comment("责任者")
    private String responsibilityPerson;// 对本文件的内容进行创造、负有责任的团体或个人。

    @Column(name = "RECORDIST", length = 100)
    @Comment("录制者")
    private String recordist;// 本件电子档案的录制者。应著录录制者姓名及其工作单位名称，姓名与单位名称之间用“,”隔开。多个录制者信息之间用“;”隔开。录制者无法考证时以“口口口”代替。

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Temporal(TemporalType.TIME)
    @Column(name = "RECORDING_TIME", length = 19)
    @Comment("录制时间")
    private Date recordingTime;// 本件电子档案的录制时间。采用GB/T 7408-2005中5.4.1条的格式，例如“2021-02-24I10:23:14

    @Column(name = "RECORDER", length = 100)
    @Comment("著录者")
    private String recorder;// 对本件电子档案进行著录的责任人姓名及其工作单位。姓名与单位名称之间用“,”隔开。多个著录者信息之间用“;”隔开。

    @Column(name = "DURATION", length = 20)
    @Comment("时长")
    private String duration;// 本录音的持续时间的数量。格式为hh:mm:ss,如“0l:06:18”。

    @Column(name = "CODING_STANDARD", length = 20)
    @Comment("音频编码标准")
    private String codingStandard;// 音频文件的压缩编码标准。如PCM,MPEG 1 Layer 1MPEG-4 ALS,AAC.HE-AAC,OGG，APEFLAC,AOC.MPC.〔其他】。

    @Column(name = "BIT_RATE", length = 10)
    @Comment("音频比特率")
    private String bitRate;// 每秒传输的音频信息的比特(bit)数如“448Kb/s”。

    @Column(name = "SAMPLING_RATE", length = 10)
    @Comment("音频采样率")
    private String samplingRate;// 单位时间内从音频模拟信号中提取组成数字信号的采样个数。48KHZ”。22.05KHz,32KHz、44.1KHz,48KHz、96KHz,192KHz.〔其他】。

    @Column(name = "QUANTIZATION_BITS", length = 10)
    @Comment("音频量化位数")
    private String quantizationBits;// 音频信息采集、处理系统从原始音频信息进行量化的参数。 8bit.16bit,24bit,〔其他】

    @Column(name = "VOCAL_TRACT", length = 2)
    @Comment("声道")
    private String vocalTract;// 1.2,4,6.8,〔其他】

    @Column(name = "IMAGE_WIDTH")
    @Comment("捕获设备类型")
    private Integer imageWidth = 0;// 静态图像水平方向的像素数量。如“1024”

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

    @Column(name = "SCENE_NUMBER", length = 3)
    @Comment("场景号")
    private String sceneNumber;// 本场景在该件电子档案内排列的顺序号。如“C01”。若对本场景进行了著录则必须采用。

    @Column(name = "SCENE_TITLE", length = 600)
    @Comment("场景题名")
    private String sceneTitle;// 揭示本场景的中心主题的标题或名称。若对本场景进行了著录则必须采用

    @Column(name = "SCENE_DESCRIPTION", length = 4000)
    @Comment("场景描述")
    private String sceneDescription;// 对本场景记录的业务活动、讲话内容人物、地点等的描述信息。当描述的场景为一项业务活动时，应按照题名撰写要求著录。
    // 当描述的场景为人物时，应著录人物信息，包括人物姓名、职务等。当描述的场景为某人讲话时，可以根据讲话内容的重要程度，作原文级或摘要级著录，原文级要照实著录，著录格式由讲话人与讲话内容两部分构成。

    @Column(name = "SCENE_START_TIME", length = 20)
    @Comment("场景起始时间")
    private String sceneStartTime;// 本场景在该件电子档案时间轴上的起点位置。格式为hh:mm:ss,如“00:10:30”。若对本场景进行了著录则必须采用。

    @Column(name = "SCENE_DURATION", length = 20)
    @Comment("场景时长")
    private String sceneDuration;// 本场景在该件电子档案中的持续时间。格式为hh:mm:ss，如“00:26:18”。若对本场景进行了著录则必须采用

}
