<!--
 * @Description: 自定义标签创建组件
-->
<template>
    <div class="custom-tag-container">
        <el-form ref="formRef" :model="formData" :rules="rules" label-width="90px" size="default">
            <!-- 标签名称 -->
            <el-form-item :label="$t('标签名称')" prop="tagName">
                <el-input
                    v-model="formData.tagName"
                    :placeholder="$t('请输入标签名称')"
                    maxlength="20"
                    show-word-limit
                    clearable
                />
            </el-form-item>   
            
            <!-- 标签颜色 -->
            <el-form-item :label="$t('标签颜色')" prop="tagColor">
                <div class="color-picker-wrapper">
                    <div class="color-options">
                        <div
                            v-for="color in colorOptions"
                            :key="color.value"
                            class="color-option"
                            :class="{ active: formData.tagColor === color.value }"
                            :style="{ backgroundColor: color.value }"
                            @click="selectColor(color.value)"
                        >
                            <i v-if="formData.tagColor === color.value" class="ri-check-line"></i>
                        </div>
                    </div>
                    <el-color-picker
                        v-model="formData.tagColor"
                        show-alpha
                        :predefine="colorPredefine"
                        class="color-picker"
                    />
                </div>
            </el-form-item>
            
            <!-- 标签描述 -->
            <el-form-item :label="$t('标签描述')" prop="description">
                <el-input
                    v-model="formData.description"
                    :placeholder="$t('请输入标签描述（可选）')"
                    type="textarea"
                    :rows="3"
                    maxlength="100"
                    show-word-limit
                />
            </el-form-item>
            
            <!-- 预览 -->
            <el-form-item :label="$t('标签预览')">
                <div class="preview-wrapper">
                    <el-tag
                        :style="{
                            backgroundColor: formData.tagColor + '33',
                            borderColor: formData.tagColor,
                            color: formData.tagColor
                        }"
                        class="preview-tag"
                    >
                        {{ formData.tagName || $t('标签名称') }}
                    </el-tag>
                </div>
            </el-form-item>
        </el-form>
    </div>
</template>

<script lang="ts" setup>
import { ref, reactive, defineProps, defineEmits, onMounted } from 'vue';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { useI18n } from 'vue-i18n';
import FileTagApi from '@/api/storage/fileTag';

const { t } = useI18n();

const props = defineProps({
    tagData: {
        type: Object,
        default: () => {}
    }
});

onMounted(() => {
    // 初始化表单数据
    console.log(props.tagData);
    if (props.tagData) {
        Object.assign(formData, props.tagData);
    }else{
        formData.tagName = '';
        formData.tagColor = '#409EFF';
        formData.description = '';
    }
});

const emit = defineEmits(['success', 'cancel']);

const formRef = ref<FormInstance>();

// 表单数据
const formData = reactive({
    tagName: '',
    tagColor: '#409EFF',
    description: ''
});

// 表单验证规则
const rules: FormRules = {
    tagName: [
        { required: true, message: t('请输入标签名称'), trigger: 'blur' },
        { min: 1, max: 20, message: t('标签名称长度为 1-20 个字符'), trigger: 'blur' }
    ],
    tagColor: [
        { required: true, message: t('请选择标签颜色'), trigger: 'change' }
    ]
};

// 预设颜色选项
const colorOptions = ref([
    { value: '#409EFF', name: '蓝色' },
    { value: '#67C23A', name: '绿色' },
    { value: '#E6A23C', name: '橙色' },
    { value: '#F56C6C', name: '红色' },
    { value: '#909399', name: '灰色' },
    { value: '#13C2C2', name: '青色' },
    { value: '#722ED1', name: '紫色' },
    { value: '#EB2F96', name: '粉色' }
]);

// 颜色选择器预设值
const colorPredefine = ref([
    '#409EFF',
    '#67C23A',
    '#E6A23C',
    '#F56C6C',
    '#909399',
    '#13C2C2',
    '#722ED1',
    '#EB2F96',
    '#000000',
    '#FFFFFF'
]);

// 选择颜色
function selectColor(color: string) {
    formData.tagColor = color;
}


// 暴露方法给父组件
defineExpose({
    formData,
    formRef
});
</script>

<style lang="scss" scoped>
.custom-tag-container {
    padding: 10px;
    
    .color-picker-wrapper {
        display: flex;
        align-items: center;
        gap: 15px;
        width: 100%;
        
        .color-options {
            display: flex;
            gap: 8px;
            flex-wrap: wrap;
            flex: 1;
            
            .color-option {
                width: 32px;
                height: 32px;
                border-radius: 50%;
                cursor: pointer;
                display: flex;
                align-items: center;
                justify-content: center;
                border: 2px solid transparent;
                transition: all 0.3s;
                box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
                
                &:hover {
                    transform: scale(1.1);
                    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
                }
                
                &.active {
                    border-color: var(--el-color-primary);
                    box-shadow: 0 0 0 2px var(--el-color-primary-light-7);
                }
                
                i {
                    color: #fff;
                    font-size: 18px;
                    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
                }
            }
        }
        
        .color-picker {
            flex-shrink: 0;
        }
    }
    
    .preview-wrapper {
        padding: 20px;
        background: var(--el-fill-color-lighter);
        border-radius: 8px;
        display: flex;
        justify-content: center;
        align-items: center;
        
        .preview-tag {
            padding: 8px 16px;
            font-size: 14px;
            border-radius: 4px;
        }
    }
}
</style>