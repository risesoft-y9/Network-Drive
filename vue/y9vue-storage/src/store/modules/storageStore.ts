import { remove } from 'lodash';
import { defineStore } from 'pinia';

export const useStorageStore = defineStore('storageStore', {
    state: () => {
        return {
            positionList: [], //岗位列表，用于切换岗位
            currentPositionId: '', //当前岗位id
            currentPositionName: '', //当前岗位名称
            tenantId: '' //当前租户Id
        };
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
        }
    },
    actions: {}
});
