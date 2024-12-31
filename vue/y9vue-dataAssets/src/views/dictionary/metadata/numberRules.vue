<template>
    <div class="numberRules" style="min-height:400px;">
        <el-form class="formClass" ref="numberRulesForm">
            <y9Table :config="tableConfig">
                <template #connectorSymbol="{row, column, index}">
                        <el-select v-if="index != 0" v-model="tableConfig.tableData[index].connectorSymbol" @blur="appendStr" placeholder="请选择连接符">
                            <el-option value="-">- (横杠)</el-option>
                            <el-option value=".">. (点号)</el-option>
                            <el-option value="*">* (星号)</el-option>
                        </el-select>
                </template>
                <template #fieldName="{row, column, index}">
                        <el-select v-model="tableConfig.tableData[index].fieldName" @change="(val)=>setFieldCName(val,index)" @blur="appendStr" placeholder="请选择元数据字段">
                            <el-option  v-for="option in metadataFieldList"
                                :label="option.disPlayName"
                                :value="option.columnName"></el-option>
                        </el-select>
                </template>
                <template #opt="{row, column, index}">
                    <i class="ri-add-circle-line" @click="addRule()" style="margin-right: 10px;"></i>
                    <i class="ri-close-circle-line" v-if="index != 0"  @click="removeRule(index)"></i>
                </template>
            </y9Table>
       
        <p>规则示例：{{ rulesStr }}</p>
        <div slot="footer" class="dialog-footer" style="text-align: center; margin-top: 15px">
            <el-button type="primary" @click="saveNumberRules()"
                ><i class="ri-save-line"></i><span>保存规则</span>
            </el-button>
            
            <el-button type="primary" @click="dialogConfig_parent.show = false"
                ><i class="ri-close-line"></i><span>关闭</span>
            </el-button>
        </div>
    </el-form>
    </div>
</template>
<script lang="ts" setup>
import {onMounted,reactive,toRefs} from 'vue';
import { ElForm } from 'element-plus';
import {getMetadataRecordConfigList} from '@/api/dataAssets/metadata';
import {getArchivesNumberRules,existRules,saveRules} from '@/api/dataAssets/numberRules';

const props = defineProps({
    categoryMark: String,
    dialogConfig_parent: {
        type: Object,
        default: () => {
            return {};
        }
    },
});

const data = reactive({
    rulesStr: '',
    tableConfig: {
        height: '400px',
        columns: [
            {
                title: '结构顺序',
                key: 'structureOrder',
                width: 80,
            },
            {
                title: '连接符',
                key: 'connectorSymbol',
                width: 160,
                slot: 'connectorSymbol',
            },
            {
                title: '元数据字段',
                key: 'fieldName',
                slot: 'fieldName',
            },
            {
                title: '操作',
                key: 'opt',
                align: 'left',
                width: 160,
                slot: 'opt',
            },
        ],
        border: false,
        tableData: [],
        pageConfig: false
    },
    metadataFieldList: [],
});

let {rulesStr,tableConfig,metadataFieldList} = toRefs(data);

onMounted(() => {
    init();
})

async function init(){
    tableConfig.value.tableData = [];
    let result = await getArchivesNumberRules(props.categoryMark);
    if (result.success) {
        if(result.data.length>0){
            tableConfig.value.tableData = result.data;
            appendStr();
        }else{
            tableConfig.value.tableData = [
                {structureOrder:1,connectorSymbol:'',fieldName:'',fieldCName:'',opt:''}
            ];
        }
    }
   
    let res = await getMetadataRecordConfigList(props.categoryMark);
    if (res.success) {
        metadataFieldList.value = res.data;
    }
}

function addRule(){
    tableConfig.value.tableData.push({structureOrder:tableConfig.value.tableData.length+1,connectorSymbol:'',fieldName:'',fieldCName:'',opt:''});
}

function removeRule(index){
    tableConfig.value.tableData.splice(index,1);
    appendStr();
}

function setFieldCName(val,index){
    tableConfig.value.tableData[index].fieldCName = metadataFieldList.value.find(item=>item.columnName==val).disPlayName;
    appendStr();
}

function appendStr(){
    let str = '';
    tableConfig.value.tableData.forEach((element,index)=> {
        if(index == 0){
            str = element.fieldCName;
        }else{
            if(element.connectorSymbol){
                str += element.connectorSymbol;
            }
            if(element.fieldCName){
                str += element.fieldCName;
            }
        }
    });
    rulesStr.value = str;
}

async function saveNumberRules(){
    console.log(tableConfig.value.tableData);
    tableConfig.value.tableData.forEach((element,index)=> {
        if(index == 0){
            if(!element.fieldName){
                ElNotification({
                    title: '提示',
                    message: '请选择第'+(index+1)+'条数据的档号结构的元数据字段',
                    type: 'error',
                    duration: 2000,
                    offset: 80
                    });
                return;
            }
        }else{
            if(!element.connectorSymbol){
                ElNotification({
                    title: '提示',
                    message: '请选择第'+(index+1)+'条数据的档号结构的连接符',
                    type: 'error',
                    duration: 2000,
                    offset: 80
                    });
                return;
            }
            if(!element.fieldName){
                ElNotification({
                    title: '提示',
                    message: '请选择第'+(index+1)+'条数据的档号结构的元数据字段',
                    type: 'error',
                    duration: 2000,
                    offset: 80
                    });
                return;
            }
        }
    });

    let result = await existRules(props.categoryMark);
    if(!result.data){
        saveData();
    }else{
        ElMessageBox.confirm('档号规则已经存在，当前操作将会覆盖之前的档号规则，是否继续？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'info'
        })
        .then(async () => {
            saveData();
        })
        .catch(() => {
            ElMessage({
                type: 'info',
                message: '已取消操作',
                offset: 65
            });
        });
    }
    
}
async function saveData(){
    let res = { success: false, msg: '' };
    const loading = ElLoading.service({ lock: true, text: '正在处理中', background: 'rgba(0, 0, 0, 0.3)' });
    res = await saveRules(props.categoryMark,JSON.stringify(tableConfig.value.tableData));
    loading.close();
    if(res.success){
        props.dialogConfig_parent.show = false;
    }
    ElNotification({
        title: res.success ? '成功' : '失败',
        message: res.msg,
        type: res.success ? 'success' : 'error',
        duration: 2000,
        offset: 80
    });
}

</script>
<style lang="scss" scoped>
.numberRules{
    i{
        font-size: 22px;
        
    }
    p{
        font-size: 18px;
        font-weight: 500;
    }
}

</style>