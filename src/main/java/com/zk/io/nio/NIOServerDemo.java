package com.zk.io.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @program: gupao_dc
 * @description:
 * @author: zk
 * @create: 2019-06-15 14:26
 **/
public class NIOServerDemo {
    private int port;
    private Selector selector;
    private ByteBuffer byteBuffer=ByteBuffer.allocate(1024);

    //初始化
    public NIOServerDemo(int port){
        this.port=port;
        try {
            ServerSocketChannel server=ServerSocketChannel.open();
            server.bind(new InetSocketAddress(port));
            //采用非阻塞
            server.configureBlocking(false);
            selector = Selector.open();

            server.register(selector, SelectionKey.OP_ACCEPT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void listen() throws IOException {
        System.out.println("listen on :"+this.port+".");
        while(true){//轮询
            selector.select();
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator iterator= keys.iterator();
            while(iterator.hasNext()){
                SelectionKey sk= (SelectionKey) iterator.next();
                iterator.remove();
                process(sk);
            }
        }
    }

    private void process(SelectionKey sk) throws IOException {
        if(sk.isAcceptable()){
            ServerSocketChannel ssc= (ServerSocketChannel) sk.channel();
            SocketChannel sc=ssc.accept();
            sc.configureBlocking(false);
            //数据准备好的时候，状态改为可读
            sk=sc.register(selector,SelectionKey.OP_READ);
        }else if(sk.isReadable()){
            SocketChannel ssc= (SocketChannel) sk.channel();
            int len = ssc.read(byteBuffer);
            if(len>0){
                byteBuffer.flip();
                String str=new String(byteBuffer.array(),0,len);
                sk=ssc.register(selector,SelectionKey.OP_WRITE);
                sk.attach(str);
                System.out.println("读取的内容为："+str);
            }
        }else if(sk.isWritable()){
            SocketChannel ssc= (SocketChannel) sk.channel();
            String content=(String)sk.attachment();

            ssc.write(ByteBuffer.wrap(("输出："+content).getBytes()));
            ssc.close();
        }
    }

    public static void main(String[] args) throws IOException {
        new NIOServerDemo(8083).listen();
    }
}
