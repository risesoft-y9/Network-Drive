<template>
    <!-- 表格 -->
    <y9Table 
        :config="tableConfig"
        :filterConfig="filterConfig"
        uniqueIdent="dataApiTableConfig"
        @on-curr-page-change="onCurrentChange"
        @on-page-size-change="onPageSizeChange"
    >
        <template v-slot:filterBtnSlot>
            <el-button
                :size="fontSizeObj.buttonSize"
                :style="{ fontSize: fontSizeObj.baseFontSize }"
                class="global-btn-main"
                type="primary"
                @click="searchData"
            >
                <i class="ri-search-line"></i>
                {{ $t('搜索') }}
            </el-button>
            <el-button
                :size="fontSizeObj.buttonSize"
                :style="{ fontSize: fontSizeObj.baseFontSize }"
                class="global-btn-second"
                @click="addDataApiTable()"
                v-if="isShow"
            >
                <i class="ri-add-line" />
                {{ $t('新增申请') }}
            </el-button>
            <el-link type="primary" :underline="false" @click="showApiDocument" style="margin-left: 15px;">
                <i class="ri-eye-line"></i> 接口文档
            </el-link>
        </template>
    </y9Table>
    <!-- 制造loading效果 -->
    <el-button v-loading.fullscreen.lock="loading" style="display: none"></el-button>
    <y9Dialog v-model:config="dialogConfig">
        <DataApiTableForm
            v-if="dialogConfig.show"
            :disabled="disabled"
            :entity="entity"
            @close="close"
            :subscribeId="subscribeId"
            :assetsId="assetsId"
            :type="type"
        ></DataApiTableForm>
    </y9Dialog>
    <y9Dialog v-model:config="dialogConfig2">
        <ApiDocumentation />
    </y9Dialog>
</template>

