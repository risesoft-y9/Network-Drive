import Request from '@/api/lib/request';
import qs from 'qs';

const dataRequest = Request();

export const dataCatalogTree = async (params) => {
    return await dataRequest({
        url: '/vue/api/getTree',
        method: 'get',
        cType: false,
        params
    });
};

export const getDataCatalog = async (id) => {
    return await dataRequest({
        url: `/vue/api/${id}`,
        method: 'get',
        cType: false
    });
};

export const saveDataCatalog = async (params) => {
    const data = qs.stringify(params);
    return await dataRequest({
        url: '/vue/api/saveData',
        method: 'post',
        cType: false,
        data
    });
};

export const deleteDataCatalog = async (id) => {
    return await dataRequest({
        url: '/vue/api/deleteData',
        method: 'POST',
        cType: false,
        params: { id: id }
    });
};

export const searchPage = async (params) => {
    return await dataRequest({
        url: '/vue/api/searchPage',
        method: 'GET',
        cType: false,
        params
    });
};

export const deleteApiData = async (id) => {
    return await dataRequest({
        url: '/vue/api/deleteApiData',
        method: 'POST',
        cType: false,
        params: { id: id }
    });
};

export const saveApiData = async (params) => {
    const data = qs.stringify(params);
    return await dataRequest({
        url: '/vue/api/saveApiData',
        method: 'post',
        cType: false,
        data
    });
};

export const getApiInfo = (id) => {
    return dataRequest({
        url: `/vue/api/getApiInfo`,
        method: 'get',
        cType: false,
        params: { id: id}
    });
};

export const searchRolePage = async (params) => {
    return await dataRequest({
        url: '/vue/api/searchRolePage',
        method: 'GET',
        cType: false,
        params
    });
};

export const saveApiRoleData = async (params) => {
    const data = qs.stringify(params);
    return await dataRequest({
        url: '/vue/api/saveApiRoleData',
        method: 'post',
        cType: false,
        data
    });
};

export const deleteApiRoleData = async (id) => {
    return await dataRequest({
        url: '/vue/api/deleteApiRoleData',
        method: 'POST',
        cType: false,
        params: { id: id }
    });
};

export const searchLogPage = async (params) => {
    return await dataRequest({
        url: '/vue/api/searchLogPage',
        method: 'GET',
        cType: false,
        params
    });
};

export const getApiTreeList = async () => {
    return await dataRequest({
        url: '/vue/api/getApiTreeList',
        method: 'GET',
        cType: false
    });
};

export const saveApiRole = async (ids, appName) => {
    let formData = new FormData();
    formData.append("ids", ids);
    formData.append("appName", appName);
    return await dataRequest({
        url: '/vue/api/saveApiRole',
        method: 'post',
        cType: false,
        data: formData
    });
};

export const getApiRole = async (params) => {
    return await dataRequest({
        url: '/vue/api/getApiRole',
        method: 'GET',
        cType: false,
        params
    });
};

export const testSql = async (params) => {
    const data = qs.stringify(params);
    return await dataRequest({
        url: '/vue/source/testSql',
        method: 'post',
        cType: false,
        data
    });
};

export const getOwners = async () => {
    return await dataRequest({
        url: '/vue/api/getAllUsers',
        method: 'GET',
        cType: false
    });
};


// DataApiTable相关API
export const getDataApiTablePage = async (params) => {
    return await dataRequest({
        url: '/vue/dataApiTable/page',
        method: 'GET',
        cType: false,
        params
    });
};

export const saveDataApiTable = async (params) => {
    return await dataRequest({
        url: '/vue/dataApiTable/save',
        method: 'POST',
        cType: false,
        JSON: true,
        data: params
    });
};

export const deleteDataApiTable = async (id) => {
    return await dataRequest({
        url: `/vue/dataApiTable/${id}`,
        method: 'DELETE',
        cType: false
    });
};

export const getDataApiTableById = async (id) => {
    return await dataRequest({
        url: `/vue/dataApiTable/${id}`,
        method: 'GET',
        cType: false
    });
};

// 获取表的外键
export const getTableForeignKeys = async (tableName, dataSourceId) => {
    return await dataRequest({
        url: `/vue/dataApiTable/foreignKeys/${tableName}`,
        method: 'GET',
        cType: false,
        params: { dataSourceId }
    });
};