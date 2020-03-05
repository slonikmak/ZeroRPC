package com.oceanos.jeroRPC;

public class MultiClientServiceImpl implements MultiClientService {

    @Override
    public int invokeMethod(int multi, int i) {
        return i*multi;
    }
}
