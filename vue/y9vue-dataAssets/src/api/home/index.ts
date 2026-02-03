import Request from '@/api/lib/request';
import qs from 'qs';

const dataRequest = Request();

export const searchRecentPage = async (params) => {
    return await dataRequest({
        url: '/vue/home/findDataRecent',
        method: 'GET',
        cType: false,
        params
    });
};

/**
 * 获取首页统计数据
 */
export const findHomeStatistics = async () => {
    return await dataRequest({
        url: '/vue/home/findHomeStatistics',
        method: 'GET',
        cType: false,
    });
};

/**
 * 获取接口每天调用次数
 */
export const findApiCallCount = async () => {
    return await dataRequest({
        url: '/vue/home/findApiCallCount',
        method: 'GET',
        cType: false,
    });
};

/**
 * 获取下载记录
 */
export const findDownloadLog = async (params) => {
    return await dataRequest({
        url: '/vue/home/findDownloadLog',
        method: 'GET',
        cType: false,
        params
    });
};

/**
 * 获取数据源数据量
 */
export const findDataSourceDataCount = async () => {
    return await dataRequest({
        url: '/vue/home/findDataSourceDataCount',
        method: 'GET',
        cType: false,
    });
};

/**
 * 获取数据推送记录
 */
export const getDataFlowLog = async (params) => {
    return await dataRequest({
        url: '/vue/home/getDataFlowLog',
        method: 'GET',
        cType: false,
        params
    });
};
