package net.risesoft.controller;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import net.risesoft.api.auth.service.SaveApiLogService;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.DataAssetsService;
import net.risesoft.y9.Y9Context;

@RestController
@RequestMapping(value = "/services/tool-api", produces = "application/json")
@RequiredArgsConstructor
public class ToolApiController {

    private final SaveApiLogService saveApiLogService;
    private final DataAssetsService dataAssetsService;

    // 1. MD5 加密接口
    @PostMapping(value = "/encrypt/md5")
    public Y9Result<String> md5Encrypt(@RequestBody String data, HttpServletRequest request) {
        String result = "success";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(data.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return Y9Result.success(sb.toString());
        } catch (NoSuchAlgorithmException e) {
            result = "加密失败：" + e.getMessage();
            return Y9Result.failure(result);
        } finally {
            saveApiLogService.asyncSave2("MD5 加密", "/services/tool-api/encrypt/md5", Y9Context.getIpAddr(request), data,
                result);
        }
    }

    // 2. Base64 编码接口
    @PostMapping(value = "/encode/base64")
    public Y9Result<String> base64Encode(@RequestBody String data, HttpServletRequest request) {
        String result = "success";
        try {
            String encoded = Base64.getEncoder().encodeToString(data.getBytes());
            return Y9Result.success(encoded);
        } catch (Exception e) {
            result = "编码失败：" + e.getMessage();
            return Y9Result.failure(result);
        } finally {
            saveApiLogService.asyncSave2("Base64 编码", "/services/tool-api/encode/base64", Y9Context.getIpAddr(request),
                data, result);
        }
    }

    // 3. Base64 解码接口
    @PostMapping(value = "/decode/base64")
    public Y9Result<String> base64Decode(@RequestBody String data, HttpServletRequest request) {
        String result = "success";
        try {
            byte[] decoded = Base64.getDecoder().decode(data);
            return Y9Result.success(new String(decoded));
        } catch (Exception e) {
            result = "解码失败：" + e.getMessage();
            return Y9Result.failure(result);
        } finally {
            saveApiLogService.asyncSave2("Base64 解码", "/services/tool-api/decode/base64", Y9Context.getIpAddr(request),
                data, result);
        }
    }

    // 4. 字符串反转接口
    @PostMapping(value = "/string/reverse")
    public Y9Result<String> reverseString(@RequestBody String data, HttpServletRequest request) {
        String result = "success";
        try {
            String reversed = new StringBuilder(data).reverse().toString();
            return Y9Result.success(reversed);
        } catch (Exception e) {
            result = "反转失败：" + e.getMessage();
            return Y9Result.failure(result);
        } finally {
            saveApiLogService.asyncSave2("字符串反转", "/services/tool-api/string/reverse", Y9Context.getIpAddr(request),
                data, result);
        }
    }

    // 5. 字符串转大写接口
    @PostMapping(value = "/string/uppercase")
    public Y9Result<String> toUppercase(@RequestBody String data, HttpServletRequest request) {
        String result = "success";
        try {
            String uppercase = data.toUpperCase();
            return Y9Result.success(uppercase);
        } catch (Exception e) {
            result = "转大写失败：" + e.getMessage();
            return Y9Result.failure(result);
        } finally {
            saveApiLogService.asyncSave2("字符串转大写", "/services/tool-api/string/uppercase", Y9Context.getIpAddr(request),
                data, result);
        }
    }

    // 6. 字符串转小写接口
    @PostMapping(value = "/string/lowercase")
    public Y9Result<String> toLowercase(@RequestBody String data, HttpServletRequest request) {
        String result = "success";
        try {
            String lowercase = data.toLowerCase();
            return Y9Result.success(lowercase);
        } catch (Exception e) {
            result = "转小写失败：" + e.getMessage();
            return Y9Result.failure(result);
        } finally {
            saveApiLogService.asyncSave2("字符串转小写", "/services/tool-api/string/lowercase", Y9Context.getIpAddr(request),
                data, result);
        }
    }

    // 8. UUID 生成接口
    @GetMapping(value = "/uuid/generate")
    public Y9Result<String> generateUUID(HttpServletRequest request) {
        String result = "success";
        try {
            String uuid = UUID.randomUUID().toString();
            return Y9Result.success(uuid);
        } catch (Exception e) {
            result = "生成失败：" + e.getMessage();
            return Y9Result.failure(result);
        } finally {
            saveApiLogService.asyncSave2("UUID 生成", "/services/tool-api/uuid/generate", Y9Context.getIpAddr(request),
                "", result);
        }
    }

    // 9. 当前时间获取接口
    @GetMapping(value = "/datetime/current")
    public Y9Result<String> getCurrentDateTime(HttpServletRequest request) {
        String result = "success";
        try {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedDateTime = now.format(formatter);
            return Y9Result.success(formattedDateTime);
        } catch (Exception e) {
            result = "获取当前时间失败：" + e.getMessage();
            return Y9Result.failure(result);
        } finally {
            saveApiLogService.asyncSave2("获取当前时间", "/services/tool-api/datetime/current", Y9Context.getIpAddr(request),
                "", result);
        }
    }

    // 11. 字符串长度计算接口
    @PostMapping(value = "/string/length")
    public Y9Result<Integer> getStringLength(@RequestBody String data, HttpServletRequest request) {
        String result = "success";
        try {
            int length = data.length();
            return Y9Result.success(length);
        } catch (Exception e) {
            result = "计算失败：" + e.getMessage();
            return Y9Result.failure(result);
        } finally {
            saveApiLogService.asyncSave2("字符串长度计算", "/services/tool-api/string/length", Y9Context.getIpAddr(request),
                data, result);
        }
    }

