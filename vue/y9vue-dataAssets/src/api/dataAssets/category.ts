
import Request from "@/api/lib/request";
import qs from "qs";

var archivesRequest = new Request();
//获取门类管理列表
export function getCategoryList(page,rows){
  const params = {
    page:page,
    rows:rows
  };
  return archivesRequest({
    url: "/vue/category/getCategoryList",
    method: 'get',
    params: params
  });
}

export function getAllCategory(){
  return archivesRequest({
    url: "/vue/category/getAllCategory",
    method: 'get',
  });
}

//保存门类信息
export function saveOrUpdate(viewType){
  const data = qs.stringify(viewType);
  return archivesRequest({
    url: "/vue/category/saveOrUpdate",
    method: 'post',
    data: data
  });
}

//删除门类
export function remove(id){
  const params = {
    id:id
  };
  return archivesRequest({
    url: "/vue/category/remove",
    method: 'post',
    params: params
  });
}



export function categoryIsExistTable(categoryMark){
  const params = {
    categoryMark:categoryMark
  };
  return archivesRequest({
    url: "/vue/category/table/categoryIsExistTable",
    method: 'post',
    params: params
  });
}

//新生成表
export function buildTable(table,fields){
  let formData = new FormData();
  formData.append("tables", table);
  formData.append("fields", fields);
  return archivesRequest({
    url: "/vue/category/table/buildTable",
    method: 'post',
    data: formData
  });
}

//修改表结构
export function updateTable(table,fields){
  let formData = new FormData();
  formData.append("tables", table);
  formData.append("fields", fields);
  return archivesRequest({
    url: "/vue/category/table/updateTable",
    method: 'post',
    data: formData
  });
}


//是否存在数据库表
export function checkTableExist(tableName){
  const params = {
    tableName:tableName
  };
  return archivesRequest({
    url: "/vue/category/table/checkTableExist",
    method: 'get',
    params: params
  });
}


export function saveTable(table,fields){
  let formData = new FormData();
  formData.append("tables", table);
  formData.append("fields", fields);
  return archivesRequest({
    url: "/vue/category/table/saveTable",
    method: 'post',
    data: formData
  });
}

export function removeTable(id){
  const params = {
    ids:id
  };
  return archivesRequest({
    url: "/vue/category/table/removeTable",
    method: 'post',
    params: params
  });
}

//获取业务表信息
export function getTable(id){
  const params = {
    id:id
  };
  return archivesRequest({
    url: "/vue/category/table/newOrModifyTable",
    method: 'get',
    params: params
  });
}

//获取表字段列表
export function getTableFieldList(tableId){
  const params = {
    tableId:tableId
  };
  return archivesRequest({
    url: "/vue/category/tableField/getTableFieldList",
    method: 'get',
    params: params
  });
}

export function saveField(field){
  return archivesRequest({
    url: "/vue/category/tableField/saveField",
    method: 'post',
    params: field
  });
}

export function deleteField(id){
  const params = {
    id:id
  };
  return archivesRequest({
    url: "/vue/category/tableField/deleteField",
    method: 'post',
    params: params
  });
}

//获取表字段信息
export function getFieldInfo(id,tableId){
  const params = {
    id:id,
    tableId:tableId
  };
  return archivesRequest({
    url: "/vue/category/tableField/newOrModifyField",
    method: 'get',
    params: params
  });
}

