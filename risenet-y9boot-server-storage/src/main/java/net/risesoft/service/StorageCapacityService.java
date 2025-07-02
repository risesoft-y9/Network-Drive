package net.risesoft.service;

import org.springframework.data.domain.Page;

import net.risesoft.entity.StorageCapacity;

public interface StorageCapacityService {

    /**
     * 保存存储记录
     * 
     * @param StorageCapacity
     */
    public void save(StorageCapacity storageCapacity);

    /**
     * 查询当前人使用网盘容量
     * 
     * @param userId
     * @return
     */
    public StorageCapacity findByCapacityOwnerId(String userId);

    /**
     * 根据Id查询存储记录
     * 
     * @param id
     * @return
     */
    public StorageCapacity findById(String id);

    /**
     * 获取所有人的存储容量列表
     * 
     * @param userName
     * @param page
     * @param rows
     * @return
     */
    public Page<StorageCapacity> findByUserName(String userName, int page, int rows);
}
