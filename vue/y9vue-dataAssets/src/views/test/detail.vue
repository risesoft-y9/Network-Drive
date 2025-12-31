<script setup lang="ts">
    import { inject, nextTick, onMounted, reactive, ref, watch } from 'vue';
    import { Search } from '@element-plus/icons-vue';
    import { useSettingStore } from '@/store/modules/settingStore';
    import Y9Table2Comp from './comps/Y9Table2Comp.vue';
    import BodyTypeComp from './comps/BodyTypeComp.vue';
    import JSONEditor from 'jsoneditor';
    import { cloneDeep, forEach, forIn } from 'lodash';
    import { getApiInfo, randomString } from './apiUtils';
    import { ElLoading, ElMessage, ElMessageBox } from 'element-plus';

    const settingStore = useSettingStore();
    const apiTest_el_tabs = ref('apiTest_el_tabs_' + randomString(10));
    const contentHeight = ref(settingStore.getWindowHeight - 60 - 30 - 30);
    // 注入 字体对象
    const fontSizeObj: any = inject('sizeObjInfo');
    watch(
        () => settingStore.getWindowHeight,
        (windowHeight) => {
            contentHeight.value = windowHeight - 60 - 30 - 30;
        }
    );
    const tokenKey = ref('');
    const tokenValue = ref('');
    const Authorization = ref(false);

    const props = defineProps({
        identifier: {
            type: String
        }
    });

    let ApiForm = reactive({});
    /**
     * body.type
     * 1、none 2、form-data 3、json 4、xml 5、html 6、javascript 7、binary
     */
    const bodyType = ref(1);
    const RequestActiveName = ref('Query');
    const ResultActiveName = ref('实时响应');
    const RequestTabPane = reactive({
        headerTitle: 'Header',
        queryTitle: 'Query',
        bodyTitle: 'Body'
    });
    const ResultTabPane = reactive({
        responsiveTitle: '实时响应',
        requestTitle: '请求头',
        resultTitle: '响应头',
        cookieTitle: 'Cookie'
    });

    //
    const headerItemList = reactive([]);
    const queryItemList = reactive([]);
    const bodyFormDataItemList = reactive([]);
    const resposeRequestItemList = reactive([]);
    const resposeResposeItemList = reactive([]);

    let cookieTableConfig = ref({
        headerBackground: true,
        pageConfig: false,
        columns: [
            {
                title: 'name',
                key: 'name'
            },
            {
                title: 'value',
                key: 'value'
            },
            {
                title: 'httpOnly',
                key: 'httpOnly'
            },
            {
                title: 'domain',
                key: 'domain'
            },
            {
                title: 'expires',
                key: 'expires'
            },
            {
                title: 'path',
                key: 'path'
            },
            {
                title: 'secure',
                key: 'secure'
            }
        ],
        tableData: [
            // {
            //     name: 'name',
            //     value: 'value',
            //     httpOnly: 'httpOnly',
            //     domain: 'domain',
            //     expires: 'expires',
            //     path: 'path',
            //     secure: 'secure'
            // }
        ]
    });

    function onDeleteItem() {
        ApiForm.header = cloneDeep(headerItemList);
        ApiForm.query = cloneDeep(queryItemList);
        ApiForm.body['2'] = cloneDeep(bodyFormDataItemList);
    }
    function onEditItem() {
        // 编辑后，需要把数据赋值回去给ApiForm
        ApiForm.header = cloneDeep(headerItemList);
        ApiForm.query = cloneDeep(queryItemList);
        ApiForm.body['2'] = cloneDeep(bodyFormDataItemList);
    }

    function setApiFormData(itemData) {
        function clearArray(array) {
            while (array.length) {
                array.pop();
            }
        }
        function pushAll(array, data) {
            for (let i = 0; i < data.length; i++) {
                array.push(data[i]);
            }
        }
        // 设置表单数据
        Object.assign(ApiForm, itemData);
        clearArray(headerItemList);
        if (Authorization.value) {
            getToken();
        }
        clearArray(queryItemList);
        clearArray(bodyFormDataItemList);
        pushAll(headerItemList, itemData.header);
        pushAll(queryItemList, itemData.query);
        pushAll(bodyFormDataItemList, itemData.body['2']);
        bodyType.value = Number(itemData.body['type']);
        if (bodyType.value > 1) {
            RequestActiveName.value = 'Body';
        } else {
            RequestActiveName.value = 'Query';
        }
    }

    const getApiData = () => {
        getApiInfo({id: props.identifier}).then((res) => {
            if(res.success) {
                setApiFormData(res.data);
            }
        });
        initResposeParams();
    };

    // 选择body类型
    function onRadioChange(value) {
        ApiForm.body['type'] = Number(value);
        bodyType.value = Number(value);
    }

    function onUploadChange(files) {
        ApiForm.body[bodyType.value] = files;
    }

    function onJsonEditorChange(text) {
        ApiForm.body[bodyType.value] = text;
    }

    function initResposeParams() {
        requstTime.value = 0;
        statusCode.value = 0;
        contentLength.value = '0';
        editor.value.set();
        while (resposeRequestItemList.length) {
            resposeRequestItemList.pop();
        }
        while (resposeResposeItemList.length) {
            resposeResposeItemList.pop();
        }
        while (cookieTableConfig.value.tableData.length) {
            cookieTableConfig.value.tableData.pop();
        }
    }
    const requstTime = ref(0);
    const statusCode = ref(0);
    const contentLength = ref('0');
    async function submit() {
        // console.log(ApiForm);
        // 1、统一处理请求头的逻辑
        let req_headers = {};
        for (let i = 0; i < ApiForm.header.length; i++) {
            const item = ApiForm.header[i];
            req_headers[item.Key] = item.Param;
        }
        if (!req_headers['Accept']) {
            req_headers['Accept'] = '*/*';
        }
        if (!req_headers['Accept-Encoding']) {
            req_headers['Accept-Encoding'] = 'gzip, deflate, br';
        }
        /**
         * 不自动配置cookie请求头，否则对于post带参数的请求，会报跨域错误。原因是cors和cookie之间本身有配置上的冲突，增加逻辑处理的复杂性
         */
        // if (!req_headers['Cache-Control']) {
        //     req_headers['Cache-Control'] = 'public';
        // }
        // if (!req_headers['Access-Control-Allow-Credentials']) {
        //     req_headers['Access-Control-Allow-Credentials'] = 'true';
        // }

        // 请求标头
        while (resposeRequestItemList.length) {
            resposeRequestItemList.pop();
        }
        forEach(req_headers, (value, key) => {
            resposeRequestItemList.push({ key: key, value: value });
        });
        // 如果没有自定义请求头，添加默认请求头
        if (!req_headers['User-Agent']) {
            resposeRequestItemList.push({ key: 'User-Agent', value: window.navigator.userAgent });
        }
        if (!req_headers['Host']) {
            resposeRequestItemList.push({ key: 'Host', value: window.location['host'] });
        }
        if (!req_headers['Origin']) {
            resposeRequestItemList.push({ key: 'Origin', value: window.location['origin'] });
        }
        if (!req_headers['Access-Control-Allow-Credentials']) {
            resposeRequestItemList.push({ key: 'Access-Control-Allow-Credentials', value: 'true' });
        }

        // 2、统一处理请求参数的逻辑
        let data = {},
            dataStr = '?',
            hasData = false,
            formData = new FormData();
        function __forArray(array) {
            for (let i = 0; i < array.length; i++) {
                const item = array[i];
                const isSelect = item.isSelect;
                const Key = item.Key;
                const Param = item.Param;
                console.log(isSelect, array);
                if (isSelect) {
                    hasData = true;
                    data[Key] = Param;
                    dataStr += `${Key}=${Param}&`;
                    formData.append(Key, Param);
                }
            }
            console.log(data, formData);
        }
        function __upload(array) {
            hasData = true;
            // 单个文件上传-示例
            // formData.append('file', array[0].raw);
            // 多个文件上传 -示例（接口测试工具不做区分，统一采用多文件上传的方式）
            for (let i = 0; i < array.length; i++) {
                formData.append('files', array[i].raw);
            }
        }
        function __bodytype_formData__() {
            if (ApiForm.method.toUpperCase() == 'GET' || ApiForm.method.toUpperCase() == 'HEAD') {
                return ElMessageBox.alert(`GET & HEAD 方式不使用body传参数`, '操作提示', {
                    confirmButtonText: 'OK'
                });
            }
            if (req_headers['Content-Type'].toLowerCase().indexOf('multipart/form-data') < 0) {
                ElMessageBox.alert(`请修改 Content-Type 为 multipart/form-data，否则可能产生跨域错误`, '操作提示', {
                    confirmButtonText: 'OK'
                });
            }
            // 如果没有自定义边界符，删除让浏览器自动设置
            if (!req_headers['Content-Type'].toLowerCase().match(/boundary/)) {
                // 自己添加，node脚本测试拿不到数据
                // req_headers['Content-Type'] += `;boundary=----${randomString(34)}`;
                delete req_headers['Content-Type'];
            }
            __forArray(ApiForm.body[2]);
        }
        function __bodytype_json__() {
            try {
                let arr = [];
                forIn(JSON.parse(ApiForm.body[3]), (value, key) => {
                    arr.push({ isSelect: true, Key: key, Param: value });
                });
                __forArray(arr);
            } catch (error) {
                console.log(error);
                return null; // 返回null，不执行下面的代码
            }
        }
        function __bodytype_xml__() {
            // console.log(ApiForm.body[4]);
            if (ApiForm.body[4]) {
                hasData = true;
                dataStr += `xml=${ApiForm.body[4]}&`; // & 为了下面的get统一处理这个字符
                data.xmlStr = ApiForm.body[4];
            }
        }
        function __bodytype_html__() {
            if (ApiForm.body[5]) {
                hasData = true;
                dataStr += `html=${ApiForm.body[5]}&`; // & 为了下面的get统一处理这个字符
                data.htmlStr = ApiForm.body[5];
            }
        }
        function __bodytype_upload__() {
            if (ApiForm.method.toUpperCase() != 'POST') {
                return ElMessageBox.alert(`文件上传的请求方式只能是POST`, '操作提示', {
                    confirmButtonText: 'OK'
                });
            } else if (!ApiForm.body[7].length) {
                return ElMessageBox.alert(`没有文件可上传`, '操作提示', {
                    confirmButtonText: 'OK'
                });
            } else if (req_headers['Content-Type'].toLowerCase().indexOf('boundary') < 0) {
                /**
                 * 数据使用formData处理后，是否设置content-type值都可以，
                 * 如果设置了，在请求头中content-type没有boundary值时，就删掉自己设置的content-type,浏览器会自动帮我们设置。
                 */
                delete req_headers['Content-Type'];
                __upload(ApiForm.body[7]);
            } else {
                __upload(ApiForm.body[7]);
            }
        }
        // console.log(ApiForm.body.type);
        switch (ApiForm.body.type) {
            case 1:
                // bydyType为none，获取query
                __forArray(ApiForm.query);
                break;
            case 2:
                // form-data
                __bodytype_formData__();
                break;
            case 3:
                // 数据可能不是标准的json格式
                __bodytype_json__();
                break;
            case 4:
                // xml
                __bodytype_xml__();
                break;
            case 5:
                // html
                __bodytype_html__();
                break;
            case 7:
                // upload
                __bodytype_upload__();
                break;
            default:
                console.log(ApiForm);
                break;
        }

        // 3、统一处理请求体参数的逻辑
        let EncodingFormatBody = null;
        switch (ApiForm.method.toUpperCase()) {
            case 'GET':
            case 'HEAD':
                // 如果有参数，GET只支持url拼接参数，不支持请求体body
                if (hasData) {
                    dataStr = dataStr.substring(0, dataStr.length - 1);
                    ApiForm.url = encodeURI(ApiForm.url.split('?')[0] + dataStr);
                }
                if (ApiForm.url.lenght >= 2000) {
                    return ElMessageBox.alert(
                        '超出浏览器最大url字符长度，请更改post请求或缩减字符参数长度',
                        '操作提示',
                        {
                            confirmButtonText: 'OK'
                        }
                    );
                }
                break;
            case 'POST':
            case 'PUT':
            case 'DELETE':
                // 如果有参数，发送body
                if (hasData) {
                    // 区分不同响应头的编码要求
                    if (
                        req_headers['Content-Type'] &&
                        req_headers['Content-Type'].toLowerCase().indexOf('application/json') > -1
                    ) {
                        // form-data格式
                        if (ApiForm.body.type == 2) {
                            EncodingFormatBody = formData;
                        } else {
                            EncodingFormatBody = JSON.stringify(data);
                        }
                    } else if (
                        req_headers['Content-Type'] &&
                        req_headers['Content-Type'].toLowerCase().indexOf('application/x-www-form-urlencoded') > -1
                    ) {
                        // dataStr在上面的代码中，是以?开头，以&结尾，都需要去掉
                        let bodyStr = dataStr.substring(0, dataStr.length - 1);
                        EncodingFormatBody = bodyStr.substring(1, bodyStr.length);
                    } else {
                        // 默认表单格式
                        EncodingFormatBody = formData;
                    }
                }
                break;
            default:
                console.warn('其它请求，自行处理，比如options,patch等');
                break;
        }

        // 4、发送请求
        let t = new Date().getTime();
        const response = await fetch(ApiForm.url, {
            method: ApiForm.method,
            headers: new Headers(req_headers),
            // withCredentials: 'include',
            body: EncodingFormatBody
        })
            .then((response) => {
                console.log(response);
                // 响应标头
                const headers = response.headers;
                while (resposeResposeItemList.length) {
                    resposeResposeItemList.pop();
                }
                let obj = {};
                for (let [key, value] of headers.entries()) {
                    obj[key] = value;
                    resposeResposeItemList.push({ key: key, value: value });
                }
                let cl = obj['content-length'];
                if (cl < 1024) {
                    contentLength.value = cl + 'B';
                } else if (cl >= 1024 && cl < 1024 * 1000) {
                    contentLength.value = cl / 1024 + 'KB';
                } else {
                    console.log(cl);
                    contentLength.value = cl / 1024 / 1000 + 'MB';
                }

                // 获取cookie
                console.log(`cookie: ${headers.getSetCookie()}`);
                while (cookieTableConfig.value.tableData.length) {
                    cookieTableConfig.value.tableData.pop();
                }
                for (let [key, value] of headers.getSetCookie()) {
                    let obj = {};
                    // { name: key, value: value,  httpOnly: '', secure: '', path: '',domain: '',expires: '' }
                    // cookieTableConfig.value.tableData.push(obj);
                }
                statusCode.value = response.status;

                // 其他处理逻辑...
                return response.json();
            })
            .then((data) => {
                console.log(data);
                editor.value.set(data);
                requstTime.value = new Date().getTime() - t;
            })
            .catch((error) => {
                console.log(error);
                editor.value.set(error.toString());
            });
    }

    // 实时响应的json编辑器
    const editor = ref(null);
    const jsonContainer = ref(null);
    function initJsonContainer() {
        jsonContainer.value = document.getElementById('json-container');
        const options = {
            selectionStyle: 'tree',
            mode: 'code',
            statusBar: true,
            mainMenuBar: false
            // onChangeText() {
            //     onJsonEditorChange(editor.value.getText());
            // }
        };
        editor.value = new JSONEditor(jsonContainer.value, options);

        // set json
        // const initialJson = {
        //     Array: [1, 2, 3],
        //     Boolean: true,
        //     Null: null,
        //     Number: 123,
        //     Object: { a: 'b', c: 'd' },
        //     String: 'Hello World'
        // };
        // editor.set(initialJson);
        editor.value.set();

        document.querySelector('#json-container .jsoneditor').style.height = '66vh';
        // console.log(editor);
        // get json
        // const updatedJson = editor.get();
    }

    function getToken() {
        const json = JSON.parse(sessionStorage.getItem(tokenKey.value));
        tokenValue.value = json.access_token;
        let find = false;
        for (let i = 0; i < ApiForm.header.length; i++) {
            const item = ApiForm.header[i];
            if (item.Key == 'Authorization') {
                item.Param = `Bearer ${tokenValue.value}`;
                find = true;
            }
        }
        if (!find) {
            ApiForm.header.push({ isSelect: true, Key: 'Authorization', Param: `Bearer ${tokenValue.value}` });
        }
    }
    function autoAddToken() {
        tokenKey.value = import.meta.env.VUE_APP_SSO_SITETOKEN_KEY;

        if (sessionStorage.getItem(tokenKey.value)) {
            Authorization.value = true;
            getToken();
            window.addEventListener('storage', (e) => {
                // console.log(e);
                if (e.key == tokenKey.value) {
                    getToken();
                }
            });
        }
    }

    async function init() {
        initJsonContainer();
        getApiData();
        autoAddToken();
    }
    onMounted(() => {
        init();
    });
