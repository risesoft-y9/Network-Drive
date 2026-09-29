package net.risesoft.service;

import org.springframework.data.domain.Page;

import net.risesoft.entity.StorageCapacity;
import net.risesoft.pojo.Y9Result;

public interface StorageCapacityService {

    /**
     * 保存存储记录
     * 
     * @param storageCapacity
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
     * 查询或创建存储容量记录（线程安全，防止并发重复创建）
     *
     * @param userId 用户ID
     * @param userName 用户名
     * @return 存储容量记录
     */
    public StorageCapacity findOrCreateCapacity(String userId, String userName);

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

    /**
     * 更新存储容量信息
     * 
     * @param storageCapacity
     * @return
     */
    Y9Result<Object> updateCapacity(StorageCapacity storageCapacity);
}
