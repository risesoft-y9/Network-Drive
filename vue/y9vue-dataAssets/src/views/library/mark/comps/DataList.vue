<template>
    <y9Card :title="`${$t('数据列表')} - ${currTreeNodeInfo.name ? currTreeNodeInfo.name : ''}`">
        <template v-slot>
            <!-- 表格 -->
            <y9Table :config="tableConfig" :filterConfig="filterConfig">
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
                <template v-slot:statusSelect>
                    <div class="label">资产状态</div>
                    <el-select v-model="state.formLine.status" clearable>
                        <el-option key="0" label="下架" value="0" />
                        <el-option key="1" label="上架" value="1" />
                    </el-select>
                </template>
                <template #shareType="{ row, column, index }">
                    <el-tag v-if="row.shareType == 0">不予共享</el-tag>
                    <el-tag v-if="row.shareType == 1">有条件共享</el-tag>
                    <el-tag v-if="row.shareType == 2">无条件共享</el-tag>
                </template>
                <template #status="{ row, column, index }">
                    <el-tag :type="row.status=='1'? 'success':'danger'">{{ row.status == '1' ? '已上架' : '未上架' }}</el-tag>
                </template>
            </y9Table>
            <!-- 制造loading效果 -->
            <el-button v-loading.fullscreen.lock="loading" style="display: none"></el-button>
        </template>
    </y9Card>
    <y9Dialog v-model:config="dialogConfig">
        <Transfer 
            v-if="dialogConfig.show"
            :entity="entity" 
            @close="close"
        ></Transfer>
    </y9Dialog>
</template>

<script lang="ts" setup>
    import { computed, h, inject, onMounted, reactive, ref, toRefs, watch } from 'vue';
    import { useI18n } from 'vue-i18n';
    import { useSettingStore } from '@/store/modules/settingStore';
    import y9_storage from '@/utils/storage';
    import { getStoragePageSize } from '@/utils';
    import { deleteDataAssets, searchPage } from '@/api/pretreat';
    import type { UploadInstance } from 'element-plus';
    import settings from '@/settings';
    import router from '@/router';
    import Transfer from './transfer.vue';

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

    let entity = ref(null);

    // 变量 对象
    const state = reactive({
        // 区域 loading
        loading: false,
        // 查询条件
        formLine: {
            name: '',
            code: '',
            status: ''
        },
        // 表格的 配置信息
        tableConfig: {
            columns: [
                { title: computed(() => t('序号')), type: 'index', width: 60, fixed: 'left' },
                { title: computed(() => t('资产名称')), key: 'name', width: 150, fixed: 'left' },
                { title: computed(() => t('资产编码')), key: 'code', width: 150 },
                { title: computed(() => t('资产摘要')), key: 'remark' },
                { title: computed(() => t('标注信息')), key: 'labelData' },
                { title: computed(() => t('上架状态')), key: 'status', slot: 'status', width: 100},
                {
                    title: computed(() => t('操作')),
                    fixed: 'right',
                    width: '120px',
                    render: (row) => {
                        let editActions = [
                            h(
                                'span',
                                {
                                    style: {
                                        cursor: 'pointer'
                                    },
                                    class: 'global-btn-second',
                                    onClick: async () => {
                                        entity.value = row;
                                        Object.assign(dialogConfig.value, {
                                            show: true,
                                            width: '47%',
                                            title: computed(() => t('标注详情')),
                                            showFooter: false
                                        });
                                    }
                                },
                                [
                                    h('i', { class: 'ri-add-line', style: { marginRight: '2px' } }),
                                    h('span', t('标注'))
                                ]
                            ),
                        ];
                        return h('span', editActions);
                    }
                }
            ],
            tableData: [],
            pageConfig: {
                currentPage: 1,
                pageSize: getStoragePageSize('fileConfig', 15),
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
                    span: settingStore.device === 'mobile' ? 24 : 6,
                    clearable: true
                },
                {
                    type: 'input',
                    value: '',
                    key: 'code',
                    label: computed(() => t('资产编码')),
                    span: settingStore.device === 'mobile' ? 24 : 6,
                    clearable: true
                },
                {
                    type: 'slot',
                    value: '',
                    slotName: 'statusSelect',
                    span: 6
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
    });

    let {
        loading,
        tableConfig,
        filterConfig,
        formLine,
        dialogConfig,
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

    const close = () => {
        dialogConfig.value.show = false;
        tableConfig.value.pageConfig.currentPage = 1;
        searchData();
    };

    // 请求 列表接口
    async function searchData() {
        let params = {
            categoryId: props.currTreeNodeInfo.id,
            page: tableConfig.value.pageConfig.currentPage,
            size: tableConfig.value.pageConfig.pageSize,
            name: formLine.value.name,
            code: formLine.value.code,
            status: formLine.value.status
        };
        let res = await searchPage(params);
        if (res.code == 0) {
            // 对返回的接口数据进行赋值与处理
            tableConfig.value.tableData = res.rows;
            tableConfig.value.pageConfig.total = res.total;
        }
    }
</script>
<style lang="scss" scoped>
    .label {
        margin-right: 10px;
        color: #606266;
        font-size: inherit;

        display: flex;
        align-items: center;
    }
</style>
