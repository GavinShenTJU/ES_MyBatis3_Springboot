package com.chendi.elasticsearch.demo.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.chendi.elasticsearch.demo.config.sqlinjector.SqlInjectorPlus;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@EnableTransactionManagement
@AllArgsConstructor
public class MyBatisPlusConfig {

    private final TransactionManager transactionManager;
    private static final String AOP_POINTCUT_EXPRESSION = "execution (* com.chendi.elasticsearch.demo.repository.impl..*.*(..))";

    /**
     * 分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    /**
     * 增强sql注入的Bean
     */
    @Bean
    public SqlInjectorPlus sqlInjectorPlus() {
        return new SqlInjectorPlus();
    }

    /**
     * 配置事务拦截器,意义不大
     */
    @Bean
    public TransactionInterceptor txAdvice() {
        log.info("start txAdvice ...");
        RuleBasedTransactionAttribute txAttrRequired = new RuleBasedTransactionAttribute();
        txAttrRequired.setName("REQUIRED事务");
        //设置事务传播机制，默认是PROPAGATION_REQUIRED：如果当前存在事务，则加入该事务；如果当前没有事务，则创建一个新的事务
        txAttrRequired.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        //设置异常回滚为Exception  默认是RuntimeException
        List<RollbackRuleAttribute> rollbackRuleAttributes = new ArrayList<>(2);
        rollbackRuleAttributes.add(new RollbackRuleAttribute(Exception.class));
        txAttrRequired.setRollbackRules(rollbackRuleAttributes);

        //事务管理规则，声明具备事务管理的方法名
        NameMatchTransactionAttributeSource source = new NameMatchTransactionAttributeSource();
        //方法名规则限制，必须以下列开头才会加入事务管理当中
        source.addTransactionalMethod("del*", txAttrRequired);
        source.addTransactionalMethod("add*", txAttrRequired);
        source.addTransactionalMethod("modify*", txAttrRequired);
        source.addTransactionalMethod("insert*", txAttrRequired);
        source.addTransactionalMethod("update*", txAttrRequired);
        source.addTransactionalMethod("batch*", txAttrRequired);
        source.addTransactionalMethod("create*", txAttrRequired);
        source.addTransactionalMethod("init*", txAttrRequired);
        return new TransactionInterceptor(transactionManager, source);
    }

    /**
     * 设置切面，没必要添加
     */
    @Bean
    public Advisor txAdviceAdvisor() {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression(AOP_POINTCUT_EXPRESSION);
        return new DefaultPointcutAdvisor(pointcut, txAdvice());
    }
}
