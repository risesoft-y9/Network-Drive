/*
 * @Author: yihong yihong@risesoft.net
 * @Date: 2024-11-06 17:37:56
 * @LastEditors: yihong yihong@risesoft.net
 * @LastEditTime: 2024-11-27 15:25:57
 * @FilePath: \vue\y9vue-dataAssets\src\api\dataAssets\dataAssets.ts
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
import Request from "@/api/lib/request";
import qs from "qs";

var archivesRequest = new Request();
//获取门类管理列表
export function getArchivesList(categoryId,columnNameAndValues,fileStatus,page,rows){
  const params = {
    categoryId:categoryId,
    columnNameAndValues:columnNameAndValues,
    fileStatus:fileStatus,
    page:page,
    rows:rows
  };
  return archivesRequest({
    url: "/vue/dataAssets/getArchivesList",
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
      url: "/vue/dataAssets/saveOrUpdate",
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
      url: "/vue/dataAssets/delete",
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
      url: "/vue/dataAssets/recordArchiving",
      method: 'post',
      params: params
    });
  }

  //生成档号
  
  export function createArchivesNo(categoryId,ids){
    const params = {
      categoryId:categoryId,
      ids:ids
    };
    return archivesRequest({
      url: "/vue/dataAssets/createArchivesNo",
      method: 'post',
      params: params
    });
  }

  //获取选择的档案列表
  export function getSelectArchivesList(archivesId){
    const params = {
      archivesId:archivesId
    };
    return archivesRequest({
      url: "/vue/dataAssets/getSelectArchivesList",
      method: 'get',
      params: params
    });
  }