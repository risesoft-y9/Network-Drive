package net.risesoft.api.auth.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES加解密
 * @author pzx
 *
 */
public class Y9CipherUtil {
	
	// GCM模式推荐的IV长度（12字节=96位）
    private static final int GCM_IV_LENGTH = 12;
    // 标签长度（16字节=128位）
    private static final int GCM_TAG_LENGTH = 16;
	
    /**
     * 加密
     * @param key
     * @param value
     * @return
     * @throws Exception
     */
    public static String encrypt(String key, String value) throws Exception {
    	// 密钥生成器  
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");  
        // 根据KEY规则初始化密钥生成器生成一个128位的随机源
		SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
		secureRandom.setSeed(key.getBytes());
		keyGen.init(128, secureRandom);
        SecretKey secretKey = keyGen.generateKey();
        
        // 生成随机IV（12字节）
        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        // 初始化加密器
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
        
        // 加密明文，结果包含密文+认证标签（GCM模式自动生成标签）
        byte[] ciphertext = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));

        // 组合IV、密文+标签（格式：IV(12字节) + 密文+标签(n字节)）
        ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + ciphertext.length);
        byteBuffer.put(iv);
        byteBuffer.put(ciphertext);
        byte[] encryptedData = byteBuffer.array();  
        return Base64.getEncoder().encodeToString(encryptedData);  
    }  
  
    /**
     * 解密
     * @param key
     * @param value
     * @return
     * @throws Exception
     */
    public static String decrypt(String key, String value) throws Exception {  
    	// 密钥生成器  
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");  
        // 根据KEY规则初始化密钥生成器生成一个128位的随机源
		SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
		secureRandom.setSeed(key.getBytes());
		keyGen.init(128, secureRandom);
        SecretKey secretKey = keyGen.generateKey();
        
        // 解码Base64
        byte[] decodedData = Base64.getDecoder().decode(value);

        // 分离IV和密文+标签
        ByteBuffer byteBuffer = ByteBuffer.wrap(decodedData);
        byte[] iv = new byte[GCM_IV_LENGTH];
        byteBuffer.get(iv); // 前12字节为IV

        byte[] ciphertextWithTag = new byte[byteBuffer.remaining()];
        byteBuffer.get(ciphertextWithTag); // 剩余部分为密文+标签

        // 初始化解密器
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

        // 解密
        byte[] plaintext = cipher.doFinal(ciphertextWithTag);
        
        return new String(plaintext, StandardCharsets.UTF_8);
    }
    
    /**
     * 生成appKey
     * @return
     */
    public static String generateAppKey() {
        // 创建一个安全的随机数生成器
        SecureRandom secureRandom = new SecureRandom();
        // 生成一个随机的字节数组
        byte[] randomBytes = new byte[32]; // 你可以根据需要调整字节长度
        secureRandom.nextBytes(randomBytes);
        // 将字节数组转换为Base64编码的字符串
        return Base64.getEncoder().encodeToString(randomBytes);
    }

}
