package net.risesoft.api.auth.util;

import java.util.Map;

import com.alibaba.ttl.TransmittableThreadLocal;

public abstract class Y9ApiThreadHoder {

    private static final TransmittableThreadLocal<Map<String, String>> PARAMS_HOLDER =
        new TransmittableThreadLocal<Map<String, String>>();
    private static final TransmittableThreadLocal<String> BODY_HOLDER = new TransmittableThreadLocal<String>();
    private static final TransmittableThreadLocal<String> APIIDS_HOLDER = new TransmittableThreadLocal<String>();

    // 存储请求者appName
    private static final TransmittableThreadLocal<String> APPNAME_HOLDER = new TransmittableThreadLocal<String>();

    public static void clear() {
        PARAMS_HOLDER.remove();
        BODY_HOLDER.remove();
        APIIDS_HOLDER.remove();
        APPNAME_HOLDER.remove();
    }

    public static Map<String, String> getParams() {
        return PARAMS_HOLDER.get();
    }

    public static void setParams(final Map<String, String> params) {
        PARAMS_HOLDER.set(params);
    }

    public static String getBody() {
        return BODY_HOLDER.get();
    }

    public static void setBody(final String body) {
        BODY_HOLDER.set(body);
    }

    public static String getApiIds() {
        return APIIDS_HOLDER.get();
    }

    public static void setApiIds(final String apiIds) {
        APIIDS_HOLDER.set(apiIds);
    }

    public static String getAppName() {
        return APPNAME_HOLDER.get();
    }

    public static void setAppName(final String appName) {
        APPNAME_HOLDER.set(appName);
    }
}
