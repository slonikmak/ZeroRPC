package com.oceanos.jeroRPC;

import java.util.stream.Stream;

public class RPCServiceException extends Exception {

    public RPCServiceException(){
        super();
    }

    public RPCServiceException(String message){
        super(message);
    }

}
