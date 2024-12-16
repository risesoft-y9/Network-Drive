/*
 * @Author: yihong yihong@risesoft.net
 * @Date: 2024-10-22 09:31:48
 * @LastEditors: yihong yihong@risesoft.net
 * @LastEditTime: 2024-11-25 18:03:07
 * @FilePath: \vue\y9vue-archives\src\api\archives\metadata.ts
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
import Request from "@/api/lib/request";
import qs from "qs";

var archivesRequest = new Request();
//获取意见框列表
export function getMetadataList(viewType,page,rows){
  const params = {
    viewType: viewType,
    page:page,
    rows:rows
  };
  return archivesRequest({
    url: "/vue/metadata/config/getMetadataList",
    method: 'get',
    params: params
  });
}


export function getMetadataOrderList(viewType){
  const params = {
    viewType: viewType,
  };
  return archivesRequest({
    url: "/vue/metadata/config/getMetadataOrderList",
    method: 'get',
    params: params
  });
}

//保存或更新元数据配置
export function saveMetadataConfig(metadataConfig){
  const data = qs.stringify(metadataConfig);
  return archivesRequest({
    url: "/vue/metadata/config/saveMetadataConfig",
    method: 'post',
    data: data
  });
}

//保存排序
export function saveOrder(idAndTabIndexs){
  const params = {
    idAndTabIndexs:idAndTabIndexs
  };
  const data = qs.stringify(params);
  return archivesRequest({
    url: "/vue/metadata/config/saveOrder",
    method: 'post',
    data: data
  });
}

//重置配置数据
export function resetConfig(viewType){
  const params = {
    viewType:viewType
  };
  return archivesRequest({
    url: "/vue/metadata/config/resetConfig",
    method: 'post',
    params: params
  });
}

//获取元数据字段列表(根据categoryId获取数据目录的顶节点)
export function getMetadataFieldList(categoryId){
  const params = {
    categoryId: categoryId
  };
  return archivesRequest({
    url: "/vue/metadata/config/getMetadataFieldList",
    method: 'get',
    params: params
  });
}


export function getMetadataRecordConfigList(viewType){
  const params = {
    viewType: viewType
  };
  return archivesRequest({
    url: "/vue/metadata/config/getMetadataRecordConfigList",
    method: 'get',
    params: params
  });
}

export function saveListFiledShow(idAndIsShowJson){
  let data = new FormData();
  data.append("idAndIsShowJson", idAndIsShowJson);
  return archivesRequest({
    url: "/vue/metadata/config/saveListFiledShow",
    method: 'post',
    data: data
  });
}

