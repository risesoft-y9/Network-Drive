<template>
    <div class="api-documentation">
        <div class="api-doc-content">
            <div class="md-html">
                <h2 class="md-title md-h2">1. 接口概述</h2>
                接口采用高效的分页查询方式，使用最后一条记录的排序字段值作为下一页查询的起始值。
                并且对API请求进行速率限制，默认限制为每秒2个请求，超过限制的请求将被拒绝，根据业务需要可以联系管理员调整限制。
                <h2 class="md-title md-h2">2. 接口详情</h2>
                <h4 class="md-title md-h4">2.1 基本信息</h4>
                <p><strong>接口路径：</strong>/search/{dataSourceId}/{tableName}</p>
                <p><strong>请求方法：</strong>POST</p>
                <p><strong>认证方式：</strong>API密钥认证 + 签名验证</p>
                <p><strong>内容类型：</strong>application/json</p>
                <h4 class="md-title md-h4">2.2 请求参数</h4>
                <h5 class="md-title md-h5">2.2.1 路径参数（申请后由平台提供）</h5>
                <p><strong>tableName</strong>：要查询的表名，必须是表中存在的合法表名。</p>
                <p><strong>dataSourceId</strong>：数据源ID，必须是已注册的数据源ID。</p>
                <h5 class="md-title md-h5">2.2.2 请求体参数</h5>
                <table class="md-table">
                    <thead>
                        <tr><th>参数名</th><th>类型</th><th>必填</th><th>描述</th></tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td>sortField</td>
                            <td>String</td>
                            <td>是</td>
                            <td>排序字段名，必须是表中存在的合法值唯一的列名，推荐用主键id列</td>
                        </tr>
                        <tr>
                            <td>size</td>
                            <td>Integer</td>
                            <td>是</td>
                            <td>每页查询的数据条数</td>
                        </tr>
                        <tr>
                            <td>lastSortValue</td>
                            <td>Object</td>
                            <td>否</td>
                            <td>上一页查询结果的最后一条记录的排序字段值</td>
                        </tr>
                        <tr>
                            <td>其他查询字段</td>
                            <td>Object</td>
                            <td>否</td>
                            <td>申请时选的查询条件字段，选了为必填项</td>
                        </tr>
                    </tbody>
                </table>
                <h4 class="md-title md-h4">2.3 请求头</h4>
                <table class="md-table">
                    <thead>
                        <tr><th>参数名</th><th>类型</th><th>必填</th><th>描述</th></tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td>x-api-key</td>
                            <td>String</td>
                            <td>是</td>
                            <td>API调用密钥</td>
                        </tr>
                        <tr>
                            <td>x-api-sign</td>
                            <td>String</td>
                            <td>是</td>
                            <td>API签名，用于验证请求的合法性和防止重放攻击</td>
                        </tr>
                    </tbody>
                </table>
                <h4 class="md-title md-h4">2.4 响应参数</h4>
                <p>响应体示例：</p>
                <pre><code>{
  "code": 200,
  "msg": "success",
  "success": true,
  "data": {
    "data": [
      {
        "COLUMN1": "value1",
        "COLUMN2": "value2",
        ...
      },
      ...
    ],
    "lastSortValue": "next_page_start_value"
  }
}</code></pre>
                <h4 class="md-title md-h4">2.5 签名验证规则</h4>
                <p>1. 生成当前时间戳(秒级)：timestamp</p>
                <p>2. 准备要加密的数据：tenantId（申请后由平台提供）和timestamp</p>
                <p>3. 使用应用密钥对数据进行加密</p>
                <p>4. 将加密结果作为 x-api-sign 请求头的值</p>
                <p>5. 验证时间戳是否在有效范围内（默认3分钟）</p>
                <h4 class="md-title md-h4">2.6 分页查询机制</h4>
                <p>1. 第一次查询时，不提供 lastSortValue 参数</p>
                <p>2. 系统返回第一页数据和最后一条记录的排序字段值</p>
                <p>3. 下一次查询时，将上一次返回的 lastSortValue 作为请求参数</p>
                <p>4. 重复步骤2-3，直到返回的 lastSortValue 为 null，表示没有更多数据</p>

                <h2 class="md-title md-h2">3. API调用示例代码</h2>
                <pre><code>import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class ApiCallExample {
    
    private static final String API_URL = "http://your-api-server.com/search/{dataSourceId}/{tableName}";
    private static final String API_KEY = "your-api-key";
    private static final String APP_NAME = "your-app-name";
    private static final String TENANT_ID = "your-tenant-id";
    
    public static void main(String[] args) throws Exception {
        // 准备请求参数
        String dataSourceId = "ds_001";
        String tableName = "users";
        String url = API_URL.replace("{dataSourceId}", dataSourceId).replace("{tableName}", tableName);
        
        // 准备请求体
        JSONObject requestBody = new JSONObject();
        requestBody.put("sortField", "id");
        requestBody.put("size", 10);
        // requestBody.put("lastSortValue", "10"); // 第一次查询不需要，后续查询需要
        // requestBody.put("", ""); // 选了查询条件字段，必填
        
        // 生成签名
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String signData = TENANT_ID + "&" + timestamp;
        String sign = generateSign(APP_NAME, signData);
        
        // 创建HTTP客户端
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // 创建POST请求
            HttpPost httpPost = new HttpPost(url);
            
            // 设置请求头
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("x-api-key", API_KEY);
            httpPost.setHeader("x-api-sign", sign);
            
            // 设置请求体
            StringEntity entity = new StringEntity(requestBody.toJSONString(), ContentType.APPLICATION_JSON);
            httpPost.setEntity(entity);
            
            // 发送请求并获取响应
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                HttpEntity responseEntity = response.getEntity();
                if (responseEntity != null) {
                    String responseString = EntityUtils.toString(responseEntity);
                    System.out.println("响应结果：" + responseString);
                    
                    // 解析响应结果，获取下一页的lastSortValue
                    JSONObject responseJson = JSONObject.parseObject(responseString);
                    if (responseJson.getBoolean("success")) {
                        JSONObject data = responseJson.getJSONObject("data");
                        String nextLastSortValue = data.getString("lastSortValue");
                        System.out.println("下一页的lastSortValue：" + nextLastSortValue);
                    }
                }
            }
        }
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
}

