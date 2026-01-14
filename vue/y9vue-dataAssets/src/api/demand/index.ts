import Request from '@/api/lib/request';
import qs from 'qs';

const dataRequest = Request();

export const searchDemandPage = async (params) => {
    return await dataRequest({
        url: '/vue/demand/searchDemandPage',
        method: 'GET',
        cType: false,
        params
    });
};

export const saveDemandData = async (params) => {
    const data = qs.stringify(params);
    return await dataRequest({
        url: '/vue/demand/saveData2',
        method: 'post',
        cType: false,
        data
    });
};

export const deleteById = async (id) => {
    return await dataRequest({
        url: '/vue/demand/deleteData2',
        method: 'POST',
        cType: false,
        params: { id: id }
    });
};
