import Request from '@/api/lib/request';
import qs from 'qs';
const platformRequest = Request();

/**
 * 一级接口 数据源类型
 * @param params
 * @returns
 */
export const getDataSourceType = async () => {
    return await platformRequest({
        url: 'vue/source/findCategoryAll',
        method: 'GET',
        cType: false,
        params: {}
    });
};

/**
 * 二级接口 根据数据源类型查找对应数据列表
 * @param params
 * @returns
 */
export const getDataSourceByType = async (params) => {
    return await platformRequest({
        url: 'vue/source/findByBaseType',
        method: 'GET',
        cType: false,
        params: params
    });
};

/**
 * 根据数据源id获取数据库表
 * @param params 
 * @returns 
 */
export const getTablesByBaseId = async (params) => {
    return await platformRequest({
        url: 'vue/source/getTablePage',
        method: 'GET',
        cType: false,
        params: params
    });
};

/**
 * 获取表对应的字段信息
 * @param dataSourceId 
 * @param tableName 
 * @returns 
 */
export const getTableColumns = async (dataSourceId,tableName) => {
    return await platformRequest({
        url: 'vue/source/findTableColumnsByTableName',
        method: 'GET',
        cType: false,
        params: {
            dataSourceId: dataSourceId,
            tableName: tableName
        }
    });
};

/**
 * 获取表数据
 * @param dataSourceId 
 * @param tableName 
 * @param page 
 * @param row 
 * @returns 
 */
export const getTableData = async (dataSourceId,tableName,columnNameAndValues,page,rows) => {
    return await platformRequest({
        url: 'vue/source/findTableDataByTableName',
        method: 'GET',
        cType: false,
        params: {
            dataSourceId: dataSourceId,
            tableName: tableName,
            columnNameAndValues:columnNameAndValues,
            page: page,
            rows: rows
        }
    });
};

/**
 * 搜索数据源
 * @param params
 * @returns
 */
export const searchDataSource = async (params) => {
    return await platformRequest({
        url: 'vue/source/searchSource',
        method: 'GET',
        cType: false,
        params: params
    });
};

/**
 * 获取数据源详情
 * @param params
 * @returns
 */
export const getDataSourceById = async (params) => {
    return await platformRequest({
        url: 'vue/source/getDataSource',
        method: 'GET',
        cType: false,
        params: params
    });
};

/**
 * 新增数据源分类信息
 * @param params
 * @returns
 */
export const addDataSourceInfo = async (params) => {
    let formData = new FormData();
    for (let ele in params) {
        formData.append(ele, params[ele]);
    }
    return await platformRequest({
        url: 'vue/source/saveDataCategory',
        method: 'POST',
        cType: false,
        data: formData
    });
};

/**
 * 保存数据源连接信息
 * @param params
 * @returns
 */
export const saveDataConnectInfo = async (params) => {
    const data = qs.stringify(params);
    return await platformRequest({
        url: 'vue/source/saveSource',
        method: 'POST',
        cType: false,
        data
    });
};

/**
 * 删除数据源信息
 * @param params
 * @returns
 */
export const deleteDataSource = async (params) => {
    const data = qs.stringify(params);
    return await platformRequest({
        url: 'vue/source/deleteSource',
        method: 'POST',
        cType: false,
        data
    });
};

/**
 * 删除数据源分类信息
 * @param params
 * @returns
 */
export const deleteDataSourceType = async (params) => {
    const data = qs.stringify(params);
    return await platformRequest({
        url: 'vue/source/deleteCategory',
        method: 'POST',
        cType: false,
        data
    });
};

/**
 * 根据类型获取关系型和其他数据源列表
 * @param params
 * @returns
 */
export const findByTypeList = async (params) => {
    return await platformRequest({
        url: 'vue/source/findByType',
        method: 'GET',
        cType: false,
        params: params
    });
};

/**
 * 检测数据源状态
 * @param params
 * @returns
 */
export const checkDataStatus = async (params) => {
    return await platformRequest({
        url: 'vue/source/checkStatus',
        method: 'GET',
        cType: false,
        params: params
    });
};

export const getDataBase = async () => {
    return await platformRequest({
        url: '/vue/source/getDataBase',
        method: 'GET',
        cType: false
    });
};

/**
 * 根据资产id获取挂接的数据库
 * @param params
 * @returns
 */
export const getDataSourceByAssetsId = async (params) => {
    return await platformRequest({
        url: '/vue/source/getDataSourceByAssetsId',
        method: 'GET',
        cType: false,
        params: params
    });
};

export const getTableByAssetsId = async (params) => {
    return await platformRequest({
        url: 'vue/source/getTableByAssetsId',
        method: 'GET',
        cType: false,
        params: params
    });
};