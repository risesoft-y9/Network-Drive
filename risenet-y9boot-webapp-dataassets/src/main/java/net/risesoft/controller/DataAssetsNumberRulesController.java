package net.risesoft.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import net.risesoft.entity.DataAssetsNumberRules;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.DataAssetsNumberRulesService;
import net.risesoft.y9.Y9LoginUserHolder;
import net.risesoft.y9.json.Y9JsonUtil;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/vue/rules", produces = MediaType.APPLICATION_JSON_VALUE)
public class DataAssetsNumberRulesController {

    private final DataAssetsNumberRulesService dataAssetsNumberRulesService;

    @GetMapping(value = "/getArchivesNumberRules")
    public Y9Result<List<DataAssetsNumberRules>> getArchivesNumberRules(String viewType) {
        List<DataAssetsNumberRules> list = dataAssetsNumberRulesService.findByCategoryMark(viewType);
        return Y9Result.success(list, "获取列表成功");
    }

    @PostMapping(value = "/existRules")
    public Y9Result<Object> existRules(@RequestParam String viewType) {
        List<DataAssetsNumberRules> list = dataAssetsNumberRulesService.findByCategoryMark(viewType);
        if (list.size() > 0) {
            return Y9Result.success(true, "规则已存在");
        } else {
            return Y9Result.success(false, "规则不存在");
        }
    }

    @PostMapping(value = "/saveRules")
    public Y9Result<Object> saveRules(@RequestParam String viewType, @RequestParam String rulesJson) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        String userId = Y9LoginUserHolder.getUserInfo().getPersonId();
        dataAssetsNumberRulesService.deleteByCategoryMark(viewType);
        List<DataAssetsNumberRules> listMap = Y9JsonUtil.readList(rulesJson, DataAssetsNumberRules.class);
        for (DataAssetsNumberRules rules : listMap) {
            rules.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
            rules.setCategoryMark(viewType);
            rules.setCreateDate(sdf.format(date));
            rules.setUserId(userId);
            dataAssetsNumberRulesService.save(rules);
        }
        return Y9Result.success(null, "保存规则成功");
    }
}
