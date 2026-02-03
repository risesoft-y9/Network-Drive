<template>
    <div class="action-btns" v-if="!isAdd">
        <el-button
            v-if="!isEditStatus"
            :size="fontSizeObj.buttonSize"
            :style="{ fontSize: fontSizeObj.baseFontSize }"
            class="global-btn-main"
            type="primary"
            @click="onEdit"
        >
            <i class="ri-edit-line"></i>
            {{ $t('编辑') }}
        </el-button>
        <div v-else>
            <el-button
                :size="fontSizeObj.buttonSize"
                :style="{ fontSize: fontSizeObj.baseFontSize }"
                class="global-btn-main"
                type="primary"
                @click="onSave"
            >
                <i class="ri-save-line"></i>
                {{ $t('保存') }}
            </el-button>
            <el-button
                :size="fontSizeObj.buttonSize"
                :style="{ fontSize: fontSizeObj.baseFontSize }"
                class="global-btn-second"
                @click="onShow"
            >
                <i class="ri-close-line"></i>
                {{ $t('取消') }}
            </el-button>
        </div>
    </div>
    <div class="item-box" v-for="(item, index) in formList" :key="index">
        <el-divider content-position="left">{{ item.title }}</el-divider>
        <y9Form ref="y9FormRef" :config="item.formData"></y9Form>
    </div>
</template>

