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
            @close="close"
            :type="3"
        ></Info>
    </y9Dialog>
</template>

<script lang="ts" setup>
    import { computed, h, inject, onMounted, reactive, ref, toRefs } from 'vue';
    import { useI18n } from 'vue-i18n';
    import { useSettingStore } from '@/store/modules/settingStore';
    import { getStoragePageSize } from '@/utils';
    import { searchSubscribePage } from '@/api/pretreat';
    import Info from '@/views/subscribe/comps/Info.vue';

    const settingStore = useSettingStore();
    // 注入 字体对象
    const fontSizeObj: any = inject('sizeObjInfo');
    const { t } = useI18n();

    let assetsId = ref('');// 资产id
    let subscribeId = ref('');// 订阅id
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
                { title: computed(() => t('用途说明')), key: 'purpose' },
                { title: computed(() => t('提供方式')), key: 'provideType' },
                { title: computed(() => t('审核状态')), key: 'reviewStatus', slot: 'reviewStatus' },
                { title: computed(() => t('备注')), key: 'reason' },
                { title: computed(() => t('订阅人')), key: 'userName' },
                { title: computed(() => t('订阅时间')), key: 'createTime', width: settingStore.getDatetimeSpan },
                { title: computed(() => t('审核时间')), key: 'reviewDate' },
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
                                    h('i', { class: 'ri-edit-circle-line' }),
                                    h('span', t('审核'))
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
        }
    });

    let {
        formLine,
        tableConfig,
        filterConfig,
        dialogConfig,
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
            name: formLine.value.name == null ? '' : formLine.value.name,
            type: 'admin'
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
</script>
<style lang="scss" scoped>
    @import '@/theme/global.scss';
    @import '@/theme/global-vars.scss';
</style>