<script lang="ts" setup>
    import { computed, h, inject, onMounted, reactive, ref, toRefs } from 'vue';
    import { ElMessage, ElMessageBox, ElNotification } from 'element-plus';
    import { useI18n } from 'vue-i18n';
    import { useSettingStore } from '@/store/modules/settingStore';
    import { getStoragePageSize } from '@/utils';
    import { getDataApiTablePage, deleteDataApiTable } from '@/api/apiService';
    import DataApiTableForm from './comps/DataApiTableForm.vue';
    import ApiDocumentation from './ApiDocumentation.vue';

    const settingStore = useSettingStore();
    // 注入 字体对象
    const fontSizeObj: any = inject('sizeObjInfo');
    const { t } = useI18n();
    const userRole = sessionStorage.getItem('userRole');

    const props = defineProps({
        subscribeId: {
            type: String,
            default: ''
        },
        assetsId: {
            type: String,
            default: ''
        },
        type: {
            type: String,
            default: ''
        },
        isShow: {
            type: Boolean,
            default: false
        },
    });

    // 变量 对象
    const state = reactive({
        // 区域 loading
        loading: false,
        // 查询条件
        formLine: {
            tableName: ''
        },
        // 表格的 配置信息
        tableConfig: {
            columns: [
                { title: computed(() => t('序号')), type: 'index', width: 60, fixed: 'left' },
                { title: computed(() => t('表名称')), key: 'tableName' },
                { title: computed(() => t('返回字段')), key: 'returnFields', render: (row) => {
                    return row.returnFields ? JSON.parse(row.returnFields).join(', ') : '';
                }},
                { title: computed(() => t('查询字段')), key: 'queryFields', render: (row) => {
                    return row.queryFields ? JSON.parse(row.queryFields).join(', ') : '';
                }},
                { title: computed(() => t('申请时间')), key: 'createTime', width: settingStore.getDatetimeSpan },
                { title: computed(() => t('权限拥有者')), key: 'owner' },
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
                                        editDataApiTable(row, false);
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
                                        ElMessageBox.confirm(`${t('是否删除')}【${row.tableName}】?`, t('提示'), {
                                            confirmButtonText: t('确定'),
                                            cancelButtonText: t('取消'),
                                            type: 'info'
                                        })
                                        .then(async () => {
                                            // 请求 移除 接口函数---
                                            loading.value = true;
                                            let result = await deleteDataApiTable(row.id);
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
                                                message: t('已取消删除'),
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
                        if(row.owner || props.type == '3') {
                            editActions = [h(
                                'span',
                                {
                                    style: {
                                        cursor: 'pointer',
                                        marginLeft: '10px',
                                    },
                                    class: 'global-btn-second',
                                    onClick: async () => {
                                        editDataApiTable(row, true);
                                    }
                                },
                                [
                                    h('i', { class: 'ri-eye-line', style: { marginRight: '2px' } }),
                                    h('span', t('查看'))
                                ]
                            )];
                            if(userRole === 'systemAdmin' && row.owner && props.isShow) {
                                editActions.push(h(
                                    'span',
                                    {
                                        style: {
                                            cursor: 'pointer',
                                            marginLeft: '10px',
                                        },
                                        class: 'global-btn-second',
                                        onClick: async () => {
                                            ElMessageBox.confirm(`${t(row.isDeleted ? '是否启用' : '是否禁用')}【${row.tableName}】?`, t('提示'), {
                                                confirmButtonText: t('确定'),
                                                cancelButtonText: t('取消'),
                                                type: 'info'
                                            })
                                            .then(async () => {
                                                loading.value = true;
                                                let result = await deleteDataApiTable(row.id);
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
                                                    message: t('已取消删除'),
                                                    offset: 65
                                                });
                                            });
                                        }
                                    },
                                    [
                                        h('i', { class: row.isDeleted ? 'ri-pushpin-line' : 'ri-unpin-line' }),
                                        h('span', t(row.isDeleted ? '启用' : '禁用'))
                                    ]
                                ));
                            }
                        }
                        if(row.isDeleted && props.type != '3') {
                            return h('span', t('已禁用'));
                        }
                        return h('span', editActions);
                    }
                }
            ],
            loading: false,
            border: false,
            headerBackground: true,
            tableData: [],
            pageConfig: {
                currentPage: 1,
                pageSize: getStoragePageSize('dataApiTableConfig', 15),
                total: 0,
                pageSizeOpts:[10, 15, 30, 60, 120, 240]
            }
        },
        // 表格过滤
        filterConfig: {
            filtersValueCallBack: (filters) => {
                formLine.value = filters;
            },
            showBorder: true,
            itemList: [
                {
                    type: 'input',
                    value: '',
                    key: 'tableName',
                    label: computed(() => t('表名称')),
                    span: 6,
                    clearable: true
                },
                {
                    type: 'slot',
                    slotName: 'filterBtnSlot',
                    span: 8
                }
            ]
        },
        //弹窗配置
        dialogConfig: {
            show: false,
            title: ''
        },
        dialogConfig2: {
            show: false,
            title: ''
        }
    });

    let {
        loading,
        formLine,
        tableConfig,
        filterConfig,
        dialogConfig,
        dialogConfig2
    } = toRefs(state);

    onMounted(() => {
        searchData();
    });

    let entity = ref(null);
    let disabled = ref(false);
    function addDataApiTable() {
        disabled.value = false;
        entity.value = null;
        Object.assign(dialogConfig.value, {
            show: true,
            width: '60%',
            title: computed(() => t('接口资源申请')),
            showFooter: false
        });
    }

    // 请求 列表接口
    async function searchData() {
        tableConfig.value.loading = true;
        let params = {
            page: tableConfig.value.pageConfig.currentPage,
            size: tableConfig.value.pageConfig.pageSize,
            tableName: formLine.value.tableName,
            subscribeId: props.subscribeId
        };
        let res = await getDataApiTablePage(params);
        if (res.code == 0) {
            // 对返回的接口数据进行赋值与处理
            tableConfig.value.tableData = res.rows;
            tableConfig.value.pageConfig.total = res.total;
        }
        tableConfig.value.loading = false;
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

    const close = (type) => {
        dialogConfig.value.show = false;
        if(type == 1) {
            tableConfig.value.pageConfig.currentPage = 1;
            searchData();
        }
    };

    function editDataApiTable(row, type) {
        disabled.value = type;
        entity.value = row;
        Object.assign(dialogConfig.value, {
            show: true,
            width: '60%',
            title: computed(() => t('接口资源申请')),
            showFooter: false
        });
    }

    // 显示调用示例
    async function showApiDocument() {
        Object.assign(dialogConfig2.value, {
            show: true,
            width: '60%',
            title: computed(() => t('接口文档')),
            showFooter: false
        });
    }
</script>

<style lang="scss" scoped>
    
</style>