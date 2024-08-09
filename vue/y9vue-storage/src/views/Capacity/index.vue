<template>
  <y9Card :showHeader="false">
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button  :size="fontSizeObj.buttonSize"
                    :style="{ fontSize: fontSizeObj.baseFontSize }" class="global-btn-second" @click="loadList"><i class="ri-refresh-line"></i>{{ $t('刷新') }}</el-button>
      </div>

      <div class="toolbar-right">
          <el-form :inline="true" @submit.native.prevent>
              <el-form-item :label="$t('人员名称')">
                <el-input v-model="userName" :placeholder="$t('输入名称搜索')" @keyup.enter.native="loadList" class="search-input global-btn-second" clearable>
                  <template #append>
                    <el-button :size="fontSizeObj.buttonSize"
                               :style="{ fontSize: fontSizeObj.baseFontSize }" slot="append" @click="loadList"><i class="ri-search-line global-btn-second"></i></el-button>
                  </template>
                </el-input>
                </el-form-item>
          </el-form>
        </div>
      </div>
    <y9Table :config="tableConfig">
      <template #capacitySlot="{row , column , index}">
        <el-form class="formClass" ref="capacityForm" :model="formData">
          <el-form-item prop="name" v-if="editIndex === index">
            <el-input type="number" style="width:200px;margin-left:15px;" ref="nameSign" v-model="formData.capacitySize" clearable/>
          </el-form-item>
          <span v-else>{{row.capacitySize}}</span>
        </el-form>
      </template>
      <template #opt="{row , column , index}">
        <div v-if="editIndex === index">
            <el-button :size="fontSizeObj.buttonSize"
                       :style="{ fontSize: fontSizeObj.baseFontSize }" style="margin-left: 14px;" type="primary" @click="saveData(capacityForm)" plain><i class="ri-check-line"></i>{{ $t('保存') }}</el-button>
            <el-button :size="fontSizeObj.buttonSize"
                       :style="{ fontSize: fontSizeObj.baseFontSize }" type="primary" @click="cancalData(capacityForm)" plain><i class="ri-close-line"></i>{{ $t('取消') }}</el-button>
        </div>
        <div v-else>
          <el-button :size="fontSizeObj.buttonSize"
                     :style="{ fontSize: fontSizeObj.baseFontSize }" class="global-btn-third" @click="edit(row,index)">
          <i class="ri-edit-2-line"></i>{{ $t('编辑') }}
          </el-button>
        </div>
      </template>
      </y9Table>
  </y9Card>
</template>

<script lang="ts" setup>
import { ref, defineProps, onMounted, watch,computed,reactive, toRefs, inject} from 'vue';
import type{ ElMessage, ElMessageBox } from 'element-plus';
import CapacityApi from '@/api/storage/capacity';
import { useI18n } from 'vue-i18n';
const { t } = useI18n();
const fontSizeObj: any = inject('sizeObjInfo')||{};
const data = reactive({
      capacityForm:'',
      formData: { id:'',capacitySize:''},
      editIndex: -1,
      userName:'',
      tableConfig: {
        columns: [
          { title: computed(() => t("序号")), type:'index', width: '60', },
          { title: computed(() => t("所属人")), key:'capacityOwnerName', width: 'auto' },
          { title: computed(() => t("总存储空间")), key: "capacitySize", width: '200',slot:'capacitySlot'},
          { title: computed(() => t("剩余存储空间")), key: "remainingLength", width: '150',},
          { title: computed(() => t("创建时间")), key: "createTime", width: '180',},
          { title: computed(() => t("修改时间")), key: "updateTime", width: '180',},
          { title: computed(() => t("操作")), key: "opt", width: '180',slot:'opt'}
        ],
        tableData: [],
        pageConfig: {
            currentPage: 1,
            pageSize: 20,
            total: 0,
        },
        border:0,
        height:window.innerHeight - 330
      },
})

let {
      capacityForm,
      formData,
      editIndex,
      userName,
      tableConfig,
} = toRefs(data);

onMounted(() => {
    loadList();
  })

function loadList() {
    let page = tableConfig.value.pageConfig.currentPage;
    let rows = tableConfig.value.pageConfig.pageSize;
    CapacityApi.getCapacityList(userName.value,page,rows).then(res => {
      tableConfig.value.tableData = res.rows;
      tableConfig.value.pageConfig.total = res.total;
    });
}

async function edit(row,index){
  editIndex.value = index;
  formData.value.id = row.id;
  let res = await CapacityApi.getCapacityInfo(row.id);
  formData.value.capacitySize = res.data.capacitySize;
}

function cancalData (fileForm){
  editIndex.value = -1;
  formData.value.id = '';
  formData.value.capacitySize = '';
  fileForm.resetFields();
 }

 function saveData(form){
  if(!form) return;
  form.validate(valid => {
    if (valid) {
      console.log(formData.value);
      CapacityApi.updateCapacity(formData.value).then(res => {
      	ElMessage({ type: res.success?'success':'error', message: res.msg, offset: 65 });
        editIndex.value = -1;
        loadList();
      });
    }
  });
 }

</script>

<style lang="scss" scoped>
@import "@/theme/global.scss";
:deep(.el-table__cell .cell){
  padding-left: 0px;
}

:deep(.el-table-column--selection .cell){
  padding-left: 12px;
  padding-right: 12px;
}

:deep(.el-table__inner-wrapper::before) {
    height: 0px;
}
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
  :deep(.el-button){
    height: 30px;
    line-height: 0;
    min-width:0px;
    box-shadow: 0px 0px 0px 0px rgb(0 0 0 / 6%);
    padding: 8px 15px;
  }
}

.formClass .el-form-item{
  margin-bottom: 0px;
  :deep(.el-button){
    min-width:40px;
  }
}

.search-input {
  width: 250px;
  margin-right: 10px;
}
</style>
