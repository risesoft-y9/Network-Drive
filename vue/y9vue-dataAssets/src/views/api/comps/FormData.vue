<template>
    <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="y9-form"
    >
        <el-divider content-position="left" border-style="dashed">基本信息</el-divider>
        <el-descriptions border :column="2">
            <el-descriptions-item>
                <template #label>
                    名称<span style="color: red;">*</span>
                </template>
                <el-form-item label="" prop="apiName">
                    <span v-if="disabled">{{ form.apiName }}</span>
                    <el-input v-else v-model="form.apiName"></el-input>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item>
                <template #label>
                    方法名<span style="color: red;">*</span>
                </template>
                <el-form-item label="" prop="apiUrl">
                    <span v-if="disabled">{{ form.apiUrl }}</span>
                    <el-input v-else v-model="form.apiUrl" :disabled="form.id"></el-input>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item :span="2">
                <template #label>
                    描述
                </template>
                <el-form-item label="" prop="remark">
                    <span v-if="disabled">{{ form.remark }}</span>
                    <el-input type="textarea" v-else v-model="form.remark"></el-input>
                </el-form-item>
            </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left" border-style="dashed">
            参数信息（<span style="color: red;">1.参数列表添加顺序与SQL参数顺序保持一致；
                2.分页查询会自动计算OFFSET和LIMIT值，必添加参数：page-当前页，size-每页数量</span>）
        </el-divider>
        <el-descriptions border :column="2">
            <el-descriptions-item>
                <template #label>
                    接口类型<span style="color: red;">*</span>
                </template>
                <el-form-item label="" prop="method">
                    <el-select :disabled="disabled" v-model="form.method" @change="datachange">
                        <el-option key="query" label="查询" value="query" />
                        <el-option key="page" label="分页" value="page" />
                        <el-option key="update" label="修改" value="update" />
                        <el-option key="insert" label="新增" value="insert" />
                        <el-option key="delete" label="删除" value="delete" />
                    </el-select>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item>
                <template #label>
                    访问类型<span style="color: red;">*</span>
                </template>
                <el-form-item label="" prop="apiType">
                    <el-select :disabled="disabled" v-model="form.apiType">
                        <el-option key='0' label="公开" value='0' />
                        <el-option key='1' label="私有" value='1' />
                    </el-select>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item :span="2">
                <template #label>
                    数据源<span style="color: red;">*</span>
                </template>
                <el-form-item label="" prop="dataSourceId">
                    <el-tree-select
                        v-model="form.dataSourceId"
                        :data="sourceTreeData"
                        filterable
                        check-strictly
                        :render-after-expand="false"
                    />
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item
                :span="2"
                label="SQL"
            >
                <el-form-item label="" prop="sqlData">
                    <span v-if="disabled">{{ form.sqlData }}</span>
                    <div v-else ref="editorRef" class="sql-editor"></div>
                </el-form-item>
            </el-descriptions-item>

            <!-- 动态参数列表 -->
            <el-descriptions-item
                :span="2"
                label="参数列表"
            >
                <el-form-item label="">
                    <div v-for="(param, index) in form.params" :key="index" class="param-item">
                        <el-input
                            v-model="param.name"
                            placeholder="参数名（小写）"
                            style="width: 120px; margin-right: 10px"
                        />
                        <el-input
                            v-model="param.value"
                            placeholder="参数值（示例，可不填）"
                            style="width: 200px; margin-right: 10px"
                        />
                        <el-select v-model="param.type" style="width: 120px; margin-right: 10px" placeholder="参数类型">
                            <el-option key="字符串" label="字符串" value="字符串" />
                            <el-option key="数字" label="数字" value="数字" />
                            <el-option key="时间" label="时间" value="时间" />
                        </el-select>
                        <el-input
                            v-model="param.remark"
                            placeholder="描述"
                            style="width: 220px; margin-right: 10px"
                        />
                        <el-button
                            icon="Delete"
                            @click="removeParam(index)"
                        />
                    </div>
                    <el-button type="primary" @click="addParam" style="margin-left: 10px;margin-top: -10px;">添加</el-button>
                </el-form-item>

                <!-- 操作按钮 -->
                <el-form-item v-if="form.method == 'query' || form.method == 'page'">
                    <el-button type="primary" @click="testRun">测试运行</el-button>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item
                :span="2"
                label="执行结果"
                v-if="resultData.length > 0"
            >
                <el-form-item label="">
                    <el-input type="textarea" v-model="resultData" rows="6"></el-input>
                </el-form-item>
            </el-descriptions-item>           
        </el-descriptions>

        <el-form-item style="float: right; margin-top: 15px;" v-if="!disabled">
            <el-button type="primary" @click="submitForm(formRef)">保存</el-button>
            <el-button @click="resetForm(formRef)">重置</el-button>
        </el-form-item>
    </el-form>
    <!-- 制造loading效果 -->
    <el-button v-loading.fullscreen.lock="saveLoading" style="display: none"></el-button>
</template>

<script lang="ts" setup>
import { ref, onMounted, watch, nextTick, computed, onBeforeUnmount } from 'vue';
import { FormInstance, Action, ElNotification } from 'element-plus';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getTableSelectTree } from '@/api/pretreat';
import y9_storage from '@/utils/storage';
import settings from '@/settings';
import * as monaco from 'monaco-editor';
import { saveApiData, testSql } from '@/api/apiService';

