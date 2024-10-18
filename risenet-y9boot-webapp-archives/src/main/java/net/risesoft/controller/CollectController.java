package net.risesoft.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.risesoft.pojo.Y9Result;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/vue/collect")
public class CollectController {

    @RequestMapping("/getCatalogList")
    public Y9Result<List<Map<String, Object>>> getCatalogList() {
        List<Map<String, Object>> catalogList = new ArrayList<>();
        // 文书
        Map<String, Object> catalog = new HashMap<>();
        catalog.put("id", "ws-01");
        catalog.put("name", "文书");

        List<Map<String, Object>> ws_listmap = new ArrayList<>();
        Map<String, Object> catalog1 = new HashMap<>();
        catalog1.put("id", "ws-011");
        catalog1.put("name", "2023年");
        List<Map<String, Object>> children1 = new ArrayList<>();
        Map<String, Object> child1 = new HashMap<>();
        child1.put("id", "ws-0111");
        child1.put("name", "开发部");
        children1.add(child1);
        Map<String, Object> child2 = new HashMap<>();
        child2.put("id", "ws-0112");
        child2.put("name", "市场部");
        children1.add(child2);
        Map<String, Object> child3 = new HashMap<>();
        child3.put("id", "ws-0113");
        child3.put("name", "财务部");
        children1.add(child3);
        catalog1.put("children", children1);
        ws_listmap.add(catalog1);
        Map<String, Object> catalog2 = new HashMap<>();
        catalog2.put("id", "ws-012");
        catalog2.put("name", "2024年");
        List<Map<String, Object>> children2 = new ArrayList<>();
        Map<String, Object> child4 = new HashMap<>();
        child4.put("id", "ws-0121");
        child4.put("name", "开发部");
        children2.add(child4);
        Map<String, Object> child5 = new HashMap<>();
        child5.put("id", "ws-0122");
        child5.put("name", "市场部");
        children2.add(child5);
        Map<String, Object> child6 = new HashMap<>();
        child6.put("id", "ws-0123");
        child6.put("name", "财务部");
        children2.add(child6);
        catalog2.put("children", children2);
        ws_listmap.add(catalog2);

        catalog.put("children", ws_listmap);
        catalogList.add(catalog);

        // 照片
        Map<String, Object> zp_catalog = new HashMap<>();
        zp_catalog.put("id", "zp-02");
        zp_catalog.put("name", "照片");

        List<Map<String, Object>> zp_listmap = new ArrayList<>();

        Map<String, Object> zp_catalog1 = new HashMap<>();
        zp_catalog1.put("id", "zp-021");
        zp_catalog1.put("name", "2023年");
        List<Map<String, Object>> zp_children1 = new ArrayList<>();

        Map<String, Object> zp_child1 = new HashMap<>();
        zp_child1.put("id", "zp-0211");
        zp_child1.put("name", "开发部");
        zp_children1.add(zp_child1);

        Map<String, Object> zp_child2 = new HashMap<>();
        zp_child2.put("id", "zp-0212");
        zp_child2.put("name", "市场部");
        zp_children1.add(zp_child2);

        Map<String, Object> zp_child3 = new HashMap<>();
        zp_child3.put("id", "zp-0213");
        zp_child3.put("name", "财务部");
        zp_children1.add(zp_child3);
        zp_catalog1.put("children", zp_children1);

        zp_listmap.add(zp_catalog1);

        Map<String, Object> zp_catalog2 = new HashMap<>();
        zp_catalog2.put("id", "zp-022");
        zp_catalog2.put("name", "2024年");
        List<Map<String, Object>> zp_children2 = new ArrayList<>();
        Map<String, Object> zp_child4 = new HashMap<>();
        zp_child4.put("id", "zp-0221");
        zp_child4.put("name", "开发部");
        zp_children2.add(zp_child4);
        Map<String, Object> zp_child5 = new HashMap<>();
        zp_child5.put("id", "zp-0222");
        zp_child5.put("name", "市场部");
        zp_children2.add(zp_child5);
        Map<String, Object> zp_child6 = new HashMap<>();
        zp_child6.put("id", "zp-0223");
        zp_child6.put("name", "财务部");
        zp_children2.add(zp_child6);
        zp_catalog2.put("children", zp_children2);
        zp_listmap.add(zp_catalog2);

        zp_catalog.put("children", zp_listmap);
        catalogList.add(zp_catalog);

        return Y9Result.success(catalogList);
    }

}
