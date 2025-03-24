<template>
    <el-tabs type="border-card">
        <el-tab-pane label="基本信息">
            <FormData
                :disabled="disabled" 
                :entity="entity"
            ></FormData>
        </el-tab-pane>
        <el-tab-pane label="数据项">
            <y9Table 
                :config="tableConfig" 
                :filterConfig="filterConfig"
                uniqueIdent="fileConfig" 
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
                    <span @click="downloadFile(row.id)" v-if="row.fileType == '文件'"><i class="ri-arrow-down-circle-line"></i>下载</span>
                    <span @click="showPage(row)" v-else><i class="ri-eye-line"></i>查看</span>
                    <span @click="deleteData(row)" v-if="entity.dataState == 'out'" style="margin-left: 10px;">
                        <i class="ri-delete-bin-line"></i>删除
                    </span>
                </template>
            </y9Table>
        </el-tab-pane>
    </el-tabs>
    <y9Dialog v-model:config="dialogConfig">
        <ApiDetail 
            v-if="dialogConfig.show"
            :identifier="identifier"
        ></ApiDetail>
    </y9Dialog>
    <y9Dialog v-model:config="dialogConfig2">
        <TableData 
            v-if="dialogConfig2.show"
            :currNode="currNode"
        ></TableData>
    </y9Dialog>
    <!-- 制造loading效果 -->
    <el-button v-loading.fullscreen.lock="loading" style="display: none"></el-button>
</template>
<script lang="ts" setup>
import { deleteMountData, getFilePage } from '@/api/pretreat';
import { useSettingStore } from '@/store/modules/settingStore';
import { getStoragePageSize } from '@/utils';
import { computed, inject, onMounted, reactive, ref, toRefs } from 'vue';
import { useI18n } from 'vue-i18n';
import y9_storage from '@/utils/storage';
import settings from '@/settings';
import FormData from '../comps/FormData.vue';
import ApiDetail from '@/views/test/detail.vue';
import TableData from '@/views/dataobject/comps/tableData.vue';
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus';

const settingStore = useSettingStore();
// 注入 字体对象
const fontSizeObj: any = inject('sizeObjInfo');
const { t } = useI18n();

const emits = defineEmits(['close']);

const props = defineProps({
    entity: {
        type: Object,
        default: () => null
    },
    disabled: {
        type: Boolean,
        required: false,
        default: () => {
            return false;
        }
    }
});

// 变量 对象
const state = reactive({
    // 区域 loading
    loading: false,
    // 查询条件
    formLine: {
        name: '',
    },
    // 表格的 配置信息
    tableConfig: {
        columns: [
            { title: computed(() => t('序号')), type: 'index', width: 60, fixed: 'left' },
            { title: computed(() => t('名称')), key: 'name' },
            { title: computed(() => t('类型')), key: 'fileType'},
            { title: '操作', width: 140, slot: 'opt_button' }
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
                label: computed(() => t('名称')),
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
    },
    dialogConfig2: {
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
    dialogConfig2
} = toRefs(state);

onMounted(() => {
    searchData();
});

// 请求 列表接口
async function searchData() {
    let params = {
        id: props.entity.id,
        page: tableConfig.value.pageConfig.currentPage,
        size: tableConfig.value.pageConfig.pageSize,
        name: formLine.value.name,
    };
    let res = await getFilePage(params);
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

function downloadFile(id) {
    const url = import.meta.env.VUE_APP_CONTEXT + 'vue/detail/fileDownload?id=' + id 
        + "&access_token=" + y9_storage.getObjectItem(settings.siteTokenKey, 'access_token');
    window.open(url);
}

let identifier = ref('');
let currNode = ref({'name': '', 'url': ''});
function showPage(row) {
    if(row.fileType == '接口') {
        identifier.value = row.identifier
        Object.assign(dialogConfig.value, {
            show: true,
            width: '60%',
            title: computed(() => t('接口详情')),
            showFooter: false
        });
    } else if(row.fileType == '数据表') {
        currNode.value.name = row.name;
        currNode.value.url = row.identifier;
        Object.assign(dialogConfig2.value, {
            show: true,
            width: '60%',
            title: computed(() => t('数据详情')),
            showFooter: false
        });
    } else if(row.fileType == '数据库') {
        ElMessage({
            type: 'info',
            message: t('数据库类型，请前往数据源管理—>数据对象处查看'),
            offset: 65
        });
    }
}

const deleteData = (row) => {
    ElMessageBox.confirm(`${t('是否删除')}【${row.name}】?`, t('提示'), {
        confirmButtonText: t('确定'),
        cancelButtonText: t('取消'),
        type: 'info'
    })
    .then(async () => {
        loading.value = true;
        let result = await deleteMountData(row.id);
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
            emits('close');
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
</script>