<script lang="ts" setup>
    import { pick } from 'lodash';
    import { inject, ref, watch, computed, onMounted, h } from 'vue';
    import { ElMessage, ElMessageBox, ElNotification } from 'element-plus';
    import { useI18n } from 'vue-i18n';
    import { getDataSourceById, saveDataConnectInfo } from '@/api/dataSource';
    const { t } = useI18n();
    // 注入 字体变量
    const fontSizeObj: any = inject('sizeObjInfo');

    const props = defineProps({
        isAdd: {
            type: Boolean
        },
        currNode: {
            type: Object,
            default: () => {
                return {};
            }
        },
        changeLoading: {
            type: Function
        },
        flxedTree: {
            type: Object
        },
        baseType: {
            type: String,
            default: ''
        }
    });

    const y9FormRef = ref(null);

    //表单初始值
    const initFormModels = {
        baseFormModel: {
            baseType: props.baseType,
            name: '',
            driver: '',
            username: '',
            password: '',
            baseSchema: '',
            runType: null,
            url: '',
            directory: ''
        },
        assistFormModel: {
            remark: '',
            isLook: 0,
            provider: '',
            contact: ''
        }
    };
    //基本信息-表单配置
    const baseFormConfig = ref({
        //描述表单配置
        descriptionsFormConfig: {
            column: 3,
            labelAlign: 'center',
            labelWidth: '150px',
            contentWidth: '180px'
        },
        //表单属性
        model: initFormModels.baseFormModel,
        //表单验证规则
        rules: {},
        //表单显示列表
        itemList: [
            {
                type: 'input',
                type1: 'input',
                type2: 'text',
                prop: 'baseType',
                label: computed(() => t('数据源类型')),
                required: true,
                props: {
                    readonly: true
                }
            },
            {
                type: 'input',
                type1: 'input',
                type2: 'text',
                prop: 'name',
                label: computed(() => t('数据源名称')),
                required: true,
                span: 2,
                props: {}
            },
            {
                type: 'input',
                type1: 'input',
                type2: 'text',
                prop: 'url',
                label: computed(() => t('连接地址')),
                required: true,
                span: 3,
                props: {
                    placeholder: `例：jdbc:${'postgresql'}://{host}:{port}/{database}`
                }
            },
            {
                type: 'input',
                type1: 'input',
                type2: 'text',
                prop: 'username',
                label: computed(() => t('用户名')),
                required: true,
                props: {}
            },
            {
                type: 'input',
                type1: 'input',
                type2: 'text',
                prop: 'password',
                label: computed(() => t('密码')),
                required: true,
                props: {}
            },
            {
                type: 'input',
                type1: 'input',
                type2: 'text',
                prop: 'baseSchema',
                label: computed(() => t('schema')),
                props: {}
            }
        ],
        itemLists: [
            {
                type: 'input',
                type1: 'input',
                type2: 'text',
                prop: 'baseType',
                label: computed(() => t('数据源类型')),
                required: true,
                props: {
                    readonly: true
                }
            },
            {
                type: 'input',
                type1: 'input',
                type2: 'text',
                prop: 'name',
                label: computed(() => t('数据源名称')),
                required: true,
                span: 2,
                props: {}
            },
            {
                type: 'input',
                type1: 'input',
                type2: 'text',
                prop: 'url',
                label: computed(() => t('连接地址')),
                required: true,
                span: 3,
                props: {
                    placeholder: `例：jdbc:${'postgresql'}://{host}:{port}/{database}`
                }
            },
            {
                type: 'input',
                type1: 'input',
                type2: 'text',
                prop: 'username',
                label: computed(() => t('用户名')),
                required: true,
                props: {}
            },
            {
                type: 'input',
                type1: 'input',
                type2: 'text',
                prop: 'password',
                label: computed(() => t('密码')),
                required: true,
                props: {}
            },
            {
                type: 'input',
                type1: 'input',
                type2: 'text',
                prop: 'baseSchema',
                label: computed(() => t('schema')),
                props: {}
            }
        ]
    });

    //辅助信息-表单配置
    const assistFormConfig = ref({
        //描述表单配置
        descriptionsFormConfig: {
            column: 3,
            labelAlign: 'center',
            labelWidth: '150px',
            contentWidth: '180px'
        },
        //表单属性
        model: initFormModels.assistFormModel,
        //表单验证规则
        rules: {},
        //表单显示列表
        itemList: [
            {
                type: 'switch',
                type1: 'switch',
                type2: 'text',
                prop: 'isLook',
                label: computed(() => t('元数据信息')),
                props: {
                    activeValue: 1,
                    inactiveValue: 0
                }
            },
            {
                type: 'input',
                type1: 'input',
                type2: 'text',
                prop: 'provider',
                label: computed(() => t('提供方')),
                props: {}
            },
            {
                type: 'input',
                type1: 'input',
                type2: 'text',
                prop: 'contact',
                label: computed(() => t('联系人')),
                props: {}
            },
            {
                type: 'textarea',
                type1: 'textarea',
                type2: 'text',
                span: 3,
                prop: 'remark',
                label: computed(() => t('备注')),
                props: {}
            }
        ],
        itemLists: [
            {
                type: 'switch',
                type1: 'switch',
                type2: 'text',
                prop: 'isLook',
                label: computed(() => t('元数据信息')),
                props: {
                    activeValue: 1,
                    inactiveValue: 0
                }
            },
            {
                type: 'input',
                type1: 'input',
                type2: 'text',
                prop: 'provider',
                label: computed(() => t('提供方')),
                props: {}
            },
            {
                type: 'input',
                type1: 'input',
                type2: 'text',
                prop: 'contact',
                label: computed(() => t('联系人')),
                props: {}
            },
            {
                type: 'textarea',
                type1: 'textarea',
                type2: 'text',
                span: 3,
                prop: 'remark',
                label: computed(() => t('备注')),
                props: {}
            }
        ]
    });

    //表单列表
    const formList = ref([
        {
            title: '基本信息',
            formData: baseFormConfig
        },
        {
            title: '辅助信息',
            formData: assistFormConfig
        }
    ]);

    //ftp类型，存放在基本信息表单中的专属表单字段
    const ftpItemList = [
        {
            type: 'radio',
            type1: 'radio',
            type2: 'text',
            prop: 'runType',
            label: '连接模式',
            required: true,
            span: 3,
            props: {
                options: [
                    {
                        label: '被动模式',
                        value: 1
                    },
                    {
                        label: '主动模式',
                        value: 0
                    }
                ]
            }
        },
        {
            type: 'input',
            type1: 'input',
            type2: 'text',
            prop: 'url',
            label: '连接地址',
            required: true,
            props: {
                placeholder: `例：127.0.0.1:21`
            }
        },
        {
            type: 'input',
            type1: 'input',
            type2: 'text',
            prop: 'directory',
            label: '文件目录',
            required: true,
            span: 2,
            props: {}
        }
    ];

    /**
     * 根据数据源类型设置对应的表单字段
     * @param type 数据源类型
     * @param newModels 表单的初始值
     */
    const changeType = (type, newModels = {}) => {
        if (!type) return;
        const models = Object.assign(
            {
                ...initFormModels.baseFormModel,
                ...initFormModels.assistFormModel,
                baseType: type
            },
            newModels
        );
        baseFormConfig.value.itemList.forEach((item)=>{
            if(item.prop=='username'||item.prop=='password'){
                if(type === 'elasticsearch'){
                    item.required=false
                }else{
                    item.required=true
                }
            }
        })
        
        baseFormConfig.value.itemList=JSON.parse(JSON.stringify(baseFormConfig.value.itemLists))
        assistFormConfig.value.itemList=JSON.parse(JSON.stringify(assistFormConfig.value.itemLists))

        baseFormConfig.value.rules = {
            baseType: [{ required: true, message: computed(() => t('请选择数据源类型')), trigger: 'change' }],
            name: [{ required: true, message: computed(() => t('请输入数据源名称')), trigger: 'blur' }],
            username: [{ required: true, message: computed(() => t('请输入用户名')), trigger: 'blur' }],
            password: [{ required: true, message: computed(() => t('请输入密码')), trigger: 'blur' }],
            runType: [{ required: true, message: computed(() => t('请选择连接模式')), trigger: 'change' }],
            url: [{ required: true, message: computed(() => t('请输入连接地址')), trigger: 'blur' }],
            directory: [{ required: true, message: computed(() => t('请输入文件目录')), trigger: 'blur' }]
        };

        //显示|隐藏对应的基本信息的字段。
        if (type === 'ftp') {
            if (baseFormConfig.value.itemList[2].prop !== 'runType') {
                //baseFormConfig.value.itemList.splice(5, 1);
                baseFormConfig.value.itemList[5].label = '编码方式';
                baseFormConfig.value.itemList.splice(2, 1, ...ftpItemList);
            }
        } else if (type === 'elasticsearch') {
            baseFormConfig.value.itemList.splice(5, 1);
            baseFormConfig.value.itemList[2].props.placeholder = `例：http||https://{host}:{port}/`;
        } else {
            baseFormConfig.value.itemList[2].props.placeholder = `例：jdbc:${type}://{host}:{port}/{database}`;
        }

        //初始化表单数据——基本信息表单
        baseFormConfig.value.model = pick(models, Object.keys(initFormModels.baseFormModel));

        //初始化表单数据——辅助信息表单
        assistFormConfig.value.model = pick(models, Object.keys(initFormModels.assistFormModel));

        //重置表单-为了重置校验
        const formRefs = y9FormRef.value;
        if (formRefs) {
            formRefs.forEach((itemRef) => {
                itemRef.elFormRef.resetFields();
            });
        }
    };

    /**
     * 切换表单状态
     * @param isEdit Boolean 是否为编辑状态
     * @param info Object 非编辑状态的字段信息
     */
    const changeFormStatus = (isEdit) => {
        //根据数据源类型，显示对应的字段
        changeType(props.currNode.baseType, dataSourceInfo.value);

        //根据表单状态来切换对应的字段显示状态
        baseFormConfig.value.itemList.forEach((item) => {
            if (isEdit) {
                item.type = item.type1;
                item.props.render = null;
            } else {
                item.type = item.type2;
                item.props.render = () => {
                    return h('span', baseFormConfig.value.model[item.prop] || '');
                };
                baseFormConfig.value.rules[item.prop] = [];
            }
        });
        assistFormConfig.value.itemList.forEach((item) => {
            if (isEdit) {
                item.type = item.type1;
                item.props.render = null;
            } else {
                item.type = item.type2;
                if(item.prop=='isLook'){
                    if(assistFormConfig.value.model.isLook==0){
                        item.props.render = () => {
                            return h('span',  '关闭');
                        };
                    }else{
                        item.props.render = () => {
                            return h('span',  '开启');
                        };
                    }
                }else{
                    item.props.render = () => {
                        return h('span',  assistFormConfig.value.model[item.prop] || '');
                    };
                }
            }
        });
    };

    //是否为编辑状态
    const isEditStatus = ref(false);

    //保存按钮触发
    const onSave = async () => {
        let formData = {
            id: props.currNode.id,
            type: props.currNode.type
        }; //记录表单数据
        let checkPassCount = 0; //记录表单校验通过数量

        for (let i = 0; i < y9FormRef.value.length; i++) {
            const itemRef = y9FormRef.value[i];
            //逐个校验表单
            const validResult = await itemRef.elFormRef.validate(() => {});
            //校验通过，提取表单数据
            if (validResult) {
                Object.assign(formData, itemRef.model);
                checkPassCount++;
            }
        }
        //所有表单校验通过，提交数据
        if (checkPassCount === formList.value.length) {
            props.changeLoading && props.changeLoading(true);

            //请求接口
            let result = await saveDataConnectInfo(formData);
            props.changeLoading && props.changeLoading(false);
            ElNotification({
                title: result.success ? t('保存成功') : t('保存失败'),
                message: result.msg,
                type: result.success ? 'success' : 'error',
                duration: 2000,
                offset: 80
            });

            await initDataSourceDetail(props.currNode.id)
            await saveFormList()
            await onShow();
            // 刷新树
            // props.flxedTree?.onRefreshTree();
        } else {
            ElMessage({
                type: 'error',
                message: t('校验不通过，请检查'),
                offset: 65
            });
        }
    };
    const saveFormList=async ()=>{
        const models = Object.assign(
            {
                ...initFormModels.baseFormModel,
                ...initFormModels.assistFormModel,
                baseType: props.currNode.baseType
            },
            dataSourceInfo.value
        );
        //初始化表单数据——基本信息表单
        baseFormConfig.value.model = pick(models, Object.keys(initFormModels.baseFormModel));
        //初始化表单数据——辅助信息表单
        assistFormConfig.value.model = pick(models, Object.keys(initFormModels.assistFormModel));
    }
    //编辑按钮触发
    const onEdit = () => {
        isEditStatus.value = true;
        //改变表单状态
        changeFormStatus(true);
    };
    //恢复展示状态
    const onShow =async () => {
        isEditStatus.value = false;
        //改变表单状态
        changeFormStatus(false);
    };

    // 定义详情数据
    let dataSourceInfo = ref({});
    // 请求数据源详情
    const initDataSourceDetail = async (id) => {
        if (id) {
            let result = await getDataSourceById({ id });
            if (result.code == 0) {
                dataSourceInfo.value = result.data;
            }
        }
    };

    watch(
        () => props.currNode.id,
        async (newId) => {
            await initDataSourceDetail(newId);
            await onShow();
        },
        {
            immediate: true,
            deep: true
        }
    );

    if (props.isAdd) {
        changeFormStatus(true);
    }

    defineExpose({
        y9FormRef,
        formList,
        changeType
    });
</script>

<style lang="scss" scoped>
    @import '@/theme/global.scss';
    .item-box {
        display: flex;
        flex-direction: column;
        margin-bottom: 10px;

        :deep(.el-divider) {
            margin-bottom: 30px;
            .el-divider__text {
                font-weight: bold;
            }
        }
    }
</style>
