<template>
    <!-- 表格 -->
    <y9Table 
        :config="tableConfig" 
        :filterConfig="filterConfig"
        uniqueIdent="apiLogConfig" 
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
    </y9Table>
</template>

<script lang="ts" setup>
    import { computed, h, inject, onMounted, reactive, ref, toRefs, watch } from 'vue';
    import { useI18n } from 'vue-i18n';
    import { useSettingStore } from '@/store/modules/settingStore';
    import { searchLogPage } from '@/api/apiService';
    import settings from '@/settings';
    import router from '@/router';
    import { getStoragePageSize } from '@/utils';

    const settingStore = useSettingStore();
    // 注入 字体对象
    const fontSizeObj: any = inject('sizeObjInfo');
    const { t } = useI18n();

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
                { title: computed(() => t('调用者')), key: 'appName' },
                { title: computed(() => t('请求地址')), key: 'requestUrl' },
                { title: computed(() => t('请求者IP')), key: 'hostIp' },
                { title: computed(() => t('接口服务器IP')), key: 'serverIp' },
                { title: computed(() => t('请求参数')), key: 'requestParams' },
                { title: computed(() => t('请求结果')), key: 'result' },
                { title: computed(() => t('请求时间')), key: 'updateTime', width: settingStore.getDatetimeSpan }
            ],
            border: false,
            headerBackground: true,
            tableData: [],
            pageConfig: {
                currentPage: 1,
                pageSize: getStoragePageSize('apiLogConfig', 15),
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
                    label: computed(() => t('调用者名称')),
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
    });

    let {
        loading,
        formLine,
        tableConfig,
        filterConfig
    } = toRefs(state);

    onMounted(() => {
        searchData();
    });

    // 请求 列表接口
    async function searchData() {
        let params = {
            page: tableConfig.value.pageConfig.currentPage,
            size: tableConfig.value.pageConfig.pageSize,
            appName: formLine.value.appName == null ? '' : formLine.value.appName
        };
        let res = await searchLogPage(params);
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
</script>
<style lang="scss" scoped>

</style>
