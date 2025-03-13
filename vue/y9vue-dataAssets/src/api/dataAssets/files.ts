/*
 * @Author: yihong yihong@risesoft.net
 * @Date: 2024-11-21 10:36:20
 * @LastEditors: yihong yihong@risesoft.net
 * @LastEditTime: 2025-01-08 16:26:04
 * @FilePath: \vue\y9vue-dataAssets\src\api\dataAssets\files.ts
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
import Request from '@/api/lib/request';

var archivesRequest = new Request();

export function getFileList(detailId){
    const params = {
      detailId:detailId,
    };
    return archivesRequest({
      url: "/vue/files/getFileList",
      method: 'get',
      params: params
    });
  }

  export function deleteFile(id){
    const params = {
      id:id
    };
    return archivesRequest({
      url: "/vue/files/deleteFile",
      method: 'post',
      params: params
    });
  }
  