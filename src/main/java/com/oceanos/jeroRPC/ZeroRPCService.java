package com.oceanos.jeroRPC;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ZeroRPCService<T> {

    static Logger logger = LoggerFactory.getLogger(ZeroRPCService.class);

    private T wrappedService;
    private ExecutorService executorService;
    private String address;
    private ObjectMapper objectMapper;
    private ZMQ.Socket rep;

    public ZeroRPCService(T wrappedService, String address){
        this.address = address;
        this.wrappedService = wrappedService;
        executorService = Executors.newSingleThreadExecutor();
        objectMapper = new ObjectMapper(/*new MessagePackFactory()*/);
    }

    public void start(){
        executorService.submit(()->{

            try (ZContext zContext = new ZContext()){
                rep = zContext.createSocket(SocketType.REP);
                rep.bind(address);
                while (!Thread.currentThread().isInterrupted()){
                    logger.debug("Waiting for receive....");
                    String requestStr = rep.recvStr();
                    logger.debug("Received request: "+requestStr);
                    try {
                        RPCMessage requestMsg = objectMapper.readValue(requestStr, RPCMessage.class);
                        Object result = invoke(requestMsg);
                        AnswerMsg answerMsg = new AnswerMsg();
                        answerMsg.setResult(result);
                        //answerMsg.setException(null);
                        processAnswer(answerMsg);
                    } catch (Exception e) {
                        processException(e);
                        //e.printStackTrace();
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }
            }

        });
    }

    private Object invoke(RPCMessage message) throws Throwable {
        //Method method = wrappedService.getClass().getMethod(message.getMethodName(), message.getParameterTypes());
        MethodType methodType = MethodType.methodType(message.getReturnType(), message.getParameterTypes());
        if (message.getArgs() == null) message.setArgs(new Object[0]);
        try {
            return MethodHandles.publicLookup().in(wrappedService.getClass())
                    .findVirtual(wrappedService.getClass(), message.getMethodName(), methodType)
                    //.unreflect(method)
                    .bindTo(wrappedService)
                    .invokeWithArguments(Arrays.asList(message.getArgs()));
        } catch (Exception e) {
            logger.debug("GET Exception");
            processException(e);
        }
        return null;
    }

    private void processException(Exception e){
        AnswerMsg answerMsg = new AnswerMsg();
        answerMsg.setResult(null);
        answerMsg.setMessage(e.getMessage());
        answerMsg.setStackTrace(e.getStackTrace());
        try {
            String answerStr = objectMapper.writeValueAsString(answerMsg);
            rep.send(answerStr);
        } catch (JsonProcessingException ex) {
            //ex.printStackTrace();
            logger.error("Error while serialize answer", ex);
        }

    }

    private void processAnswer(AnswerMsg answerMsg){
        try {
            String answerStr = objectMapper.writeValueAsString(answerMsg);
            rep.send(answerStr);
        } catch (JsonProcessingException e) {
            //e.printStackTrace();
            logger.error("Error while serialize answer", e);
        }

    }

    public void shutdown(){
        executorService.shutdownNow();
    }
}
