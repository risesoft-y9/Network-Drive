package net.risesoft.model;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchPage<T> implements Serializable {

    private static final long serialVersionUID = 6108914052737501595L;

    /**
     * 内容列表
     */
    private List<T> rows;

    /**
     * 每页大小
     */
    private int size;
    /**
     * 当前页
     */
    private int currpage;

    /**
     * 总页数
     */
    private int totalpages;

    /**
     * 总数量
     */
    private long total;
}