const emits = defineEmits(['close']);
const props = defineProps({
    parentId: {
        type: String
    },
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

let formRef = ref<FormInstance>();

let rules = ref({
    apiName: [{ required: true, message: '名称不能为空', trigger: ['change', 'blur'] }],
    apiUrl: [{ required: true, message: '方法名不能为空', trigger: ['change', 'blur'] }],
    dataSourceId: [{ required: true, message: '不能为空', trigger: ['change', 'blur'] }],
    method: [{ required: true, message: '请求方式不能为空', trigger: ['change', 'blur'] }],
    apiType: [{ required: true, message: '访问类型不能为空', trigger: ['change', 'blur'] }]
});

const form = ref({ parentId: props.parentId, params: [{ name: '', value: '', type: '', remark: '' }] });

let saveLoading = ref(false);
const submitForm = async (formEl: FormInstance | undefined) => {
    return new Promise<void>((resolve, reject) => {
        if (!formEl) {
            reject();
            return;
        }
        formEl.validate((valid) => {
            if (valid) {
                saveLoading.value = true;
                form.value.sqlData = editorInstance.getValue();
                let params: any = Object.assign({}, form.value);
                saveApiData(params)
                    .then((res: any) => {
                        saveLoading.value = false;
                        if (res.success) {
                            emits('close');
                        }
                        ElNotification({
                            title: res.success ? '成功' : '失败',
                            message: res.msg,
                            type: res.success ? 'success' : 'error',
                            duration: 2000,
                            offset: 80
                        });
                        resolve();
                    })
                    .catch((e: any) => {
                        saveLoading.value = false;
                        ElMessage.error('程序出错:' + e);
                        reject();
                    });
            } else {
                reject();
            }
        });
    });
}

const resetForm = (formEl: FormInstance | undefined) => {
    if (!formEl) return
    formEl.resetFields()
}

watch(
    () => props.entity,
    (nv, ov) => {
        if (props.entity) {
            form.value = props.entity;
        }
    },
    { immediate: true }
);

let sql = ref('SELECT * FROM TABLE WHERE id = ?');
let querysql = ref('SELECT * FROM TABLE WHERE id = ?');
let updatesql = ref('UPDATE TABLE SET name = ? WHERE id = ?');
let deletesql = ref('DELETE FROM TABLE WHERE id = ?');
let pagesql = ref('SELECT * FROM TABLE LIMIT ? OFFSET ?');
let insertsql = ref('INSERT INTO TABLE (id, name) VALUES (?, ?)');

const datachange = () => {
    if(form.value.method == 'update') {
        editorInstance.setValue(updatesql.value);
    } else if(form.value.method == 'delete') {
        editorInstance.setValue(deletesql.value);
    } else if(form.value.method == 'page') {
        editorInstance.setValue(pagesql.value);
    } else if(form.value.method == 'insert') {
        editorInstance.setValue(insertsql.value);
    } else {
        editorInstance.setValue(querysql.value);
    }    
}

// 编辑器实例
const editorRef = ref(null);
let editorInstance = null;

onMounted(() => {
    initSourceTreeData();
    if (props.entity) {
        sql.value = form.value.sqlData;
        form.value.params = JSON.parse(form.value.params);
        form.value.apiType = form.value.apiType == 0 ? '0' : '1';
        form.value.dataSourceId = "s-" + form.value.dataSourceId;
    }
    // 初始化编辑器
    editorInstance = monaco.editor.create(editorRef.value, {
        value: sql.value,
        language: 'sql',
        theme: 'vs-light',
        minimap: { enabled: false },
        automaticLayout: true
    })
});

// 销毁编辑器
onBeforeUnmount(() => {
    if (editorInstance) {
        editorInstance.dispose()
    }
    emits('close', 1);
})

// 执行结果
const resultData = ref('');

// 参数操作方法
const addParam = () => {
    form.value.params.push({ name: '', value: '', type: '', remark: '' })
}

const removeParam = (index) => {
    form.value.params.splice(index, 1)
}

// 测试运行方法
const testRun = async () => {
    // 获取SQL内容
    const rawSQL = editorInstance.getValue();

    const params = form.value.params;
    
    // 模拟API调用
    const mockData = await testSql({dataSourceId: form.value.dataSourceId.split('-')[1], sqlData: rawSQL, params: params});
    
    // 处理结果
    if(mockData.success) {
        resultData.value = JSON.stringify(mockData.data);
    }
    
    ElMessage.success(mockData.msg);
}

let sourceTreeData = ref([]);
async function initSourceTreeData() {
    sourceTreeData.value = [];
    await getTableSelectTree('source').then((res) => {
        if(res.success) {
            sourceTreeData.value = res.data;
        }
    });
}

</script>

<style lang="scss" scoped>
.sql-editor {
    width: 100%;
    height: 200px;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
}

.param-item {
    display: flex;
    align-items: center;
    margin-bottom: 10px;
}

.el-form-item {
    margin-bottom: 0px !important;
}

:deep(.y9-form) {
    .el-descriptions__body {
        tr {
            display: flex;

            .el-descriptions__label {
                width: 150px;
                vertical-align: middle;
                line-height: 32px;
            }

            .el-descriptions__content {
                flex: 1;
            }

            .el-select {
                width: 100% !important;
            }
        }
    }
}
</style>