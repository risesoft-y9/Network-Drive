import Request from "@/api/lib/request";
import qs from "qs";

var archivesRequest = new Request();
//获取门类管理列表
export function getArchivesRecordList(categoryId,page,rows){
  const params = {
    categoryId:categoryId,
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