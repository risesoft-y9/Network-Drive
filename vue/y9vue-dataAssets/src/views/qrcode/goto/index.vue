<template>
    <y9Card style="height: calc(100vh - 2px)" title="数据信息">
        <y9Form :config="y9FormConfig"></y9Form>
    </y9Card>
</template>

<script lang="ts" setup>
    import { computed, h, onMounted, ref } from 'vue';
    import { useSettingStore } from '@/store/modules/settingStore';
    import { useI18n } from 'vue-i18n';
    import y9_storage from '@/utils/storage';
    import { getDataByQrCode } from '@/api/pretreat';

    const { t } = useI18n();
    const settingStore = useSettingStore();

    // 请求参数
    let params = ref({});
    // 数据信息
    let info = ref({});
    //表单配置
    let y9FormConfig = ref({
        descriptionsFormConfig: {
            //描述表单配置
            column: 1,
            labelAlign: 'center',
            labelWidth: '150px'
        },
        model: {},
        rules: {}, //表单验证规则
        itemList: [
            //表单显示列表
            {
                type: 'text',
                prop: 'name',
                label: computed(() => t('数据名称')),
                props: {
                    render: () => {
                        //text类型渲染的内容
                        return h('span', info.value.name);
                    }
                }
            },
            {
                type: 'text',
                prop: 'code',
                label: computed(() => t('数据编码')),
                props: {
                    render: () => {
                        //text类型渲染的内容
                        return h('span', info.value.code);
                    }
                }
            },
            {
                type: 'text',
                prop: 'remark',
                label: computed(() => t('数据摘要')),
                props: {
                    render: () => {
                        //text类型渲染的内容
                        return h('span', info.value?.remark);
                    }
                }
            },
            {
                type: 'text',
                prop: 'dataProvider',
                label: computed(() => t('数据提供方')),
                props: {
                    render: () => {
                        //text类型渲染的内容
                        return h('span', info.value.dataProvider);
                    }
                }
            },
            {
                type: 'text',
                prop: 'dataSize',
                label: computed(() => t('数据量')),
                props: {
                    render: () => {
                        //text类型渲染的内容
                        return h('span', info.value.dataSize);
                    }
                }
            },
            {
                type: 'text',
                prop: 'dataPurpose',
                label: computed(() => t('主要用途')),
                props: {
                    render: () => {
                        //text类型渲染的内容
                        return h('span', info.value?.dataPurpose);
                    }
                }
            },
            {
                type: 'text',
                prop: 'shareTypeName',
                label: computed(() => t('共享类型')),
                props: {
                    render: () => {
                        //text类型渲染的内容
                        return h('span', info.value.shareTypeName);
                    }
                }
            },
            {
                type: 'text',
                prop: 'dataType',
                label: computed(() => t('数据类型')),
                props: {
                    render: () => {
                        //text类型渲染的内容
                        return h('span', info.value.dataType);
                    }
                }
            }
        ]
    });

    onMounted(() => {
        let queryParams = y9_storage.getObjectItem('query');
        //console.log(queryParams)
        let codeValue = queryParams.code;
        if (queryParams.code.slice(-1) !== '/') {
            codeValue += '/';
        }
        params.value = { tenantId: queryParams.tenantId, qrcode: codeValue };
        initInfo();
    });

    async function initInfo() {
        if (!params.value.tenantId || !params.value.qrcode) return;
        let result = await getDataByQrCode(params.value);
        if (result.code == 0) {
            info.value = result.data;
        }
    }
</script>

<style lang="scss" scoped></style>
