import Request from '@/api/lib/request';
import qs from 'qs';

var storageRequest = new Request();
export default {
    treeFolder(parentId) {
        var url = '/vue/fileNode/topFolder?parentId=' + parentId;
        return storageRequest.get(url);
    },
    treeFolderById(params) {
        var url = '/vue/fileNode/listFolder?id=' + params.parentId;
        return storageRequest.get(url);
    },
    treePublicFolderById(params) {
        var url = '/vue/fileNode/listPublicFolder?id=' + params.parentId;
        return storageRequest.get(url);
    },
    list(id = '', searchName = '', fileNodeType, listType, orderProp, orderAsc) {
        var url =
            '/vue/fileNode/list?id=' +
            id +
            '&searchName=' +
            searchName +
            '&fileNodeType=' +
            fileNodeType +
            '&listType=' +
            listType +
            '&orderProp=' +
            orderProp +
            '&orderAsc=' +
            orderAsc;
        return storageRequest.get(url);
    },
    publicList(id = '', searchName = '', fileNodeType, startTime, endTime, listType, orderProp, orderAsc) {
        var url =
            '/vue/fileNode/publicList?id=' +
            id +
            '&searchName=' +
            searchName +
            '&startTime=' +
            startTime +
            '&endTime=' +
            endTime +
            '&fileNodeType=' +
            fileNodeType +
            '&listType=' +
            listType +
            '&orderProp=' +
            orderProp +
            '&orderAsc=' +
            orderAsc;
        return storageRequest.get(url);
    },
    manageList(id = '', searchName = '', fileNodeType, startTime, endTime, listType, orderProp, orderAsc) {
        var url =
            '/vue/fileNode/manageList?id=' +
            id +
            '&searchName=' +
            searchName +
            '&startTime=' +
            startTime +
            '&endTime=' +
            endTime +
            '&fileNodeType=' +
            fileNodeType +
            '&listType=' +
            listType +
            '&orderProp=' +
            orderProp +
            '&orderAsc=' +
            orderAsc;
        return storageRequest.get(url);
    },
    sharedList() {
        var url = '/vue/fileNode/sharedList';
        return storageRequest.get(url);
    },
    getDownloadRecord(fileId, page, rows) {
        var url = '/vue/fileNode/getDownloadRecord?fileId=' + fileId + '&page=' + page + '&rows=' + rows;
        return storageRequest.get(url);
    },
    deletedList() {
        var url = '/vue/fileNode/deletedList';
        return storageRequest.get(url);
    },
    saveFileNode(fileNode = {}) {
        return storageRequest.post('/vue/fileNode/saveFolder', qs.stringify(fileNode));
    },
    setLinkPwd(id, encryption, linkPassword) {
        return storageRequest.get(
            '/vue/fileNode/setLinkPwd?id=' +
                id +
                '&encryption=' +
                encryption +
                '&linkPassword=' +
                encodeURI(linkPassword)
        );
    },
    createLink(id, linkPassword) {
        var formData = new FormData();
        formData.append('fileId', id);
        formData.append('linkPassword', encodeURI(linkPassword));
        return storageRequest.post('/vue/fileShareLink/createLink',formData);
    },
    checkLink(tenantId,pwd,linkKey) {
        var formData = new FormData();
        formData.append('tenantId', tenantId);
        formData.append('pwd', pwd);
        formData.append('linkKey', linkKey);
        return storageRequest.post('/link/checkLink', formData);
    },
    get(id) {
        var url = '/vue/fileNode/' + id;
        return storageRequest.get(url);
    },
    getNetParentId(id) {
        var url = '/vue/fileNode/getNetParentId?id=' + id;
        return storageRequest.get(url);
    },
    delete(ids = []) {
        return storageRequest.delete('/vue/fileNode', {
            params: {
                ids: ids.join()
            }
        });
    },
    emptyRecycleBin() {
        return storageRequest.delete('/vue/fileNode/emptyRecycleBin');
    },
    permanentlyDelete(ids = []) {
        return storageRequest.delete('/vue/fileNode/permanently', {
            params: {
                ids: ids.join()
            }
        });
    },
    restore(idArr = []) {
        var formData = new FormData();
        formData.append('ids', idArr.join());
        return storageRequest.post('/vue/fileNode/restore', formData);
    },
    move(idArr = [], targetId = null) {
        var formData = new FormData();
        formData.append('ids', idArr.join());
        formData.append('targetId', targetId);
        return storageRequest.post('/vue/fileNode/move', formData);
    },
    upload(file, parentId = '', listType) {
        var formData = new FormData();
        formData.append('file', file);
        formData.append('parentId', parentId);
        formData.append('listType', listType);
        return storageRequest.post('/vue/fileNode/uploadFile', formData);
    },
    saveFeedback(id, feedbackReason) {
        var formData = new FormData();
        formData.append('id', id);
        formData.append('feedbackReason', feedbackReason);
        return storageRequest.post('/vue/fileNode/saveFeedback', formData);
    },
    getFileText(fileId) {
        return storageRequest.get('/vue/fileNode/getFileText?fileStoreId=' + fileId);
    },
    checkPwd(id, tenantId, pwd) {
        var formData = new FormData();
        formData.append('id', id);
        formData.append('tenantId', tenantId);
        formData.append('pwd', pwd);
        return storageRequest.post('/link/checkPwd', formData);
    },
    setFolderPassword(fileNode = {}) {
        return storageRequest.post('/vue/fileNode/setFolderPassword', qs.stringify(fileNode));
    },
    checkFolderPassword(fileNode = {}) {
        return storageRequest.post('/vue/fileNode/checkFolderPassword', qs.stringify(fileNode));
    },
    resetFolderPassword(fileNode = {}) {
        return storageRequest.post('/vue/fileNode/resetFolderPassword', qs.stringify(fileNode));
    },
    cancelFolderPassword(fileNode = {}) {
        return storageRequest.post('/vue/fileNode/cancelFolderPassword', qs.stringify(fileNode));
    },
    decryptPassword(fileNode = {}) {
        return storageRequest.post('/vue/fileNode/decryptPassword', qs.stringify(fileNode));
    },
    setCollect(id) {
        var formData = new FormData();
        formData.append('fileId', id);
        return storageRequest.post('/vue/fileNodeCollect/setCollect', formData);
    },
    cancelCollect(id) {
        var formData = new FormData();
        formData.append('fileId', id);
        return storageRequest.post('/vue/fileNodeCollect/cancelCollect', formData);
    },
    getCollectList(id = '', searchName = '', listType, orderProp, orderAsc) {
        var url =
            '/vue/fileNode/getCollectList?id=' +
            id +
            '&searchName=' +
            searchName +
            '&listType=' +
            listType +
            '&orderProp=' +
            orderProp +
            '&orderAsc=' +
            orderAsc;
        return storageRequest.get(url);
    },
    openCollectFolder(id = '', searchName = '', listType, orderProp, orderAsc) {
        return storageRequest.get('/vue/fileNode/openCollectFolder');
    }
};
