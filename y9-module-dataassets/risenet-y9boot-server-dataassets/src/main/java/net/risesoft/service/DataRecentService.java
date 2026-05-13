package net.risesoft.service;

import org.springframework.data.domain.Page;

import net.risesoft.entity.DataRecentEntity;

public interface DataRecentService {

    /**
     * 异步保存最近操作数据
     *
     * @param dataRecentEntity 最近操作数据实体
     */
    void saveAsync(DataRecentEntity dataRecentEntity);

    /**
     * 分页获取最近操作数据列表
     *
     * @param page 页码
     * @param size 每页数量
     * @return 最近操作数据分页列表
     */
    Page<DataRecentEntity> findAll(Integer page, Integer size);

}
