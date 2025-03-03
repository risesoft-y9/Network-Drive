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
                <template #opt_button="{ row, column, index }">
                    <span @click="handleView(row)"><i class="ri-eye-line"></i>查看</span>
                    <el-dropdown>
                        <span class="el-dropdown-link">挂接
                            <el-icon class="el-icon--right">
                                <arrow-down/>
                            </el-icon>
                        </span>
                        <template #dropdown>
                            <el-dropdown-menu>
                                <el-dropdown-item @click="handle(row, 1)" v-if="row.mountType == '数据' || !row.mountType">数据</el-dropdown-item>
                                <el-dropdown-item @click="handle(row, 2)" v-if="row.mountType == '文件' || !row.mountType">文件</el-dropdown-item>
                                <el-dropdown-item @click="handle(row, 3)" v-if="row.mountType == '接口' || !row.mountType">接口</el-dropdown-item>
                            </el-dropdown-menu>
                        </template>
                    </el-dropdown>
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
    <y9Dialog v-model:config="dialogConfig2">
        <el-upload
            ref="uploadRef"
            class="upload-demo"
            :http-request="customUpload"
            multiple=true
            :auto-upload="false"
        >
            <template #trigger>
                <el-button type="primary">选择文件</el-button>
            </template>

            <span style="color: red;margin-left: 15px;">(可多选)</span>

            <el-button class="ml-3" type="success" @click="submitUpload">上传</el-button>
        </el-upload>
    </y9Dialog>
    <y9Dialog v-model:config="dialogConfig3">
        <el-tree-select
            v-model="checkInterfaceData"
            :data="interfaceTreeData"
            multiple
            :render-after-expand="false"
        />
        <el-button class="ml-4" type="primary" @click="submitSave">提交</el-button>
    </y9Dialog>
    <y9Dialog v-model:config="dialogConfig4">
        <el-tree-select
            v-model="checkTableData"
            :data="tableTreeData"
            multiple
            filterable
            check-strictly
            :render-after-expand="false"
        />
        <el-button class="ml-4" type="primary" @click="submitSave2">提交</el-button>
    </y9Dialog>
</template>

<script lang="ts" setup>
    import { computed, h, inject, onMounted, reactive, ref, toRefs, watch } from 'vue';
    import { ElMessage, ElMessageBox, ElNotification } from 'element-plus';
    import { useI18n } from 'vue-i18n';
    import { useSettingStore } from '@/store/modules/settingStore';
    import y9_storage from '@/utils/storage';
    import { getStoragePageSize } from '@/utils';
    import { getSelectTree, getTableSelectTree, saveAssetsInterface, saveAssetsTable, searchPage } from '@/api/pretreat';
    import type { UploadInstance } from 'element-plus';
    import settings from '@/settings';
    import router from '@/router';
    import Details from '../comps/details.vue';

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
                { title: computed(() => t('挂接类型')), key: 'mountType', width: 100},
                { title: '操作', width: 180, slot: 'opt_button' }
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
        },
        dialogConfig2: {
            show: false,
            title: ''
        },
        dialogConfig3: {
            show: false,
            title: ''
        },
        dialogConfig4: {
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
        dialogConfig2,
        dialogConfig3,
        dialogConfig4
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

    let assetsId = ref('');
    const handle = (row, type) => {
        assetsId.value = row.id;
        if(type == 3) {
            initTreeData();
            Object.assign(dialogConfig3.value, {
                show: true,
                width: '30%',
                title: computed(() => t('接口列表')),
                showFooter: false
            });
        } else if(type == 2) {
            Object.assign(dialogConfig2.value, {
                show: true,
                width: '40%',
                title: computed(() => t('文件上传')),
                showFooter: false
            });
        } else if(type == 1) {
            initTableTreeData();
            Object.assign(dialogConfig4.value, {
                show: true,
                width: '30%',
                title: computed(() => t('数据列表')),
                showFooter: false
            });
        }
    }

    // 文件上传
    const uploadRef = ref<UploadInstance>();
    const submitUpload = () => {
        uploadRef.value!.submit();
    }

    function customUpload(content) {
        let uploadUrl = import.meta.env.VUE_APP_CONTEXT + `vue/detail/fileUpload?assetsId=` + assetsId.value;
        const formData = new FormData();
        formData.append('file', content.file); // 添加文件到FormData对象中
 
        // 使用 fetch API 发送请求，并设置 headers
        fetch(uploadUrl, {
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + y9_storage.getObjectItem(settings.siteTokenKey, 'access_token'),
            },
            body: formData,
        })
        .then(response => response.json()) // 服务器返回 JSON 格式的数据
        .then(data => {
            //console.log('上传成功:', data);
            if (data.success) {
                dialogConfig2.value.show = false;
                searchData();
            }
            ElNotification({
                title: data.success ? t('成功') : t('失败'),
                message: data.msg,
                type: data.success ? 'success' : 'error',
                duration: 2000,
                offset: 80
            });
        })
        .catch(error => {
            console.error('上传失败:', error);
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

    // 获取接口列表
    let checkInterfaceData = ref('');
    let interfaceTreeData = ref([]);
    async function initTreeData() {
        checkInterfaceData.value = '';
        interfaceTreeData.value = [];
        await getSelectTree().then((res) => {
            if(res.success) {
                interfaceTreeData.value = res.data;
            }
        });
    }

    const submitSave = () => {
        //console.log(checkInterfaceData.value);
        saveAssetsInterface(checkInterfaceData.value, assetsId.value).then((res) => {
            if(res.success) {
                dialogConfig3.value.show = false;
                searchData();
            }
            ElNotification({
                title: res.success ? t('成功') : t('失败'),
                message: res.msg,
                type: res.success ? 'success' : 'error',
                duration: 2000,
                offset: 80
            });
        });
    }

    // 获取数据表列表
    let checkTableData = ref('');
    let tableTreeData = ref([]);
    async function initTableTreeData() {
        checkTableData.value = '';
        tableTreeData.value = [];
        await getTableSelectTree().then((res) => {
            if(res.success) {
                tableTreeData.value = res.data;
            }
        });
    }

    const submitSave2 = () => {
        saveAssetsTable(checkTableData.value, assetsId.value).then((res) => {
            if(res.success) {
                dialogConfig4.value.show = false;
                searchData();
            }
            ElNotification({
                title: res.success ? t('成功') : t('失败'),
                message: res.msg,
                type: res.success ? 'success' : 'error',
                duration: 2000,
                offset: 80
            });
        });
    }
</script>
<style lang="scss" scoped>
.ml-3 {
    float: right;
}

.ml-4 {
    float: right;
    margin-top: 15px;
}

.el-dropdown-link {
    cursor: pointer;
    color: var(--el-color-primary);
    line-height: 23px;
    margin-left: 10px;
    display: flex;
    align-items: center;
}
</style>
