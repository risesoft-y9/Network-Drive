<template>
    <el-form
        ref="fieldFormRef"
        :inline-message="true"
        :inline="true"
        label-position="right"
        label-width="150px"
        :model="metadataForm"
        :rules="rules"
        :status-icon="true"
        class="editMetadata"
    >
        <!-- 基础信息 -->
        <el-divider content-position="left">基础信息</el-divider>
        <el-row>
            <el-col :span="12">
                <el-form-item label="元数据标识">
                    <el-input v-model="metadataForm.columnName" :readonly="true" :disabled="true"></el-input>
                </el-form-item>
            </el-col>
            <el-col :span="12">
                <el-form-item label="元数据名称" prop="disPlayName">
                    <el-input v-model="metadataForm.disPlayName" clearable></el-input>
                </el-form-item>
            </el-col>
        </el-row>
        <el-row>
            <el-col :span="12">
                <el-form-item label="元数据含义">
                    <el-input v-model="metadataForm.description"></el-input>
                </el-form-item>
            </el-col>
            <el-col :span="12">
                <el-form-item label="来源">
                    <el-input v-model="metadataForm.fieldOrigin" :readonly="true" :disabled="true"></el-input>
                </el-form-item>
            </el-col>
        </el-row>
        <!--基础设置-->
        <el-divider content-position="left">列表设置</el-divider>
        <el-row>
            <el-col :span="12">
                <el-form-item label="列显示宽度" prop="disPlayWidth">
                    <el-input v-model="metadataForm.disPlayWidth" clearable></el-input>
                </el-form-item>
            </el-col>
            <el-col :span="12">
                <el-form-item label="列对齐方式" prop="disPlayAlign">
                    <el-select v-model="metadataForm.disPlayAlign" placeholder="请选择" class="select-input">
                        <el-option key="left" label="靠左" value="left"></el-option>
                        <el-option key="center" label="居中" value="center"></el-option>
                        <el-option key="right" label="靠右" value="right"></el-option>
                    </el-select>
                </el-form-item>
            </el-col>
        </el-row>
        <el-row>
            <el-col :span="12">
                <el-form-item label="是否列表显示">
                    <el-switch
                        v-model="metadataForm.isListShow"
                        :active-value="1"
                        :inactive-value="0"
                        inline-prompt
                        active-text="是"
                        inactive-text="否"
                    />
                </el-form-item>
            </el-col>
            <el-col :span="12">
                <el-form-item label="是否可排序">
                    <el-switch
                        v-model="metadataForm.isOrder"
                        :active-value="1"
                        :inactive-value="0"
                        inline-prompt
                        active-text="是"
                        inactive-text="否"
                    />
                </el-form-item>
            </el-col>
        </el-row>
        <!--著录设置-->
        <el-divider content-position="left">著录设置</el-divider>
        <el-row>
            <el-col :span="12">
                <el-form-item label="是否著录">
                    <el-switch
                        v-model="metadataForm.isRecord"
                        :active-value="1"
                        :inactive-value="0"
                        inline-prompt
                        active-text="是"
                        inactive-text="否"
                        @change ="switchRechange"
                    />
                </el-form-item>
            </el-col>
            <el-col :span="12">
                <el-form-item label="是否著录必填">
                    <el-switch
                        v-model="metadataForm.isRecordNull"
                        :active-value="1"
                        :inactive-value="0"
                        inline-prompt
                        active-text="是"
                        inactive-text="否"
                    />
                </el-form-item>
            </el-col>
        </el-row>
        <el-row v-if="metadataForm.isRecord == '1'">
            <el-col :span="12">
                <!-- <el-form-item label="输入框宽度" prop="re_inputBoxWidth">
                    <el-input v-model="metadataForm.re_inputBoxWidth" clearable></el-input>
                </el-form-item> -->
                <el-form-item label="著录输入框类型" prop="re_inputBoxType">
                    <el-select v-model="metadataForm.re_inputBoxType" placeholder="请选择" class="select-input">
                        <el-option key="input" label="文本输入框" value="input"></el-option>
                        <el-option key="select" label="下拉框" value="select"></el-option>
                        <el-option key="date" label="日期" value="date"></el-option>
                        <el-option key="dateTime" label="日期时间" value="dateTime"></el-option>
                    </el-select>
                </el-form-item>
            </el-col>
            <el-col :span="12">
                <el-form-item label="是否单独一行" prop="re_isOneLine">
                    <el-switch
                        v-model="metadataForm.re_isOneLine"
                        :active-value="1"
                        :inactive-value="0"
                        inline-prompt
                        active-text="是"
                        inactive-text="否"
                    />
                </el-form-item>
            </el-col>
        </el-row>
        <!-- <el-row v-if="metadataForm.isRecord == '1'">
            <el-col :span="24">
                <el-form-item label="著录输入框类型" prop="re_inputBoxType">
                    <el-select v-model="metadataForm.re_inputBoxType" placeholder="请选择" class="select-input">
                        <el-option key="input" label="文本输入框" value="input"></el-option>
                        <el-option key="select" label="下拉框" value="select"></el-option>
                        <el-option key="date" label="日期" value="date"></el-option>
                        <el-option key="dateTime" label="日期时间" value="dateTime"></el-option>
                    </el-select>
                </el-form-item>
            </el-col>
        </el-row> -->

        <!--查询设置-->
        <el-divider content-position="left">查询设置</el-divider>
        <el-row>
            <el-col :span="24">
                <el-form-item label="是否开启搜索条件">
                    <el-switch
                        v-model="metadataForm.openSearch"
                        :active-value="1"
                        :inactive-value="0"
                        active-text="开启"
                        inactive-text="关闭"
                        inline-prompt
                        @change="switchchange"
                    />
                </el-form-item>
            </el-col>
        </el-row>
        <el-row v-if="metadataForm.openSearch == '1'">
            <el-col :span="24">
                <el-form-item label="输入框类型" prop="inputBoxType">
                    <el-select v-model="metadataForm.inputBoxType" placeholder="请选择">
                        <el-option key="search" label="文本输入框(带搜索图标)" value="search"></el-option>
                        <el-option key="input" label="文本输入框" value="input"></el-option>
                        <el-option key="select" label="下拉框" value="select"></el-option>
                        <el-option key="date" label="日期" value="date"></el-option>
                    </el-select>
                </el-form-item>
            </el-col>
        </el-row>
        <el-row v-if="metadataForm.inputBoxType == 'select' && metadataForm.openSearch == '1'">
            <el-col :span="24">
                <el-form-item label="数据字典" prop="optionClass">
                    <el-input v-model="metadataForm.optionClass" :readonly="true" @click="selectOption"></el-input>
                </el-form-item>
            </el-col>
        </el-row>
    </el-form>
