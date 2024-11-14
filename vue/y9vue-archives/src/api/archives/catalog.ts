/*
 * @Author: yihong yihong@risesoft.net
 * @Date: 2024-11-01 15:48:52
 * @LastEditors: yihong yihong@risesoft.net
 * @LastEditTime: 2024-11-04 16:51:49
 * @FilePath: \vue\y9vue-archives\src\api\archives\catalog.ts
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
import Request from '@/api/lib/request';

var archivesRequest = new Request();

export function getCatelogTree(params){
    return archivesRequest({
      url: "/vue/catalog/getCatelogTree",
      method: 'get',
      cType: false,
      params: params
    });
  }
