/*
 * @Author: yihong yihong@risesoft.net
 * @Date: 2026-03-26 11:45:38
 * @LastEditors: yihong yihong@risesoft.net
 * @LastEditTime: 2026-04-01 11:35:05
 * @FilePath: \y9-vue\y9vue-storage\src\api\storage\operateLog.ts
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
import Request from '@/api/lib/request';
import qs from 'qs';

let storageRequest = new Request();
export default {
    getOperateLogList(query, page, rows) {
        let url = '/vue/fileTagLog/getOperateLogList?' + qs.stringify(query) + '&page=' + page + '&rows=' + rows;
        return storageRequest.get(url);
    }
};
