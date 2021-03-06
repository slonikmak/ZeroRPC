package com.oceanos.jeroRPC;

import java.util.List;
import java.util.Map;

public interface Service {
    int getRandom();
    String intToString(int i);
    List<String> stringsToList(String s1, String s2);
    String concatList(List<String> list);
    String echo(String str);
    void print(String msg);
    int boxedArg(Integer i);
    void throwException() throws Exception;
    int mapSize(Map<String, Integer> map);
    Map<String, Integer> createMap(String key, int value);
    SimplePOJO addPOJO(String name, Integer age);
}
