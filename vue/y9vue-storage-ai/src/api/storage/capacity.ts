import Request from '@/api/lib/request';
import qs from 'qs';

let storageRequest = new Request();

export default {
    getCapacityList(userName = '', page, rows) {
        let url = '/vue/capacity/getCapacityList?userName=' + userName + '&page=' + page + '&rows=' + rows;
        return storageRequest.get(url);
    },
    updateCapacity(storageCapacity = {}) {
        return storageRequest.post('/vue/capacity/updateCapacity', qs.stringify(storageCapacity));
    },
    getCapacityInfo(id = '') {
        var formData = new FormData();
        formData.append('id', id);
        return storageRequest.post('/vue/capacity/getCapacityInfo', formData);
    },
    getCapacitySize() {
        return storageRequest.post('/vue/capacity/getCapacitySize');
    }
};
