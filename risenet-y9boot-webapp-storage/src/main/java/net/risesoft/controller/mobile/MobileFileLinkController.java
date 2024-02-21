package net.risesoft.controller.mobile;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.risesoft.consts.UtilConsts;
import net.risesoft.entity.FileNode;
import net.risesoft.service.FileNodeService;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.json.Y9JsonUtil;
import net.risesoft.y9.util.Y9Util;

/**
 * 文件空间处理接口
 *
 * @author yihong
 *
 */
@RestController
@RequestMapping("/linkMobile")
public class MobileFileLinkController {

    protected Logger log = LoggerFactory.getLogger(MobileFileLinkController.class);

    @Autowired
    private FileNodeService fileNodeService;

    /**
     * 设置直链文件密码
     * 
     * @param tenantId 租户id
     * @param userId 用户id
     * @param fileId 文件id
     * @param encryption 是否加密
     * @param password 文件夹密码
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/setLinkPassword")
    public void setLinkPassword(@RequestHeader("auth-tenantId") String tenantId,
        @RequestHeader("auth-userId") String userId, @RequestParam String fileId, @RequestParam Boolean encryption,
        @RequestParam String password, HttpServletResponse response) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>(16);
        try {
            Y9LoginUserHolder.setTenantId(tenantId);
            Y9LoginUserHolder.setPersonId(userId);
            FileNode fileNode = fileNodeService.findById(fileId);
            if (null != fileNode) {
                fileNode.setEncryption(encryption);
                fileNode.setLinkPassword(encryption ? password : "");
                fileNodeService.saveNode(fileNode);
                map.put(UtilConsts.SUCCESS, true);
                map.put("msg", "设置文件直链密码成功");
            } else {
                map.put(UtilConsts.SUCCESS, false);
                map.put("msg", "设置文件直链密码失败");
            }
        } catch (Exception e) {
            map.put(UtilConsts.SUCCESS, false);
            map.put("msg", "设置文件直链密码失败");
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }

    /**
     * 验证直链文件密码
     * 
     * @param tenantId 租户id
     * @param userId 用户id
     * @param fileId 文件id
     * @param password 文件夹密码
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/checkLinkPassword")
    public void checkLinkPassword(@RequestHeader("auth-tenantId") String tenantId,
        @RequestHeader("auth-userId") String userId, @RequestParam String fileId, @RequestParam String password,
        HttpServletResponse response) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>(16);
        try {
            Y9LoginUserHolder.setTenantId(tenantId);
            Y9LoginUserHolder.setPersonId(userId);
            FileNode fileNode = fileNodeService.findById(fileId);
            if (null != fileNode) {
                if (fileNode.getLinkPassword().equals(password)) {
                    map.put(UtilConsts.SUCCESS, true);
                    map.put("msg", "密码验证成功，正在为您下载");
                } else {
                    map.put(UtilConsts.SUCCESS, false);
                    map.put("msg", "密码输入错误，请输入正确的密码");
                }
            } else {
                map.put(UtilConsts.SUCCESS, false);
                map.put("msg", "密码验证失败");
            }
        } catch (Exception e) {
            map.put(UtilConsts.SUCCESS, false);
            map.put("msg", "密码验证失败");
            e.printStackTrace();
        }
        Y9Util.renderJson(response, Y9JsonUtil.writeValueAsString(map));
        return;
    }
}
