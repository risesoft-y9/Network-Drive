<template>
    <div>
        <el-upload
            ref="uploadRef"
            class="avatar-uploader"
            action="#"
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleChangeFileList"
            :on-remove="handleRemove"
            style="width: 100%"
        >
            <template v-if="imageUrl">
                <el-image :src="imageUrl" class="avatar" fit="contain" />
            </template>
            <el-icon v-else class="avatar-uploader-icon">
                <Plus />
            </el-icon>
        </el-upload>
        <!-- vueCropper 剪裁图片实现-->
        <el-dialog title="图片剪裁" v-model="dialogVisible" append-to-body>
            <div class="cropper-content">
                <div class="cropper" style="text-align: center">
                    <VuePictureCropper
                        :box-style="boxStyle"
                        ref="cropperRef"
                        :img="corpImage"
                        :preset-mode="presetModeOptions"
                        :options="options"
                    ></VuePictureCropper>
                </div>
            </div>
            <div>
                <el-row>
                    <el-col :span="18">
                        <el-button @click="rotateImg(1)" :icon="RefreshRight">旋 转 ＋</el-button>
                        <el-button @click="rotateImg(-1)" :icon="RefreshLeft">旋 转 －</el-button>
                    </el-col>
                    <el-col :span="6" style="text-align: right">
                        <el-button @click="dialogVisible = false">取 消</el-button>
                        <el-button type="primary" @click="finish" :loading="loading">确 认</el-button>
                    </el-col>
                </el-row>
            </div>
        </el-dialog>
    </div>
</template>
<script lang="ts" setup>
import { ref, onMounted, watch, nextTick } from 'vue';
import { ElMessage, ElNotification } from 'element-plus';
import type { UploadFile, UploadFiles } from 'element-plus';
import y9_storage from '@/utils/storage';
import settings from '@/settings';
import VuePictureCropper, { cropper } from 'vue-picture-cropper/dist/esm';

const dialogVisible = ref(false);

let loading = ref(false);
const imageUrl = ref('');
const corpImage = ref();
const cropperRef = ref();

const props = defineProps({
    name: {
        type: String
    },
    action: {
        type: String
    },
    assetsId: {
        type: String,
        default: 0
    },
    baseUri: {
        type: String,
        default: ''
    },
    modelValue: {
        type: String
    },
    width: {
        type: Number,
        default: 256
    },
    height: {
        type: Number,
        default: 256
    },
    form: {
        type: Object,
        default: () => {
            return {};
        }
    },
    accept: {
        type: String,
        required: false,
        default: () => {
            return 'jpg,jpeg,png';
        }
    }
});
let options = ref({
    viewMode: 1,
    dragMode: 'move',
    aspectRatio: props.width / props.height,
    cropBoxResizable: false,
    fixedBox: true, // 固定截图框大小 不允许改变
    original: false, // 上传图片按照原始比例渲染
    autoCropWidth: 300, // 默认生成截图框宽度
    autoCropHeight: 200, // 默认生成截图框高度
    centerBox: false
});

let options1 = ref({
    //img: '', // 裁剪图片的地址
    dragMode: 'move',
    cropBoxResizable: false,
    aspectRatio: props.width / props.height,
    info: true, // 裁剪框的大小信息
    outputSize: 1, // 裁剪生成图片的质量
    outputType: 'jpeg', // 裁剪生成图片的格式
    canScale: false, // 图片是否允许滚轮缩放
    autoCrop: true, // 是否默认生成截图框
    // autoCropWidth: 300, // 默认生成截图框宽度
    // autoCropHeight: 200, // 默认生成截图框高度
    fixedBox: true, // 固定截图框大小 不允许改变
    fixed: true, // 是否开启截图框宽高固定比例
    fixedNumber: [props.width, props.height], // 截图框的宽高比例
    full: true, // 是否输出原图比例的截图
    canMoveBox: true, // 截图框能否拖动
    original: false, // 上传图片按照原始比例渲染
    centerBox: true, // 截图框是否被限制在图片里面
    infoTrue: true // true 为展示真实输出图片宽高 false 展示看到的截图框宽高
});

