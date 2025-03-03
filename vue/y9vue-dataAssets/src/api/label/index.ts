import Request from '@/api/lib/request';
import qs from 'qs';

const dataRequest = Request();

export const dataCatalogTree = async (params) => {
    return await dataRequest({
        url: '/vue/label/getTree',
        method: 'get',
        cType: false,
        params
    });
};

export const getDataCatalog = async (id) => {
    return await dataRequest({
        url: `/vue/label/${id}`,
        method: 'get',
        cType: false
    });
};

export const saveDataCatalog = async (params) => {
    const data = qs.stringify(params);
    return await dataRequest({
        url: '/vue/label/saveData',
        method: 'post',
        cType: false,
        data
    });
};

export const deleteDataCatalog = async (id) => {
    return await dataRequest({
        url: '/vue/label/deleteData',
        method: 'POST',
        cType: false,
        params: { id: id }
    });
};

export const searchPage = async (params) => {
    return await dataRequest({
        url: '/vue/label/searchPage',
        method: 'GET',
        cType: false,
        params
    });
};

export const saveLabelData = async (id, name, code, tabIndex, parentId) => {
    let formData = new FormData();
    formData.append("id", id);
    formData.append("name", name);
    formData.append("code", code);
    formData.append("tabIndex", tabIndex);
    formData.append("parentId", parentId);
    return await dataRequest({
        url: '/vue/label/saveLabelData',
        method: 'post',
        cType: false,
        data: formData
    });
};

export const deleteLabelData = async (id) => {
    return await dataRequest({
        url: '/vue/label/deleteLabelData',
        method: 'POST',
        cType: false,
        params: { id: id }
    });
};

export const getLabelDataList = async () => {
    return await dataRequest({
        url: '/vue/label/getLabelDataList',
        method: 'GET',
        cType: false
    });
};

export const saveAssetsLabel = async (ids, assetsId) => {
    let formData = new FormData();
    formData.append("ids", ids);
    formData.append("assetsId", assetsId);
    return await dataRequest({
        url: '/vue/label/saveAssetsLabel',
        method: 'post',
        cType: false,
        data: formData
    });
};

export const getLabels = async (params) => {
    return await dataRequest({
        url: '/vue/label/getLabels',
        method: 'GET',
        cType: false,
        params
    });
};
