/*
 * @Author: yihong yihong@risesoft.net
 * @Date: 2025-12-05 16:50:59
 * @LastEditors: yihong yihong@risesoft.net
 * @LastEditTime: 2026-04-16 17:46:55
 * @FilePath: \y9-vue\y9vue-storage\src\api\storage\fileTag.ts
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
import Request from '@/api/lib/request';
import { forEach } from 'lodash';
import qs from 'qs';

let storageRequest = new Request();

export default {
    getTagList(tagName = '', page, rows) {
        let url = '/vue/fileTag/getTagList?tagName=' + tagName + '&page=' + page + '&rows=' + rows;
        return storageRequest.get(url);
    },
    getAllTagList(tagName = '') {
        return storageRequest.get('/vue/fileTag/getAllTagList?tagName=' + tagName);
    },
    getAllTag() {
        return storageRequest.get('/vue/fileTag/getAllTag');
    },
    saveFileTag(fileTag = {}) {
        return storageRequest.post('/vue/fileTag/saveFileTag', qs.stringify(fileTag));
    },
    deleteFileTag(ids = [],listType = '') {
        return storageRequest.delete('/vue/fileTag/deleteFileTag', {
            params: {
                ids: ids.join(),
                listType: listType
            }
        });
    },
    deleteTagAndRelation(ids = [],listType = '') {
        return storageRequest.delete('/vue/fileTag/deleteTagAndRelation', {
            params: {
                ids: ids.join(),
                listType: listType
            }
        });
    },
    addFileTagToFile(fileNodeIds = [], tagIds = [],listType = '') {
        var formData = new FormData();
        formData.append('fileNodeIds', fileNodeIds.join());
        formData.append('tagIds', tagIds.join());
        formData.append('listType', listType);
        return storageRequest.post('/vue/fileTag/addFileTagToFile', formData);
    },
    simpleFileToTag(fileId: string, tagId: string,listType: string = '') {
        var formData = new FormData();
        formData.append('fileId', fileId);
        formData.append('tagId', tagId);
        formData.append('listType', listType);
        return storageRequest.post(
            '/vue/fileTag/simpleFileToTag',
            formData
        );
    },
    removeFileTag(fileId: string, tagId: string,listType: string = '') {
        return storageRequest.delete(
            '/vue/fileTag/removeTagFromFile',
            {
                params: {
                    fileId: fileId,
                    tagId: tagId,
                    listType: listType
                }
            }
        );
    },
    saveCustomTag(tag,fileId,tagOpt = '') { 
        var formData = new FormData();
        forEach(tag, (value, key) => {
            formData.append(key, value);
        });
        formData.append('fileId', fileId);
        if(tagOpt == 'add') {
            return storageRequest.post('/vue/fileTag/saveCustomTag', formData);
        }else{
            return storageRequest.post('/vue/fileTag/updateCustomTag', formData);
        }
    },
    updateFileTag(tagId: string, tagName: string) {
        return storageRequest.post(
            '/vue/fileTag/updateFileTag',
            qs.stringify({
                tagId: tagId,
                tagName: tagName
            })
        );
    },
    checkIsUsed(tagIds = []){
        var formData = new FormData();
        formData.append('tagIds', tagIds.join());
        return storageRequest.post('/vue/fileTag/checkIsUsed', formData);
    }
};
