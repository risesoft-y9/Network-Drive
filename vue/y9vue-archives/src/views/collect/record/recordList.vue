<template>
    <y9Card :showHeader="false">
        <div class="toolbar">
            <div class="toolbar-left">
                <el-form :inline="true" @submit.native.prevent>
                    <el-form-item :label="$t('档号')" style="width: 150px">
                        <el-input
                            v-model="userName"
                            :placeholder="$t('输入档号搜索')"
                            @keyup.enter.native="loadList"
                            class="search-input global-btn-second"
                            clearable
                        >
                        </el-input>
                    </el-form-item>
                    <el-form-item :label="$t('题名')">
                        <el-input
                            v-model="userName"
                            :placeholder="$t('输入题名搜索')"
                            @keyup.enter.native="loadList"
                            class="search-input global-btn-second"
                            clearable
                        >
                        </el-input>
                    </el-form-item>
                </el-form>
            </div>

            <div class="toolbar-right">
                <el-button
                    :size="fontSizeObj.buttonSize"
                    :style="{ fontSize: fontSizeObj.baseFontSize }"
                    class="global-btn-main"
                    @click="loadList"
                    ><i class="ri-search-line"></i>{{ $t('查询') }}</el-button
                >
                <el-button
                    :size="fontSizeObj.buttonSize"
                    :style="{ fontSize: fontSizeObj.baseFontSize }"
                    class="global-btn-second"
                    @click="loadList"
                    ><i class="ri-refresh-line"></i>{{ $t('重置') }}</el-button
                >
                <el-button
                    :size="fontSizeObj.buttonSize"
                    :style="{ fontSize: fontSizeObj.baseFontSize }"
                    class="global-btn-second"
                    @click="loadList"
                    ><i class="ri-search-line"></i>{{ $t('高级搜索') }}</el-button
                >
            </div>
        </div>
        <el-row :gutter="30" style="padding: 0px 10px 10px 0px">
            <el-col :span="3">
                <el-dropdown ref="dropdownIsShowRef" :hide-on-click="false" :max-height="dropdown_height">
                    <span class="el-dropdown-link">
                        <el-button
                            class="global-btn-main"
                            :size="fontSizeObj.buttonSize"
                            :style="{ fontSize: fontSizeObj.baseFontSize }"
                            ><i class="ri-eye-line"></i>{{ $t('显示/隐藏列') }}&nbsp;&nbsp;<i
                                class="ri-arrow-down-s-line"
                            ></i
                        ></el-button>
                    </span>
                    <template #dropdown>
                        <el-dropdown-menu>
                            <!-- <el-dropdown-item>
                                <el-checkbox
                                    v-model="checkAll"
                                    :indeterminate="isIndeterminate"
                                    @change="handleCheckAllChange"
                                >
                                    全选/取消
                                </el-checkbox>
                            </el-dropdown-item> -->
                            <template v-for="(item,index) in metadataFieldList" :key="item.id">
                                <el-dropdown-item style="overflow-y: auto;">
                                    <el-checkbox
                                        v-model="fieldChecked[index]"
                                        label="Option 1"
                                        true-value="1"
                                        false-value="0"
                                        :checked="item.isListShow"
                                        size="large"
                                        @change="(val) =>selectShowOrHide(val, item.id)"
                                        >{{ $t(item.disPlayName) }}</el-checkbox
                                    >
                                </el-dropdown-item>
                            </template>
                            <el-dropdown-item>
                                <el-button
                                    :size="fontSizeObj.buttonSize"
                                    :style="{ fontSize: fontSizeObj.baseFontSize }"
                                    class="global-btn-main"
                                    @click="saveChecked"
                                    ><i class="ri-save-line"></i>{{ $t('保存') }}</el-button
                                >
                            </el-dropdown-item>
                        </el-dropdown-menu>
                    </template>
                </el-dropdown>
            </el-col>
            <el-col :span="2">
                <el-button
                    class="global-btn-main"
                    :size="fontSizeObj.buttonSize"
                    :style="{ fontSize: fontSizeObj.baseFontSize }"
                    style="width: 90px"
                    @click="addRecord"
                    ><i class="ri-file-add-line"></i>{{ $t('新增') }}
                    </el-button>
                <!-- <el-dropdown>
                    <span class="el-dropdown-link">
                        <el-button
                            class="global-btn-main"
                            :size="fontSizeObj.buttonSize"
                            :style="{ fontSize: fontSizeObj.baseFontSize }"
                            ><i class="ri-file-add-line"></i>{{ $t('新增') }}&nbsp;&nbsp;<i
                                class="ri-arrow-down-s-line"
                            ></i
                        ></el-button>
                    </span>
                    <template #dropdown>
                        <el-dropdown-menu>
                            <el-dropdown-item>
                                <el-checkbox
                                    v-model="checked1"
                                    label="Option 1"
                                    size="large"
                                    @click.native="changeOrder($event, 'FILE_NAME')"
                                    >{{ $t('文件名') }}</el-checkbox
                                >
                            </el-dropdown-item>
                        </el-dropdown-menu>
                    </template>
                </el-dropdown> -->
            </el-col>
            <el-col :span="6">
                <el-dropdown :hide-on-click="false" :max-height="dropdown_height">
                    <span class="el-dropdown-link">
                        <el-button
                            class="global-btn-main"
                            :size="fontSizeObj.buttonSize"
                            :style="{ fontSize: fontSizeObj.baseFontSize }"
                            ><i class="ri-list-settings-line"></i>{{ $t('批量操作') }}&nbsp;&nbsp;<i
                                class="ri-arrow-down-s-line"
                            ></i
                        ></el-button>
                    </span>
                    <template #dropdown>
                        <el-dropdown-menu>
                            <el-dropdown-item>
                                <span
                                    :size="fontSizeObj.buttonSize"
                                    :style="{ fontSize: fontSizeObj.baseFontSize }"
                                    ><i class="ri-delete-bin-2-line"></i>{{ $t('删除') }}</span>
                            </el-dropdown-item>
                        </el-dropdown-menu>
                    </template>
                </el-dropdown>
            </el-col>
            <el-col :span="6"><div class="grid-content ep-bg-purple" /></el-col>
        </el-row>
        <el-drawer
            class="drawerSearch"
            title=""
            :visible.sync="drawer"
            :direction="direction"
            :with-header="false"
            :modal="false"
            :modal-append-to-body="false"
            size="30%"
            >
            <div style="margin: 8px;">
                <el-form ref="searchForm" label-position="right" label-width="120px" :inline="true" :inline-message="true" :status-icon="true">
                <el-form-item v-for="(item,index) in searchConf" :label="(item.labelName == undefined || item.labelName == '' || item.labelName == null) ? item.disPlayName : item.labelName" :key="index">
                    <template v-if="item.inputBoxType == 'input' || item.inputBoxType == 'textarea'">
                    <el-input v-model="searchValue[index]" clearable></el-input>
                    </template>
                    <template v-if="item.inputBoxType == 'select'">
                    <el-select v-model="searchValue[index]" :placeholder="`请选择` + ((item.labelName == undefined || item.labelName == '' || item.labelName == null) ? item.disPlayName : item.labelName)" clearable>
                        <el-option
                            v-for="(option,index) in item.optionClass"
                            :label="option.label == undefined ? option.value : option.label"
                            :value="option.value"
                            :key="index"
                        />
                    </el-select>
                    </template>
                    <template v-else>
                    <el-input v-model="searchValue[index]" clearable></el-input> 
                    </template>
                </el-form-item>
                </el-form>  
            </div>
            <div style="text-align: center;margin: 15px 10px 10px;">
                    <span slot="footer" class="dialog-footer">
                    <el-button type="primary" size="small" @click="searchList">查询</el-button>
                    <el-button type="primary" size="small" @click="resetSearch">重置</el-button>
                    <el-button size="small" @click="drawer = false">取消</el-button>
                    </span>
                </div>
            </el-drawer>
        <y9Table
            :config="dataTableConfig"
            @on-curr-page-change="onCurrPageChange"
            @on-page-size-change="onPageSizeChange"
        >
            <template #optButton="{ row, column, index }">
                <span style="font-weight: 600" @click="editRecord(row)"><i class="ri-edit-line"></i>编辑</span>
            </template>
        </y9Table>
        <y9Dialog v-model:config="dialogConfig">
            <detail ref="detailRef" v-if="dialogConfig.type == 'addRecord'" :formData="formData" :metadataFieldList="metadataFieldList"/>
        </y9Dialog> 
    </y9Card>
