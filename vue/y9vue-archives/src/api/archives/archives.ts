/*
 * @Author: yihong yihong@risesoft.net
 * @Date: 2024-11-06 17:37:56
 * @LastEditors: yihong yihong@risesoft.net
 * @LastEditTime: 2024-11-25 11:40:46
 * @FilePath: \vue\y9vue-archives\src\api\archives\archives.ts
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
    url: "/vue/archives/getArchivesList",
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
      url: "/vue/archives/saveOrUpdate",
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
      url: "/vue/archives/delete",
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
      url: "/vue/archives/recordArchiving",
      method: 'post',
      params: params
    });
  }

  //生成档号
  
  export function createArchivesNo(ids){
    const params = {
      ids:ids
    };
    return archivesRequest({
      url: "/vue/archives/createArchivesNo",
      method: 'post',
      params: params
    });
  }