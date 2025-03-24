package net.risesoft.api.auth.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import net.risesoft.api.auth.util.Y9ApiThreadHoder;

/**
 * API服务参数校验
 * @author pzx
 *
 */
public class CheckApiParams {
	
	/**
	 * 获取header信息
	 * @param request
	 * @return
	 */
	public static Map<String, String> getHeaders(HttpServletRequest request) {
	    Map<String, String> headers = new HashMap<>();
	    Enumeration<String> headerNames = request.getHeaderNames();
	    while (headerNames.hasMoreElements()) {
	        String headerName = headerNames.nextElement();
	        String headerValue = request.getHeader(headerName);
	        headers.put(headerName, headerValue);
	    }
	    return headers;
	}
	
	// 请求参数
	public static Map<String, String> handleParams(HttpServletRequest request) {
		Map<String, String> map = new HashMap<>();
	    // 获取所有参数Map
	    Map<String, String[]> params = request.getParameterMap();
	    params.forEach((key, values) -> {
	        //System.out.println("Param: " + key + " = " + String.join(", ", values));
	        map.put(key, String.join(", ", values));
	    });
	    return map;
	}
	
	// 获取原始文本格式的 Body
    public static String getRawBody(HttpServletRequest request) throws IOException {
        // 设置字符编码（解决中文乱码）
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());

        try (BufferedReader reader = request.getReader()) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }

	public static void processRequest(HttpServletRequest request) throws IOException {
	    // 获取所有参数
	    Y9ApiThreadHoder.setParams(handleParams(request));

	    // 获取Body内容（适用于非表单数据，如JSON）
	    if ("POST".equalsIgnoreCase(request.getMethod())) {
	    	String contentType = request.getContentType();
            if (contentType.contains("application/json")) {
            	Y9ApiThreadHoder.setBody(getRawBody(request));
            }
	    }
	}

}