</template>

<script lang="ts" setup>
    import { $deepAssignObject } from '@/utils/object.ts';
    import { useSettingStore } from '@/store/modules/settingStore';
    import { getMetadataFieldList,saveListFiledShow } from '@/api/archives/metadata';
    import { getArchivesRecordList,saveFormData} from '@/api/archives/record';
    import detail from './detail.vue';

    const props = defineProps({
        currTreeNodeInfo: {
            //当前tree节点信息
            type: Object,
            default: () => {
                return {};
            }
        },
        parentCatalog: String
    });
    const { t } = useI18n();
    const fontSizeObj: any = inject('sizeObjInfo') || {};
    let total = ref(0);
    //调整表格高度适应屏幕
    const tableHeight = ref(useSettingStore().getWindowHeight - 362);

    window.onresize = () => {
        return (() => {
            tableHeight.value = useSettingStore().getWindowHeight - 362;
        })();
    };
    const dropdown_height = ref(useSettingStore().getWindowHeight-400);
    const data = reactive({
        detailRef:'',
        formData:{},
        optType:'',
        //当前节点信息
        currInfo: props.currTreeNodeInfo,
        dataTableConfig: {
            columns: [],
            tableData: [],
            pageConfig: {
                // 分页配置，false隐藏分页
                currentPage: 1, //当前页数，支持 v-model 双向绑定
                pageSize: 20, //每页显示条目个数，支持 v-model 双向绑定
                total: total.value //总条目数
            },
            height: tableHeight.value
        },
        //弹窗配置
        dialogConfig: {
            show: false,
            title: '',
            onOkLoading: true,
            onOk: (newConfig) => {
                return new Promise(async (resolve, reject) => {
                    const fieldInstance = detailRef.value.detailFormRef;
                    fieldInstance.validate(async (valid) => {
                        if (valid) {
                            let res = { success: false, msg: '' };
                            let isAllEmpty = detailRef.value.checkIfAllEmpty();
                            if(isAllEmpty){
                                ElNotification({
                                    title: '失败',
                                    message: "请输入或者填写内容",
                                    type: 'error',
                                    duration: 2000,
                                    offset: 80
                                });
                                reject();
                                return;
                            }
                            let formData = detailRef.value.detailForm;
                            res = await saveFormData(optType.value,props.currTreeNodeInfo.id,JSON.stringify(formData));
                            if (res == undefined) {
                                reject();
                                return;
                            }
                            ElNotification({
                                title: res.success ? '成功' : '失败',
                                message: res.msg,
                                type: res.success ? 'success' : 'error',
                                duration: 2000,
                                offset: 80
                            });
                            if (res.success) {
                                getRecordList();
                            }
                            resolve();
                        } else {
                            ElMessage({
                                type: 'error',
                                message: '验证不通过，请检查',
                                offset: 65
                            });
                            reject();
                        }
                    });
                });
            },
            visibleChange: (visible) => {
                // console.log('visible',visible)
            }
        },
        metadataFieldList: [],//元数据字段列表
        fieldChecked: [],
        selectField: [],
        dropdownIsShowRef: ''
    });

    let { detailRef,formData,optType,currInfo, dataTableConfig,dialogConfig,metadataFieldList,fieldChecked,selectField,dropdownIsShowRef } = toRefs(data);

    watch(
        () => props.currTreeNodeInfo,
        (newVal, oldVal) => {
            currInfo.value = $deepAssignObject(currInfo.value, newVal);
            getRecordList();
            getMetadataField();
        },
        { deep: true }
    );

    onMounted(() => {
       
        getRecordList();
    });

    async function getMetadataField() {
        dataTableConfig.value.columns = [{
				type: "selection",
				width: 60,
			}];
       let res = await getMetadataFieldList(props.currTreeNodeInfo.id);
       if (res.success) {
            metadataFieldList.value = res.data;
            for(let item of metadataFieldList.value){
                if(item.isListShow==1){
                    dataTableConfig.value.columns.push({
                        title: computed(() => t(item.disPlayName)),
                        key: item.columnName,
                        width: item.disPlayWidth,
                        sortable: item.isOrder==1?true:false,
                        align: item.disPlayAlign
                    });
                }
                
            }

            dataTableConfig.value.columns.push({
                title: computed(() => t('操作')),
                key: 'optButton',
                width: '150',
                align: 'center',
                fixed: 'right',
                slot: 'optButton'
            });
            getRecordList();
       }
    }

    function selectShowOrHide(val,id){
        if(selectField.value.length == 0){
            selectField.value.push({id:id,isShow:val});
        }else{
            let has = false;
            for (let index = 0; index < selectField.value.length; index++) {
                if(selectField.value[index].id == id){
                    selectField.value[index].isShow = val;
                    has = true;
                }
            }
            if(!has){
                selectField.value.push({id:id,isShow:val});
            }
        }
    }

    async function getRecordList() {
             dataTableConfig.value.tableData = [];
            let page = dataTableConfig.value.pageConfig.currentPage;
            let rows = dataTableConfig.value.pageConfig.pageSize;
            let res = await getArchivesRecordList(
                props.currTreeNodeInfo.id,
                page,
                rows
            );
            if (res.success) {
                metadataFieldList.value.forEach((item) => {
                    if(item.dataType == 'Date' || item.re_inputBoxType == 'date' || item.re_inputBoxType == 'dateTime'){
                        res.rows.forEach((row) => {
                            row[item.columnName] = convertTimestamp(row[item.columnName],item.re_inputBoxType);
                        });
                    }
                })
                console.log('rowaaa',res.rows);
                dataTableConfig.value.tableData = res.rows;
                dataTableConfig.value.pageConfig.total = res.total;
            }
    }
    
    function convertTimestamp(timestamp,timeType){
        if(timestamp){
            const date = new Date(Number(timestamp));
            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const day = String(date.getDate()).padStart(2, '0');
            if (timeType == 'date'){
                return `${year}-${month}-${day}`;
            } else {
                const hours = String(date.getHours()).padStart(2, '0');
                const minutes = String(date.getMinutes()).padStart(2, '0');
                const seconds = String(date.getSeconds()).padStart(2, '0');
                return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
            }
        }
    }

    //当前页改变时触发
    function onCurrPageChange(currPage) {
        dataTableConfig.value.pageConfig.currentPage = currPage;
        getRecordList();
    }

    //每页条数改变时触发
    function onPageSizeChange(pageSize) {
        dataTableConfig.value.pageConfig.pageSize = pageSize;
        getRecordList();
    }

    function addRecord() {
        optType.value = 'add';
        Object.assign(dialogConfig.value, {
            show: true,
            width: '50%',
            type: 'addRecord',
            title: '新增文件',
            showFooter: true,
            margin: '2vh auto'
        });
    }

    function editRecord(row){
        formData.value = row;
        optType.value = 'edit';
        Object.assign(dialogConfig.value, {
            show: true,
            width: '50%',
            type: 'addRecord',
            title: '修改文件',
            showFooter: true,
            margin: '2vh auto'
        });
    }

    async function saveChecked(){
        console.log('jsonStr',JSON.stringify(selectField.value));
        let res = await saveListFiledShow(JSON.stringify(selectField.value));
        ElNotification({
            title: res.success ? '成功' : '失败',
            message: res.msg,
            type: res.success ? 'success' : 'error',
            duration: 2000,
            offset: 80
        });
        if(res.success){
            dropdownIsShowRef.value.handleClose();
            getMetadataField();
        }
    }

</script>

<style lang="scss" scoped>
    @import '../../../theme/global.scss';
    .toolbar:after {
        clear: both;
        content: '';
        display: block;
    }

    .toolbar-left {
        float: left;
    }

    .toolbar-right {
        float: right;
        :deep(.el-button) {
            height: 30px;
            line-height: 0;
            min-width: 0px;
            box-shadow: 0px 0px 0px 0px rgb(0 0 0 / 6%);
        }
    }

</style>
