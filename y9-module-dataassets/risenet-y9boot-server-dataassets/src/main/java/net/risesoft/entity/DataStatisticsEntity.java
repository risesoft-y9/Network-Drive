package net.risesoft.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Comment;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.risesoft.base.BaseEntity;

@Entity
@Table(name = "Y9_DATAASSETS_STATISTICS")
@org.hibernate.annotations.Table(comment = "数据统计表", appliesTo = "Y9_DATAASSETS_STATISTICS")
@NoArgsConstructor
@Data
public class DataStatisticsEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID", length = 38, nullable = false)
    @Comment(value = "主键")
    private String id;

    @Column(name = "DATATIME", length = 100)
    @Comment(value = "数据时间")
    private String dataTime;

    @Column(name = "SOURCEID", length = 50)
    @Comment(value = "数据库ID")
    private String sourceId;

    @Column(name = "DATACOUNT")
    @Comment(value = "数据量")
    private Integer dataCount;

}
