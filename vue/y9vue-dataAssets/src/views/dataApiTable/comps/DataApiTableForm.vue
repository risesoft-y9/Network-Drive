<template>
    <div class="data-api-table-form">
        <y9Form ref="ruleRef" :config="formConfig">
            <!-- 数据源选择 -->
            <template #dataSourceId>
                <el-tree-select
                    v-model="formData.dataSourceId"
                    :data="dataSourceList"
                    filterable
                    check-strictly
                    :render-after-expand="false"
                    @change="handleDataSourceChange"
                    :disabled="disabled"
                ></el-tree-select>
            </template>

            <!-- 所属者选择 -->
            <template #owner>
                <el-select
                    v-model="formData.owner"
                    placeholder="请选择接口调用者"
                    clearable
                    filterable
                >
                    <el-option
                        v-for="owner in ownerOptions"
                        :key="owner"
                        :label="owner"
                        :value="owner"
                    ></el-option>
                </el-select>
            </template>
            
            <!-- 表配置区域 -->
            <template #tableConfigs>
                <div>
                    <!-- 添加表按钮 -->
                    <div class="add-table-btn-container" v-if="!disabled && entity == null">
                        <el-button 
                            type="primary" 
                            size="small" 
                            @click="addTableConfig"
                            :disabled="!formData.dataSourceId || tableList.length === 0"
                        >
                            + 添加数据表
                        </el-button>
                    </div>
                    
                    <!-- 表配置列表 - 使用折叠面板 -->
                    <el-collapse v-if="formData.tableConfigs.length > 0" class="table-config-collapse">
                        <el-collapse-item 
                            v-for="(config, index) in formData.tableConfigs" 
                            :key="index"
                            class="table-config-item"
                        >
                            <!-- 表配置头部 - 包含删除按钮 -->
                            <template #title>
                                <div class="table-config-header">
                                    <span class="config-index">表配置 {{ index + 1 }} - {{ config.tableName || '未选择' }}</span>
                                    <el-button 
                                        type="danger" 
                                        size="small" 
                                        @click="removeTableConfig(index)"
                                        v-if="!disabled && entity == null"
                                    >
                                        删除
                                    </el-button>
                                </div>
                            </template>
                            
                            <!-- 表选择 -->
                            <div class="form-item">
                                <label class="form-label required">表名称</label>
                                <el-select
                                    v-model="config.tableName"
                                    :disabled="disabled || !formData.dataSourceId"
                                    placeholder="请选择表"
                                    clearable
                                    filterable
                                    @change="handleTableChange(config, index)"
                                >
                                    <el-option
                                        v-for="table in tableList"
                                        :key="table.name"
                                        :label="table.name + ' (' + table.cname + ')'"
                                        :value="table.name"
                                    ></el-option>
                                </el-select>
                            </div>
                        
                            <!-- 查询字段 -->
                            <div class="form-item">
                                <label class="form-label">查询字段</label>
                                <div v-if="config.tableName && formData.dataSourceId">
                                    <el-scrollbar height="200px" class="field-scrollbar">
                                        <el-checkbox-group 
                                            v-model="config.queryFields" 
                                            :disabled="disabled"
                                            class="field-checkbox-group"
                                        >
                                            <el-checkbox 
                                                v-for="field in getTableFields(config.tableName)" 
                                                :key="field.columnName"
                                                :label="field.columnName"
                                                class="field-checkbox"
                                            >
                                                <div class="field-info">
                                                    <span class="field-name">{{ field.columnName }}</span>
                                                    <span v-if="field.comment" class="field-comment">({{ field.comment }})</span>
                                                    <span class="field-type">{{ field.typeName }}</span>
                                                    <span v-if="field.dataLength" class="field-length">({{ field.dataLength }})</span>
                                                </div>
                                            </el-checkbox>
                                        </el-checkbox-group>
                                    </el-scrollbar>
                                </div>
                                <div v-else class="text-muted">请先选择表</div>
                            </div>
                        
                            <!-- 返回字段 -->
                            <div class="form-item">
                                <label class="form-label required">返回字段</label>
                                <div v-if="config.tableName && formData.dataSourceId">
                                    <el-scrollbar height="200px" class="field-scrollbar">
                                        <el-checkbox-group 
                                            v-model="config.returnFields" 
                                            :disabled="disabled"
                                            class="field-checkbox-group"
                                        >
                                            <el-checkbox 
                                                v-for="field in getTableFields(config.tableName)" 
                                                :key="field.columnName"
                                                :label="field.columnName"
                                                class="field-checkbox"
                                                checked
                                            >
                                                <div class="field-info">
                                                    <span class="field-name">{{ field.columnName }}</span>
                                                    <span v-if="field.comment" class="field-comment">({{ field.comment }})</span>
                                                    <span class="field-type">{{ field.typeName }}</span>
                                                    <span v-if="field.dataLength" class="field-length">({{ field.dataLength }})</span>
                                                </div>
                                            </el-checkbox>
                                        </el-checkbox-group>
                                    </el-scrollbar>
                                </div>
                                <div v-else class="text-muted">请先选择表</div>
                            </div>
                        </el-collapse-item>
                    </el-collapse>
                    
                    <!-- 空状态提示 -->
                    <div v-if="formData.tableConfigs.length === 0" class="empty-tip">
                        请点击"添加数据表"按钮开始配置表信息
                    </div>
                </div>
            </template>
        </y9Form>
        
        <!-- 按钮区域 -->
        <div class="dialog-footer" v-if="!disabled || userRole === 'systemAdmin'">
            <el-button @click="cancel">{{ $t('取消') }}</el-button>
            <el-button type="primary" @click="submitForm">{{ $t('提交') }}</el-button>
        </div>
    </div>
