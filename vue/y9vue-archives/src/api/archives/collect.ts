
import Request from "@/api/lib/request";
import qs from "qs";

var archivesRequest = new Request();
//获取意见框列表
export function getCatalogList(){
  const params = {
  };
  return archivesRequest({
    url: "/vue/collect/getCatalogList",
    method: 'get',
    params: params
  });
}



 
//保存意见框
// export function saveOrUpdate(opinionFrame){
//   const data = qs.stringify(opinionFrame);
//   return itemAdminRequest({
//     url: "/vue/opinionFrame/saveOrUpdate",
//     method: 'post',
//     data: data
//   });
// }


// export function searchOpinionFrame(page,rows,keyword){
//   const params = {
//     keyword:keyword,
//     page:page,
//     rows:rows
//   };
//   return itemAdminRequest({
//     url: "/vue/opinionFrame/search",
//     method: 'get',
//     params: params
//   });
// }



