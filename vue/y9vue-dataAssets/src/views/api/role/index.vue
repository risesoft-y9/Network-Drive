<template>
    <!-- 表格 -->
    <y9Table 
        :config="tableConfig" 
        :filterConfig="filterConfig"
        uniqueIdent="apiRoleConfig" 
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
                @click="handleClick()"
            >
                <i class="ri-add-line" />
                {{ $t('新增') }}
            </el-button>
        </template>
        <template #status="{ row }">
            {{ row.status == 1 ? '禁用' : '正常' }}
        </template>
    </y9Table>
    <!-- 制造loading效果 -->
    <el-button v-loading.fullscreen.lock="loading" style="display: none"></el-button>

    <y9Dialog v-model:config="addDialogConfig">
        <y9Form ref="ruleFormRef" :config="formConfig"></y9Form>
    </y9Dialog>

    <y9Dialog v-model:config="dialogConfig">
        <Transfer 
            v-if="dialogConfig.show"
            :id="id" 
            @close="close"
        ></Transfer>
    </y9Dialog>
</template>

<script lang="ts" setup>
    import { computed, h, inject, onMounted, reactive, ref, toRefs, watch } from 'vue';
    import { ElMessage, ElMessageBox, ElNotification } from 'element-plus';
    import { useI18n } from 'vue-i18n';
    import { useSettingStore } from '@/store/modules/settingStore';
    import { deleteApiRoleData, saveApiRoleData, searchRolePage } from '@/api/apiService';
    import settings from '@/settings';
    import router from '@/router';
    import { getStoragePageSize } from '@/utils';
    import Transfer from './transfer.vue';

    const settingStore = useSettingStore();
    // 注入 字体对象
    const fontSizeObj: any = inject('sizeObjInfo');
    const { t } = useI18n();

    const id = ref('');

    // 变量 对象
    const state = reactive({
        // 区域 loading
        loading: false,
        // 查询条件
        formLine: {
            appName: ''
        },
        // 表格的 配置信息
        tableConfig: {
            columns: [
                { title: computed(() => t('序号')), type: 'index', width: 60, fixed: 'left' },
                { title: computed(() => t('名称')), key: 'appName' },
                { title: computed(() => t('密钥')), key: 'appKey' },
                // { title: computed(() => t('权限')), key: 'apiIds' },
                { title: computed(() => t('白名单')), key: 'ipAddr' },
                { title: computed(() => t('备注')), key: 'remark' },
                { title: computed(() => t('状态')), key: 'status', slot: 'status' },
                { title: computed(() => t('更新时间')), key: 'updateTime', width: settingStore.getDatetimeSpan },
                {
                    title: computed(() => t('操作')),
                    fixed: 'right',
                    width: '220px',
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
                                        id.value = row.appName;
                                        Object.assign(dialogConfig.value, {
                                            show: true,
                                            width: '47%',
                                            title: computed(() => t('添加权限')),
                                            showFooter: false
                                        });
                                    }
                                },
                                [
                                    h('i', { class: 'ri-add-line', style: { marginRight: '2px' } }),
                                    h('span', t('授权'))
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
                                        editClick(row);
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
                                        ElMessageBox.confirm(`${t('是否删除')}【${row.appName}】?`, t('提示'), {
                                            confirmButtonText: t('确定'),
                                            cancelButtonText: t('取消'),
                                            type: 'info'
                                        })
                                        .then(async () => {
                                            // 请求 移除 接口函数---
                                            loading.value = true;
                                            let result = await deleteApiRoleData(row.appName);
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
            border: false,
            headerBackground: true,
            tableData: [],
            pageConfig: {
                currentPage: 1,
                pageSize: getStoragePageSize('apiRoleConfig', 15),
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
                    key: 'appName',
                    label: computed(() => t('名称')),
                    span: 6,
                    clearable: true
                },
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
        }
    });

    let {
        loading,
        formLine,
        tableConfig,
        filterConfig,
        dialogConfig,
    } = toRefs(state);

    onMounted(() => {
        searchData();
    });

    const close = () => {
        dialogConfig.value.show = false;
        tableConfig.value.pageConfig.currentPage = 1;
        searchData();
    };

    // 请求 列表接口
    async function searchData() {
        let params = {
            page: tableConfig.value.pageConfig.currentPage,
            size: tableConfig.value.pageConfig.pageSize,
            appName: formLine.value.appName == null ? '' : formLine.value.appName
        };
        let res = await searchRolePage(params);
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

    let oldName = ref('');
    // 菜单表单ref
    const ruleFormRef = ref();
    // 菜单 表单
    let formConfig = ref({
        model: {},
        rules: {
            appName: [{ required: true, message: computed(() => t('请输入调用者名称')), trigger: 'blur' }]
        },
        itemList: [
            {
                type: 'input',
                prop: 'appName',
                label: computed(() => t('名称（英文）')),
                required: true
            },
            // {
            //     type: 'input',
            //     prop: 'apiIds',
            //     label: computed(() => t('接口权限'))
            // },
            {
                type: 'input',
                prop: 'ipAddr',
                label: computed(() => t('白名单IP'))
            },
            {
                type: 'textarea',
                prop: 'remark',
                label: computed(() => t('描述')),
                rows: 3
            },
            {
                type: 'radio',
                prop: 'status',
                label: computed(() => t('状态')),
                props: {
                    options: [
                        {
                            value: 0,
                            label: '正常'
                        },
                        {
                            value: 1,
                            label: '禁用'
                        }
                    ]
                }
            },
        ],
        descriptionsFormConfig: {
            labelWidth: '200px',
            labelAlign: 'center'
        }
    });

    // 增加菜单 弹框的变量配置 控制
    let addDialogConfig = ref({
        show: false,
        title: computed(() => t('信息')),
        width: '40%',
        onOk: (newConfig) => {
            return new Promise<void>(async (resolve, reject) => {
                const ruleFormInstance = ruleFormRef.value?.elFormRef;
                await ruleFormInstance.validate(async (valid) => {
                    if (valid) {
                        if(oldName.value != '' && oldName.value != ruleFormRef.value.model.appName) {
                            ElNotification({
                                title: '失败',
                                message: "名称不能修改，只能删除重建",
                                type: 'error',
                                duration: 2000,
                                offset: 80
                            });
                            reject();
                        }else {
                            let params = {
                                ...ruleFormRef.value.model
                            };

                            let result = await saveApiRoleData(params);
                            if (result.success) {
                                searchData();
                            }
                            ElNotification({
                                title: result.success ? t('成功') : t('失败'),
                                message: result.msg,
                                type: result.success ? 'success' : 'error',
                                duration: 2000,
                                offset: 80
                            });
                            resolve();
                        }
                    } else {
                        reject();
                    }
                });
            });
        }
    });

    function handleClick() {
        oldName.value = '';
        formConfig.value.model = {};
        formConfig.value.model.status = 0;
        addDialogConfig.value.show = true;
    }

    function editClick(row) {
        oldName.value = row.appName;
        formConfig.value.model = row;
        addDialogConfig.value.show = true;
    }
</script>
<style lang="scss" scoped>

</style>
