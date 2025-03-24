<template>
    <y9Card :title="`${$t('API列表')} - ${currTreeNodeInfo.name ? currTreeNodeInfo.name : ''}`">
        <template v-slot>
            <!-- 表格 -->
            <y9Table 
                :config="tableConfig" 
                :filterConfig="filterConfig"
                uniqueIdent="apiConfig" 
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
                        @click="addApi()"
                    >
                        <i class="ri-add-line" />
                        {{ $t('新增') }}
                    </el-button>
                </template>
                <template #apiType="{ row }">
                    {{ row.apiType == 1 ? '私有' : '公开' }}
                </template>
            </y9Table>
            <!-- 制造loading效果 -->
            <el-button v-loading.fullscreen.lock="loading" style="display: none"></el-button>
        </template>
    </y9Card>
    <y9Dialog v-model:config="dialogConfig">
        <FormData 
            v-if="dialogConfig.show" 
            :parentId="currTreeNodeInfo.id" 
            :disabled="disabled" 
            :entity="entity" 
            @close="close"
        ></FormData>
    </y9Dialog>
    <y9Dialog v-model:config="dialogConfig2">
        <ApiInfo 
            v-if="dialogConfig2.show"
            :id="entity.id"
        ></ApiInfo>
    </y9Dialog>
</template>

<script lang="ts" setup>
    import { computed, h, inject, onMounted, reactive, ref, toRefs, watch } from 'vue';
    import { ElMessage, ElMessageBox, ElNotification } from 'element-plus';
    import { useI18n } from 'vue-i18n';
    import { useSettingStore } from '@/store/modules/settingStore';
    import y9_storage from '@/utils/storage';
    import { getStoragePageSize } from '@/utils';
    import { deleteApiData, searchPage } from '@/api/apiService';
    import settings from '@/settings';
    import router from '@/router';
    import FormData from './FormData.vue';
    import ApiInfo from './ApiInfo.vue';

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
        // 查询条件
        formLine: {
            apiName: ''
        },
        // 表格的 配置信息
        tableConfig: {
            columns: [
                { title: computed(() => t('序号')), type: 'index', width: 60, fixed: 'left' },
                { title: computed(() => t('名称')), key: 'apiName' },
                { title: computed(() => t('方法名')), key: 'apiUrl' },
                { title: computed(() => t('接口类型')), key: 'apiType', slot: 'apiType' },
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
                                        apiInfo(row);
                                    }
                                },
                                [
                                    h('i', { class: 'ri-eye-line', style: { marginRight: '2px' } }),
                                    h('span', t('查看'))
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
                                        editApi(row);
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
                                            let result = await deleteApiData(row.id);
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
                pageSize: getStoragePageSize('apiConfig', 15),
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
                    key: 'apiName',
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
    // 监听系统id 当发生改变时重新请求数据
    watch(
        () => props.currTreeNodeInfo.id,
        async (new_, old_) => {
            if (new_ && new_ !== old_) {
                searchData();
            }
        }
    );

    let entity = ref(null);
    let disabled = ref(false);
    function addApi() {
        disabled.value = false;
        entity.value = null;
        Object.assign(dialogConfig.value, {
            show: true,
            width: '60%',
            title: computed(() => t('新建API')),
            showFooter: false
        });
    }

    // 请求 列表接口
    async function searchData() {
        let params = {
            parentId: props.currTreeNodeInfo.id,
            page: tableConfig.value.pageConfig.currentPage,
            size: tableConfig.value.pageConfig.pageSize,
            apiName: formLine.value.apiName,
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

    const close = (type) => {
        dialogConfig.value.show = false;
        if(type == 1) {
            tableConfig.value.pageConfig.currentPage = 1;
            searchData();
        }
    };

    function editApi(row) {
        disabled.value = false;
        entity.value = row;
        Object.assign(dialogConfig.value, {
            show: true,
            width: '60%',
            title: computed(() => t('编辑API')),
            showFooter: false
        });
    }

    function apiInfo(row) {
        entity.value = row;
        Object.assign(dialogConfig2.value, {
            show: true,
            width: '55%',
            title: computed(() => t('接口详情')),
            showFooter: false
        });
    }

</script>
<style lang="scss" scoped>

</style>