    // 15. 数组排序接口
    @PostMapping(value = "/array/sort")
    public Y9Result<int[]> sortArray(@RequestBody int[] array, HttpServletRequest request) {
        String result = "success";
        try {
            for (int i = 0; i < array.length - 1; i++) {
                for (int j = 0; j < array.length - i - 1; j++) {
                    if (array[j] > array[j + 1]) {
                        int temp = array[j];
                        array[j] = array[j + 1];
                        array[j + 1] = temp;
                    }
                }
            }
            return Y9Result.success(array);
        } catch (Exception e) {
            result = "排序失败：" + e.getMessage();
            return Y9Result.failure(result);
        } finally {
            saveApiLogService.asyncSave2("数组排序", "/services/tool-api/array/sort", Y9Context.getIpAddr(request),
                java.util.Arrays.toString(array), result);
        }
    }

    // 16. 数组去重接口
    @PostMapping(value = "/array/unique")
    public Y9Result<int[]> uniqueArray(@RequestBody int[] array, HttpServletRequest request) {
        String result = "success";
        try {
            int[] uniqueResult = java.util.Arrays.stream(array).distinct().toArray();
            return Y9Result.success(uniqueResult);
        } catch (Exception e) {
            result = "去重失败：" + e.getMessage();
            return Y9Result.failure(result);
        } finally {
            saveApiLogService.asyncSave2("数组去重", "/services/tool-api/array/unique", Y9Context.getIpAddr(request),
                java.util.Arrays.toString(array), result);
        }
    }

    // 18. 生成指定长度的随机字符串接口
    @GetMapping(value = "/random/string")
    public Y9Result<String> generateRandomString(@RequestParam int length, HttpServletRequest request) {
        String result = "success";
        try {
            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
            StringBuilder sb = new StringBuilder(length);
            Random random = new Random();
            for (int i = 0; i < length; i++) {
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }
            return Y9Result.success(sb.toString());
        } catch (Exception e) {
            result = "生成失败：" + e.getMessage();
            return Y9Result.failure(result);
        } finally {
            saveApiLogService.asyncSave2("生成随机字符串", "/services/tool-api/random/string", Y9Context.getIpAddr(request),
                "length=" + length, result);
        }
    }

    // 19. 计算字符串的 SHA-256 哈希值接口
    @PostMapping(value = "/encrypt/sha256")
    public Y9Result<String> sha256Encrypt(@RequestBody String data, HttpServletRequest request) {
        String result = "success";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(data.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return Y9Result.success(sb.toString());
        } catch (NoSuchAlgorithmException e) {
            result = "加密失败：" + e.getMessage();
            return Y9Result.failure(result);
        } finally {
            saveApiLogService.asyncSave2("SHA-256 加密", "/services/tool-api/encrypt/sha256",
                Y9Context.getIpAddr(request), data, result);
        }
    }

    // 20. 计算两数相乘接口
    @GetMapping(value = "/math/multiply")
    public Y9Result<Integer> multiply(@RequestParam int a, @RequestParam int b, HttpServletRequest request) {
        String result = "success";
        try {
            int product = a * b;
            return Y9Result.success(product);
        } catch (Exception e) {
            result = "相乘失败：" + e.getMessage();
            return Y9Result.failure(result);
        } finally {
            saveApiLogService.asyncSave2("两数相乘", "/services/tool-api/math/multiply", Y9Context.getIpAddr(request),
                "a=" + a + ", b=" + b, result);
        }
    }

    // 获取所有已上架的资产
    @GetMapping(value = "/assets/all")
    public Y9Result<List<Map<String, Object>>> getAllAssets(String userId, @RequestHeader("tenant_Id") String tenantId,
        HttpServletRequest request) {
        String result = "success";
        try {
            return Y9Result.success(dataAssetsService.getAllAssets(userId, tenantId));
        } catch (Exception e) {
            result = "获取失败：" + e.getMessage();
            return Y9Result.failure(result);
        } finally {
            saveApiLogService.asyncSave2("获取所有已上架的资产", "/services/tool-api/assets/all", Y9Context.getIpAddr(request),
                "", result);
        }
    }

    // 获取人员已订阅的资产列表
    @GetMapping(value = "/assets/subscribe")
    public Y9Result<List<Map<String, Object>>> getSubscribeAssets(String userId,
        @RequestHeader("tenant_Id") String tenantId, HttpServletRequest request) {
        String result = "success";
        try {
            return Y9Result.success(dataAssetsService.getAssetsByUserId(userId, tenantId));
        } catch (Exception e) {
            result = "获取失败：" + e.getMessage();
            return Y9Result.failure(result);
        } finally {
            saveApiLogService.asyncSave2("获取人员已订阅的资产列表", "/services/tool-api/assets/subscribe",
                Y9Context.getIpAddr(request), "", result);
        }
    }

    // 获取资产挂接的文件
    @GetMapping(value = "/assets/file")
    public Y9Result<Map<String, Object>> getMountFileData(Long assetsId, @RequestHeader("tenant_Id") String tenantId,
        HttpServletRequest request) {
        String result = "success";
        try {
            return Y9Result.success(dataAssetsService.getMountFileData(assetsId));
        } catch (Exception e) {
            result = "获取失败：" + e.getMessage();
            return Y9Result.failure(result);
        } finally {
            saveApiLogService.asyncSave2("获取资产挂接的文件", "/services/tool-api/assets/file", Y9Context.getIpAddr(request),
                "", result);
        }
    }
}
