package net.risesoft.service.Impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.config.TencentOcrProperties;

/**
 * 腾讯云 OCR 服务（通用文字识别）
 * <p>
 * 完全不依赖 SDK，通过 HTTP + TC3-HMAC-SHA256 签名直接调用腾讯云 API。 适合中文 + 表格场景，识别率显著高于本地 Tesseract。
 *
 * @author yihong
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TencentOcrService {

    private static final String HOST = "ocr.tencentcloudapi.com";
    private static final String SERVICE = "ocr";
    private static final String ACTION = "GeneralBasicOCR";
    private static final String VERSION = "2018-11-19";
    private static final String ALGORITHM = "TC3-HMAC-SHA256";
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private final TencentOcrProperties properties;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    public static byte[] toBytes(java.io.InputStream is) throws java.io.IOException {
        try (java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream(); java.io.InputStream input = is) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                bos.write(buffer, 0, read);
            }
            return bos.toByteArray();
        }
    }

    // ====================== 工具方法 ======================

    /**
     * 生成腾讯云 API v3 签名
     */
    private static String sign(String secretId, String secretKey, String host, String service, String action,
        String version, String payload, long timestamp) throws Exception {

        // 1. 计算日期
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String date = sdf.format(new Date(timestamp * 1000));

        // 2. 拼接规范请求串
        String httpMethod = "POST";
        String canonicalUri = "/";
        String canonicalQueryString = "";
        String canonicalHeaders = "content-type:application/json; charset=utf-8\n" + "host:" + host + "\n"
            + "x-tc-action:" + action.toLowerCase() + "\n";
        String signedHeaders = "content-type;host;x-tc-action";
        String hashedRequestPayload = sha256Hex(payload);
        String canonicalRequest = httpMethod + "\n" + canonicalUri + "\n" + canonicalQueryString + "\n"
            + canonicalHeaders + "\n" + signedHeaders + "\n" + hashedRequestPayload;

        // 3. 拼接待签名字符串
        String credentialScope = date + "/" + service + "/tc3_request";
        String hashedCanonicalRequest = sha256Hex(canonicalRequest);
        String stringToSign = ALGORITHM + "\n" + timestamp + "\n" + credentialScope + "\n" + hashedCanonicalRequest;

        // 4. 计算签名
        byte[] secretDate = hmacSha256(("TC3" + secretKey).getBytes(StandardCharsets.UTF_8), date);
        byte[] secretService = hmacSha256(secretDate, service);
        byte[] secretSigning = hmacSha256(secretService, "tc3_request");
        String signature = bytesToHex(hmacSha256(secretSigning, stringToSign));

        // 5. 拼接 Authorization
        return ALGORITHM + " " + "Credential=" + secretId + "/" + credentialScope + ", " + "SignedHeaders="
            + signedHeaders + ", " + "Signature=" + signature;
    }

    // ====================== 私有方法 ======================

    private static String sha256Hex(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash);
    }

    private static byte[] hmacSha256(byte[] key, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    // ====================== TC3-HMAC-SHA256 签名 ======================

    /**
     * 调用腾讯云通用文字识别 API 提取图片中的文字
     *
     * @param imageBytes 图片字节数组
     * @return 识别出的文本（多行以换行符分隔）；失败时返回 null
     */
    public String recognizeText(byte[] imageBytes) {
        if (!properties.isEnabled()) {
            return null;
        }
        if (imageBytes == null || imageBytes.length == 0) {
            return null;
        }
        String secretId = properties.getSecretId();
        String secretKey = properties.getSecretKey();
        if (isInvalidKey(secretId, secretKey)) {
            LOGGER.warn("腾讯云 OCR 未配置密钥（secret-id / secret-key），跳过云端识别");
            return null;
        }

        try {
            String imageBase64 = Base64.getEncoder().encodeToString(imageBytes);
            String payload = buildPayload(imageBase64);
            long timestamp = System.currentTimeMillis() / 1000;

            String authorization = sign(secretId, secretKey, HOST, SERVICE, ACTION, VERSION, payload, timestamp);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Host", HOST);
            headers.set("X-TC-Action", ACTION);
            headers.set("X-TC-Version", VERSION);
            headers.set("X-TC-Timestamp", String.valueOf(timestamp));
            headers.set("X-TC-Region", properties.getRegion());
            headers.set("Authorization", authorization);

            String url = "https://" + HOST;
            ResponseEntity<String> response =
                restTemplate.postForEntity(url, new HttpEntity<>(payload, headers), String.class);

            if (response.getBody() == null) {
                LOGGER.warn("腾讯云 OCR 返回空响应");
                return null;
            }

            return parseResponse(response.getBody());

        } catch (Exception e) {
            LOGGER.warn("腾讯云 OCR 调用失败: {}", e.getMessage(), e);
            return null;
        }
    }

    private boolean isInvalidKey(String id, String key) {
        return id == null || id.isEmpty() || id.contains("YOUR_SECRET_ID") || key == null || key.isEmpty()
            || key.contains("YOUR_SECRET_KEY");
    }

    /**
     * 构建 JSON 请求体
     */
    private String buildPayload(String imageBase64) throws Exception {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("ImageBase64", imageBase64);
        params.put("LanguageType", "zh");
        return objectMapper.writeValueAsString(params);
    }

    /**
     * 解析腾讯云 OCR 响应
     */
    private String parseResponse(String body) throws Exception {
        JsonNode root = objectMapper.readTree(body);
        JsonNode response = root.get("Response");

        if (response == null) {
            LOGGER.warn("腾讯云 OCR 响应格式异常: {}", body);
            return null;
        }

        // 检查是否有错误
        JsonNode error = response.get("Error");
        if (error != null) {
            String errorCode = error.has("Code") ? error.get("Code").asText() : "Unknown";
            String errorMsg = error.has("Message") ? error.get("Message").asText() : "Unknown";
            LOGGER.warn("腾讯云 OCR 返回错误 [{}]: {}", errorCode, errorMsg);
            return null;
        }

        // 解析识别结果
        JsonNode detections = response.get("TextDetections");
        if (detections == null || !detections.isArray() || detections.size() == 0) {
            LOGGER.warn("腾讯云 OCR 未识别到任何文字");
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (JsonNode detection : detections) {
            JsonNode text = detection.get("DetectedText");
            if (text != null && !text.asText().trim().isEmpty()) {
                if (sb.length() > 0) {
                    sb.append('\n');
                }
                sb.append(text.asText().trim());
            }
        }
        String result = sb.toString();
        LOGGER.info("腾讯云 OCR 识别成功，共 {} 个文本块", detections.size());
        return result;
    }
}
