<!--
 * @Author: yihong yihong@risesoft.net
 * @Date: 2026-03-13 16:58:12
 * @LastEditors: yihong yihong@risesoft.net
 * @LastEditTime: 2026-03-16 16:36:01
 * @FilePath: \y9-vue\y9vue-storage\src\components\storage\Tag\tagDetail.vue
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
-->
<template>
    <div class="tag-view-dialog">
        <div class="tag-info-item">
            <span class="label">{{ $t('标签名称') }}:</span>
            <!-- 查看模式 -->
            <!-- <template v-if="!isEditing"> -->
                <span class="value">{{ currentViewTag.tagName }}</span>
                <!-- <el-tooltip  :content="$t('编辑')" placement="top">
                    <i class="ri-edit-line edit-icon" @click="startEdit"></i>
                </el-tooltip> -->
            <!-- </template> -->
            <!-- 编辑模式 -->
            <!-- <template v-else>
                <el-input
                    v-model="editTagName"
                    class="tag-name-input"
                    placeholder="请输入标签名称"
                    ref="inputRef"
                    @keyup.enter="saveEdit"
                />
                <div class="edit-actions">
                    <el-tooltip  :content="$t('保存')" placement="top">
                        <i class="ri-save-3-line" :title="$t('保存')" @click="saveEdit"></i>
                    </el-tooltip>
                    <el-tooltip  :content="$t('取消')" placement="top">
                         <i class="ri-close-circle-line" :title="$t('取消')" @click="cancelEdit"></i>
                    </el-tooltip>
                </div>
            </template> -->
        </div>
        <div class="tag-info-item">
            <span class="label">{{ $t('标签 ID') }}:</span>
            <span class="value">{{ currentViewTag.id }}</span>
        </div>
        <div class="tag-info-item">
            <span class="label">{{ $t('所属文件') }}:</span>
            <span class="value">{{ currentViewFile.name }}</span>
        </div>
        <div class="tag-info-item">
            <span class="label">{{ $t('标签类型') }}:</span>
            <span class="value">{{ currentViewTag.tagType == 'customTag' ? $t('自定义标签') : $t('系统标签') }}</span>
        </div>
        <div class="tag-info-item" v-if="currentViewTag.createTime">
            <span class="label">{{ $t('创建时间') }}:</span>
            <span class="value">{{ currentViewTag.createTime }}</span>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, onMounted, defineProps } from 'vue';
import type { Tag } from '@/api/storage/file';
import { resolveComponent } from 'vue';

const props = defineProps({
    currentViewTag: {
        type: Object,
        default: () => ({})
    },
    currentViewFile: {
        type: Object,
        default: () => ({})
    }
});

const emit = defineEmits(['updateSuccess', 'cancelEdit']);

const isEditing = ref(false);
const editTagName = ref('');
const inputRef = ref();

function startEdit() {
    isEditing.value = true;
    editTagName.value = props.currentViewTag.tagName;
    nextTick(() => {
        inputRef.value?.focus();
        inputRef.value?.select();
    });
}

async function saveEdit() {
    if (!editTagName.value || editTagName.value.trim() === '') {
        ElMessage({ type: 'warning', message: t('标签名称不能为空'), offset: 65 });
        return;
    }
    
    if (editTagName.value === props.currentViewTag.tagName) {
        cancelEdit();
        return;
    }
    
    try {
        const res = await FileTagApi.updateFileTag(props.currentViewFile.id, props.currentViewTag.id, editTagName.value.trim());
        if (res.success) {
            ElMessage({ type: 'success', message: t('修改标签成功'), offset: 65 });
            emit('updateSuccess');
            isEditing.value = false;
        } else {
            ElMessage({ type: 'error', message: res.msg || t('修改失败'), offset: 65 });
        }
    } catch (error) {
        ElMessage({ type: 'error', message: t('修改失败'), offset: 65 });
    }
}

function cancelEdit() {
    isEditing.value = false;
    editTagName.value = '';
    emit('cancelEdit');
}

</script>
<style lang="scss" scoped>
.tag-view-dialog {
    padding: 20px;
    
    .tag-info-item {
        display: flex;
        align-items: center;
        padding: 12px 0;
        border-bottom: 1px solid var(--el-border-color-lighter);
        
        &:last-child {
            border-bottom: none;
        }
        
        .label {
            width: 100px;
            font-weight: 600;
            color: var(--el-text-color-regular);
            flex-shrink: 0;
        }
        
        .value {
            flex: 1;
            color: var(--el-text-color-primary);
            word-break: break-all;
        }
        
        .edit-icon {
            margin-left: 10px;
            font-size: 16px;
            color: var(--el-color-primary);
            cursor: pointer;
            padding: 4px;
            border-radius: 4px;
            
            &:hover {
                background-color: var(--el-color-primary-light-9);
                color: var(--el-color-primary-light-3);
            }
        }
        
        .tag-name-input {
            flex: 1;
            margin-right: 10px;
            
            :deep(.el-input__wrapper) {
                padding: 5px 10px;
            }
        }
        
        .edit-actions {
            display: flex;
            gap: 8px;
            
            .el-button {
                min-width: 60px;
            }
            i{
                font-size: 1.2vw;
                color: var(--el-color-primary) !important;
            }
        }
    }
}
</style>
