package com.chendi.elasticsearch.demo.config.sqlinjector;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.extension.injector.methods.InsertBatchSomeColumn;

import java.util.List;

/**
 * 重写DefaultSqlInjector
 */
public class SqlInjectorPlus extends DefaultSqlInjector {

    @Override
    public List<AbstractMethod> getMethodList(Class<?> mapperClass) {
        //继承原有方法
        List<AbstractMethod> methodList = super.getMethodList(mapperClass);
        //注入新方法
        methodList.add(new InsertBatchSomeColumn());
        return methodList;
    }
}
