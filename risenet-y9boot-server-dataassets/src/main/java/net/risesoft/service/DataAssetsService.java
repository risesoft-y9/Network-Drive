package net.risesoft.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import net.risesoft.entity.DataAssets;
import net.risesoft.entity.FileInfo;
import net.risesoft.entity.SubscribeBaseEntity;
import net.risesoft.entity.SubscribeEntity;
import net.risesoft.model.AssetsModel;
import net.risesoft.pojo.Y9Page;
import net.risesoft.pojo.Y9Result;

public interface DataAssetsService {

    DataAssets findById(Long id);

    /**
     * 保存数据资产
     * 
     * @param dataAssets
     * @return
     */
    Y9Result<String> saveDataAssets(DataAssets dataAssets);

    /**
     * 分页获取数据资产列表
     * 
     * @param categoryId
     * @param name
     * @param code
     * @param page
     * @param size
     * @return
     */
    Page<DataAssets> searchPage(String categoryId, String name, String code, Integer status, String dataState, int page,
        int size);

    /**
     * 删除数据资产
     * 
     * @param id
     * @return
     */
    Y9Result<String> deleteDataAssets(Long id);

    /**
     * 上传文件
     * 
     * @param file
     * @param assetsId
     * @return
     */
    Y9Result<String> fileUpload(MultipartFile file, Long assetsId);

    /**
     * 上下架资产
     * 
     * @param id
     * @return
     */
    Y9Result<String> updownData(Long id);

    /**
     * 给资产赋码
     * 
     * @param id
     * @return
     */
    Y9Result<String> genQr(Long id);

    /**
     * 生成数据资产编号
     * 
     * @return
     */
    Y9Result<String> genCode(String categoryId, String pCode);

    /**
     * 获取数据资产挂接的文件
     * 
     * @param id
     * @param page
     * @param size
     * @return
     */
    Page<FileInfo> getFilePage(Long id, String name, int page, int size);

    /**
     * 文件下载
     * 
     * @param id
     * @param response
     * @param request
     */
    void fileDownload(Long id, HttpServletResponse response, HttpServletRequest request);

    /**
     * 上传资产图片
     * 
     * @param file
     * @param assetsId
     * @return
     */
    Y9Result<DataAssets> uploadPicture(MultipartFile file, Long assetsId);

    /**
     * 保存挂接的接口信息
     * 
     * @param ids
     * @return
     */
    Y9Result<String> saveAssetsInterface(String ids, Long assetsId);

    /**
     * 保存挂接的数据表信息
     * 
     * @param ids
     * @param assetsId
     * @return
     */
    Y9Result<String> saveAssetsTable(String ids, Long assetsId);

    /**
     * 资产入库出库
     * 
     * @param id
     * @param dataState
     * @return
     */
    Y9Result<String> examineData(Long id, String dataState);

    /**
     * 删除挂接数据
     * 
     * @param id
     * @return
     */
    Y9Result<String> deleteMountData(Long id);

    /**
     * 可信数据空间-获取数据资产列表
     * 
     * @param page
     * @param size
     * @return
     */
    Y9Page<Map<String, Object>> searchPage2(String searchText, String categoryId, String appScenarios, String dataZone, String productType,
        Integer sort, int page, int size);

    /**
     * 根据id获取数据资产信息
     * 
     * @param id
     * @return
     */
    Y9Result<AssetsModel> getDataById(Long id);

    /**
     * 根据二维码获取信息
     * 
     * @param qrcode
     * @return
     */
    Y9Result<AssetsModel> getDataByQrCode(String qrcode);

    /**
     * 可信数据空间-获取过滤条件
     * 
     * @return
     */
    Y9Result<List<Map<String, Object>>> getDataFilter();
    
    /**
     * 保存订阅信息
     * @param subscribeEntity
     * @return
     */
    Y9Result<String> saveSubscribe(SubscribeEntity subscribeEntity);
    
    /**
     * 获取订阅列表
     * @param name 资产名称
     * @param page
     * @param size
     * @return
     */
    Y9Page<Map<String, Object>> searchSubscribePage(String type, String name, int page, int size);
    
    /**
     * 审核订阅信息
     * @param id
     * @param reviewStatus
     * @param reason
     * @return
     */
    Y9Result<String> review(String id, String reviewStatus, String reason);
    
    /**
     * 根据id获取订阅信息
     * @param id
     * @return
     */
    Map<String, Object> getSubscribeById(String id);

    /**
     * 保存订阅-库表推送信息
     * @param subscribeBaseEntity
     * @return
     */
    Y9Result<String> saveSubscribeBase(SubscribeBaseEntity subscribeBaseEntity);

    /**
     * 根据id获取订阅-库表推送信息
     * @param id
     * @return
     */
    SubscribeBaseEntity getSubscribeBaseById(String id);

    /**
     * 根据id获取订阅信息
     * @param id
     * @return
     */
    SubscribeEntity findById(String id);

}
