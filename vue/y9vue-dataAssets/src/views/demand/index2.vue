<template>
    <!-- 表格 -->
    <y9Table
        :config="tableConfig"
        :filterConfig="filterConfig"
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
            <span @click="handleExamine(row)"><i class="ri-edit-circle-line"></i>审核</span>
            <span @click="deleteData(row)"><i class="ri-delete-bin-line" style="margin-left: 10px;"></i>删除</span>
        </template>
    </y9Table>
    <el-button v-loading.fullscreen.lock="loading" style="display: none"></el-button>
    <y9Dialog v-model:config="dialogConfig">
        <FormView
            v-if="dialogConfig.show"
            :type="type"
            :entity="entity"
            @close="close"
        ></FormView>
    </y9Dialog>
</template>
<script lang="ts" setup>
    import { reactive, toRefs, ref, onMounted, inject, computed } from 'vue';
    import { ElMessage, ElMessageBox, ElNotification } from 'element-plus';
    import { useI18n } from 'vue-i18n';
    import { useSettingStore } from '@/store/modules/settingStore';
    import { deleteById, searchDemandPage } from '@/api/demand';
    import FormView from './XQFormData.vue';

    const { t } = useI18n();
    const settingStore = useSettingStore();

    // 注入 字体对象
    const fontSizeObj: any = inject('sizeObjInfo');

    let entity = ref({});

    // 变量 对象
    const state = reactive({
        // 区域 loading
        loading: false,
        // 查询条件
        formLine: {
            title: ''
        },
        // 表格的 配置信息
        tableConfig: {
            columns: [
                { title: computed(() => t('序号')), type: 'index', width: 60, fixed: 'left' },
                { title: computed(() => t('标题')), key: 'title' },
                { title: computed(() => t('内容')), key: 'description' },
                { title: computed(() => t('申请人')), key: 'publisher', width: 100 },
                { title: computed(() => t('申请时间')), key: 'createTime', width: settingStore.getDatetimeSpan },
                { title: computed(() => t('审核状态')), key: 'examineStatus', width: 100, formatter: (row) => {
                    return row.feedback != '' ? '已处理' : '未处理';
                } },
                { title: computed(() => t('操作')), fixed: 'right', width: 200, slot: 'opt_button' }
            ],
            loading: false,
            border: false,
            headerBackground: true,
            tableData: [],
            pageConfig: {
                currentPage: 1,
                pageSize: 15,
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
                    key: 'title',
                    label: computed(() => t('标题')),
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
        dialogConfig
    } = toRefs(state);

    onMounted(() => {
        searchData();
    });

    let type = ref('');
    const handleExamine = (row) => {
        type.value = 'examine';
        entity.value = row;
        Object.assign(dialogConfig.value, {
            show: true,
            width: '60%',
            title: computed(() => t('审核')),
            showFooter: false
        });
    }

    const deleteData = (row) => {
        ElMessageBox.confirm(`${t('是否删除')}【${row.title}】?`, t('提示'), {
            confirmButtonText: t('确定'),
            cancelButtonText: t('取消'),
            type: 'info'
        })
        .then(async () => {
            loading.value = true;
            let result = await deleteById(row.id);
            ElNotification({
                title: result.success ? t('成功') : t('失败'),
                message: result.msg,
                type: result.success ? 'success' : 'error',
                duration: 2000,
                offset: 80
            });
            loading.value = false;
            // 重新请求 列表数据
            if(result.success){
                searchData();
            }
        })
        .catch((e) => {
            loading.value = false;
            ElMessage({
                type: 'info',
                message: t('已取消删除'),
                offset: 65
            });
        });
    }

    const close = () => {
        dialogConfig.value.show = false;
        tableConfig.value.pageConfig.currentPage = 1;
        searchData();
    };

    // 请求 列表接口
    async function searchData() {
        tableConfig.value.loading = true;
        let params = {
            page: tableConfig.value.pageConfig.currentPage,
            size: tableConfig.value.pageConfig.pageSize,
            searchText: formLine.value.title,
            pageType: '1'
        };
        let res = await searchDemandPage(params);
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
</script>
<style lang="scss" scoped>
    
</style>
