<template>
    <el-row class="rowCss">
        <el-input v-model="file.name" :prefix-icon="Document" :readonly="true" class="w-50 m-2" />
    </el-row>
    <el-row class="rowCss">
        <el-input v-model="file.createTime" :prefix-icon="Calendar" :readonly="true" class="w-50 m-2" />
    </el-row>
    <el-row class="rowCss">
        <el-input v-model="file.fileSize" :prefix-icon="Coin" :readonly="true" class="w-50 m-2" />
    </el-row>
    <el-row class="rowCss">
        <el-col :span="4" class="label-col"><span>有效期设置：</span></el-col>
        <el-col :span="20">
         <el-radio-group v-model="validPeriod" aria-label="label position">
            <el-radio-button value="1天">1天</el-radio-button>
            <el-radio-button value="7天">7天</el-radio-button>
            <el-radio-button value="30天">30天</el-radio-button>
            <el-radio-button value="1年">1年</el-radio-button>
            <el-radio-button value="永久有效">永久有效</el-radio-button>
        </el-radio-group>
      </el-col>
    </el-row>
    <!-- <el-row class="rowCss" v-if="expireTime">
        <el-col :span="4" class="label-col"><span>到期时间：</span></el-col>
        <el-col :span="20">
            <span>{{ expireTime }}</span>
        </el-col>
    </el-row> -->
    <!-- <el-row class="rowCss">
        <el-col :span="4" class="label-col"><span>加密设置：</span></el-col>
        <el-col :span="20">
        <el-radio-group v-model="encryption" @change="changePwd">
            <el-radio :label="true" border>{{ $t('加密') }}</el-radio>
            <el-radio :label="false" border>{{ $t('不加密') }}</el-radio>
        </el-radio-group>
      </el-col>
    </el-row> -->
    <!-- <el-row class="rowCss" v-if="urlShow">
        <el-input
            ref="linkRef"
            v-model="file.fileUrl"
            :prefix-icon="Link"
            :readonly="true"
            class="w-50 m-2"
            @click="selectlink"
        />
        
    </el-row> -->
    
    <!-- <el-row class="rowCss center" v-if="!encryption">
        <el-button 
                :size="fontSizeObj.buttonSize"
                :style="{ fontSize: fontSizeObj.baseFontSize }"
                class="global-btn-main"
                type="primary"
                @click="copyLink"
                >{{ $t('复制链接') }}
            </el-button>
    </el-row> -->
    <el-row class="rowCss">
        <el-col :span="4" class="label-col"><span>下载码：</span></el-col>
        <el-col :span="20">
         <el-radio-group v-model="createType" aria-label="label position">
            <el-radio-button value="随机生成">随机生成</el-radio-button>
            <el-radio-button value="自定义">自定义</el-radio-button>
        </el-radio-group>
      </el-col>
    </el-row>
    <!-- 自定义：显示输入框 -->
    <el-row v-if="createType === '自定义'" class="rowCssTwo">
        <el-col :span="4" class="label-col"><span>自定义码：</span></el-col>
        <el-col :span="20"> 
            <el-input v-if="pwdShow"
                v-model="decodedLinkPassword"
                :prefix-icon="Lock"
                class="w-50 m-2"
                clearable
                maxlength="4"
                show-password
                show-word-limit
                type="password"
                >
                <template #append>四位数密码（字母、数字）</template>
            </el-input>
            <el-input v-if="linkPwdShow"
                v-model="linkPwd"
                :prefix-icon="Lock"
                class="w-50 m-2"
                type="text"
                style="width: 30%;"
            >
            </el-input>
        </el-col>   
    </el-row>
    <el-row  :gutter="10" class="rowCss center">
        <el-button v-if="copyLinkBtn"
                :size="fontSizeObj.buttonSize"
                :style="{ fontSize: fontSizeObj.baseFontSize }"
                class="global-btn-main"
                type="primary"
                @click="copyLinkPwd"
                >{{ $t('复制链接及下载码') }}
            </el-button>
        <el-button 
                v-if="createLinkBtn"
                :size="fontSizeObj.buttonSize"
                :style="{ fontSize: fontSizeObj.baseFontSize }"
                class="global-btn-main"
                type="primary"
                @click="submitPwd('save')"
                >{{ $t('创建链接') }}
            </el-button>
    </el-row>
   
