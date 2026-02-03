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

export const getDataById = async (id) => {
    return await dataRequest({
        url: '/vue/detail/getDataById',
        method: 'GET',
        cType: false,
        params: { id: id }
    });
};

// 订阅模块
export const dataCatalogTree2 = async (params) => {
    return await dataRequest({
        url: '/vue/catalog/getCatelogTree2',
        method: 'get',
        cType: false,
        params
    });
};

export const searchCatelogTree2 = async (params) => {
    return await dataRequest({
        url: '/vue/catalog/searchCatelogTree2',
        method: 'get',
        cType: false,
        params
    });
};

export const searchPage2 = async (params) => {
    return await dataRequest({
        url: '/vue/detail/searchPage2',
        method: 'GET',
        cType: false,
        params
    });
};

// 保存订阅信息
export const saveSubscribe = async (params) => {
    const data = qs.stringify(params);
    return await dataRequest({
        url: '/vue/detail/saveSubscribe',
        method: 'post',
        cType: false,
        data
    });
};

export const searchSubscribePage = async (params) => {
    return await dataRequest({
        url: '/vue/detail/searchSubscribePage',
        method: 'GET',
        cType: false,
        params
    });
};

// 审核订阅
export const reviewData = async (params) => {
    const data = qs.stringify(params);
    return await dataRequest({
        url: '/vue/detail/review',
        method: 'post',
        cType: false,
        data
    });
};

export const getSubscribeById = async (id) => {
    return await dataRequest({
        url: '/vue/detail/getSubscribeById',
        method: 'GET',
        cType: false,
        params: { id: id }
    });
};

export const saveSubscribeBase = async (params) => {
    const data = qs.stringify(params);
    return await dataRequest({
        url: '/vue/detail/saveSubscribeBase',
        method: 'post',
        cType: false,
        data
    });
};

export const getSubscribeBaseById = async (id) => {
    return await dataRequest({
        url: '/vue/detail/getSubscribeBaseById',
        method: 'GET',
        cType: false,
        params: { id: id }
    });
};

// 检验订阅库表推送信息
export const checkSubscribeBase = async (params) => {
    const data = qs.stringify(params);
    return await dataRequest({
        url: '/vue/detail/checkSubscribeBase',
        method: 'post',
        cType: false,
        data
    });
};
