<template>
    <div
        v-loading="loading"
        element-loading-background="rgba(0, 0, 0, 0.8)"
        element-loading-spinner="el-icon-loading"
        element-loading-text="正在处理中"
        style="margin: 8px"
    >
        <table border="0" cellpadding="0" cellspacing="1" class="layui-table" lay-skin="line row">
            <tbody>
                <tr>
                    <td class="lefttd" style="width: 15%">表英文名称<font style="color: red">*</font></td>
                    <td class="rigthtd">
                        <el-row>
                            <el-col :span="7"
                                ><font style="margin-left: 10px; color: red">{{ prefix }}</font></el-col
                            >
                            <el-col :span="17"
                                ><el-input v-model="table.tableName" :disabled="isEdit"></el-input
                            ></el-col>
                        </el-row>
                    </td>
                    <td class="lefttd" style="width: 15%">表中文名称<font style="color: red">*</font></td>
                    <td class="rigthtd">
                        <el-input v-model="table.tableCnName" style="text-align: center"></el-input>
                    </td>
                </tr>
                <tr>
                    <td class="lefttd">门类标识</td>
                    <td class="rigthtd" style="text-align: center">
                        <span>{{ table.categoryMark }}</span>
                    </td>
                    <td class="lefttd">所属门类</td>
                    <td class="rigthtd" style="text-align: center">
                        <span>{{ table.categoryName }}</span>
                    </td>
                </tr>
                <tr>
                    <td class="lefttd">备注</td>
                    <td class="rigthtd" colspan="3">
                        <el-input
                            v-model="table.tableMemo"
                            :rows="4"
                            placeholder="请输入内容"
                            type="textarea"
                        ></el-input>
                    </td>
                </tr>
            </tbody>
        </table>
        <el-divider>表字段</el-divider>
        <div style="margin-bottom: 15px">
            <el-button type="primary" @click="addField"><i class="ri-add-line"></i><span>字段</span></el-button>
        </div>
        <el-table :data="fieldList" border height="300" style="width: 100%">
            <el-table-column align="center" label="序号" type="index" width="60"></el-table-column>
            <el-table-column align="center" label="中文名称" prop="fieldCnName">
                <template #default="fieldCnName_cell">
                    <span>
                        <font v-if="fieldCnName_cell.row.state == 0" color="red" title="数据库表中不存在此字段">!</font>
                        {{ fieldCnName_cell.row.fieldCnName }}
                    </span>
                </template>
            </el-table-column>
            <el-table-column align="center" label="字段名称" prop="fieldName" width="auto"></el-table-column>
            <el-table-column align="center" label="字段类型" prop="fieldType" width="150"></el-table-column>
            <el-table-column align="center" label="是否允许为空" prop="isMayNull" width="130">
                <template #default="isMayNull_cell">
                    <font v-if="isMayNull_cell.row.isMayNull == 0">非空</font>
                    <font v-if="isMayNull_cell.row.isMayNull == 1">空</font>
                </template>
            </el-table-column>
            <el-table-column align="center" label="操作" prop="optcell" width="100">
                <template #default="scope">
                    <template v-if="!(scope.row.opt == 'false' || scope.row.fieldName == 'id')">
                        <i
                            class="ri-edit-line"
                            style="margin-right: 15px; font-size: 18px"
                            title="编辑"
                            @click="editField(scope)"
                        ></i>
                        <i class="ri-delete-bin-line" style="font-size: 18px" title="删除" @click="delField(scope)"></i>
                    </template>
                </template>
            </el-table-column>
        </el-table>
        <div slot="footer" class="dialog-footer" style="text-align: center; margin-top: 15px">
            <!-- <el-button type="primary" @click="saveY9Table"><i class="ri-save-line"></i><span>保存</span></el-button> -->
            <el-button type="primary" @click="buildY9Table"
                ><i class="ri-send-plane-line"></i><span>新生成表</span>
            </el-button>
            <el-button type="primary" @click="updateY9Table"
                ><i class="ri-edit-box-line"></i><span>修改表结构</span>
            </el-button>
            <el-button type="primary" @click="deleteY9Table"
                ><i class="ri-delete-bin-line"></i><span>删除表</span>
            </el-button>
            <el-button type="primary" @click="dialogConfig_parent.show = false"
                ><i class="ri-close-line"></i><span>关闭</span>
            </el-button>
        </div>
    </div>
    <y9Dialog v-model:config="dialogConfig">
        <newOrModifyField
            ref="newOrModifyFieldRef"
            :fieldId="fieldId"
            :fieldList="fieldList"
            :pushField="pushTableField"
            :tableId="table.id"
            :updateField="updateField"
        />
    </y9Dialog>
