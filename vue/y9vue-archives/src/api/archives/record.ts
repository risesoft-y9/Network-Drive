/*
 * @Author: yihong yihong@risesoft.net
 * @Date: 2024-11-06 17:37:56
 * @LastEditors: yihong yihong@risesoft.net
 * @LastEditTime: 2024-11-19 14:41:08
 * @FilePath: \vue\y9vue-archives\src\api\archives\record.ts
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
import Request from "@/api/lib/request";
import qs from "qs";

var archivesRequest = new Request();
//获取门类管理列表
export function getArchivesRecordList(categoryId,columnNameAndValues,page,rows){
  const params = {
    categoryId:categoryId,
    columnNameAndValues:columnNameAndValues,
    page:page,
    rows:rows
  };
  return archivesRequest({
    url: "/vue/record/getArchivesRecordList",
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
      url: "/vue/record/saveOrUpdate",
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
      url: "/vue/record/delete",
      method: 'post',
      params: params
    });
  }