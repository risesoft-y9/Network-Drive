package net.risesoft.service;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import net.risesoft.entity.DataAssets;
import net.risesoft.entity.FileInfo;
import net.risesoft.model.SearchPage;
import net.risesoft.pojo.Y9Result;

public interface DataAssetsService {

    SearchPage<DataAssets> list(String categoryId, Boolean isDeleted, int page, int rows);

    SearchPage<DataAssets> listByColumnNameAndValues(String categoryId, Boolean isDeleted, String columnNameAndValues,
                                                     int page, int rows);

    DataAssets save(DataAssets dataAssets);

    DataAssets findById(Long id);

    void delete(String categoryId, Long[] id);

    void signDelete(String categoryId, Long[] id);

    void recordArchiving(Long[] ids);

    Y9Result<String> createAssetsNo(String categoryId, Long[] ids);

    List<DataAssets> findByDataAssetsIdIn(Long[] ids);

    /**
     * 保存数据资产
     * @param dataAssets
     * @return
     */
    Y9Result<String> saveDataAssets(DataAssets dataAssets);

    /**
     * 分页获取数据资产列表
     * @param categoryId
     * @param name
     * @param code
     * @param page
     * @param size
     * @return
     */
    Page<DataAssets> searchPage(String categoryId, String name, String code, Integer status, String dataState, int page, int size);

    /**
     * 删除数据资产
     * @param id
     * @return
     */
    Y9Result<String> deleteDataAssets(Long id);

    /**
     * 上传文件
     * @param file
     * @param assetsId
     * @return
     */
    Y9Result<String> fileUpload(MultipartFile file, Long assetsId);

    /**
     * 上下架资产
     * @param id
     * @return
     */
    Y9Result<String> updownData(Long id);

    /**
     * 给资产赋码
     * @param id
     * @return
     */
    Y9Result<String> genQr(Long id);

    /**
     * 生成数据资产编号
     * @return
     */
    Y9Result<String> genCode(String categoryId, String pCode);

    /**
     * 获取数据资产挂接的文件
     * @param id
     * @param page
     * @param size
     * @return
     */
    Page<FileInfo> getFilePage(Long id, String name, int page, int size);

    /**
     * 文件下载
     * @param id
     * @param response
     * @param request
     */
    void fileDownload(Long id, HttpServletResponse response, HttpServletRequest request);

    /**
     * 上传资产图片
     * @param file
     * @param assetsId
     * @return
     */
    Y9Result<DataAssets> uploadPicture(MultipartFile file, Long assetsId);

    /**
     * 保存挂接的接口信息
     * @param ids
     * @return
     */
    Y9Result<String> saveAssetsInterface(String ids, Long assetsId);

    /**
     * 保存挂接的数据表信息
     * @param ids
     * @param assetsId
     * @return
     */
    Y9Result<String> saveAssetsTable(String ids, Long assetsId);

    /**
     * 资产入库出库
     * @param id
     * @param dataState
     * @return
     */
    Y9Result<String> examineData(Long id, String dataState);

    /**
     * 删除挂接数据
     * @param id
     * @return
     */
    Y9Result<String> deleteMountData(Long id);

}
