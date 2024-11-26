
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




