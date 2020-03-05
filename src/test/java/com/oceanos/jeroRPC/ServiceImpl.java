package com.oceanos.jeroRPC;

import java.util.List;
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
        return List.of(s1, s2);
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
}
