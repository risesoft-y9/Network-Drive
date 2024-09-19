package net.risesoft.controller.mobile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.consts.UtilConsts;
import net.risesoft.entity.StorageCapacity;
import net.risesoft.service.StorageCapacityService;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.json.Y9JsonUtil;
import net.risesoft.y9.util.Y9Util;

/**
 * 文件空间管理接口
 *
 * @author yihong
 *
 */
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/mobile/fileCapacity")
public class MobileFileCapacityController {

    private final StorageCapacityService storageCapacityService;
    protected Logger log = LoggerFactory.getLogger(MobileFileCapacityController.class);

    /**
     * 获取文件空间管理列表
     * 
     * @param tenantId 租户id
     * @param userId 用户id
     * @param userName 用户名称
     * @param page 页数
     * @param rows 总行数
     * @param response
     */
    @RequestMapping(value = "/getFileCapacityList")
    public void getFileCapacityList(@RequestHeader("auth-tenantId") String tenantId,
        @RequestHeader("auth-userId") String userId, @RequestParam(required = false) String userName,
        @RequestParam Integer page, @RequestParam Integer rows, HttpServletResponse response) {
        Map<String, Object> ret_map = new HashMap<String, Object>();
        try {
            Y9LoginUserHolder.setTenantId(tenantId);
            Y9LoginUserHolder.setPersonId(userId);
            List<Map<String, Object>> items = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (page < 1) {
                page = 1;
            }
            Page<StorageCapacity> scList = storageCapacityService.findByUserName(userName, page, rows);
            for (StorageCapacity sc : scList.getContent()) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", sc.getId());
                map.put("capacitySize", FileUtils.byteCountToDisplaySize(sc.getCapacitySize()));
                map.put("remainingLength", FileUtils.byteCountToDisplaySize(sc.getRemainingLength()));
                map.put("capacityOwnerId", sc.getCapacityOwnerId());
                map.put("capacityOwnerName", sc.getCapacityOwnerName());
                map.put("operatorId", sc.getOperatorId());
                map.put("operatorName", sc.getOperatorName());
                map.put("updateTime", sc.getUpdateTime() != null ? sdf.format(sc.getUpdateTime()) : "");
                map.put("createTime", sdf.format(sc.getCreateTime()));
                items.add(map);
            }
            ret_map.put("rows", items);
            ret_map.put("currPage", page);
            ret_map.put("totalPages", scList.getTotalPages());
            ret_map.put("total", scList.getTotalElements());
            ret_map.put(UtilConsts.SUCCESS, true);
            ret_map.put("msg", "获取文件空间管理列表成功");
        } catch (Exception e) {
            ret_map.put(UtilConsts.SUCCESS, false);
            ret_map.put("msg", "获取文件空间管理列表失败");
            LOGGER.error("获取文件空间管理列表失败", e);
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(ret_map));
    }

    /**
     * 根据id获取存储空间信息
     * 
     * @param tenantId 租户id
     * @param userId 用户id
     * @param id 存储空间id
     * @param response
     */
    @RequestMapping(value = "/getCapacityInfo")
    public void getCapacityInfo(@RequestHeader("auth-tenantId") String tenantId,
        @RequestHeader("auth-userId") String userId, @RequestParam String id, HttpServletResponse response) {
        Y9LoginUserHolder.setTenantId(tenantId);
        Y9LoginUserHolder.setPersonId(userId);
        Map<String, Object> ret_map = new HashMap<String, Object>();
        try {
            StorageCapacity sc = storageCapacityService.findById(id);
            ret_map.put("storageCapacity", sc);
            ret_map.put(UtilConsts.SUCCESS, true);
            ret_map.put("msg", "获取存储空间信息成功");
        } catch (Exception e) {
            ret_map.put(UtilConsts.SUCCESS, false);
            ret_map.put("msg", "获取存储空间信息失败");
            LOGGER.error("获取存储空间信息失败", e);
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(ret_map));
    }

    /**
     * 更新存储空间容量
     * 
     * @param tenantId 租户id
     * @param userId 用户id
     * @param id 存储空间id
     * @param capacitySize 存储容量大小
     * @param response
     */
    @RequestMapping(value = "/updateCapacity")
    public void updateCapacity(@RequestHeader("auth-tenantId") String tenantId,
        @RequestHeader("auth-userId") String userId, @RequestParam String id, @RequestParam Long capacitySize,
        HttpServletResponse response) {
        Y9LoginUserHolder.setTenantId(tenantId);
        Y9LoginUserHolder.setPersonId(userId);
        Map<String, Object> ret_map = new HashMap<String, Object>();
        try {
            StorageCapacity sc = storageCapacityService.findById(id);
            if (null != sc) {
                if (capacitySize > sc.getCapacitySize()) {
                    Long size = capacitySize - sc.getCapacitySize();
                    sc.setRemainingLength(size + sc.getRemainingLength());
                }
                if (capacitySize < sc.getCapacitySize()) {
                    Long size = sc.getCapacitySize() - capacitySize;
                    sc.setRemainingLength(sc.getRemainingLength() - size);
                }
                sc.setCapacitySize(capacitySize);
                sc.setUpdateTime(new Date());
                storageCapacityService.save(sc);
            }
            ret_map.put(UtilConsts.SUCCESS, true);
            ret_map.put("msg", "存储空间设置更新成功");
        } catch (Exception e) {
            ret_map.put(UtilConsts.SUCCESS, false);
            ret_map.put("msg", "存储空间设置更新失败");
            LOGGER.error("存储空间设置更新失败", e);
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(ret_map));
    }
}
