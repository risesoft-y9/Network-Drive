import Request from '@/api/lib/request';
import qs from 'qs';
const platformRequest = Request();

/**
 * 获取接口数据
 */
export const getTreeData = async () => {
    return await platformRequest({
        url: 'vue/apionline/getTree',
        method: 'GET',
        cType: false
    });
};

/**
 * 保存数据
 */
export const saveTreeData = async (data) => {
    return await platformRequest({
        url: 'vue/apionline/saveData',
        method: 'POST',
        JSON: true,
        data
    });
};

/**
 * 删除节点
 * @param params
 * @returns
 */
export const removeNode = async (params) => {
    const data = qs.stringify(params);
    return await platformRequest({
        url: 'vue/apionline/deleteData',
        method: 'POST',
        cType: false,
        data
    });
};

export const randomString = (e) => {
    var e = e || 32,
        t = 'ABCDEFGHJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789',
        a = t.length,
        n = '';
    for (let i = 0; i < e; i++) n += t.charAt(Math.floor(Math.random() * a));
    return n;
};

export const getApiInfo = async (params) => {
    return await platformRequest({
        url: 'vue/apionline/getApiInfo',
        method: 'GET',
        cType: false,
        params
    });
};
