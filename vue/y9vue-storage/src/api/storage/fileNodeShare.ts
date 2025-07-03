import Request from '@/api/lib/request';

var storageRequest = new Request();

export default {
    share(fileNodeIdArr = [], orgUnitIdArr = []) {
        var formData = new FormData();
        formData.append('fileNodeIds', fileNodeIdArr.join());
        formData.append('orgUnitIds', orgUnitIdArr.join());
        return storageRequest.post('/vue/fileNodeShare/share', formData);
    },
    publicTo(fileNodeIdArr = [], orgUnitIdArr = []) {
        var formData = new FormData();
        formData.append('fileNodeIds', fileNodeIdArr.join());
        formData.append('orgUnitIds', orgUnitIdArr.join());
        return storageRequest.post('/vue/fileNodeShare/publicTo', formData);
    },
    cancelShare(fileNodeIdArr = []) {
        return storageRequest.delete('/vue/fileNodeShare', {
            params: {
                fileNodeIds: fileNodeIdArr.join()
            }
        });
    },
    list() {
        var url = '/vue/fileNodeShare/myList';
        return storageRequest.get(url);
    },
    getFilePublicRecord(fileId, page, rows) {
        var url = '/vue/fileNodeShare/getFilePublicRecord?fileId=' + fileId + '&page=' + page + '&rows=' + rows;
        return storageRequest.get(url);
    },
    deletePublic(publicIdArr = []) {
        return storageRequest.delete('/vue/fileNodeShare/deletePublic', {
            params: {
                publicIds: publicIdArr.join()
            }
        });
    }
};
