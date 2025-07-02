import Request from '@/api/lib/request';
import qs from 'qs';

const dataRequest = Request();

// 数据目录树
export const dataCatalogTree = async (params) => {
    return await dataRequest({
        url: '/vue/catalog/getCatelogTree',
        method: 'get',
        cType: false,
        params
    });
};

export const searchCatelogTree = async (params) => {
    return await dataRequest({
        url: '/vue/catalog/searchCatelogTree',
        method: 'get',
        cType: false,
        params
    });
};

// 获取数据资产编码
export const getDataCode = async (params) => {
    return await dataRequest({
        url: '/vue/detail/genCode',
        method: 'get',
        cType: false,
        params
    });
};

// 保存数据资产
export const saveData = async (params) => {
    const data = qs.stringify(params);
    return await dataRequest({
        url: '/vue/detail/saveDataAssets',
        method: 'post',
        cType: false,
        data
    });
};

/**
 * 分页获取列表
 * @param params
 * @returns
 */
export const searchPage = async (params) => {
    return await dataRequest({
        url: '/vue/detail/searchPage',
        method: 'GET',
        cType: false,
        params
    });
};

// 删除
export const deleteDataAssets = async (id) => {
    return await dataRequest({
        url: '/vue/detail/deleteDataAssets',
        method: 'POST',
        cType: false,
        params: { id: id }
    });
};

// 上下架
export const updownData = async (id) => {
    return await dataRequest({
        url: '/vue/detail/updownData',
        method: 'POST',
        cType: false,
        params: { id: id }
    });
};

// 入库、出库
export const examineData = async (id, dataState) => {
    return await dataRequest({
        url: '/vue/detail/examineData',
        method: 'POST',
        cType: false,
        params: { id: id, dataState: dataState }
    });
};

// 赋码
export const genQr = async (id) => {
    return await dataRequest({
        url: '/vue/detail/genQr',
        method: 'POST',
        cType: false,
        params: { id: id }
    });
};

/**
 * 获取数据资产挂接文件
 * @param params 
 */
export const getFilePage = async (params) => {
    return await dataRequest({
        url: '/vue/detail/getFilePage',
        method: 'GET',
        cType: false,
        params
    });
};

// 获取挂接的接口列表
export const getSelectTree = async () => {
    return await dataRequest({
        url: '/vue/apionline/getSelectTree',
        method: 'GET',
        cType: false
    });
};

// 资产挂接接口
export const saveAssetsInterface = async (ids, assetsId) => {
    let formData = new FormData();
    formData.append("ids", ids);
    formData.append("assetsId", assetsId);
    return await dataRequest({
        url: '/vue/detail/saveAssetsInterface',
        method: 'post',
        cType: false,
        data: formData
    });
};

// 获取挂接的表列表
export const getTableSelectTree = async (type) => {
    return await dataRequest({
        url: '/vue/source/getTableSelectTree',
        method: 'GET',
        cType: false,
        params: { type: type }
    });
};

// 资产挂接数据表
export const saveAssetsTable = async (ids, assetsId) => {
    let formData = new FormData();
    formData.append("ids", ids);
    formData.append("assetsId", assetsId);
    return await dataRequest({
        url: '/vue/detail/saveAssetsTable',
        method: 'post',
        cType: false,
        data: formData
    });
};

// 删除挂接数据
export const deleteMountData = async (id) => {
    return await dataRequest({
        url: '/vue/detail/deleteMountData',
        method: 'POST',
        cType: false,
        params: { id: id }
    });
};

export const getDataByQrCode = async (params) => {
    return await dataRequest({
        url: '/service/qrcode/getDataByQrCode',
        method: 'GET',
        cType: false,
        params
    });
};