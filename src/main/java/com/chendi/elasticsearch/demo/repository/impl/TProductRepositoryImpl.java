package com.chendi.elasticsearch.demo.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chendi.elasticsearch.demo.entity.mybatis.TProduct;
import com.chendi.elasticsearch.demo.repository.TProductRepository;
import com.chendi.elasticsearch.demo.repository.mapper.TProductMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author gavins
 * @description 针对表【t_product】的数据库操作Service实现
 * @createDate 2023-10-10 23:18:04
 */
@Service
public class TProductRepositoryImpl extends ServiceImpl<TProductMapper, TProduct> implements TProductRepository {


    @Override
    public List<TProduct> queryByTitle(String title) {
        QueryWrapper<TProduct> wrapper = new QueryWrapper<>();
        wrapper.eq("title", title);
        List<TProduct> tProducts = baseMapper.selectList(wrapper);
        return tProducts;
    }

    @Override
    public Integer insertBatchSomeColumn(List<TProduct> tProducts) {
        return baseMapper.insertBatchSomeColumn(tProducts);
    }
}




