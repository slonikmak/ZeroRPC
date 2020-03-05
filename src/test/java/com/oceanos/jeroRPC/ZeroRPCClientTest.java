package com.oceanos.jeroRPC;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ZeroRPCClientTest {

    static Service service;
    static ZeroRPCClient<Service> zeroRPCClient;
    static ZeroRPCService<Service> zeroRPCService;

    @BeforeAll
    static void setup(){
        zeroRPCClient = new ZeroRPCClient<>(Service.class, "tcp://localhost:5000");
        zeroRPCService = new ZeroRPCService<>(new ServiceImpl(), "tcp://*:5000");
        service = zeroRPCClient.getService();
        zeroRPCService.start();
        zeroRPCClient.start();
    }


    @Test
    public void echoTest(){
        String requestString = "Str";
        String result = service.echo(requestString);
        assertEquals(requestString, result);
    }

    @Test
    public void randomTest(){
        int targetValue = 5;
        int result = service.getRandom();
        assertEquals(targetValue, result);
    }

    @Test
    public void intToStringTest(){
        String targetValue = "5";
        String result = service.intToString(5);
        assertEquals(targetValue, result);
    }

    @Test
    public void boxingArgTest(){
        int expected = 3;
        int actual = service.boxedArg(expected);
        assertEquals(expected, actual);
    }

    @Test
    public void stringToListTest(){
        List<String> expected = List.of("s1","s2");
        List<String> actual = service.stringsToList(expected.get(0), expected.get(1));
        assertLinesMatch(expected,actual);
    }

    @Test
    public void throwExceptionFromMethodTest(){
        assertThrows(Exception.class, ()->{
            service.throwException();
        });
    }

    @Test
    public void concatListTest(){
        String expected = "s1,s2";
        List<String> list = new ArrayList<>();
        list.add("s1");
        list.add("s2");
        String actual = service.concatList(list);
        assertEquals(expected, actual);
    }

    @Test
    public void tryToSendImmutableListToListArg(){
        String expected = "s1,s2";
        List<String> list = List.of("s1","s2");

        assertDoesNotThrow(()->{
            service.concatList(list);
        });

    }

    @Test
    public void mapAsArgumentTest(){
        Map<String, Integer> map = new HashMap<>();
        map.put("1",1);
        map.put("2",2);
        int actual = service.mapSize(map);
        assertEquals(map.size(), actual);
    }

    @Test
    public void mapAsReturnedType(){
        String key = "1";
        int value = 1;
        Map<String, Integer> actual = service.createMap(key, value);
        Map<String, Integer> expected = new HashMap<>();
        expected.put(key, value);
        assertEquals(expected.get(key), actual.get(key));
    }

    @Test
    public void returnPOJOTest(){
        SimplePOJO actual = service.addPOJO("John", 55);
        assertEquals(1, actual.getId());
    }



    @AfterAll
    static void shutdown(){
        zeroRPCClient.shutdown();
        zeroRPCService.shutdown();
    }


}