</template>

<script lang="ts" setup>
    import { onMounted } from 'vue';
    import newOrModifyField from './newOrModifyField.vue';
    import {
        categoryIsExistTable,
        buildTable,
        checkTableExist,
        getTable,
        getTableFieldList,
        saveTable,
        updateTable,
        removeTable,
        deleteField
    } from '@/api/archives/category';
    import { ElNotification } from 'element-plus';

    const props = defineProps({
        categoryData: {
            type: Object,
            default: () => {
                return {};
            }
        },
        dialogConfig_parent: {
            type: Object,
            default: () => {
                return {};
            }
        },
        reloadTable: Function
    });
    const data = reactive({
        loading: false,
        title: '',
        table: {},
        tableOldName: '',
        prefix: 'Y9_ARCHIVES_',
        tableNames: '',
        databaseName: '',
        fieldList: [],
        newOrModifyFieldRef: '',
        isEdit: false,
        //弹窗配置
        dialogConfig: {
            show: false,
            title: '',
            onOkLoading: true,
            onOk: (newConfig) => {
                return new Promise(async (resolve, reject) => {
                    let valid = await newOrModifyFieldRef.value.validForm();
                    if (!valid) {
                        reject();
                        return;
                    }
                    let res = await newOrModifyFieldRef.value.saveOrModifyField();
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
                    if (res.success && res.type == undefined) {
                        let res1 = await getTableFieldList(table.value.id);
                        fieldList.value = res1.data;
                    }
                    resolve();
                });
            },
            visibleChange: (visible) => {
                // console.log('visible',visible)
            }
        },
        updateField: null,
        fieldId: '',
        editIndex: ''
    });
    let {
        loading,
        title,
        table,
        tableOldName,
        prefix,
        tableNames,
        databaseName,
        fieldList,
        newOrModifyFieldRef,
        isEdit,
        dialogConfig,
        updateField,
        fieldId,
        editIndex
    } = toRefs(data);

    onMounted(() => {
        initTable();
    });

    async function initTable() {
        fieldList.value = [];
        tableOldName.value = '';
        table.value.tableName = '';
        loading.value = true;

        let check = await categoryIsExistTable(props.categoryData.categoryMark);
        if (check.success) {
            loading.value = false;
            if (check.data) {
                isEdit.value = true;
                tableOldName.value = check.data.tableName;
                let result = await getTableFieldList(check.data.id);
                fieldList.value = result.data;
                let res = await getTable(check.data.id);
                if (res.success) {
                    table.value = res.data.categoryTable;
                    if (
                        table.value.tableName.indexOf('y9_archives_') > -1 ||
                        table.value.tableName.indexOf('Y9_ARCHIVES_') > -1
                    ) {
                        prefix.value = table.value.tableName.substring(0, 12);
                        table.value.tableName = table.value.tableName.substring(12);
                    }
                    databaseName.value = res.data.databaseName;
                    tableNames.value = res.data.tableNames;
                }
            } else {
                table.value = {
                    id: '',
                    categoryName: props.categoryData.categoryName,
                    categoryMark: props.categoryData.categoryMark
                };
                let type = 'varchar';
                if (databaseName.value == 'oracle' || 'kingbase' == databaseName.value || 'dm' == databaseName.value) {
                    type = 'VARCHAR2';
                }
                let field = {
                    id: '',
                    state: 0,
                    tableId: '',
                    fieldCnName: '主键',
                    fieldName: 'id',
                    fieldLength: 38,
                    fieldType: type + '(' + 38 + ')',
                    isMayNull: 0,
                    isSystemField: 1,
                    oldFieldName: '',
                    opt: 'false'
                };
                let field1 = {id:"",state:0,tableId:"",fieldCnName:"详情表id",fieldName:"detail_id"
							,fieldLength:50,fieldType:type + "("+50+")",isMayNull:0,isSystemField:1,oldFieldName:"",opt:"false"};
                fieldList.value.push(field);
                fieldList.value.push(field1);
            }
        }
    }

    async function validForm() {
        let tableName = table.value.tableName;
        let tableName0 = prefix.value + table.value.tableName;
        if (!table.value.tableName) {
            ElNotification({
                title: '失败',
                message: '表单验证不通过,请输入表英文名称',
                type: 'error',
                duration: 2000,
                offset: 80
            });
            return false;
        }
        if (!table.value.tableCnName) {
            ElNotification({
                title: '失败',
                message: '表单验证不通过，请输入表中文名称',
                type: 'error',
                duration: 2000,
                offset: 80
            });
            return false;
        }

        let pattern = new RegExp('\\s+');
        let b = pattern.test(tableName);
        if (b) {
            ElNotification({
                title: '失败',
                message: '表英文名称不能包含空格',
                type: 'error',
                duration: 2000,
                offset: 80
            });
            return false;
        }
        pattern = new RegExp('"|\'');
        b = pattern.test(tableName);
        if (b) {
            ElNotification({
                title: '失败',
                message: '表英文名称不能包含单双引号',
                type: 'error',
                duration: 2000,
                offset: 80
            });
            return false;
        }
        pattern = new RegExp('^[a-z|A-Z|_]+');
        b = pattern.test(tableName);
        if (!b) {
            ElNotification({
                title: '失败',
                message: '表英文名称必须以字母开头',
                type: 'error',
                duration: 2000,
                offset: 80
            });
            return false;
        }
        pattern = new RegExp('^[a-z|A-Z|0-9|_|-]+$');
        b = pattern.test(tableName);
        if (!b) {
            ElNotification({
                title: '失败',
                message: '表英文名称必须全为字母或者数字',
                type: 'error',
                duration: 2000,
                offset: 80
            });
            return false;
        }
        let y9TableName = tableNames.value.split(',');
        if (table.value.id == '') {
            //新增时需要验证表名称是否已存在
            for (let i = 0; i < y9TableName.length; i++) {
                if (tableName0 == y9TableName[i]) {
                    ElNotification({
                        title: '失败',
                        message: '表英文名称已存在记录，请修改',
                        type: 'error',
                        duration: 2000,
                        offset: 80
                    });
                    return false;
                }
            }
        } else {
            //修改时，如果修改的名称与旧名称一样则不验证,不一样则需验证表名称是否已存在
            if (tableName0 != tableOldName.value) {
                for (let i = 0; i < y9TableName.length; i++) {
                    if (tableName0 == y9TableName[i]) {
                        ElNotification({
                            title: '失败',
                            message: '表英文名称已存在记录，请修改',
                            type: 'error',
                            duration: 2000,
                            offset: 80
                        });
                        return false;
                    }
                }
            }
        }
        return true;
    }

    async function saveY9Table() {
        let valid = await validForm();
        if (!valid) {
            return;
        }
        let fields = JSON.stringify(fieldList.value).toString();
        table.value.tableName = prefix.value + table.value.tableName;
        let tables = JSON.stringify(table.value).toString();
        loading.value = true;
        let res = await saveTable(tables, fields);
        if (res.success) {
            loading.value = false;
            props.dialogConfig_parent.show = false;
            props.reloadTable();
        } else {
            loading.value = false;
            table.value.tableName = table.value.tableName.substring(8);
        }
        ElNotification({
            title: res.success ? '成功' : '失败',
            message: res.msg,
            type: res.success ? 'success' : 'error',
            duration: 2000,
            offset: 80
        });
    }

    function addField() {
        fieldId.value = '';
        updateField.value = null;
        Object.assign(dialogConfig.value, {
            show: true,
            width: '35%',
            type: 'newOrModify',
            title: '添加字段',
            cancelText: '取消'
        });
    }

    function pushTableField(obj, type) {
        if (type == 'update') {
            fieldList.value.forEach((element, index) => {
                if (index == editIndex.value) {
                    fieldList.value[index] = obj;
                }
            });
        } else {
            fieldList.value.push(obj);
        }
    }

    function editField(scope) {
        editIndex.value = scope.$index;
        fieldId.value = scope.row.id;
        updateField.value = scope.row;
        Object.assign(dialogConfig.value, {
            show: true,
            width: '35%',
            type: 'newOrModify',
            title: '编辑字段',
            cancelText: '取消'
        });
    }

    async function delField(scope) {
        fieldList.value.splice(scope.$index, 1);
        let res = await deleteField(scope.row.id);
        if (res.success) {
            ElNotification({
                title: '成功',
                message: '删除成功',
                type: 'success',
                duration: 2000,
                offset: 80
            });
        }
        console.log('wewewewewewe', fieldList.value);
    }

    async function buildY9Table() {
        let valid = await validForm();
        if (!valid) {
            return;
        }
        let res = await checkTableExist(prefix.value + table.value.tableName);
        let msg = '将创建新的数据库表，继续吗？';
        if (res.data == 'exist') {
            msg = '数据库已存在该表，将删除重新创建，继续吗？';
        }
        ElMessageBox.confirm(msg, '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'info'
        })
            .then(async () => {
                let result = { success: false, msg: '' };
                let fields = JSON.stringify(fieldList.value).toString();
                table.value.tableName = prefix.value + table.value.tableName;
                let tables = JSON.stringify(table.value).toString();
                loading.value = true;
                result = await buildTable(tables, fields);
                ElNotification({
                    title: result.success ? '成功' : '失败',
                    message: result.msg,
                    type: result.success ? 'success' : 'error',
                    duration: 2000,
                    offset: 80
                });
                if (result.success) {
                    props.dialogConfig_parent.show = false;
                    loading.value = false;
                    props.reloadTable();
                } else {
                    loading.value = false;
                    table.value.tableName = table.value.tableName.substring(8);
                }
            })
            .catch(() => {
                ElMessage({
                    type: 'info',
                    message: '已取消操作',
                    offset: 65
                });
            });
    }

    async function updateY9Table() {
        let valid = await validForm();
        if (!valid) {
            return;
        }
        ElMessageBox.confirm('确认修改物理数据库表追加字段吗?', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'info'
        })
            .then(async () => {
                let result = { success: false, msg: '' };
                let fields = JSON.stringify(fieldList.value).toString();
                table.value.tableName = prefix.value + table.value.tableName;
                let tables = JSON.stringify(table.value).toString();
                loading.value = true;
                result = await updateTable(tables, fields);
                ElNotification({
                    title: result.success ? '成功' : '失败',
                    message: result.msg,
                    type: result.success ? 'success' : 'error',
                    duration: 2000,
                    offset: 80
                });
                if (result.success) {
                    props.dialogConfig_parent.show = false;
                    loading.value = false;
                    props.reloadTable();
                } else {
                    loading.value = false;
                    table.value.tableName = table.value.tableName.substring(8);
                }
            })
            .catch(() => {
                ElMessage({
                    type: 'info',
                    message: '已取消操作',
                    offset: 65
                });
            });
    }

    async function deleteY9Table() {
        ElMessageBox.confirm('确认删除该门类创建的数据库表吗?', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'info'
        })
            .then(async () => {
                let result = { success: false, msg: '' };
                result = await removeTable(table.value.id);
                ElNotification({
                    title: result.success ? '成功' : '失败',
                    message: result.msg,
                    type: result.success ? 'success' : 'error',
                    duration: 2000,
                    offset: 80
                });
                if (result.success) {
                    props.dialogConfig_parent.show = false;
                    props.reloadTable();
                }
            })
            .catch(() => {
                ElMessage({
                    type: 'info',
                    message: '已取消操作',
                    offset: 65
                });
            });
    }
</script>
<style lang="scss">
    .newOrModifyTable .el-dialog__body {
        padding-bottom: 10px;
    }

    .newOrModifyTable .el-divider--horizontal {
        margin: 25px 0;
    }
</style>
<style lang="scss" scoped>
    .layui-table {
        width: 100%;
        border-collapse: collapse;
        border-spacing: 0;

        td {
            position: revert;
            padding: 5px 10px;
            min-height: 32px;
            line-height: 32px;
            font-size: 14px;
            border-width: 1px;
            border-style: solid;
            border-color: #e6e6e6;
            display: table-cell;
            vertical-align: inherit;
        }

        .lefttd {
            background: #f5f7fa;
            text-align: center;
            // margin-right: 4px;
            width: 14%;
        }

        .rightd {
            display: flex;
            flex-wrap: wrap;
            word-break: break-all;
            white-space: pre-wrap;
        }
    }
</style>
