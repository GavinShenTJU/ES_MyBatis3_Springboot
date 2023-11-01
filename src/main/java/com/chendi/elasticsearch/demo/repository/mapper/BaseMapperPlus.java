package com.chendi.elasticsearch.demo.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.Collection;

public interface BaseMapperPlus<T> extends BaseMapper<T> {
    /**
     * 高效率批量插入
     * entityList数量不能太大，否则存在丢包问题；
     * 单次entityList数量务必控制在一万内，或者在service中再次封装控制数量；
     *
     * @param entityList 数据列表
     * @return 插入成功条数
     */
    Integer insertBatchSomeColumn(Collection<T> entityList);
}
