<template>
    <!-- 表格 -->
    <y9Table 
        :config="tableConfig" 
        :filterConfig="filterConfig"
        uniqueIdent="myConfig" 
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
        </template>
        <template #reviewStatus="{ row, column, index }">
            <el-tag type="primary">{{ row.reviewStatus }}</el-tag>
        </template>
    </y9Table>

    <y9Dialog v-model:config="dialogConfig">
        <Info 
            v-if="dialogConfig.show"
            :id="assetsId"
            :subscribeId="subscribeId"
            :type="2"
            @close="close"
        ></Info>
    </y9Dialog>
    <y9Dialog v-model:config="dialogConfig2">
        <ItemData
            v-if="dialogConfig2.show"
            :assetsId="assetsId"
        ></ItemData>
    </y9Dialog>
    <y9Dialog v-model:config="dialogConfig3">
        <y9Form ref="ruleFormRef" :config="formConfig"></y9Form>
    </y9Dialog>
</template>

<script lang="ts" setup>
    import { computed, h, inject, onMounted, reactive, ref, toRefs, watch } from 'vue';
    import { useI18n } from 'vue-i18n';
    import { useSettingStore } from '@/store/modules/settingStore';
    import { getStoragePageSize } from '@/utils';
    import { getSubscribeBaseById, saveSubscribeBase, searchSubscribePage } from '@/api/pretreat';
    import Info from '@/views/subscribe/comps/Info.vue';
    import ItemData from '@/views/pretreat/comps/ItemData.vue';
    import { ElNotification } from 'element-plus';

    const settingStore = useSettingStore();
    // 注入 字体对象
    const fontSizeObj: any = inject('sizeObjInfo');
    const { t } = useI18n();

    let assetsId = ref('');
    let subscribeId = ref('');
    // 变量 对象
    const state = reactive({
        // 查询条件
        formLine: {
            name: ''
        },
        // 表格的 配置信息
        tableConfig: {
            loading: false,
            columns: [
                { title: computed(() => t('序号')), type: 'index', width: 60, fixed: 'left' },
                { title: computed(() => t('资产名称')), key: 'assetsName' },
                { title: computed(() => t('所属目录')), key: 'catalog' },
                // { title: computed(() => t('用途说明')), key: 'purpose' },
                { title: computed(() => t('提供方式')), key: 'provideType' },
                { title: computed(() => t('审核状态')), key: 'reviewStatus', slot: 'reviewStatus' },
                // { title: computed(() => t('备注')), key: 'reason' },
                { title: computed(() => t('审核人')), key: 'reviewName' },
                { title: computed(() => t('订阅时间')), key: 'createTime', width: settingStore.getDatetimeSpan },
                { title: computed(() => t('审核时间')), key: 'reviewDate' },
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
                                        assetsId.value = row.assetsId;
                                        subscribeId.value = row.id;
                                        Object.assign(dialogConfig.value, {
                                            show: true,
                                            width: '60%',
                                            title: computed(() => t('详情')),
                                            showFooter: false
                                        });
                                    }
                                },
                                [
                                    h('i', { class: 'ri-eye-line', style: { marginRight: '2px' } }),
                                    h('span', t('详情'))
                                ]
                            )
                        ];
                        if(row.reviewStatus == '通过') {
                            if(row.provideType == '文件下载') {
                                editActions.push(
                                    h(
                                        'span',
                                        {
                                            style: {
                                                cursor: 'pointer',
                                                marginLeft: '10px',
                                        },
                                        class: 'global-btn-success',
                                        onClick: async () => {
                                            assetsId.value = row.assetsId;
                                            Object.assign(dialogConfig2.value, {
                                                show: true,
                                                width: '60%',
                                                title: computed(() => t('下载列表')),
                                                showFooter: false
                                            });
                                        }
                                    },
                                    [
                                        h('i', { class: 'ri-download-2-line', style: { marginRight: '2px' } }),
                                        h('span', t('下载'))
                                    ]
                                ))
                            }
                            if(row.provideType == '在线查看') {
                                editActions.push(
                                    h(
                                        'span',
                                        {
                                            style: {
                                                cursor: 'pointer',
                                                marginLeft: '10px',
                                        },
                                        class: 'global-btn-success',
                                        onClick: async () => {
                                            assetsId.value = row.assetsId;
                                            Object.assign(dialogConfig2.value, {
                                                show: true,
                                                width: '60%',
                                                title: computed(() => t('在线预览')),
                                                showFooter: false
                                            });
                                        }
                                    },
                                    [
                                        h('i', { class: 'ri-window-line', style: { marginRight: '2px' } }),
                                        h('span', t('预览'))
                                    ]
                                ))
                            }
                            if(row.provideType == '接口请求') {
                                editActions.push(
                                    h(
                                        'span',
                                        {
                                            style: {
                                                cursor: 'pointer',
                                                marginLeft: '10px',
                                        },
                                        class: 'global-btn-success',
                                        onClick: async () => {
                                            assetsId.value = row.assetsId;
                                            Object.assign(dialogConfig2.value, {
                                                show: true,
                                                width: '60%',
                                                title: computed(() => t('接口详情')),
                                                showFooter: false
                                            });
                                        }
                                    },
                                    [
                                        h('i', { class: 'ri-link', style: { marginRight: '2px' } }),
                                        h('span', t('接口'))
                                    ]
                                ))
                            }
                            if(row.provideType == '库表推送') {
                                editActions.push(
                                    h(
                                        'span',
                                        {
                                            style: {
                                                cursor: 'pointer',
                                                marginLeft: '10px',
                                        },
                                        class: 'global-btn-success',
                                        onClick: async () => {
                                            subscribeId.value = row.id;
                                            await getSubscribeBase();
                                            Object.assign(dialogConfig3.value, {
                                                show: true
                                            });
                                        }
                                    },
                                    [
                                        h('i', { class: 'ri-edit-box-line', style: { marginRight: '2px' } }),
                                        h('span', t('填报'))
                                    ]
                                ))
                            }
                        }
                        if(row.id) {
                            return h('span', editActions);
                        } else {
                            return '';
                        }
                    }
                }
            ],
            border: false,
            headerBackground: true,
            tableData: [],
            pageConfig: {
                currentPage: 1,
                pageSize: getStoragePageSize('myConfig', 15),
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
                    key: 'name',
                    label: computed(() => t('资产名称')),
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
        },
        dialogConfig2: {
            show: false,
            title: ''
        }
    });

    let {
        formLine,
        tableConfig,
        filterConfig,
        dialogConfig,
        dialogConfig2,
    } = toRefs(state);

    onMounted(() => {
        searchData();
    });

    // 请求 列表接口
    async function searchData() {
        tableConfig.value.loading = true;
        let params = {
            page: tableConfig.value.pageConfig.currentPage,
            size: tableConfig.value.pageConfig.pageSize,
            name: formLine.value.name == null ? '' : formLine.value.name
        };
        let res = await searchSubscribePage(params);
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

    const close = () => {
        dialogConfig.value.show = false;
        searchData();
    };

    // 菜单表单ref
    const ruleFormRef = ref();
    // 菜单 表单
    let formConfig = ref({
        model: {},
        rules: {
            url: [{ required: true, message: computed(() => t('请输入数据库连接信息')), trigger: 'blur' }],
            username: [{ required: true, message: computed(() => t('请输入用户名')), trigger: 'blur' }],
            password: [{ required: true, message: computed(() => t('请输入密码')), trigger: 'blur' }],
            remark: [{ required: true, message: computed(() => t('请输入备注')), trigger: 'blur' }],
        },
        itemList: [
            {
                type: 'input',
                prop: 'url',
                label: computed(() => t('连接信息')),
                required: true,
                props: {
                    placeholder: `例：jdbc:mysql://{host}:{port}/{database}`
                }
            },
            {
                type: 'input',
                prop: 'username',
                label: computed(() => t('用户名')),
                required: true,
            },
            {
                type: 'input',
                prop: 'password',
                label: computed(() => t('密码')),
                required: true,
                props: {
                    type: 'password',
                    showPassword: true
                }
            },
            {
                type: 'textarea',
                prop: 'remark',
                label: computed(() => t('备注')),
                rows: 3,
                required: true,
                props: {
                    placeholder: `填写数据推送的详细信息，如表名称、需要的数据信息等`
                }
            }
        ],
        descriptionsFormConfig: {
            labelWidth: '200px',
            labelAlign: 'center'
        }
    });

    // 增加菜单 弹框的变量配置 控制
    let dialogConfig3 = ref({
        show: false,
        title: computed(() => t('库表信息')),
        width: '40%',
        onOk: (config) => {
            return new Promise<void>(async (resolve, reject) => {
                const ruleFormInstance = ruleFormRef.value?.elFormRef;
                await ruleFormInstance.validate(async (valid) => {
                    if (valid) {
                        let params = {
                            ...ruleFormRef.value.model,
                            subscribeId: subscribeId.value
                        };

                        let result = await saveSubscribeBase(params);
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
                    } else {
                        reject();
                    }
                });
            });
        }
    });

    // 根据id获取订阅-库表推送信息
    async function getSubscribeBase() {
        let result = await getSubscribeBaseById(subscribeId.value);
        if (result.success) {
            formConfig.value.model = result.data || {};
        }
    }
</script>
<style lang="scss" scoped>
    @import '@/theme/global.scss';
    @import '@/theme/global-vars.scss';
</style>
