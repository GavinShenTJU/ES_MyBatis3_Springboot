package com.chendi.elasticsearch.demo.config;


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;

import java.util.Collection;

@Getter
@Slf4j
public class PreciseShardingAlgorithmForPrice implements PreciseShardingAlgorithm<Double> {
    @Override
    public String doSharding(Collection<String> collection, PreciseShardingValue<Double> preciseShardingValue) {
        log.info("[分表算法]");

        // 真实节点
        collection.stream().forEach((item) -> {
            log.info("[存在的表<{}>]", item);
        });
        log.info("[表名<{}> 列名<{}>]", preciseShardingValue.getLogicTableName(), preciseShardingValue.getColumnName());

        //精确分片
        log.info("[分表列的值<{}>]", preciseShardingValue.getValue());
        // 根据当前日期来分库分表
        Double price = preciseShardingValue.getValue();
        int index = 0;
        if (price > 30d) {
            index = 1;
        }
        String tbName = preciseShardingValue.getLogicTableName() + "_";
        tbName = tbName + index;

        for (String each : collection) {
            if (each.equals(tbName)) {
                return each;
            }
        }
        throw new IllegalArgumentException();
    }
}
