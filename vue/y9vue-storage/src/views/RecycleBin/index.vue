<template>
  <y9Card :showHeader="false">
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button type="primary" v-if="multipleSelection.length" class="global-btn-second" @click="permanentlyDelete" :size="fontSizeObj.buttonSize"
                     :style="{ fontSize: fontSizeObj.baseFontSize }"><i class="ri-delete-bin-line"></i>{{ $t('彻底删除') }}</el-button>
        <el-button v-if="multipleSelection.length" class="global-btn-second" @click="restore" :size="fontSizeObj.buttonSize"
                     :style="{ fontSize: fontSizeObj.baseFontSize }"><i class="ri-arrow-go-forward-fill"></i>{{ $t('还原') }}</el-button>
        <el-button :size="fontSizeObj.buttonSize"
         :style="{ fontSize: fontSizeObj.baseFontSize }" class="global-btn-second" v-on:click="emptyRecycleBin" plain><i class="ri-delete-bin-line"></i>{{ $t('清空回收站') }}</el-button>
        <el-button :size="fontSizeObj.buttonSize"
          :style="{ fontSize: fontSizeObj.baseFontSize }" class="global-btn-main" type="primary" @click="loadList"><i class="ri-refresh-line"></i>{{ $t('刷新') }}</el-button>
      </div>
    </div>
    <el-table
        ref="multipleTable"
        v-loading="loading"
        :data="rows"
        tooltip-effect="dark"
        style="width: 100%"
        :height="tableHeight"
        :empty-text="$t('暂无文件')"
        @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="45"></el-table-column>
      <!-- <el-table-column type="index" label="序号" width="60"></el-table-column> -->
      <el-table-column prop="name" :label="$t('文件名')">
        <template #default="name">
          <FileNameWithIcon :file-node="name.row" />
        </template>
      </el-table-column>
      <el-table-column prop="fileSize" :label="$t('大小')" width="120">
        <template #default="fileSize">
          {{ fileSize.row.fileSize ? fileSize.row.fileSize : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" :label="$t('删除时间')" width="170"> </el-table-column>
    </el-table>
  </y9Card>
</template>

<script lang="ts" setup>
import { ref, defineProps, onMounted, watch,computed,reactive, toRefs} from 'vue';
import type{ ElMessage, ElMessageBox } from 'element-plus';
import FileApi from '@/api/storage/file';
import FileNameWithIcon from '@/components/storage/FileNameWithIcon/index.vue';
import { useI18n } from 'vue-i18n';
const { t } = useI18n();
// 注入 字体对象
const fontSizeObj: any = inject('sizeObjInfo')||{};
const data = reactive({
      multipleTable:'',
      rows: [],
      multipleSelection: [],
      loading: false,
      tableHeight: window.innerHeight - 260,
      tableScreenHeight: window.innerHeight,
})

let {
      multipleTable,
      rows,
      multipleSelection,
      loading,
      tableHeight,
      tableScreenHeight,
} = toRefs(data);

onMounted(() => {
    loadList();
    window.onresize = () => {
      return (() => {
        window.screenHeight = window.innerHeight;
        tableScreenHeight.value = window.screenHeight;
      })();
    };
  })

function toggleSelection(rows) {
    if (rows) {
      rows.forEach(row => {
        multipleTable.value.toggleRowSelection(row);
      });
    } else {
      multipleTable.value.clearSelection();
    }
}

function handleSelectionChange(val) {
      multipleSelection.value = val;
}
    
function permanentlyDelete() {
    ElMessageBox.confirm(t('文件删除后将无法恢复，您确定要彻底删除所选文件吗？'), t('提示'), {
      confirmButtonText: t('确定'),
      cancelButtonText: t('取消'),
      type: 'info'
    }).then(() => {
      var IdArr = multipleSelection.value.map(item => item.id);
      FileApi.permanentlyDelete(IdArr).then(() => {
        loadList();
      });
    }).catch(() => {
      ElMessage({ type: "info", message: t("已取消操作"),offset:65});
    });
}

function loadList() {
    loading.value = true;
    FileApi.deletedList().then(res => {
      rows.value = res.data;
      loading.value = false;
    });
}

function restore() {
    ElMessageBox.confirm(t('确定还原所选文件？'), t('提示'), {
      confirmButtonText: t('确定'),
      cancelButtonText: t('取消'),
      type: 'info'
    }).then(() => {
      var IdArr = multipleSelection.value.map(item => item.id);
      FileApi.restore(IdArr).then(() => {
        loadList();
      });
    }).catch(() => {
      ElMessage({ type: "info", message: t("已取消操作"),offset:65});
    });
}

function emptyRecycleBin() {
    ElMessageBox.confirm(t('清空回收站后将无法恢复，您确定要清空回收站吗？'), t('提示'), {
      confirmButtonText: t('确定'),
      cancelButtonText: t('取消'),
      type: 'info'
    }).then(() => {
      FileApi.emptyRecycleBin().then(() => {
        loadList();
      });
    }).catch(() => {
        ElMessage({ type: "info", message: t("已取消操作"),offset:65});
    });
}
</script>

<style lang="scss" scoped>
@import "@/theme/global.scss";
:deep(.el-table__cell .cell){
  padding-left: 0px;
  font-size: v-bind('fontSizeObj.baseFontSize');
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
}

:deep(.el-table__empty-text) {
  font-size: v-bind('fontSizeObj.baseFontSize');
}

</style>