</script>
<template>
    <el-container>
        <el-main>
            <div>
                <div class="mainContainer">
                    <el-form v-model="ApiForm" style="padding-top: 25px;">
                        <el-form-item label="接口名称">
                            <el-input v-model="ApiForm.name"></el-input>
                        </el-form-item>
                        <el-form-item label="请求方法">
                            <el-select v-model="ApiForm.method" placeholder="GET">
                                <el-option label="GET" value="GET" />
                                <el-option label="POST" value="POST" />
                                <el-option label="DELETE" value="DELETE" />
                                <el-option label="PUT" value="PUT" />
                                <el-option label="HEAD" value="HEAD" />
                                <el-option label="OPTIONS" value="OPTIONS" />
                            </el-select>
                        </el-form-item>
                        <el-form-item label="请求地址">
                            <el-input v-model="ApiForm.url"></el-input>
                        </el-form-item>
                        <el-form-item>
                            <el-tabs class="apiTest_el_tabs" v-model="RequestActiveName" @tab-click="handleClick">
                                <el-tab-pane name="Header">
                                    <template #label>
                                        <div class="label-title">
                                            <span>{{ RequestTabPane.headerTitle }}</span>
                                            <span>（{{ ApiForm.header?.length }}）</span>
                                        </div>
                                    </template>
                                    <Y9Table2Comp
                                        key="header"
                                        :itemList="headerItemList"
                                        @on-delete="onDeleteItem"
                                        @on-edit="onEditItem"
                                    ></Y9Table2Comp>
                                </el-tab-pane>
                                <el-tab-pane name="Query">
                                    <template #label>
                                        <div class="label-title">
                                            <span>{{ RequestTabPane.queryTitle }}</span>
                                            <span>（{{ ApiForm.query?.length }}）</span>
                                        </div>
                                    </template>
                                    <Y9Table2Comp
                                        key="query"
                                        :itemList="queryItemList"
                                        @on-delete="onDeleteItem"
                                        @on-edit="onEditItem"
                                    ></Y9Table2Comp>
                                </el-tab-pane>
                                <el-tab-pane name="Body">
                                    <template #label>
                                        <div class="label-title">
                                            <span>{{ RequestTabPane.bodyTitle }}</span>
                                            <span></span>
                                        </div>
                                    </template>
                                    <div class="body-content">
                                        <BodyTypeComp
                                            :type="bodyType"
                                            :itemList="bodyFormDataItemList"
                                            :ApiForm="ApiForm"
                                            @on-delete="onDeleteItem"
                                            @on-edit="onEditItem"
                                            @on-radio-change="onRadioChange"
                                            @on-json-editor-change="onJsonEditorChange"
                                            @on-upload-change="onUploadChange"
                                        ></BodyTypeComp>
                                    </div>
                                </el-tab-pane>
                            </el-tabs>
                        </el-form-item>
                        <el-form-item>
                            <el-divider>
                                <el-button type="primary" @click="submit">发送</el-button>
                                <!-- <el-button>保存</el-button> -->
                            </el-divider>
                            <div class="fixed-status">
                                <span><i class="ri-global-line"></i>&nbsp; 状态：</span>
                                <span>{{ statusCode }}</span
                                >&nbsp;
                                <span>时间：</span>
                                <span
                                    >{{
                                        `${new Date().getHours()}:${new Date().getMinutes()}:${new Date().getSeconds()}`
                                    }}
                                    &nbsp; {{ requstTime }}ms</span
                                >
                                &nbsp;
                                <span>大小：</span>
                                <span>{{ contentLength }} <i class="ri-arrow-down-circle-fill"></i></span>
                            </div>
                            <el-tabs
                                class="apiTest_el_tabs respose-el-tabs"
                                v-model="ResultActiveName"
                                @tab-click="handleClick"
                            >
                                <el-tab-pane name="实时响应">
                                    <template #label>
                                        <div class="label-title">
                                            <span>{{ ResultTabPane.responsiveTitle }}</span>
                                            <span></span>
                                        </div>
                                    </template>
                                    <div id="json-container"></div>
                                </el-tab-pane>
                                <el-tab-pane name="请求头">
                                    <template #label>
                                        <div class="label-title">
                                            <span>{{ ResultTabPane.requestTitle }}</span>
                                            <span>（{{ resposeRequestItemList.length }}）</span>
                                        </div>
                                    </template>
                                    <div class="requestTitle-content">
                                        <div v-for="item in resposeRequestItemList" :key="item.key">
                                            <el-row>
                                                <el-col :span="8"
                                                    ><span>{{ item.key }}</span></el-col
                                                >
                                                <el-col :span="16"
                                                    ><span>{{ item.value }}</span></el-col
                                                >
                                            </el-row>
                                        </div>
                                    </div>
                                </el-tab-pane>
                                <el-tab-pane name="响应头">
                                    <template #label>
                                        <div class="label-title">
                                            <span>{{ ResultTabPane.resultTitle }}</span>
                                            <span>（{{ resposeResposeItemList.length }}）</span>
                                        </div>
                                    </template>
                                    <div class="resultTitle-content">
                                        <div v-for="item in resposeResposeItemList" :key="item.key">
                                            <el-row>
                                                <el-col :span="8"
                                                    ><span>{{ item.key }}</span></el-col
                                                >
                                                <el-col :span="16"
                                                    ><span>{{ item.value }}</span></el-col
                                                >
                                            </el-row>
                                        </div>
                                    </div>
                                </el-tab-pane>
                                <el-tab-pane name="Cookie">
                                    <template #label>
                                        <div class="label-title">
                                            <span>{{ ResultTabPane.cookieTitle }}</span>
                                            <span>（{{ cookieTableConfig.tableData.length }}）</span>
                                        </div>
                                    </template>
                                    <div class="cookieTitle-content">
                                        <y9Table :config="cookieTableConfig"></y9Table>
                                    </div>
                                </el-tab-pane>
                            </el-tabs>
                        </el-form-item>
                    </el-form>
                </div>
            </div>
        </el-main>
    </el-container>