const boxStyle = ref({
    width: '800px',
    height: '500px',
    backgroundColor: '#f8f8f8',
    margin: 'auto'
});

let presetModeOptions = ref({
    mode: 'fixedSize',
    width: props.width,
    height: props.height
});

const uploadRef = ref();
const fileList = ref([]);

const getBase64 = (file: any) => {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        let imgResult: any = '';
        reader.readAsDataURL(file);
        reader.onload = function () {
            imgResult = reader.result;
        };
        reader.onerror = function (error) {
            reject(error);
        };
        reader.onloadend = function () {
            resolve(imgResult);
        };
    });
};

const handleChangeFileList = (file: UploadFile, fileList: UploadFiles) => {
    let testmsg = file.name.substring(file.name.lastIndexOf('.') + 1);
    const isJPG = testmsg === 'jpg' || testmsg === 'jpeg';
    const isPNG = testmsg === 'png';
    //const isLt2M = file.size / 1024 / 1024 < 2;
    if (!isJPG && !isPNG) {
        ElMessage.error('上传图片只能是 JPG 或 PNG 格式!');
        return false;
    }

    getBase64(file.raw).then((base64) => {
        dialogVisible.value = true;
        corpImage.value = base64;
    });
};

const degreeNum = ref(0);
// 操作：旋转
const rotateImg = (num: number) => {
    degreeNum.value += num;
    // 设置旋转一次的幅度为 90°
    cropper.rotateTo((degreeNum.value % 4) * 90);
};

const finish = () => {
    cropper.getFile().then((file: any) => {
        handleUpload({ file: file });
    });
};

watch(
    () => props.modelValue,
    (nv, ov) => {
        setImageUrl();
    }
);

const setImageUrl = () => {
    if (props.modelValue) {
        imageUrl.value = props.modelValue;
    } else {
        imageUrl.value = '';
    }
};

const handleUpload = (option: any) => {
    let uploadUrl = import.meta.env.VUE_APP_CONTEXT + `vue/detail/uploadPicture?assetsId=` + props.assetsId;
    const formData = new FormData();
    formData.append('file', option.file); // 添加文件到FormData对象中

    // 使用 fetch API 发送请求，并设置 headers
    fetch(uploadUrl, {
        method: 'POST',
        headers: {
            'Authorization': 'Bearer ' + y9_storage.getObjectItem(settings.siteTokenKey, 'access_token'),
        },
        body: formData,
    })
    .then(response => response.json()) // 服务器返回 JSON 格式的数据
    .then(res => {
        if (res.success) {
            imageUrl.value = corpImage.value;
            dialogVisible.value = false;
            emits('setId', res.data?.id);
            emits('update:modelValue', res.data?.picture);
        }
        ElNotification({
            title: res.success ? '成功' : '失败',
            message: res.msg,
            type: res.success ? 'success' : 'error',
            duration: 2000,
            offset: 80
        });
    })
    .catch(error => {
        console.error('上传失败:', error);
    });
};

type Emit = {
    (event: 'update:modelValue', val: string): void;
    (event: 'setId', val: null): void;
};
const emits = defineEmits<Emit>();

const handleRemove = (uploadFile: UploadFile, uploadFiles: UploadFiles) => {
    emits('update:modelValue', '');
};

onMounted(() => {
    setImageUrl();
});
</script>
<style lang="scss" scoped>
[class^='ri-'],
[class*=' ri-'] {
    font-size: 50px;
    font-weight: 600;
}

.cropper-content {
    margin-bottom: 20px;
}

.avatar-uploader .avatar {
    width: 178px;
    height: 178px;
    display: block;
}
</style>
<style>
.avatar-uploader .el-upload {
    border: 1px dashed var(--el-border-color);
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
    border-color: var(--el-color-primary);
}

.el-icon.avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 178px;
    height: 178px;
    text-align: center;
}
</style>