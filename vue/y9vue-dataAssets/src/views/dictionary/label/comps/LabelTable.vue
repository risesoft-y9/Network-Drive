<template>
    <y9Card :title="`${$t('标签列表')} - ${currTreeNodeInfo.name ? currTreeNodeInfo.name : ''}`">
        <template v-slot>
            <!-- 表格 -->
            <y9Table 
                :config="tableConfig" 
                :filterConfig="filterConfig"
                uniqueIdent="labelConfig" 
                @on-curr-page-change="onCurrentChange"
                @on-page-size-change="onPageSizeChange"
            >
                <template v-slot:filterBtnSlot>
                    <el-button
                        :size="fontSizeObj.buttonSize"
                        :style="{ fontSize: fontSizeObj.baseFontSize }"
                        class="global-btn-second"
                        @click="addLabel()"
                    >
                        <i class="ri-add-line" />
                        {{ $t('新增') }}
                    </el-button>
                </template>
                <template #name="{ row }">
                    <el-tag :type="success">{{ row.name }}</el-tag>
                </template>
            </y9Table>
            <!-- 制造loading效果 -->
            <el-button v-loading.fullscreen.lock="loading" style="display: none"></el-button>
        </template>
    </y9Card>
    <y9Dialog v-model:config="dialogConfig">
        <el-form-item label="名称">
            <el-input v-model="labelData.name" placeholder="必填项" />
        </el-form-item>
        <el-form-item label="代码">
            <el-input v-model="labelData.code" placeholder="必填项" />
        </el-form-item>
        <el-form-item label="排序">
            <el-input v-model="labelData.tabIndex" placeholder="填数字" />
        </el-form-item>
        <el-button class="ml-3" type="primary" @click="submitSave">提交</el-button>
    </y9Dialog>
</template>

