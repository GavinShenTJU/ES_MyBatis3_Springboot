package com.chendi.elasticsearch.demo.controller;

import com.chendi.elasticsearch.demo.dto.QueryReq;
import com.chendi.elasticsearch.demo.entity.db.Product;
import com.chendi.elasticsearch.demo.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/")
    public String index(){
        return "index";
    }

    @PostMapping("/add_product")
    @ResponseBody
    public Map<String, String> addProduct(@RequestBody Product product) {
        productService.addProduct(product);
        Map<String, String> map = new HashMap<>();
        map.put("msg", "ok");
        return map;
    }

    @GetMapping("/search_product")
    @ResponseBody
    public Map<String, Object> search(QueryReq req) throws IOException {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Map<String, Object> result = productService.searchProduct(req);
        stopWatch.stop();
        final double totalTimeSeconds = stopWatch.getTotalTimeSeconds();
        System.out.println("耗时：" + totalTimeSeconds);
        return result;
    }

    @PostMapping("/del_product")
    @ResponseBody
    public Map<String, Object> del(@RequestBody List<Integer> ids) throws IOException {
        return productService.del(ids);
    }

    @GetMapping("/aggregation")
    @ResponseBody
    public Map<String, Object> aggregation() throws IOException {
        return productService.aggregation();
    }
}
