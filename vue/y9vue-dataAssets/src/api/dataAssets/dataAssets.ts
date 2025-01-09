/*
 * @Author: yihong yihong@risesoft.net
 * @Date: 2024-11-06 17:37:56
 * @LastEditors: yihong yihong@risesoft.net
 * @LastEditTime: 2025-01-08 17:16:02
 * @FilePath: \vue\y9vue-dataAssets\src\api\dataAssets\dataAssets.ts
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
import Request from "@/api/lib/request";
import qs from "qs";

var archivesRequest = new Request();
//获取门类管理列表
export function getDataAssetsList(categoryId,columnNameAndValues,fileStatus,page,rows){
  const params = {
    categoryId:categoryId,
    columnNameAndValues:columnNameAndValues,
    fileStatus:fileStatus,
    page:page,
    rows:rows
  };
  return archivesRequest({
    url: "/vue/detail/getDataAssetsList",
    method: 'get',
    params: params
  });
}

export function saveFormData(saveType,categoryId,formDataJson){
    let data = new FormData();
    data.append("saveType", saveType);
    data.append("categoryId", categoryId);
    data.append("formDataJson", formDataJson);
    return archivesRequest({
      url: "/vue/detail/saveOrUpdate",
      method: 'post',
      data: data
    });
  }

  export function deleteData(categoryId,ids){
    const params = {
      categoryId:categoryId,
      ids:ids
    };
    return archivesRequest({
      url: "/vue/detail/delete",
      method: 'post',
      params: params
    });
  }

  //著录归档
  export function recordArchiving(ids){
    const params = {
      ids:ids
    };
    return archivesRequest({
      url: "/vue/detail/recordArchiving",
      method: 'post',
      params: params
    });
  }

  //生成资产编号
  
  export function createAssetsNo(categoryId,ids){
    const params = {
      categoryId:categoryId,
      ids:ids
    };
    return archivesRequest({
      url: "/vue/detail/createAssetsNo",
      method: 'post',
      params: params
    });
  }
