package com.zk.io.nio.test;

import java.nio.IntBuffer;
import java.security.SecureRandom;

/**
 * @program: gupao_dc
 * @description:
 * @author: zk
 * @create: 2019-06-15 18:33
 **/
public class FlipTest {
    public static void main(String[] args) {
        // 分配内存大小为10的缓存区
        IntBuffer buffer = IntBuffer.allocate(10);

        System.out.println("capacity:" + buffer.capacity());

        for (int i = 0; i < 5; ++i) {
            int randomNumber = new SecureRandom().nextInt(20);
            buffer.put(randomNumber);
        }

        System.out.println("before flip limit:" + buffer.limit());

        buffer.flip();

        System.out.println("after flip limit:" + buffer.limit());

        System.out.println("enter while loop");

        while (buffer.hasRemaining()) {
            System.out.println("position:" + buffer.position());
            System.out.println("limit:" + buffer.limit());
            System.out.println("capacity:" + buffer.capacity());
            System.out.println("元素:" + buffer.get());
        }
    }
}
