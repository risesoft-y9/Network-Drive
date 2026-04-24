package net.risesoft.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import net.risesoft.api.log.AccessLogApi;
import net.risesoft.model.log.AccessLog;
import net.risesoft.model.log.AccessLogQuery;
import net.risesoft.pojo.Y9Page;

@RestController
@RequestMapping("/vue/fileTagLog")
@RequiredArgsConstructor
public class FileTagLogController {

    private final AccessLogApi accessLogApi;

    /**
     * 获取所有标签
     */
    @RequestMapping(value = "/getOperateLogList", method = RequestMethod.GET)
    public Y9Page<AccessLog> getOperateLogList(AccessLogQuery accessLogQuery, Integer page, Integer rows) {
        accessLogQuery.setSystemName("storage");
        // accessLogQuery.setParamsJson("dept");
        // accessLogQuery.setOperateName("新增自定义文件标签");
        accessLogQuery.setModularName("net.risesoft.controller.FileTagController");
        return accessLogApi.search(accessLogQuery, page, rows);
    }

}
