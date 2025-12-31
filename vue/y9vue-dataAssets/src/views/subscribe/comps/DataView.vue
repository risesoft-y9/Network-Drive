<template>
    <y9Card :title="`${$t('数据列表')} - ${currTreeNodeInfo.name ? currTreeNodeInfo.name : ''}`">
        <template v-slot>
            <!-- 表格 -->
            <y9Table 
                :config="tableConfig" 
                :filterConfig="filterConfig"
                uniqueIdent="dataConfig" 
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
                <template #opt_button="{ row, column, index }">
                    <span @click="handleView(row)" v-if="row.tag != '不予共享'"><i class="ri-eye-line"></i>订阅</span>
                </template>
            </y9Table>
        </template>
    </y9Card>
    <y9Dialog v-model:config="dialogConfig">
        <Info 
            v-if="dialogConfig.show"
            :id="id"
            @close="close"
            :type="1"
        ></Info>
    </y9Dialog>
</template>

<script lang="ts" setup>
    import { computed, h, inject, onMounted, reactive, ref, toRefs, watch } from 'vue';
    import { useI18n } from 'vue-i18n';
    import { useSettingStore } from '@/store/modules/settingStore';
    import { getStoragePageSize } from '@/utils';
    import { searchPage2 } from '@/api/pretreat';
    import Info from './Info.vue';

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
        // 查询条件
        formLine: {
            name: '',
        },
        // 表格的 配置信息
        tableConfig: {
            loading: false,
            columns: [
                { title: computed(() => t('序号')), type: 'index', width: 60, fixed: 'left' },
                { title: computed(() => t('资产名称')), key: 'title' },
                { title: computed(() => t('资产编码')), key: 'code' },
                { title: computed(() => t('资产摘要')), key: 'description' },
                { title: computed(() => t('数据类型')), key: 'mountType', width: 100 },
                { title: computed(() => t('共享类型')), key: 'tag', width: 100 },
                { title: computed(() => t('操作')), fixed: 'right', width: 100, slot: 'opt_button' }
            ],
            tableData: [],
            pageConfig: {
                currentPage: 1,
                pageSize: getStoragePageSize('dataConfig', 15),
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
                    label: computed(() => t('搜索内容')),
                    span: settingStore.device === 'mobile' ? 24 : 6,
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
        tableConfig,
        filterConfig,
        formLine,
        dialogConfig
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
        tableConfig.value.loading = true;
        let params = {
            categoryId: props.currTreeNodeInfo.id,
            page: tableConfig.value.pageConfig.currentPage,
            size: tableConfig.value.pageConfig.pageSize,
            searchText: formLine.value.name
        };
        let res = await searchPage2(params);
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

    let id = ref(null);
    const handleView = (row) => {
        id.value = row.id;
        Object.assign(dialogConfig.value, {
            show: true,
            width: '60%',
            title: computed(() => t('详情')),
            showFooter: false
        });
    }
</script>
<style lang="scss" scoped>

</style>
