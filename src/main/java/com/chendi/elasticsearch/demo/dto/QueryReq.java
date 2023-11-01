package com.chendi.elasticsearch.demo.dto;

import lombok.Data;

@Data
public class QueryReq {
    //分页
    Integer page = 1;
    Integer limit = 10;

    //价格区间
    Double max_price;
    Double min_price;
    //关键字查询
    String keyword = "";

    Byte search_type = 0;
}
