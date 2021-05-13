package org.transaction.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.transaction.server.netty.NettyServer;

import javax.annotation.Resource;
import java.net.InetSocketAddress;

@SpringBootApplication
public class ServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
        //启动netty
        InetSocketAddress address = new InetSocketAddress("127.0.0.1", 1001);
        new NettyServer().start(address);
    }

}
