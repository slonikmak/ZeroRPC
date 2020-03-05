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

public class MultiThreadingTest {

    static ExecutorService executorService;
    static CountDownLatch countDownLatch;
    static ZeroRPCService<MultiClientService> zeroRPCService;
    static MultiClientService service;
    static ZeroRPCClient<MultiClientService> client;
    static int val1 = 0;
    static int val2 = 0;
    static int val3 = 0;

    @BeforeAll
    public static void setup(){
        zeroRPCService = new ZeroRPCService<>(new MultiClientServiceImpl(), "tcp://*:5000");
        client = new ZeroRPCClient<>(MultiClientService.class, "tcp://localhost:5000");
        service = client.getService();
        executorService = Executors.newFixedThreadPool(3);
        countDownLatch = new CountDownLatch(3);
        zeroRPCService.start();
        client.start();
    }

    @Test
    public void multiThreadAccessToServiceTest(){
        executorService.submit(()->{
            for (int i = 0; i < 10; i++) {
                val1 = service.invokeMethod(1,i);
            }
            countDownLatch.countDown();
        });
        executorService.submit(()->{
            for (int i = 0; i < 10; i++) {
                val2 = service.invokeMethod(10,i);
            }
            countDownLatch.countDown();
        });
        executorService.submit(()->{
            for (int i = 0; i < 10; i++) {
                val3 = service.invokeMethod(100,i);
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
        client.shutdown();
        executorService.shutdownNow();
    }
}
