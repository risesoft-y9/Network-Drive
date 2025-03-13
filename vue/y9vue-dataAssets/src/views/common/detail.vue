<!--
 * @Author: yihong yihong@risesoft.net
 * @Date: 2024-11-07 14:54:27
 * @LastEditors: yihong yihong@risesoft.net
 * @LastEditTime: 2025-01-09 11:02:06
 * @FilePath: \vue\y9vue-dataAssets\src\views\common\detail.vue
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
-->
<template>
    <div style="height: 80vh;
    overflow: scroll;">
    <y9Card :showHeader="false">
        <el-form ref="detailFormRef"
        :inline-message="false"
        :inline="true"
        label-position="right"
        label-width="150px"
        :model="detailForm"
        :rules="dynamicRules"
        :status-icon="true"
        class="detailPageForm" >
            <template v-for="(item,index) in formFieldList" :key="item.id">
                <template v-if="item.isRecord==1">
                    <el-row v-if="item.re_isOneLine == 1">
                        <el-col :span="24" style="margin-bottom: 25px;">
                            <el-form-item :label="item.disPlayName" :prop="item.columnName" style="width:88%">
                                <template v-if="item.re_inputBoxType == 'input' || item.queryType == 'textarea'">
                                    <el-input v-model="detailForm[item.columnName]" clearable />
                                </template>
                                <template v-if="item.re_inputBoxType == 'date'">
                                    <el-date-picker style="width: 200px;"
                                        v-model="detailForm[item.columnName]"
                                        clearable
                                        type="date"
                                        value-format="YYYY-MM-DD"
                                    />
                                </template>
                                <template v-if="item.re_inputBoxType == 'dateTime'">
                                    <el-date-picker style="width: 200px;"
                                        v-model="detailForm[item.columnName]"
                                        clearable
                                        type="datetime"
                                        value-format="YYYY-MM-DD hh:mm:ss"
                                    />
                                </template>
                                <template v-if="item.re_inputBoxType == 'select'">
                                    <el-select v-model="detailForm[item.columnName]" :placeholder="item.disPlayName" class="select-input" clearable>
                                        <el-option
                                            v-for="option in item.optionValue"
                                            :label="option.label == undefined ? option.value : option.label"
                                            :value="option.value"
                                        />
                                    </el-select>
                                </template>
                                <template v-if="item.re_inputBoxType == 'radio'">
                                    <el-radio-group v-model="detailForm[item.columnName]" :placeholder="item.disPlayName" clearable>
                                        <el-radio
                                            v-for="option in item.optionValue"
                                            :label="option.value"
                                            :value="option.value"
                                        ></el-radio>
                                    </el-radio-group>
                                </template>
                                <template v-if="item.re_inputBoxType == 'checkbox'">
                                    <el-checkbox-group v-model="detailForm[item.columnName]" :placeholder="item.disPlayName" clearable>
                                        <el-checkbox
                                            v-for="option in item.optionValue"
                                            :label="option.label == undefined ? option.value : option.label"
                                            :value="option.value"
                                        />
                                    </el-checkbox-group>
                                </template>
                            </el-form-item>
                        </el-col>
                    </el-row>
                    <el-form-item v-else :label="item.disPlayName" :prop="item.columnName">
                        <template v-if="item.re_inputBoxType == 'input' || item.queryType == 'textarea'">
                            <el-input v-model="detailForm[item.columnName]" clearable />
                        </template>
                        <template v-if="item.re_inputBoxType == 'date'">
                            <el-date-picker style="width: 200px;"
                                v-model="detailForm[item.columnName]"
                                clearable
                                type="date"
                                value-format="YYYY-MM-DD"
                            />
                        </template>
                        <template v-if="item.re_inputBoxType == 'dateTime'">
                            <el-date-picker style="width: 200px;"
                                v-model="detailForm[item.columnName]"
                                clearable
                                type="datetime"
                                value-format="YYYY-MM-DD hh:mm:ss"
                            />
                        </template>
                        <template v-if="item.re_inputBoxType == 'select'">
                            <el-select v-model="detailForm[item.columnName]" :placeholder="item.disPlayName" class="select-input" clearable>
                                <el-option
                                    v-for="option in item.optionClass"
                                    :label="option.label == undefined ? option.value : option.label"
                                    :value="option.value"
                                />
                            </el-select>
                        </template>
                        <template v-if="item.re_inputBoxType == 'radio'">
                            <el-radio-group v-model="detailForm[item.columnName]" :placeholder="item.disPlayName" clearable>
                                <el-radio
                                    v-for="option in item.optionValue"
                                    :label="option.value"
                                    :value="option.value"
                                ></el-radio>
                            </el-radio-group>
                        </template>
                        <template v-if="item.re_inputBoxType == 'checkbox'">
                            <el-checkbox-group v-model="detailForm[item.columnName]" :placeholder="item.disPlayName" clearable>
                                <el-checkbox
                                    v-for="option in item.optionValue"
                                    :label="option.label == undefined ? option.value : option.label"
                                    :value="option.value"
                                />
                            </el-checkbox-group>
                        </template>
                    </el-form-item>
                </template>
            </template>
        </el-form>
    </y9Card>
    </div>
