<template>
    <el-form
        :model="form"
    >
        <el-divider content-position="left" border-style="dashed">基本信息</el-divider>
        <el-descriptions border :column="2">
            <el-descriptions-item label="接口名称" :span="2">
                <el-form-item label="">
                    <span>{{ form.name }}</span>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item label="访问类型">
                <el-form-item label="">
                    <span>{{ form.type }}</span>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item label="请求方式">
                <el-form-item label="">
                    <span>{{ form.method }}</span>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item :span="2" label="请求URL">
                <el-form-item label="">
                    <span>{{ form.url }}</span>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item :span="2" label="描述">
                <el-form-item label="">
                    <span>{{ form.remark }}</span>
                </el-form-item>
            </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left" border-style="dashed">参数信息</el-divider>
        <el-descriptions border :column="2">
            <el-descriptions-item
                :span="2"
                label="Header"
            >
                <el-form-item label="">
                    <el-table :data="headerData" border style="width: 100%">
                        <el-table-column prop="name" label="参数名称" width="180" />
                        <el-table-column prop="type" label="参数类型" width="180" />
                        <el-table-column prop="remark" label="描述" />
                    </el-table>
                </el-form-item>
            </el-descriptions-item>

            <el-descriptions-item
                :span="2"
                label="参数列表"
            >
                <el-form-item label="">
                    <el-table :data="form.params" border style="width: 100%">
                        <el-table-column prop="name" label="参数名称" width="180" />
                        <el-table-column prop="value" label="参数值（示例）" width="180" />
                        <el-table-column prop="type" label="参数类型" width="180" />
                        <el-table-column prop="remark" label="描述" />
                    </el-table>
                </el-form-item>
            </el-descriptions-item>        
        </el-descriptions>
        
        <div>
            <el-collapse accordion>
                <el-collapse-item title="调用示例">
                    <pre><code>{{ javaCode }}</code></pre>
                </el-collapse-item>
            </el-collapse>
        </div>
    </el-form>
    <!-- 制造loading效果 -->
    <el-button v-loading.fullscreen.lock="saveLoading" style="display: none"></el-button>
</template>

<script lang="ts" setup>
import { ref, onMounted, watch, computed } from 'vue';
import y9_storage from '@/utils/storage';
import settings from '@/settings';
import { getApiInfo } from '@/api/apiService';

const props = defineProps({
    id: {
        type: String
    }
});

const form = ref({});

let saveLoading = ref(false);

onMounted(() => {
    initData();
});

function initData() {
    saveLoading.value = true;
    getApiInfo(props.id).then((res) => {
        if(res.success) {
            form.value = res.data;
        }
    });
    saveLoading.value = false;
}

const headerData = [
    {
        name: 'x-api-key',
        type: '字符串',
        remark: '平台提供的appKey值',
    },
    {
        name: 'x-api-sign',
        type: '字符串',
        remark: '签名戳（由平台提供的参数加密生成），参考下面的示例',
    }
];

const javaCode = ref(`package net.risesoft.util;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

public class Test {
	
	static String serverUrl = "http://localhost:7056/dataAssets/services/rest/get/searchData/1769056869124661248";
	static String tenantId = "111111111111111111111111111111111";
	static String appName = "test";
	static String appKey = "123456";
	
	public static void main(String[] args) {
            try {
                String res = httpClient(tenantId, appName);
                System.out.println(res);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
	
	public static String httpClient(String tenantId, String appName) throws Exception {
            // 每次都要获取最新时间戳,时效性3分钟
            String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
            //计算sign
            String sign = generateSign(appName, tenantId + "&" + timeStamp);

            // 创建CloseableHttpClient
            CloseableHttpClient client = HttpClientBuilder.create().build();
            HttpGet httpGet = new HttpGet(serverUrl + "?id=00023822621e4e98b7fdce822907fa75");
            httpGet.setHeader("x-api-key", appKey);
            httpGet.setHeader("x-api-sign", sign);

            CloseableHttpResponse response = client.execute(httpGet);
            String obj = EntityUtils.toString(response.getEntity());
            return obj;
    }
    
    public static String httpPostClient(String tenantId, String appName, String json) throws Exception {
		// 每次都要获取最新时间戳,时效性3分钟
		String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
		//计算sign
		String sign = generateSign(appName, tenantId + "&" + timeStamp);

		// 创建CloseableHttpClient
		CloseableHttpClient client = HttpClientBuilder.create().build();
		HttpPost httpPost = new HttpPost(serverUrl);
		httpPost.setHeader("x-api-key", appKey);
		httpPost.setHeader("x-api-sign", sign);
		httpPost.setHeader("Content-Type", "application/json");
		
		if(StringUtils.isNotBlank(json)) {
			HttpEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
			httpPost.setEntity(entity);
		}

		CloseableHttpResponse response = client.execute(httpPost);
		String obj = EntityUtils.toString(response.getEntity());
		return obj;
	}
	
	/**
     * 生成签名
     */
    private static final int GCM_IV_LENGTH = 12;
    // 标签长度
    private static final int GCM_TAG_LENGTH = 16;
    private static String generateSign(String key, String value) {
        // 密钥生成器
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        // 根据KEY规则初始化密钥生成器生成一个128位的随机源
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        secureRandom.setSeed(key.getBytes());
        keyGen.init(128, secureRandom);
        SecretKey secretKey = keyGen.generateKey();
        
        // 生成随机IV
        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        // 初始化加密器
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
        
        // 加密明文
        byte[] ciphertext = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));

        // 组合IV、密文+标签
        ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + ciphertext.length);
        byteBuffer.put(iv);
        byteBuffer.put(ciphertext);
        byte[] encryptedData = byteBuffer.array();
        return Base64.getEncoder().encodeToString(encryptedData);
    }
}`);
</script>

<style lang="scss" scoped>

</style>