<script lang="ts" setup>
    import { computed, h, inject, onMounted, reactive, ref, toRefs, watch } from 'vue';
    import { ElMessage, ElMessageBox, ElNotification } from 'element-plus';
    import { useI18n } from 'vue-i18n';
    import { useSettingStore } from '@/store/modules/settingStore';
    import y9_storage from '@/utils/storage';
    import { getStoragePageSize } from '@/utils';
    import { deleteLabelData, saveLabelData, searchPage } from '@/api/label';
    import settings from '@/settings';
    import router from '@/router';

    const settingStore = useSettingStore();
    // 注入 字体对象
    const fontSizeObj: any = inject('sizeObjInfo');
    const { t } = useI18n();
    const props = defineProps({
        currTreeNodeInfo: {
            //当前tree节点信息
            type: Object,
            default: () => {
                return {};
            }
        }
    });

    // 变量 对象
    const state = reactive({
        // 区域 loading
        loading: false,
        // 表格的 配置信息
        tableConfig: {
            columns: [
                { title: computed(() => t('序号')), type: 'index', width: 60, fixed: 'left' },
                { title: computed(() => t('名称')), key: 'name', slot: 'name' },
                { title: computed(() => t('代码')), key: 'code' },
                { title: computed(() => t('排序号')), key: 'tabIndex' },
                { title: computed(() => t('添加时间')), key: 'createTime', width: settingStore.getDatetimeSpan },
                {
                    title: computed(() => t('操作')),
                    fixed: 'right',
                    width: '180px',
                    render: (row) => {
                        let editActions = [
                            h(
                                'span',
                                {
                                    style: {
                                        cursor: 'pointer',
                                        marginLeft: '10px',
                                    },
                                    class: 'global-btn-second',
                                    onClick: async () => {
                                        editLabel(row);
                                    }
                                },
                                [
                                    h('i', { class: 'ri-edit-line', style: { marginRight: '2px' } }),
                                    h('span', t('编辑'))
                                ]
                            ),
                            h(
                                'span',
                                {
                                    style: {
                                        cursor: 'pointer',
                                        marginLeft: '10px',
                                    },
                                    class: 'global-btn-second',
                                    onClick: async () => {
                                        ElMessageBox.confirm(`${t('是否删除')}【${row.name}】?`, t('提示'), {
                                            confirmButtonText: t('确定'),
                                            cancelButtonText: t('取消'),
                                            type: 'info'
                                        })
                                        .then(async () => {
                                            // 请求 移除 接口函数---
                                            loading.value = true;
                                            let result = await deleteLabelData(row.id);
                                            ElNotification({
                                                title: result.success ? t('成功') : t('失败'),
                                                message: result.msg,
                                                type: result.success ? 'success' : 'error',
                                                duration: 2000,
                                                offset: 80
                                            });
                                            loading.value = false;
                                            // 重新请求 列表数据
                                            searchData();
                                        })
                                        .catch(() => {
                                            ElMessage({
                                                type: 'info',
                                                message: t('已取消移除'),
                                                offset: 65
                                            });
                                        });
                                    }
                                },
                                [
                                    h('i', { class: 'ri-delete-bin-line', style: { marginRight: '2px' } }),
                                    h('span', t('删除'))
                                ]
                            )
                        ];
                        return h('span', editActions);
                    }
                }
            ],
            tableData: [],
            pageConfig: {
                currentPage: 1,
                pageSize: getStoragePageSize('labelConfig', 15),
                total: 0,
                pageSizeOpts:[10, 15, 30, 60, 120, 240]
            }
        },
        // 表格过滤
        filterConfig: {
            showBorder: true,
            itemList: [
                {
                    type: 'slot',
                    slotName: 'filterBtnSlot',
                    span: 6
                }
            ]
        },
        //弹窗配置
        dialogConfig: {
            show: false,
            title: ''
        },
        // 标签参数
        labelData: {
            id: '',
            parentId: '',
            name: '',
            code: '',
            tabIndex: 1
        }
    });

    let {
        loading,
        tableConfig,
        filterConfig,
        dialogConfig,
        labelData
    } = toRefs(state);

    onMounted(() => {
        searchData();
    });
    // 监听系统id 当发生改变时重新请求数据
    watch(
        () => props.currTreeNodeInfo.id,
        async (new_, old_) => {
            if (new_ && new_ !== old_) {
                searchData();
            }
        }
    );

    function addLabel() {
        labelData.value.id = '';
        labelData.value.name = '';
        labelData.value.code = '';
        labelData.value.tabIndex = 1;
        labelData.value.parentId = props.currTreeNodeInfo.id;
        Object.assign(dialogConfig.value, {
            show: true,
            width: '20%',
            title: computed(() => t('新增标签')),
            showFooter: false
        });
    }

    // 请求 列表接口
    async function searchData() {
        let params = {
            parentId: props.currTreeNodeInfo.id,
            page: tableConfig.value.pageConfig.currentPage,
            size: tableConfig.value.pageConfig.pageSize,
        };
        let res = await searchPage(params);
        if (res.code == 0) {
            // 对返回的接口数据进行赋值与处理
            tableConfig.value.tableData = res.rows;
            tableConfig.value.pageConfig.total = res.total;
        }
    }

    // 分页操作
    function onCurrentChange(currPage) {
        tableConfig.value.pageConfig.currentPage = currPage;
        searchData();
    }

    function onPageSizeChange(pageSize) {
        tableConfig.value.pageConfig.pageSize = pageSize;
        searchData();
    }

    function editLabel(row) {
        labelData.value = row;
        Object.assign(dialogConfig.value, {
            show: true,
            width: '20%',
            title: computed(() => t('编辑标签')),
            showFooter: false
        });
    }

    const submitSave = () => {
        saveLabelData(labelData.value.id, labelData.value.name, labelData.value.code, 
            labelData.value.tabIndex, labelData.value.parentId).then((res) => {
            if(res.success) {
                dialogConfig.value.show = false;
                searchData();
            }
            ElNotification({
                title: res.success ? t('成功') : t('失败'),
                message: res.msg,
                type: res.success ? 'success' : 'error',
                duration: 2000,
                offset: 80
            });
        });
    }
</script>
<style lang="scss" scoped>
.ml-3 {
    float: right;
}
</style>