</template>
<script lang="ts" setup>
import { onMounted, watch } from 'vue';

    const props = defineProps({
        formData: {
            type: Object,
            default: () => {
                return {};
            }
        },
        optType: String,
        metadataFieldList: Array
    });
    const data = reactive({
        formFieldList: [],
        detailForm: {},
        detailFormRef: '',
        rules: {
            disPlayName: { required: true, message: '请输入显示名称' },
            disPlayWidth: { required: true, message: '请输入显示宽度' },
            disPlayAlign: { required: true, message: '请选择显示位置' }
        },
        recordInit: false,//著录初始化
        recordField:[],
        requiredFields:[],
    });
    let { detailForm, formFieldList, detailFormRef,recordInit,recordField,requiredFields } = toRefs(data);

    onMounted(() => {
        formFieldList.value = props.metadataFieldList;
        initRecordRules();
        if(props.optType=='add'){
            detailForm.value = {};
        }else{
             Object.assign(detailForm.value,props.formData);
             console.log('detailFormAtfer', detailForm.value);
             
        }

    });

    defineExpose({
        detailForm,
        detailFormRef,
        checkIfAllEmpty
    });

    const dynamicRules = computed(() => {
        const rules: Record<string, any[]> = {};
        Object.keys(detailForm).forEach(key => {
            rules[key] = [];
            if (requiredFields.value.includes(key)) {
                console.log("必填");
                
                rules[key].push({ required: true, message: `请输入或者选择必填项！`, trigger: 'blur' });
            }
        });
        return rules;
        });

        const addField = (field: string, required: boolean = false) => {
            detailForm[field] = '';
            if (required) {
                requiredFields.value.push(field);
            }
        };

        function checkIfAllEmpty(){
            let allEmpty = true;
            for (const key in detailForm.value) {
                if (detailForm.value[key]) {
                allEmpty = false;
                break;
                }
            }
            if (allEmpty) {
                console.log('所有表单字段都为空');
                return true;
            } else {
                console.log('表单中有非空字段');
                return false;
            }
        }
        
    function initRecordRules(){
        formFieldList.value.forEach((item) => {
            detailForm.value[item.columnName] = '';
            if(item.isRecordRequired == 1){
                addField(item.columnName,true);
            }
        });
    }

    async function initRecordField(){
        if (recordInit.value) {
            recordField.value.forEach((element) => {
                if (element.queryType == 'checkbox') {
                    //处理checkbox值
                    if (element.value != null && element.value != undefined && element.value != '') {
                        let arr = element.value.split(',');
                        element.value = arr;
                    } else {
                        element.value = [];
                    }
                } else if (element.queryType == 'date') {
                    //处理date值
                    if (element.value != null && element.value != undefined && element.value != '') {
                        let arr = element.value.split(',');
                        element.value = arr;
                    } else {
                        element.value = '';
                    }
                }
            });
            return;
        }
        let res = await getRecordField(itemId.value);
        if (res.success) {
            recordInit.value = true;
            recordField.value = [];
            res.data.forEach((element) => {
                if (element.queryType == 'radio' || element.queryType == 'checkbox' || element.queryType == 'select') {
                    if (element.optionValue.indexOf('[') > -1) {
                        //静态数据
                        element.optionValue = JSON.parse(element.optionValue);
                    } else if (element.optionValue.indexOf('(') > -1) {
                        //动态数据
                        let str = element.optionValue.split('(')[1];
                        let type = str.slice(0, str.length - 1); //数据字典类型标识
                        getOptionValueList(type).then((res) => {
                            if (res.success) {
                                let data = res.data;
                                let option = []; //选项
                                for (let obj of data) {
                                    let optionObj = {};
                                    optionObj.value = obj.code;
                                    optionObj.label = obj.name;
                                    option.push(optionObj);
                                }
                                element.optionValue = option;
                            }
                        });
                    }
                }
                recordField.value.push(element);
            });
        }
    }

   
</script>
<style lang="scss" scoped>
.y9-dialog-content{
    .el-form-item:last-child{
        margin-bottom: 26px !important;
    }
}
</style>
<style>
.detailPageForm .select-input {
        width: 198px !important;
}
.detailPageForm .el-input__wrapper{
    width: 177px !important;;
}
</style>
