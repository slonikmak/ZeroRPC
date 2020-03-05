package com.oceanos.jeroRPC;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

public class MultiClientTest {

    static MultiClientService service1;
    static MultiClientService service2;
    static MultiClientService service3;
    static ZeroRPCClient<MultiClientService> client1;
    static ZeroRPCClient<MultiClientService> client2;
    static ZeroRPCClient<MultiClientService> client3;
    static ZeroRPCService<MultiClientService> zeroRPCService;
    static int val1 = 0;
    static int val2 = 0;
    static int val3 = 0;
    static ExecutorService executorService;
    static CountDownLatch countDownLatch;

    @BeforeAll
    public static void setup(){
        zeroRPCService = new ZeroRPCService<>(new MultiClientServiceImpl(), "tcp://*:5000");
        client1 = new ZeroRPCClient<>(MultiClientService.class, "tcp://localhost:5000");
        client2 = new ZeroRPCClient<>(MultiClientService.class, "tcp://localhost:5000");
        client3 = new ZeroRPCClient<>(MultiClientService.class, "tcp://localhost:5000");
        service1 = client1.getService();
        service2 = client2.getService();
        service3 = client3.getService();
        zeroRPCService.start();
        client1.start();
        client3.start();
        client2.start();
        executorService = Executors.newFixedThreadPool(3);
        countDownLatch = new CountDownLatch(3);
    }

    @Test
    public void multiClientTest(){
        executorService.submit(()->{
            for (int i = 0; i < 10; i++) {
                val1 = service1.invokeMethod(1,i);
            }
            countDownLatch.countDown();
        });
        executorService.submit(()->{
            for (int i = 0; i < 10; i++) {
                val2 = service2.invokeMethod(10,i);
            }
            countDownLatch.countDown();
        });
        executorService.submit(()->{
            for (int i = 0; i < 10; i++) {
                val3 = service3.invokeMethod(100,i);
            }
            countDownLatch.countDown();
        });
        try {
            countDownLatch.await();
            assertEquals(9, val1);
            assertEquals(90, val2);
            assertEquals(900, val3);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @AfterAll
    public static void shutdown(){
        zeroRPCService.shutdown();
        client1.shutdown();
        client2.shutdown();
        client3.shutdown();
    }
}