</template>

<script lang="ts" setup>
    import { computed, onMounted, reactive, ref } from 'vue';
    import { ElMessage, ElNotification } from 'element-plus';
    import { useI18n } from 'vue-i18n';
    import { getOwners, getTableForeignKeys, saveDataApiTable } from '@/api/apiService';
    import { getDataBase, getTableColumns, getTablesByBaseId } from '@/api/dataSource';

    const { t } = useI18n();
    
    const props = defineProps({
        entity: {
            type: Object,
            default: null
        },
        disabled: {
            type: Boolean,
            default: false
        }
    });

    const userRole = sessionStorage.getItem('userRole');
    
    const emit = defineEmits(['close']);
    
    const ruleRef = ref();
    
    // 表字段缓存
    const tableFieldsCache = reactive({});
    
    // 表单数据
    const formData = ref({
        id: '',
        dataSourceId: '',
        // 存储多个表的配置，结构：{tableName: string, queryFields: string[], returnFields: string[]}
        tableConfigs: [] as Array<{tableName: string, queryFields: string[], returnFields: string[]}>,
        queryParams: '',
        owner: '',
        remark: ''
    });
    
    // 数据源列表
    const dataSourceList = ref([]);
    // 表列表
    const tableList = ref([]);
    
    // 表单配置
    const formConfig = reactive({
        model: formData.value,
        rules: {
            dataSourceId: [{ required: true, message: computed(() => t('请选择数据源')), trigger: 'change' }]
        },
        labelWidth: '120px',
        itemList: [
            {
                type: 'slot',
                label: '数据源',
                required: true,
                props:{
                    slotName: "dataSourceId",
                }
            },
            {
                type: 'slot',
                label: '数据表配置',
                props:{
                    slotName: "tableConfigs",
                }
            },
            {
                type: 'textarea',
                props: {
                    type: 'textarea',
                    row: 3,
                    placeholder: '请描述下申请数据的用途或其它相关信息',
                    disabled: props.disabled
                },
                label: '备注',
                prop: 'remark'
            }
        ],
        itemList2: [
            {
                type: 'input',
                props: {
                    type: 'text',
                    placeholder: '请输入固定查询条件',
                },
                label: '固定查询条件',
                prop: 'queryParams'
            },
            {
                type: 'slot',
                label: '所属者',
                props:{
                    slotName: "owner",
                }
            }
        ]
    });
    
    // 初始化数据
    onMounted(() => {
        if (userRole === 'systemAdmin') {
            formConfig.itemList.push(...formConfig.itemList2);
            loadOwnerOptions();
        }
        loadDataSourceList();
        if (props.entity) {
            initFormData(props.entity);
        }
    });

    // 加载所属者选择器
    const ownerOptions = ref([]);
    async function loadOwnerOptions() {
        try {
            await getOwners().then((res) => {
                if(res.success) {
                    ownerOptions.value = res.data;
                }
            });
        } catch (error) {
            ElMessage.error('加载所属者失败');
        }
    }
    
    // 加载数据源列表
    async function loadDataSourceList() {
        try {
            await getDataBase().then((res) => {
                if(res.success) {
                    dataSourceList.value = res.data;
                }
            });
        } catch (error) {
            ElMessage.error('加载数据源失败');
        }
    }
    
    // 处理数据源变化
    async function handleDataSourceChange(value) {
        if (value) {
            // 清空表配置
            formData.value.tableConfigs = [];
            // 清空表字段缓存
            Object.keys(tableFieldsCache).forEach(key => delete tableFieldsCache[key]);
            // 重新加载表列表
            await loadTableList(value);
        }
    }
    
    // 加载表列表
    async function loadTableList(dataSourceId) {
        try {
            let params = {
                id: dataSourceId,
                name: ''
            };
            let res = await getTablesByBaseId(params);
            if (res.code == 0) {
                // 对返回的接口数据进行赋值与处理
                tableList.value = res.data;
            }
        } catch (error) {
            ElMessage.error('加载表列表失败');
        }
    }
    
    // 处理表变化
    async function handleTableChange(config, index) {
        if (config.tableName && formData.value.dataSourceId) {
            // 加载该表的字段
            await loadTableFields(formData.value.dataSourceId, config.tableName);
            // 加载该表的外键
            const foreignKeys = await loadTableForeignKeys(formData.value.dataSourceId, config.tableName);
            // 赋予查询字段默认值
            config.queryFields = foreignKeys;
        }
    }

    // 加载表的外键
    async function loadTableForeignKeys(dataSourceId, tableName) {
        try {
            const res = await getTableForeignKeys(tableName, dataSourceId);
            if (res.code === 0) {
                return res.data || [];
            }
        } catch (error) {
            ElMessage.error('加载外键列表失败');
        }
        return [];
    }
    
    // 加载表字段
    async function loadTableFields(dataSourceId, tableName) {
        // 如果字段已缓存，直接返回
        if (tableFieldsCache[tableName]) {
            return;
        }
        
        try {
            const res = await getTableColumns(dataSourceId, tableName);
            if (res.code === 0) {
                tableFieldsCache[tableName] = res.data;
            }
        } catch (error) {
            ElMessage.error('加载字段列表失败');
        }
    }
    
    // 获取表字段
    function getTableFields(tableName) {
        return tableFieldsCache[tableName] || [];
    }
    
    // 添加表配置
    function addTableConfig() {
        formData.value.tableConfigs.push({
            tableName: '',
            queryFields: [],
            returnFields: []
        });
    }
    
    // 移除表配置
    function removeTableConfig(index) {
        formData.value.tableConfigs.splice(index, 1);
    }
    
    // 初始化表单数据（编辑模式）
    function initFormData(entity) {
        formData.value.dataSourceId = entity.dataSourceId || '';
        formData.value.queryParams = entity.queryParams || '';
        formData.value.owner = entity.owner || '';
        formData.value.remark = entity.remark || '';
        formData.value.id = entity.id || '';
        
        // 如果传入的是单个表配置
        if (entity.tableName) {
            formData.value.tableConfigs = [{
                tableName: entity.tableName,
                queryFields: [],
                returnFields: []
            }];
            
            // 解析字段数组
            if (entity.queryFields) {
                try {
                    formData.value.tableConfigs[0].queryFields = JSON.parse(entity.queryFields);
                } catch (e) {
                    formData.value.tableConfigs[0].queryFields = [];
                }
            }
            
            if (entity.returnFields) {
                try {
                    formData.value.tableConfigs[0].returnFields = JSON.parse(entity.returnFields);
                } catch (e) {
                    formData.value.tableConfigs[0].returnFields = [];
                }
            }
        }
        
        // 异步加载表和字段数据
        setTimeout(async () => {
            if (formData.value.dataSourceId) {
                await loadTableList(formData.value.dataSourceId);
                setTimeout(async () => {
                    for (const config of formData.value.tableConfigs) {
                        if (config.tableName) {
                            await loadTableFields(formData.value.dataSourceId, config.tableName);
                        }
                    }
                }, 100);
            }
        }, 100);
    }
    
    // 提交表单
    async function submitForm() {
        const ruleFormRef = ruleRef.value.elFormRef;
        if (!ruleFormRef) return;
        
        // 基本表单验证
        await ruleFormRef.validate(async (valid, fields) => {
            if (valid) {
                try {
                    // 检查是否至少添加了一个表配置
                    if (formData.value.tableConfigs.length === 0) {
                        ElMessage.error('请至少添加一个表配置');
                        return;
                    }
                    
                    // 验证每个表配置
                    const invalidConfigs = [];
                    const emptyTableNameConfigs = formData.value.tableConfigs.filter(config => !config.tableName);
                    const noReturnFieldsConfigs = formData.value.tableConfigs.filter(config => config.tableName && config.returnFields.length === 0);
                    
                    if (emptyTableNameConfigs.length > 0) {
                        invalidConfigs.push(`有 ${emptyTableNameConfigs.length} 个表配置未选择表名称`);
                    }
                    
                    if (noReturnFieldsConfigs.length > 0) {
                        const tableNames = noReturnFieldsConfigs.map(config => config.tableName).join(', ');
                        invalidConfigs.push(`表 ${tableNames} 请至少选择一个返回字段`);
                    }
                    
                    if (invalidConfigs.length > 0) {
                        ElMessage.error(invalidConfigs.join('；'));
                        return;
                    }
                    
                    // 批量保存每个表的配置
                    let successCount = 0;
                    let failCount = 0;
                    let successMessages = [''];
                    
                    for (const tableConfig of formData.value.tableConfigs) {
                        // 构建提交数据
                        const submitData = {
                            id: formData.value.id,
                            dataSourceId: formData.value.dataSourceId,
                            tableName: tableConfig.tableName,
                            queryFields: JSON.stringify(tableConfig.queryFields),
                            returnFields: JSON.stringify(tableConfig.returnFields),
                            queryParams: ruleRef.value.model.queryParams,
                            owner: ruleRef.value.model.owner,
                            remark: ruleRef.value.model.remark
                        };
                        
                        try {
                            const res = await saveDataApiTable(submitData);
                            if (res.success) {
                                successCount++;
                            } else {
                                failCount++;
                                successMessages.push(`表 ${tableConfig.tableName} 失败：${res.msg}`);
                            }
                        } catch (error) {
                            failCount++;
                            successMessages.push(`表 ${tableConfig.tableName} 失败：${error.message}`);
                        }
                    }
                    
                    // 显示保存结果
                    if (failCount === 0) {
                        ElNotification({
                            title: t('成功'),
                            message: `成功保存 ${successCount} 个表的配置`,
                            type: 'success',
                            duration: 2000,
                            offset: 80
                        });
                        emit('close', 1);
                    } else {
                        ElNotification({
                            title: t('部分成功'),
                            message: `成功保存 ${successCount} 个表，失败 ${failCount} 个表，失败详情：${successMessages.join('；')}`,
                            type: 'warning',
                            duration: 2000,
                            offset: 80
                        });
                        emit('close', 1); // 即使有失败，也关闭弹窗，让用户重新检查
                    }
                } catch (error) {
                    ElMessage.error('保存失败：' + error.message);
                }
            }
        });
    }
    
    // 取消
    function cancel() {
        emit('close', 0);
    }
