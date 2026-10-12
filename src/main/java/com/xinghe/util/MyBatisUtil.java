package com.xinghe.util;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.Reader;

public final class MyBatisUtil {

    private static final SqlSessionFactory SQL_SESSION_FACTORY;

    static {
        // 工厂只创建一次，配置流在读取完成后关闭。
        try (Reader reader = Resources.getResourceAsReader("mybatis-config.xml")) {
            SQL_SESSION_FACTORY = new SqlSessionFactoryBuilder().build(reader);
        } catch (IOException e) {
            throw new IllegalStateException("无法读取 MyBatis 配置 mybatis-config.xml", e);
        }
    }

    private MyBatisUtil() {
    }

    /**
     * 获取共享工厂；每次操作应独立打开并关闭 SqlSession。
     */
    public static SqlSessionFactory getSqlSessionFactory() {
        return SQL_SESSION_FACTORY;
    }
}
