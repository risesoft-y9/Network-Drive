/*
 * @Author: yihong yihong@risesoft.net
 * @Date: 2024-11-01 17:47:37
 * @LastEditors: yihong yihong@risesoft.net
 * @LastEditTime: 2024-11-05 11:08:06
 * @FilePath: \vue\y9vue-dataAssets\src\store\modules\archivesStore.ts
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
import { remove } from "lodash";
import { defineStore } from 'pinia';

export const useArchivesStore = defineStore('archivesStore', {
    state: () => {
        return {
            positionList: [],//岗位列表，用于切换岗位
            currentPositionId: '',//当前岗位id
            currentPositionName: '',//当前岗位名称
            tenantId:'', //当前租户Id
            parentCustomId:'',//父目录CustomId
        }
    },
    getters: {
        getPositionList(state) {
            return state.positionList;
        },
        getCurrentPositionId(state) {
            return state.currentPositionId;
        },
        getCurrentPositionName(state) {
            return state.currentPositionName;
        },
        getTenantId(state) {
            return state.tenantId;
        },
        getParentCustomId(state) {
            return state.parentCustomId;
        }
    },
    actions: {
       
    }
})