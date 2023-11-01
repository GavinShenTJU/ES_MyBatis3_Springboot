package com.chendi.elasticsearch.demo.repository.mapper;

import com.chendi.elasticsearch.demo.entity.mybatis.TProduct;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author gavins
 * @description 针对表【t_product】的数据库操作Mapper
 * @createDate 2023-10-10 23:18:04
 * @Entity com.chendi.elasticsearch.demo.entity.mybatis.TProduct
 */
@Mapper
public interface TProductMapper extends BaseMapperPlus<TProduct> {

}




