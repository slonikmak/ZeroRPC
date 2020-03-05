package com.oceanos.jeroRPC;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.msgpack.jackson.dataformat.MessagePackFactory;
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
        objectMapper = new ObjectMapper(new MessagePackFactory());
    }

    public void start(){
        executorService.submit(()->{
            logger.info("Start");
            try (ZContext zContext = new ZContext()){
                rep = zContext.createSocket(SocketType.REP);
                rep.bind(address);
                while (!Thread.currentThread().isInterrupted()){
                    logger.debug("Waiting for receive....");
                    byte[] requestBytes = rep.recv();
                    logger.debug("Received request: ");
                    try {
                        RPCMessage requestMsg = objectMapper.readValue(requestBytes, RPCMessage.class);
                        Object result = invoke(requestMsg);
                        if (result instanceof Exception) processException((Exception) result);
                        else {
                            AnswerMsg answerMsg = new AnswerMsg();
                            answerMsg.setResult(result);
                            processAnswer(answerMsg);
                        }

                    } catch (Exception e) {
                        logger.debug("Catch exception "+e.getMessage());
                        processInternalException(e);
                    } catch (Throwable throwable) {
                        logger.debug("Catch throwable "+throwable.getMessage());
                        processInternalException(throwable);
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
            logger.debug("Catch Exception "+e.getMessage());
            return e;
        }
    }

    private void processInternalException(Throwable throwable){
        RPCServiceException exception = new RPCServiceException("Internal RPCService exception:" + throwable.getMessage());
        exception.setStackTrace(throwable.getStackTrace());
        processException(exception);
    }

    private void processException(Exception e){
        AnswerMsg answerMsg = new AnswerMsg();
        answerMsg.setResult(null);
        answerMsg.setException(e);
        try {
            byte[] answerBytes = objectMapper.writeValueAsBytes(answerMsg);
            logger.debug("Send answer ");
            rep.send(answerBytes);
        } catch (JsonProcessingException ex) {
            logger.error("Error while serialize answer message", ex);
        }

    }

    private void processAnswer(AnswerMsg answerMsg){
        try {
            byte[] answerBytes = objectMapper.writeValueAsBytes(answerMsg);
            logger.debug("Send answer ");
            rep.send(answerBytes);
        } catch (JsonProcessingException e) {
            logger.error("Error while serialize answer", e);
        }

    }

    public void shutdown(){
        executorService.shutdownNow();
        logger.info("Shutdown");
    }
}
