package com.oceanos.jeroRPC;

import java.util.concurrent.CompletableFuture;

/**
 * @author Oceanos
 */
class Request {
    private RPCMessage rpcMessage;
    private CompletableFuture<AnswerMsg> future;

    Request(RPCMessage rpcMessage, CompletableFuture<AnswerMsg> future) {
        this.rpcMessage = rpcMessage;
        this.future = future;
    }

    RPCMessage getRpcMessage() {
        return rpcMessage;
    }

    CompletableFuture<AnswerMsg> getFuture() {
        return future;
    }
}
