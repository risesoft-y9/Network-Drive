/*
 * @Author: yihong yihong@risesoft.net
 * @Date: 2026-07-16 16:24:48
 * @LastEditors: yihong yihong@risesoft.net
 * @LastEditTime: 2026-10-09 10:33:43
 * @FilePath: \vue\y9vue-storage\src\api\storage\capacity.ts
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
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
    },
    // 获取上传大小限制（后台返回字节）
    getUploadLimit() {
        return storageRequest.post('/vue/uploadLimit/getUploadLimit');
    },
    // 保存上传大小限制，uploadSize 单位：字节
    updateUploadLimit(uploadSize = 0) {
        return storageRequest.post('/vue/uploadLimit/updateUploadLimit', qs.stringify({ uploadSize: uploadSize }));
    }
};
