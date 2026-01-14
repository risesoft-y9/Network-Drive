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
