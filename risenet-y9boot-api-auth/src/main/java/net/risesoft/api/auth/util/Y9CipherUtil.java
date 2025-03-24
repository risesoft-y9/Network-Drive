package net.risesoft.api.auth.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES加解密
 * @author pzx
 *
 */
public class Y9CipherUtil {
	
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
        
    	Cipher cipherEncrypt = Cipher.getInstance("AES");  
        cipherEncrypt.init(Cipher.ENCRYPT_MODE, secretKey);  
        byte[] encrypted = cipherEncrypt.doFinal(value.getBytes(StandardCharsets.UTF_8));  
        return Base64.getEncoder().encodeToString(encrypted);  
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
        
        Cipher cipherDecrypt = Cipher.getInstance("AES");  
        cipherDecrypt.init(Cipher.DECRYPT_MODE, secretKey);  
        byte[] decodedEncrypted = Base64.getDecoder().decode(value);  
        byte[] decrypted = cipherDecrypt.doFinal(decodedEncrypted);  
        return new String(decrypted, StandardCharsets.UTF_8);
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
