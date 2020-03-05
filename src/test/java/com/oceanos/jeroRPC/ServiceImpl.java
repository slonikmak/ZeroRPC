package com.oceanos.jeroRPC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServiceImpl implements Service {
    @Override
    public int getRandom() {
        return 5;
    }

    @Override
    public String intToString(int i) {
        return String.valueOf(i);
    }

    @Override
    public List<String> stringsToList(String s1, String s2) {
        return List.of(s1, s2).stream().collect(Collectors.toList());
    }

    @Override
    public String concatList(List<String> list) {
        return String.join(",", list);
    }

    @Override
    public String echo(String str) {
        return str;
    }

    @Override
    public void print(String msg) {
        System.out.println(msg);
    }

    @Override
    public int boxedArg(Integer i) {
        return i;
    }

    @Override
    public void throwException() throws Exception {
        int a = 1/0;
    }

    @Override
    public int mapSize(Map<String, Integer> map) {
        return map.size();
    }

    @Override
    public Map<String, Integer> createMap(String key, int value) {
        Map<String, Integer> result = new HashMap<>();
        result.put(key, value);
        return result;
    }

    @Override
    public SimplePOJO addPOJO(String name, Integer age) {
        SimplePOJO result = new SimplePOJO();
        result.setAge(age);
        result.setName(name);
        result.setId(1);
        return result;
    }
}
