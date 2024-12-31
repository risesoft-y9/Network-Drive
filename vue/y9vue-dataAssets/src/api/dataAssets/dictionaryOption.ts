import Request from "@/api/lib/request";
import qs from "qs";

var archivesRequest = new Request();
//获取数据字典列表
export function getOptionClassList(){
  const params = {
    name:""
  };
  return archivesRequest({
    url: "/vue/dataDict/getOptionClassList",
    method: 'get',
    params: params
  });
}

//获取数据字典
export function getOptionClass(type){
  const params = {
    type:type
  };
  return archivesRequest({
    url: "/vue/dataDict/getOptionClass",
    method: 'get',
    params: params
  });
}

//保存数据字典
export function saveOptionClass(optionClass){
  const data = qs.stringify(optionClass);
  return archivesRequest({
    url: "/vue/dataDict/saveOptionClass",
    method: 'post',
    data: data
  });
}

//删除数据字典
export function delOptionClass(type){
  const params = {
    type:type
  };
  return archivesRequest({
    url: "/vue/dataDict/delOptionClass",
    method: 'post',
    params: params
  });
}

//获取数据字典值列表
export function getOptionValueList(type){
  const params = {
    type:type
  };
  return archivesRequest({
    url: "/vue/dataDict/getOptionValueList",
    method: 'get',
    params: params
  });
}

//获取数据字典值
export function getOptionValue(id){
  const params = {
    id:id
  };
  return archivesRequest({
    url: "/vue/dataDict/getOptionValue",
    method: 'get',
    params: params
  });
}

//保存数据字典值
export function saveOptionValue(optionValue){
  const data = qs.stringify(optionValue);
  return archivesRequest({
    url: "/vue/dataDict/saveOptionValue",
    method: 'post',
    data: data
  });
}

//删除数据字典值
export function delOptionValue(id){
  const params = {
    id:id
  };
  return archivesRequest({
    url: "/vue/dataDict/delOptionValue",
    method: 'post',
    params: params
  });
}

//设置默认选中
export function updateOptionValue(id){
  const params = {
    id:id
  };
  return archivesRequest({
    url: "/vue/dataDict/updateOptionValue",
    method: 'post',
    params: params
  });
}

//保存排序
export function saveOrder(ids){
  const params = {
    ids:ids
  };
  return archivesRequest({
    url: "/vue/dataDict/saveOrder",
    method: 'post',
    params: params
  });
}