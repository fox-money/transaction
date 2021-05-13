package org.transcation.global.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.transcation.global.connection.TransactionConnection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.Connection;

/**
 * 获取sql连接池切面 获得事物的控制权
 * @author: huml
 * @createTime: 2021/1/18
 */
@Component
@Aspect
public class DataSourceAspect {

    /**
     * java在获取数据库连接的时候, 是调用的DataSource呃getConnection方法
     * 我们只需要拦截这个方法,将返回的连接池变为我们自己定义的连接, 就可以获得事物的控制权
     * @param proceed
     * @return
     * @throws Throwable
     */
    @Around(value = "execution(* javax.sql.DataSource.getConnection(..))")
    public Connection invoke(ProceedingJoinPoint proceed) throws Throwable {

        Connection connection = (TransactionConnection) proceed.proceed();

        return new TransactionConnection(connection);

    }


}
