package com.zk.io.nio.tomcat;


import com.zk.io.nio.tomcat.http.GPRequest;
import com.zk.io.nio.tomcat.http.GPResponse;
import com.zk.io.nio.tomcat.http.GPServlet;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;


import java.io.FileInputStream;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @program: gupao_dc
 * @description:
 * @author: zk
 * @create: 2019-06-16 12:24
 **/
public class ZKNettyTomcat {
    private int port = 8080;
    private Map<String, GPServlet> servletMapping = new HashMap<String, GPServlet>();

    private Properties webxml = new Properties();

    public void init() throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        String webInfo = this.getClass().getResource("/").getPath();
        FileInputStream fis = new FileInputStream(webInfo + "web.properties");
        webxml.load(fis);
        for (Object obj : webxml.keySet()) {
            String key = obj.toString();
            if ( key.endsWith(".url")) {
                String servletName = key.replaceAll("\\.url$", "");
                String className =  webxml.getProperty(servletName + ".className");
                String url = webxml.getProperty(key);
                GPServlet obj1 = (GPServlet) Class.forName(className).newInstance();
                servletMapping.put(url, obj1);
            }
        }
    }

    public void start() throws Exception {
        init();
        //boss线程
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        // Worker线程
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try{
            //ServetBootstrap--->ServerSocketChannel
            ServerBootstrap server = new ServerBootstrap();
            server.group(bossGroup, workerGroup)
                    // 主线程处理类
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        // 客户端初始化处理
                        protected void initChannel(SocketChannel client) throws Exception {
                            // 无锁化串行编程
                            //Netty对HTTP协议的封装，顺序有要求
                            // HttpResponseEncoder 编码器
                            client.pipeline().addLast(new HttpResponseEncoder());
                            // HttpRequestDecoder 解码器
                            client.pipeline().addLast(new HttpRequestDecoder());
                            // 业务逻辑处理
                            client.pipeline().addLast(new ZKTomcatHandler());
                        }
                    })
                    // 针对主线程的配置 分配线程最大数量 128
                    .option(ChannelOption.SO_BACKLOG, 128)
                    // 针对子线程的配置 保持长连接
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            // 启动服务器
            ChannelFuture f = server.bind(port).sync();
            System.out.println("ZK Tomcat 已启动，监听的端口是：" + port);
            f.channel().closeFuture().sync();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            // 关闭线程池
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }

    private class ZKTomcatHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof HttpRequest){
                HttpRequest req = (HttpRequest) msg;

                // 转交给我们自己的request实现
                GPRequest request = new GPRequest(ctx,req);
                // 转交给我们自己的response实现
                GPResponse response = new GPResponse(ctx,req);
                // 实际业务处理
                String url = request.getUrl();

                if(servletMapping.containsKey(url)){
                    servletMapping.get(url).service(request, response);
                }else{
                    response.write("404 - Not Found");
                }

            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

        }
    }

    public static void main(String[] args) throws Exception {
        new ZKNettyTomcat().start();
    }
}