</script>

<style lang="scss" scoped>
    .data-api-table-form {
        .add-table-btn-container {
            margin-bottom: 15px;
        }
        
        .table-config-item {
            background-color: #f9f9f9;
            border: 1px solid #ebeef5;
            border-radius: 4px;
            padding: 15px;
            transition: all 0.3s;
            
            &:hover {
                box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
            }
        }
        
        .table-config-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding-left: 10px;
            
            .config-index {
                font-weight: bold;
                color: #333;
                margin-right: 10px;
            }
        }
        
        .form-item {
            margin-bottom: 15px;
            
            .form-label {
                display: inline-block;
                width: 120px;
                text-align: right;
                margin-right: 10px;
                vertical-align: top;
                line-height: 32px;
                
                &.required::after {
                    content: '*';
                    color: #f56c6c;
                    margin-left: 4px;
                }
            }
            
            .el-select {
                width: 300px;
            }
        }
        
        .field-scrollbar {
            margin-top: 12px;
        }
        
        .field-checkbox-group {
            display: flex;
            flex-wrap: wrap;
            gap: 15px;
            padding: 10px;
        }
        
        .text-muted {
            color: #909399;
            padding: 10px;
            background-color: #a39e9e1a;
        }
        
        .empty-tip {
            text-align: center;
            color: #909399;
            padding: 30px;
            background-color: #a39e9e1a;
        }
        
        /* 折叠面板样式优化 */
        .table-config-collapse {
            border: 1px solid #ebeef5;
            border-radius: 4px;
            overflow: hidden;
            
            .el-collapse-item {
                border-bottom: 1px solid #ebeef5;
                
                &:last-child {
                    border-bottom: none;
                }
            }
            
            .el-collapse-item__header {
                background-color: #f9f9f9;
                padding: 12px 15px;
                border-left: 3px solid #409eff;
                
                &:focus {
                    color: #409eff;
                }
            }
            
            .el-collapse-item__content {
                padding: 15px;
                background-color: #fff;
            }
        }
        
        /* 字段信息样式 */
        .field-checkbox {
            display: block;
            width: 100%;
            border-radius: 4px;
            transition: all 0.3s;
            
            &:hover {
                background-color: #f5f7fa;
            }
            
            .el-checkbox__input {
                vertical-align: top;
                margin-right: 10px;
            }
        }
        
        .field-info {
            display: inline-block;
            vertical-align: middle;
        }
        
        .field-name {
            font-weight: 500;
            color: #303133;
            margin-right: 5px;
        }
        
        .field-comment {
            color: #606266;
            margin-right: 10px;
            font-size: 13px;
        }
        
        .field-type {
            color: #909399;
            margin-right: 5px;
            font-size: 12px;
            font-family: monospace;
        }
        
        .field-length {
            color: #909399;
            font-size: 12px;
            font-family: monospace;
        }
        
        /* 字段复选框组样式 */
        .field-checkbox-group {
            display: flex;
            flex-direction: column;
            gap: 5px;
            padding: 0;
        }
        
        /* 表单项样式优化 */
        .form-item {
            margin-bottom: 20px;
            display: flex;
            align-items: flex-start;
            width: 95%;
            
            .form-label {
                display: inline-block;
                width: 100px;
                text-align: right;
                margin-right: 20px;
                margin-top: 8px;
                vertical-align: top;
                line-height: 20px;
            }
            
            > div {
                flex: 1;
                min-width: 0;
            }
        }
        
        .dialog-footer {
            display: flex;
            justify-content: flex-end;
            margin-top: 20px;
            padding-top: 20px;
            border-top: 1px solid #ebeef5;
        }
    }
</style>