package com.chendi.elasticsearch.demo.repository;

import com.chendi.elasticsearch.demo.entity.mybatis.TProduct;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author gavins
* @description 针对表【t_product】的数据库操作Service
* @createDate 2023-10-10 23:18:04
*/
public interface TProductRepository extends IService<TProduct> {
    List<TProduct> queryByTitle(String title);
    Integer insertBatchSomeColumn(List<TProduct> tProducts);
}