// 示例：查询所有数据（分页查询）
public static void main(String[] args) throws Exception {
    String dataSourceId = "ds_001";
    String tableName = "users";
    int pageSize = 1000;
    String sortField = "id";
    
    String lastSortValue = null;
    
    // 创建HTTP客户端
    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
        // 循环查询所有数据
        while (true) {            
            // 准备请求体
            JSONObject requestBody = new JSONObject();
            requestBody.put("sortField", sortField);
            requestBody.put("size", pageSize);
            if (lastSortValue != null) {
                requestBody.put("lastSortValue", lastSortValue);
            }
            // 其它查询条件
            
            // 发送请求
            JSONObject response = sendRequest(httpClient, dataSourceId, tableName, requestBody);
            
            if (response != null && response.getBoolean("success")) {
                JSONObject data = response.getJSONObject("data");
                JSONArray dataList = data.getJSONArray("data");
                
                // 处理数据
                for (int i = 0; i < dataList.size(); i++) {
                    JSONObject item = dataList.getJSONObject(i);
                    ...
                }
                
                // 获取下一页的lastSortValue
                lastSortValue = data.getString("lastSortValue");
                
                // 如果没有更多数据，退出循环
                if (lastSortValue == null || dataList.size() < pageSize) {
                    System.out.println("已查询完所有数据");
                    break;
                }
            } else {
                System.out.println("查询失败：" + response.getString("msg"));
                break;
            }
        }
    }
}

private static JSONObject sendRequest(CloseableHttpClient httpClient, String dataSourceId, String tableName, JSONObject requestBody) throws Exception {
    String url = API_URL.replace("{dataSourceId}", dataSourceId).replace("{tableName}", tableName);
    
    // 生成签名
    String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
    String signData = TENANT_ID + "&" + timestamp;
    String sign = generateSign(APP_NAME, signData);
    
    // 创建POST请求
    HttpPost httpPost = new HttpPost(url);
    
    // 设置请求头
    httpPost.setHeader("Content-Type", "application/json");
    httpPost.setHeader("x-api-key", API_KEY);
    httpPost.setHeader("x-api-sign", sign);
    
    // 设置请求体
    StringEntity entity = new StringEntity(requestBody.toJSONString(), ContentType.APPLICATION_JSON);
    httpPost.setEntity(entity);
    
    // 发送请求并获取响应
    try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
        HttpEntity responseEntity = response.getEntity();
        if (responseEntity != null) {
            String responseString = EntityUtils.toString(responseEntity);
            return JSONObject.parseObject(responseString);
        }
    }
    return null;
}</code></pre>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>

</script>

<style lang="scss" scoped>
.api-documentation {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.api-doc-content {
  flex: 1;
  overflow: hidden;
  padding: 10px;
}

.md-html {
  height: 100%;
  overflow-y: auto;
  padding: 10px;
  font-size: 14px;
  line-height: 1.6;
  color: #303133;
}

/* 标题样式 */
.md-title {
  margin: 15px 0;
  font-weight: bold;
  color: #303133;
}

.md-h1 {
  font-size: 24px;
  border-bottom: 2px solid #e4e7ed;
  padding-bottom: 10px;
}

.md-h2 {
  font-size: 20px;
  border-bottom: 1px solid #e4e7ed;
  padding-bottom: 8px;
}

.md-h3 {
  font-size: 18px;
}

.md-h4 {
  font-size: 16px;
}

.md-h5 {
  font-size: 14px;
}

.md-h6 {
  font-size: 12px;
}

/* 段落样式 */
.md-html p {
  margin: 10px 0;
}

/* 代码块样式 */
.md-html pre {
  background-color: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 10px;
  margin: 10px 0;
  overflow-x: auto;
}

.md-html code {
  background-color: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 3px;
  padding: 2px 5px;
  font-family: monospace;
  font-size: 13px;
}

/* 表格样式 */
.md-table {
  border-collapse: collapse;
  width: 100%;
  margin: 10px 0;
  font-size: 14px;
}

.md-table th,
.md-table td {
  border: 1px solid #e4e7ed;
  padding: 8px 12px;
  text-align: left;
}

.md-table th {
  background-color: #f5f7fa;
  font-weight: bold;
}

.md-table tr:nth-child(even) {
  background-color: #fafafa;
}
</style>