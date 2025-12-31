package net.risesoft.api.auth.handler;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import net.risesoft.api.auth.service.RateLimitService;
import net.risesoft.api.auth.service.SaveApiLogService;
import net.risesoft.api.auth.util.Y9ApiThreadHoder;
import net.risesoft.api.auth.util.Y9CipherUtil;
import net.risesoft.y9.Y9Context;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.json.Y9JsonUtil;
import net.risesoft.y9public.entity.ApiRoleEntity;
import net.risesoft.y9public.repository.ApiRoleRepository;

/**
 * 接口请求拦截器
 *
 * @author pzx
 *
 */
@Component
public class ApiAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private ApiRoleRepository apiRoleRepository;

    @Autowired
    private RateLimitService rateLimitService;

    @Autowired
    private SaveApiLogService saveApiLogService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String requestUrl = request.getRequestURL().toString();
        String requestParams = Y9JsonUtil.writeValueAsString(request.getParameterMap());

        // HandlerMethod handlerMethod = (HandlerMethod) handler;
        // ApiAuth apiAuthRequired = handlerMethod.getMethodAnnotation(ApiAuth.class);

        String ip = Y9Context.getIpAddr(request);// 请求者ip
        String appName = "", ipAddr = "";
        Double permitsPerSecond = 2.0;
        // 认证逻辑（检查API密钥）
        String apiKey = request.getHeader("x-api-key");
        if (StringUtils.isBlank(apiKey)) {
            saveApiLogService.asyncSave("不存在", requestUrl, ip, requestParams, "x-api-key不能为空");
            errorMsg(response, HttpServletResponse.SC_UNAUTHORIZED, "x-api-key不能为空");
            return false;
        } else {
            ApiRoleEntity apiRoleEntity = apiRoleRepository.findByAppKey(apiKey);
            if (apiRoleEntity == null || apiRoleEntity.getStatus() == 1) {
                saveApiLogService.asyncSave(apiKey, requestUrl, ip, requestParams, "密钥不对，或者被禁用");
                errorMsg(response, HttpServletResponse.SC_UNAUTHORIZED, "密钥不对，或者被禁用");
                return false;
            } else {
                appName = apiRoleEntity.getAppName();
                ipAddr = apiRoleEntity.getIpAddr();
                permitsPerSecond = apiRoleEntity.getPermitsPerSecond();
                Y9ApiThreadHoder.setApiIds(apiRoleEntity.getApiIds());
            }
        }

        // 检查调用ip权限
        if (StringUtils.isBlank(ip) || (StringUtils.isNotBlank(ipAddr) && !ipAddr.contains(ip))) {
            saveApiLogService.asyncSave(appName, requestUrl, ip, requestParams, "调用者IP没权限");
            errorMsg(response, HttpServletResponse.SC_FORBIDDEN, "没调用权限");
            return false;
        }

        // 检查签名
        String sign = request.getHeader("x-api-sign");
        if (StringUtils.isBlank(sign)) {
            saveApiLogService.asyncSave(appName, requestUrl, ip, requestParams, "x-api-sign不能为空");
            errorMsg(response, 401, "x-api-sign不能为空");
            return false;
        } else {
            // 解密获取到的签名，得到tenantId和timestamp参数值
            String json = Y9CipherUtil.decrypt(appName, sign);
            String[] data = json.split("&");
            String tenantId = data[0];
            String timestamp = data[1];
            if (StringUtils.isNotBlank(tenantId)) {
                Y9LoginUserHolder.setTenantId(tenantId);
            } else {
                saveApiLogService.asyncSave(appName, requestUrl, ip, requestParams, "sign不符合规范");
                errorMsg(response, 401, "sign不符合规范");
                return false;
            }
            if (StringUtils.isNotBlank(timestamp)) {
                long time = (System.currentTimeMillis() / 1000 - Long.valueOf(timestamp)) / 60;// 计算相差多少分钟
                if (time > 3 || time < 0) {
                    saveApiLogService.asyncSave(appName, requestUrl, ip, requestParams, "时间戳校验失败");
                    errorMsg(response, 401, "签名已过期，请重新获取");
                    return false;
                }
            } else {
                saveApiLogService.asyncSave(appName, requestUrl, ip, requestParams, "sign不符合规范");
                errorMsg(response, 401, "sign不符合规范");
                return false;
            }
        }

        // 检查调用者是否超过了请求速率
        if (!rateLimitService.tryAcquire(appName, permitsPerSecond)) {
            saveApiLogService.asyncSave(appName, requestUrl, ip, requestParams, "请求频率太快");
            errorMsg(response, HttpServletResponse.SC_NOT_IMPLEMENTED, "Too Many Requests");
            return false;
        }
        Y9ApiThreadHoder.setAppName(appName);

        // if (apiAuthRequired != null) {
        //
        // }

        // 参数校验
        CheckApiParams.processRequest(request);

        saveApiLogService.asyncSave(appName, requestUrl, ip, requestParams, "success");
        return true;
    }

    public void errorMsg(HttpServletResponse response, int code, String msg) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        PrintWriter out = response.getWriter();
        out.append("{\"code\":" + code + ",\"msg\":\"" + msg + "\",\"success\":false}");
    }
}
