import Request from '@/api/lib/request';

var archivesRequest = new Request();

export function getArchivesFileList(archivesId){
    const params = {
        archivesId:archivesId,
    };
    return archivesRequest({
      url: "/vue/files/getArchivesFileList",
      method: 'get',
      params: params
    });
  }

  export function deleteArchivesFile(id){
    const params = {
      id:id
    };
    return archivesRequest({
      url: "/vue/files/deleteFile",
      method: 'post',
      params: params
    });
  }
  