</template>
<style lang="scss" scoped>
    .el-container {
        & > .el-main {
            padding: 0 25px;
            & > div {
                background-color: rgb(233, 233, 239);
                border-radius: 5px;
                .mainContainer {
                    height: v-bind('contentHeight+"px"');
                    overflow: auto;
                    background-color: var(--el-bg-color);
                    border-radius: 5px;
                    padding: 0 25px;

                    .folder {
                        height: v-bind('contentHeight+"px"');
                        display: flex;
                        justify-content: center;
                        align-items: center;
                    }
                    .el-form {
                        .el-form-item {
                            .el-form-item__content {
                                .el-tabs {
                                    width: 100%;
                                    .requestTitle-content,
                                    .resultTitle-content {
                                        & > div {
                                            width: 100%;
                                            border: rgb(236, 238, 245) solid 1px;
                                            .el-row {
                                                .el-col:first-child {
                                                    span {
                                                        padding: 0 15px;
                                                        overflow: scroll;
                                                    }
                                                }
                                            }

                                            &:not(:first-child) {
                                                border-top: none;
                                            }
                                        }
                                    }
                                    .cookieTitle-content {
                                        :deep(.y9-table-div) {
                                            background-color: var(--el-bg-color);
                                            padding: 0;
                                            .el-table__empty-text {
                                                background-repeat: no-repeat;
                                                background-position: 50.5% 18px;
                                                line-height: 200px;
                                            }
                                        }
                                        .no-data-ui {
                                            position: relative;
                                            z-index: 1;
                                            width: 100%;
                                            min-height: 180px;
                                            display: flex;
                                            justify-content: center;
                                            align-items: center;
                                        }
                                    }
                                }
                                .respose-el-tabs {
                                    position: relative;
                                    top: -40px;
                                }
                            }
                            .apiTest_el_tabs {
                                .label-title {
                                    span:last-child {
                                        color: rgb(102, 182, 104);
                                    }
                                }
                            }
                            .fixed-status {
                                position: relative;
                                z-index: 1;
                                line-height: 40px;
                                height: 40px;
                                width: 400px;
                                text-align: right;
                                left: calc(100% - 400px);
                                & > span:nth-child(even) {
                                    color: rgb(102, 182, 104);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
</style>
<style>
    #json-container .jsoneditor {
        border: thin solid rgb(236, 238, 245);
        height: auto;
        min-height: 20vh;
        .ace-jsoneditor .ace_gutter {
            background: var(--el-bg-color);
            border-right: thin solid rgb(236, 238, 245);
            color: #c0c0c0;
        }
    }
    .apiTest_el_tabs div.el-tabs__nav-wrap::after {
        height: 1px;
    }
    /**修复其它项目没有设置唯一类名导致的公共样式影响*/
    .apiTest_el_tabs.el-tabs--top {
        flex-direction: column;
    }
</style>
