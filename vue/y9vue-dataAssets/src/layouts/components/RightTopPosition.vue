<template>
    <div class="item" v-if="positions?.length">
        <el-dropdown @command="onMenuClick" :hide-on-click="true" class="position-el-dropdown">
            <div class="name" @click="(e) => e.preventDefault()">
                <!-- show & if 的vue指令 仅用于适配移动端 -->
                <div>
                    <i class="ri-route-line"></i>
                    <span>{{ $t('选择岗位') }}</span>
                </div>
            </div>
            <template #dropdown>
                <el-dropdown-menu>
                    <div v-for="(item, index) in archivesStore.positionList" :key="index">
                        <el-dropdown-item :command="item" v-if="item.id !== positionId">
                            <div class="el-dropdown-item">
                                <div> <i class="ri-shield-user-line"></i>{{ item.name }} </div>
                            </div>
                        </el-dropdown-item>
                    </div>
                </el-dropdown-menu>
            </template>
        </el-dropdown>
    </div>
    <div class="item" v-else style="display: none">
        <div class="position-el-dropdown">
            <!-- show & if 的vue指令 仅用于适配移动端 -->
            <div class="name" style="cursor: not-allowed; color: #aaa">
                <i class="ri-route-line"></i>
                <span>{{ $t('选择岗位') }}</span>
            </div>
        </div>
    </div>
    <div class="item notify">
        <i class="ri-user-location-line"></i>
        <span>{{ positionName }}</span>
    </div>
</template>
<script lang="ts" setup>
    import { ref, computed, ComputedRef, defineComponent } from 'vue';
    import { useRoute } from 'vue-router';
    import y9_storage from '@/utils/storage';
    import { useArchivesStore } from '@/store/modules/archivesStore';
    const fontSizeObj: any = inject('sizeObjInfo');
    let positionName = sessionStorage.getItem('positionName') ? sessionStorage.getItem('positionName') : '';

    const archivesStore = useArchivesStore();
    console.log(archivesStore.positionList);
    // const personInfo = ref();
    // 获取当前登录用户信息
    const userInfo =JSON.parse(sessionStorage.getItem('ssoUserInfo'));
    // 岗位列表
    let positions: any = archivesStore.positionList;
    let positionId = sessionStorage.getItem('positionId');
    positions.forEach((item, index) => {
        if (item.id == positionId) {
            positions.splice(index, 1);
        }
    });
    const currentrRute = useRoute();
    // 点击菜单
    const onMenuClick = async (command: any) => {
        sessionStorage.setItem('positionId', command?.id);
        // 设置 positionName
        sessionStorage.setItem('positionName', command?.name);
        archivesStore.$patch({
            currentPositionId: command?.id,
            currentPositionName: command?.todoCount,
        });
        window.location.href = import.meta.env.VUE_APP_HOST_INDEX;
    };
</script>
<style lang="scss" scoped>
    @import '@/theme/global-vars.scss';
    .item {
        overflow: hidden;
        padding: 0 11px;
        min-width: 5px;
        display: flex;
        align-items: center;
        i {
            position: relative;
            // top: 4px;
        }
        span {
            font-size: v-bind('fontSizeObj.baseFontSize');
            margin-left: 5px;
        }
        &:hover {
            cursor: pointer;
            outline: none;
            color: var(--el-color-primary);
        }

        &.notify {
            .badge {
                top: -4px;
                z-index: 1;
                & > .el-badge__content--danger {
                    background-color: var(--el-color-danger);
                }
            }
        }
    }
    .position-el-dropdown {
        z-index: 9999;
        min-width: 5px;
        height: $headerHeight;
        text-align: center;
        line-height: 50px;
        font-size: v-bind('fontSizeObj.largeFontSize');
        :deep(.el-dropdown--default) {
            display: flex;
            align-items: center;
        }
        i {
            position: relative;
            top: 4px;
        }
        span {
            margin-left: 5px;
        }

        .badge {
            // position: absolute;
            top: -4px;
            margin-left: 7px;
            z-index: 1;
            & > .el-badge__content--danger {
                background-color: var(--el-color-danger);
            }
        }
    }

    .el-dropdown-item {
        div {
            width: 100%;
            display: flex;
            padding: 0 5px;
            top: 2px;
            left: -5px;
        }
    }

    :deep(.el-badge) {
        .el-badge__content {
            border: none;
        }
        sup {
            top: 0;
        }
    }
</style>
