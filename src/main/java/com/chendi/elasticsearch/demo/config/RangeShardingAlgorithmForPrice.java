package com.chendi.elasticsearch.demo.config;


import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingValue;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;

@Getter
@Slf4j
public class RangeShardingAlgorithmForPrice implements RangeShardingAlgorithm<Double> {
    @Override
    public Collection<String> doSharding(Collection<String> collection, RangeShardingValue<Double> rangeShardingValue) {
        Double low = ObjectUtil.isEmpty(rangeShardingValue.getValueRange().lowerEndpoint()) ? 0d : rangeShardingValue.getValueRange().lowerEndpoint();
        Double high = ObjectUtil.isEmpty(rangeShardingValue.getValueRange().upperEndpoint()) ? Double.MAX_VALUE : rangeShardingValue.getValueRange().upperEndpoint();

        String tbName = rangeShardingValue.getLogicTableName() + "_";
        if (high <= 30d) {
            int index = 0;
            String tmpTbName = tbName + index;
            for (String each : collection) {
                if (each.equals(tmpTbName)) {
                    return Collections.singleton(tmpTbName);
                }
            }
        }

        if (low > 30d) {
            int index = 1;
            String tmpTbName = tbName + index;
            for (String each : collection) {
                if (each.equals(tmpTbName)) {
                    return Collections.singleton(tmpTbName);
                }
            }
        }

        if (low <= 30d && high > 30d) {
            return collection;
        }

        throw new IllegalArgumentException();
    }
}
