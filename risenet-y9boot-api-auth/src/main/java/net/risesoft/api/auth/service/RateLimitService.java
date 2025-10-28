package net.risesoft.api.auth.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Service;

import com.google.common.util.concurrent.RateLimiter;

@Service
public class RateLimitService {

    // 使用ConcurrentHashMap来存储每个API调用者的RateLimiter
    private final ConcurrentMap<String, RateLimiter> rateLimiters = new ConcurrentHashMap<>();

    /**
     * 检查API调用者是否达到了他们的请求速率限制。
     * 
     * @param caller API调用者的标识（例如：IP地址、API密钥等）
     * @return 如果调用者超过了速率限制，则返回false；否则返回true。
     */
    public synchronized boolean tryAcquire(String caller, double permitsPerSecond) {
        // 获取或创建调用者的RateLimiter
        RateLimiter rateLimiter = rateLimiters.computeIfAbsent(caller, k -> RateLimiter.create(permitsPerSecond));

        // 尝试获取一个令牌（即尝试发起一个请求）
        if (rateLimiter.tryAcquire()) {
            return true; // 如果成功获取令牌，则允许请求
        } else {
            return false; // 如果未能获取令牌，则拒绝请求
        }
    }
}
