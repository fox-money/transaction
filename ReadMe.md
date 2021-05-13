开发一个事物框架, 首先就是要拿到事物的控制权, 自己定义事物的提交与回滚.
我们知道, 在JDBC里面, 各个数据库是通过实现javax.sql.DataSource接口来获得数据库连接的:

_public interface DataSource  extends CommonDataSource, Wrapper {

/**
* <p>Attempts to establish a connection with the data source that
* this {@code DataSource} object represents.
*
* @return  a connection to the data source
* @exception SQLException if a database access error occurs
* @throws java.sql.SQLTimeoutException  when the driver has determined that the
* timeout value specified by the {@code setLoginTimeout} method
* has been exceeded and has at least tried to cancel the
* current database connection attempt
  */
  Connection getConnection() throws SQLException;

/**
* <p>Attempts to establish a connection with the data source that
* this {@code DataSource} object represents.
*
* @param username the database user on whose behalf the connection is
*  being made
* @param password the user's password
* @return  a connection to the data source
* @exception SQLException if a database access error occurs
* @throws java.sql.SQLTimeoutException  when the driver has determined that the
* timeout value specified by the {@code setLoginTimeout} method
* has been exceeded and has at least tried to cancel the
* current database connection attempt
* @since 1.4
  */
  Connection getConnection(String username, String password)
  throws SQLException;
  }_
  
通过调用DataSource的getConnection方法, 可以获得一个Connection连接, Connection中定义了事物的相关方法.那么, 我们就可以利用切面编程,
在调用getConnection方法的时候返回我们自定义的连接, 获取事物的控制权.

@Around(value = "execution(* javax.sql.DataSource.getConnection(..))")
public Connection invoke(ProceedingJoinPoint proceed) throws Throwable {

        Connection connection = (TransactionConnection) proceed.proceed();

        return new TransactionConnection(connection);

    }

拿到事物的控制权之后, 我们就可以开始实现我们的分布式事务逻辑了.

**@GlobalTranscation**: 全局事物注解, 被该注解修饰的方法需要开启或加入一个全局事物;

**BranchTransaction**: 全局事务对象, 记录了事物的ID及状态等信息, 并提供了当前事物线程的阻塞与唤醒方法;

**Branch**: 记录分支事物对应的netty通道ID, 用于在全局事务完成的时候服务端通知客户端;

**TranscationManage**: 事物管理者 生成全局事物ID 向服务端发送消息 注册事物


使用netty进行服务端与客户端之间的消息传递
