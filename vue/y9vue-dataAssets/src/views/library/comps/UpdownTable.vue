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
                <template #status="{ row, column, index }">
                    <el-tag :type="row.status=='1'? 'success':'danger'">{{ row.status == '1' ? '已上架' : '未上架' }}</el-tag>
                </template>
                <template #opt_button="{ row, column, index }">
                    <span @click="handleView(row)" style="margin-right: 10px;"><i class="ri-eye-line"></i>查看</span>
                    <span @click="handle(row)">
                        <i :class="row.status == '1'? 'ri-arrow-down-line':'ri-arrow-up-line'"></i>{{ row.status == '1' ? '下架':'上架' }}
                    </span>
                </template>
            </y9Table>
            <!-- 制造loading效果 -->
            <el-button v-loading.fullscreen.lock="loading" style="display: none"></el-button>
        </template>
    </y9Card>
    <y9Dialog v-model:config="dialogConfig">
        <Details 
            v-if="dialogConfig.show" 
            :disabled="disabled" 
            :entity="entity"
        ></Details>
    </y9Dialog>
</template>

<script lang="ts" setup>
    import { computed, h, inject, onMounted, reactive, ref, toRefs, watch } from 'vue';
    import { ElMessage, ElMessageBox, ElNotification } from 'element-plus';
    import { useI18n } from 'vue-i18n';
    import { useSettingStore } from '@/store/modules/settingStore';
    import y9_storage from '@/utils/storage';
    import { getStoragePageSize } from '@/utils';
    import { searchPage, updownData } from '@/api/pretreat';
    import settings from '@/settings';
    import router from '@/router';
    import Details from '@/views/pretreat/comps/details.vue';

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
            code: ''
        },
        // 表格的 配置信息
        tableConfig: {
            columns: [
                { title: computed(() => t('序号')), type: 'index', width: 60, fixed: 'left' },
                { title: computed(() => t('资产编码')), key: 'code' },
                { title: computed(() => t('资产名称')), key: 'name' },
                { title: computed(() => t('上架状态')), key: 'status', slot: 'status', width: 100},
                { title: '操作', width: 160, slot: 'opt_button' }
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

    let disabled = ref(false);
    const handleView = (row) => {
        entity.value = row;
        disabled.value = true;
        Object.assign(dialogConfig.value, {
            show: true,
            width: '60%',
            title: computed(() => t('详情')),
            showFooter: false
        });
    }

    const handle = (row) => {
        let text = row.status == '1' ? '下架':'上架';
        ElMessageBox.confirm(`${t('是否'+text)}【${row.name}】?`, t('提示'), {
            confirmButtonText: t('确定'),
            cancelButtonText: t('取消'),
            type: 'info'
        })
        .then(async () => {
            let result = await updownData(row.id);
            ElNotification({
                title: result.success ? t('成功') : t('失败'),
                message: result.msg,
                type: result.success ? 'success' : 'error',
                duration: 2000,
                offset: 80
            });
            // 重新请求 列表数据
            if(result.success){
                searchData();
            }
        })
        .catch((e) => {
            ElMessage({
                type: 'info',
                message: t('已取消操作'),
                offset: 65
            });
        });
    }

    // 请求 列表接口
    async function searchData() {
        let params = {
            categoryId: props.currTreeNodeInfo.id,
            page: tableConfig.value.pageConfig.currentPage,
            size: tableConfig.value.pageConfig.pageSize,
            name: formLine.value.name,
            code: formLine.value.code
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

</style>
