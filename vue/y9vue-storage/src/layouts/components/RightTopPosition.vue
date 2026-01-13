<template>
    <div v-if="positions?.length" class="item">
        <el-dropdown :hide-on-click="true" class="position-el-dropdown" @command="onMenuClick">
            <div class="name" @click="(e) => e.preventDefault()">
                <!-- show & if 的vue指令 仅用于适配移动端 -->
                <div>
                    <i class="ri-route-line"></i>
                    <span>{{ $t('选择岗位') }}</span>
                </div>
            </div>
            <template #dropdown>
                <el-dropdown-menu>
                    <div v-for="(item, index) in storageStore.positionList" :key="index">
                        <el-dropdown-item v-if="item.id !== positionId" :command="item">
                            <div class="el-dropdown-item">
                                <div><i class="ri-shield-user-line"></i>{{ item.name }}</div>
                            </div>
                        </el-dropdown-item>
                    </div>
                </el-dropdown-menu>
            </template>
        </el-dropdown>
    </div>
    <div v-else class="item" style="display: none">
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
    import { useRoute } from 'vue-router';
    import { useStorageStore } from '@/store/modules/storageStore';

    const fontSizeObj: any = inject('sizeObjInfo');
    let positionName = sessionStorage.getItem('positionName') ? sessionStorage.getItem('positionName') : '';

    const storageStore = useStorageStore();
    console.log(storageStore.positionList);
    // const personInfo = ref();
    // 获取当前登录用户信息
    const userInfo = JSON.parse(sessionStorage.getItem('ssoUserInfo'));
    // 岗位列表
    let positions: any = storageStore.positionList;
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
        storageStore.$patch({
            currentPositionId: command?.id,
            currentPositionName: command?.todoCount
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
        color: var(--el-menu-text-color);
        cursor: pointer;
        line-height: 60px;
        position: relative;
        border: none;

        i {
            position: relative;
            font-size: v-bind('fontSizeObj.extraLargeFont');
            // top: 4px;
        }

        span {
            font-size: v-bind('fontSizeObj.baseFontSize');
            margin-left: 5px;
        }

        &:hover {
            color: var(--el-color-primary);
        }

        &:hover {
            cursor: pointer;
            border-bottom: none; // 鼠标划过或点击时不显示下划线
        }

        .name {
            color: var(--el-text-color-primary);
            border: none;

            & > div {
                span {
                    line-height: 60px;
                    text-align: end;
                }
            }

            i {
                line-height: 20px;
            }

            .badge {
                margin-left: 5px;
            }

            &:hover {
                cursor: pointer;
                border: none; // 鼠标划过或点击时不显示下划线
                color: var(--el-color-primary);
            }
        }

        /**当前岗位 */
        &.notify {
            .badge {
                // top: -4px;
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
        font-size: v-bind('fontSizeObj.extraLargeFont');

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
        &:hover {
            cursor: pointer;
            border: none; // 鼠标划过或点击时不显示下划线
            color: var(--el-color-primary);
        }

        .badge {
            // position: absolute;
            //top: -4px;
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
            border: none;
        }
    }

    :deep(.el-badge) {
        .el-badge__content {
            border: none;
        }
    }
</style>
