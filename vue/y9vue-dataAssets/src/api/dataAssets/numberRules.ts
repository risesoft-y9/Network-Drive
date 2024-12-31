
import Request from "@/api/lib/request";

var archivesRequest = new Request();
//获取当前门类的档号规则
export function getArchivesNumberRules(viewType){
  const params = {
    viewType:viewType
  };
  return archivesRequest({
    url: "/vue/rules/getArchivesNumberRules",
    method: 'get',
    params: params
  });
}

//判断是否存在档号规则
export function existRules(viewType){
  const params = {
    viewType:viewType
  };
  return archivesRequest({
    url: "/vue/rules/existRules",
    method: 'post',
    params: params
  });
}

//保存档号规则
export function saveRules(viewType,rulesJson){
    let formData = new FormData();
    formData.append("viewType", viewType);
    formData.append("rulesJson", rulesJson);
    return archivesRequest({
      url: "/vue/rules/saveRules",
      method: 'post',
      data: formData
    });
  }

