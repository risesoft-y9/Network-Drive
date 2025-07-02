<template>
    <!-- 表格 -->
    <y9Table 
        :config="tableConfig" 
        :filterConfig="filterConfig"
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
        <template #baseSelect>
            <div class="label">数据库</div>
            <el-tree-select
                v-model="checkTableData"
                :data="tableTreeData"
                filterable
                check-strictly
                :render-after-expand="false"
            />
        </template>
    </y9Table>
    <!-- 制造loading效果 -->
    <el-button v-loading.fullscreen.lock="loading" style="display: none"></el-button>

    <y9Dialog v-model:config="dialogConfig">
        <TableView 
            v-if="dialogConfig.show"
            :currNode="currNode" 
            @close="close"
        ></TableView>
    </y9Dialog>
</template>

<script lang="ts" setup>
    import { computed, h, inject, onMounted, reactive, ref, toRefs, watch } from 'vue';
    import { useI18n } from 'vue-i18n';
    import { useSettingStore } from '@/store/modules/settingStore';
    import settings from '@/settings';
    import router from '@/router';
    import TableView from './comps/tableData.vue';
    import { getTableSelectTree } from '@/api/pretreat';
    import { getTablesByBaseId } from '@/api/dataSource';

    const settingStore = useSettingStore();
    // 注入 字体对象
    const fontSizeObj: any = inject('sizeObjInfo');
    const { t } = useI18n();

    const currNode = ref(null);

    // 变量 对象
    const state = reactive({
        // 区域 loading
        loading: false,
        // 查询条件
        formLine: {
            name: ''
        },
        // 表格的 配置信息
        tableConfig: {
            columns: [
                { title: computed(() => t('序号')), type: 'index', width: 60, fixed: 'left' },
                { title: computed(() => t('名称')), key: 'name' },
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
                                        currNode.value = row;
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
                                    h('span', t('查看'))
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
            pageConfig: false
        },
        // 表格过滤
        filterConfig: {
            filtersValueCallBack: (filters) => {
                formLine.value = filters;
            },
            showBorder: true,
            itemList: [
                {
                    type: 'slot',
                    value: '',
                    slotName: 'baseSelect',
                    span: 6
                },
                {
                    type: 'input',
                    value: '',
                    key: 'name',
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
        initTableTreeData();
        searchData();
    });

    // 获取数据表列表
    let checkTableData = ref('');
    let tableTreeData = ref([]);
    async function initTableTreeData() {
        checkTableData.value = '';
        tableTreeData.value = [];
        await getTableSelectTree('base').then((res) => {
            if(res.success) {
                tableTreeData.value = res.data;
            }
        });
    }

    // 请求 列表接口
    async function searchData() {
        let params = {
            id: checkTableData.value.replace("s-", ""),
            name: formLine.value.name == null ? '' : formLine.value.name
        };
        let res = await getTablesByBaseId(params);
        if (res.code == 0) {
            // 对返回的接口数据进行赋值与处理
            tableConfig.value.tableData = res.data;
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