</template>
<script lang="ts" setup>
    import { computed, defineProps, inject, nextTick, reactive, toRefs, watch } from 'vue';
    import { Calendar, Coin, Document, Link, Lock } from '@element-plus/icons-vue';
    import FileApi from '@/api/storage/file';
    import type { ElMessage } from 'element-plus';
    import { useStorageStore } from '@/store/modules/storageStore';
    import { useI18n } from 'vue-i18n';

    const { t } = useI18n();
    const fontSizeObj: any = inject('sizeObjInfo') || {};
    const storageStore = useStorageStore();
    const props = defineProps({
        fileObject: {
            //表格配置
            type: Object,
            default: () => {
                return {};
            }
        }
    });

    let data = reactive({
        linkUrl: props.fileObject.fileUrl,
        file: {},
        linkRef: '',
        decodedLinkPassword:'',
        copyLinkBtn:false,
        createLinkBtn:true,
        encryption:false,
        pwdShow: true,
        urlShow: true,
        linkPwd:'',
        linkPwdShow: false,
        expireTime: '',
        createType: '随机生成',
        validPeriod: '1年'
    });
    let { linkUrl, file, linkRef ,decodedLinkPassword,copyLinkBtn,createLinkBtn,encryption,pwdShow,urlShow,linkPwd,linkPwdShow,expireTime,createType,validPeriod} = toRefs(data);
    watch(
        () => props.fileObject,
        (newVal) => {
            file.value = newVal;
            if (newVal.encryption) {
                // try {
                //     if(props.fileObject.linkPassword){
                //         decodedLinkPassword.value = atob(props.fileObject.linkPassword);
                //     }
                // } catch(e){
                //     decodedLinkPassword.value = '';
                // }
            }
        },
        {
            deep: true,
            immediate: true
        }
    );


    // 根据有效期计算到期时间
    const calcexpireTime = computed(() => {
        const period = validPeriod.value;
        if (!period || period === '永久有效') {
            return '';
        }
        const now = new Date();
        const match = period.match(/^(\d+)(天|年)$/);
        if (!match) return '';
        const num = parseInt(match[1]);
        const unit = match[2];
        if (unit === '天') {
            now.setDate(now.getDate() + num);
        } else if (unit === '年') {
            now.setFullYear(now.getFullYear() + num);
        }
        const y = now.getFullYear();
        const m = String(now.getMonth() + 1).padStart(2, '0');
        const d = String(now.getDate()).padStart(2, '0');
        return `${y}-${m}-${d}`;
    });

    // 同步到期时间到 file 和 fileObject
    watch(calcexpireTime, (val) => {
        expireTime.value = val;
        props.fileObject.expireTime = val;
    }, { immediate: true });

    /**
     * 编码 tenantId + linkKey 到 URL 安全字符串
     * 规则: btoa(tenantId + '|' + linkKey) → URL-safe Base64
     */
    function encodeTenantKey(tenantId: string, linkKey: string): string {
        return btoa(tenantId + '|' + linkKey)
            .replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');
    }

    // 生成6位随机密码（字母+数字）
    function generateRandomCode(): string {
        const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
        let code = '';
        for (let i = 0; i < 4; i++) {
            code += chars.charAt(Math.floor(Math.random() * chars.length));
        }
        return code;
    }

    // 监听创建类型切换
    watch(createType, (val) => {
        if (val === '随机生成') {
            decodedLinkPassword.value = generateRandomCode();
        } else {
            decodedLinkPassword.value = '';
        }
    }, { immediate: true });

    function changePwd(val) {
        props.fileObject.encryption = val;
        linkPwdShow.value = false;
        if (val) {
           // props.fileObject.fileUrl = '';
            urlShow.value = false;
            copyLinkBtn.value = false;
            pwdShow.value = true;
            createLinkBtn.value = true;
        } else {
            urlShow.value = true;
            createLinkBtn.value = false;
            pwdShow.value = false;
            props.fileObject.fileUrl = linkUrl.value;
            decodedLinkPassword.value = '';
            //submitPwd('clear');
        }
    }

    function copyLink(){
        if (file.value.fileUrl) {
            navigator.clipboard.writeText(file.value.fileUrl)
                .then(() => {
                    ElMessage({ type: 'success', message: t('链接已复制'), offset: 65 });
                })
                .catch(err => {
                    console.error('复制失败:', err);
                    ElMessage({ type: 'error', message: t('复制失败'), offset: 65 });
                });
        } else {
            ElMessage({ type: 'error', message: t('链接为空'), offset: 65 });
        }
    }

    function copyLinkPwd(){
        if (file.value.fileUrl) {
            let linkUrl = "下载链接：" + file.value.fileUrl + "\n下载码：" + linkPwd.value;
            navigator.clipboard.writeText(linkUrl)
                .then(() => {
                    ElMessage({ type: 'success', message: t('链接已复制'), offset: 65 });
                })
                .catch(err => {
                    console.error('复制失败:', err);
                    ElMessage({ type: 'error', message: t('复制失败'), offset: 65 });
                });
        } else {
            ElMessage({ type: 'error', message: t('链接为空'), offset: 65 });
        }
    }

    function selectlink() {
        nextTick(() => {
            linkRef.value.focus();
            linkRef.value.select();
            if (file.value.fileUrl) {
                navigator.clipboard.writeText(file.value.fileUrl)
                .then(() => {
                    ElMessage({ type: 'success', message: t('链接已复制'), offset: 65 });
                })
                .catch(err => {
                    console.error('复制失败:', err);
                    ElMessage({ type: 'error', message: t('复制失败'), offset: 65 });
                });
            }
        });
    }

    function submitPwd(type) {
        if (decodedLinkPassword.value) {
            if(decodedLinkPassword.value.length < 4){
                ElMessage({ type: 'error', message: t('请输入长度为四位的下载码'), offset: 65 });
                return;
            }
            let key = generateRandomCode();
            let linkKey = encodeTenantKey(storageStore.tenantId, key);
            FileApi.createLink({
                fileId: file.value.id,
                validPeriod: validPeriod.value,
                expireTime: expireTime.value,
                linkPassword: decodedLinkPassword.value,
                linkKey: linkKey
            }).then(res => {
                if(res.success){
                    let link = res.data;
                    file.value.fileUrl =  link.linkUrl + '/link/' + link.linkKey;
                    copyLinkBtn.value = true;
                    createLinkBtn.value = false;
                    pwdShow.value = false;
                    urlShow.value = true;
                    linkPwdShow.value = true;
                    linkPwd.value = atob(link.linkPassword);
                }
                if (type == 'save') {
                    ElMessage({ type: res.success ? 'success' : 'error', message: res.msg, offset: 65 });
                }
            });
            // FileApi.setLinkPwd(file.value.id, file.value.encryption, decodedLinkPassword.value).then((res) => {
            //     if (type == 'save') {
            //         ElMessage({ type: res.success ? 'success' : 'error', message: res.msg, offset: 65 });
            //     }
            // });
        } else {
            ElMessage({ type: 'error', message: t('请输入文件下载密码'), offset: 65 });
            return;
        }
    }
</script>
<style lang="scss" scoped>
    @import '@/theme/global.scss';

    .rowCss {
        padding: 10px 0px;
        color: var(--el-input-text-color, var(--el-text-color-regular));
        :deep(.el-input__wrapper) {
            box-shadow: 0 0 0 0px var(--el-input-border-color, var(--el-border-color)) inset;
        }

        
    }
    .label-col {
        text-align: right;
    }
   
    .rowCssTwo {
        padding: 10px 0px;    
        color: var(--el-input-text-color, var(--el-text-color-regular));
    }
    .center {
            text-align: center;
            display: block;
    }
</style>