</template>

<script lang="ts" setup>
    const props = defineProps({
        metadata: {
            type: Object,
            default: () => {
                return {};
            }
        }
    });

    const data = reactive({
        metadataForm: {},
        fieldFormRef: '',
        rules: {
            disPlayName: { required: true, message: '请输入显示名称' },
            disPlayWidth: { required: true, message: '请输入显示宽度' },
            disPlayAlign: { required: true, message: '请选择显示位置' },
        }
    });
    let { metadataForm, rules, fieldFormRef } = toRefs(data);

    onMounted(() => {
        metadataForm.value = props.metadata;
    });

    defineExpose({
        metadataForm,
        fieldFormRef
    });

    function switchchange(val) {
        if (val == 1) {
            rules.value.inputBoxType = { required: true, message: '请选择输入框类型' };
        } else {
            rules.value.inputBoxType = {};
        }
    }

    function switchRechange(val) {
        if (val == 1) {
            rules.value.re_inputBoxType = { required: true, message: '请选择输入框类型' };
            rules.value.re_inputBoxWidth = { required: true, message: '请选择输入框类型' };
        } else {
            rules.value.re_inputBoxType = {};
            rules.value.re_inputBoxWidth = {};
        }
    }
</script>
<style>
    .editMetadata .el-form-item {
        margin-bottom: 0;
    }
    .editMetadata .el-row {
        padding-bottom: 15px;
    }
    .editMetadata .select-input {
        width: 172px;
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
