package com.xinghe.util;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import com.baomidou.mybatisplus.core.MybatisXMLConfigBuilder;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;

import java.io.IOException;
import java.io.Reader;

public final class MyBatisPlusUtil {

    private static final SqlSessionFactory SQL_SESSION_FACTORY;

    static {
        // 独立加载 Plus 配置，不影响原生 MyBatis 的会话工厂。
        try (Reader reader = Resources.getResourceAsReader("mybatis-plus-config.xml")) {
            Configuration configuration = new MybatisXMLConfigBuilder(reader).parse();
            MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
            interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
            configuration.addInterceptor(interceptor);
            SQL_SESSION_FACTORY = new MybatisSqlSessionFactoryBuilder().build(configuration);
        } catch (IOException e) {
            throw new IllegalStateException("无法读取 MyBatis-Plus 配置 mybatis-plus-config.xml", e);
        }
    }

    private MyBatisPlusUtil() {
    }

    /**
     * 获取共享工厂；每次操作应独立打开并关闭 SqlSession。
     */
    public static SqlSessionFactory getSqlSessionFactory() {
        return SQL_SESSION_FACTORY;
    }

}
