package com.oceanos.jeroRPC;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class ZeroRPCClient<T> {

    static Logger logger = LoggerFactory.getLogger(ZeroRPCClient.class);

    private T service;
    private ExecutorService executorService;
    private String address;
    private ZMQ.Socket req;
    private ObjectMapper objectMapper;
    private LinkedBlockingQueue<Request> requestQueue;

    public ZeroRPCClient(Class<T> serviceType, String address) {
        this.address = address;
        executorService = Executors.newSingleThreadExecutor();
        service = generateService(serviceType);
        requestQueue = new LinkedBlockingQueue<>();
        objectMapper = new ObjectMapper(/*new MessagePackFactory()*/);
    }

    public T getService() {
        return service;
    }

    public void start() {
        executorService.submit(() -> {
            logger.info("Start");
            try (ZContext context = new ZContext()) {
                req = context.createSocket(SocketType.REQ);
                req.connect(address);

                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Request request = requestQueue.take();
                        logger.debug("Take request " + request.getRpcMessage().getMethodName());
                        String requestStr = objectMapper.writeValueAsString(request.getRpcMessage());
                        logger.debug("Send request " + requestStr);
                        req.send(requestStr);
                        String answerStr = req.recvStr();
                        logger.debug("Receive answer " + answerStr);
                        AnswerMsg answerMsg = objectMapper.readValue(answerStr, AnswerMsg.class);
                        request.getFuture().complete(answerMsg);
                    } catch (InterruptedException e) {
                        logger.debug("Interrupt loop");
                        return;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    private T generateService(Class<T> serviceType) {
        return (T) Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class[]{serviceType},
                (proxy, method, args) -> {
                    logger.debug("Call method " + method.getName());
                    logger.debug("Args type" + Arrays.toString(method.getParameterTypes()));
                    //CompletableFuture<AnswerMsg> future = invokeAsync(method.getName(), method.getReturnType(), args);
                    //TODO: исправление ошибки при попытке дессериализации ImmutableList реализовать иначе
                    /*for (int i = 0; i < args.length; i++) {
                        Object o = args[i];
                        if (o.getClass().getName().contains("java.util.ImmutableCollections")){
                            args[i] = ((List)args[i]).stream().collect(Collectors.toList());
                        }
                    }*/
                    CompletableFuture<AnswerMsg> future = invokeAsync(method, args);
                    AnswerMsg answerMsg = future.get();
                    if (answerMsg.getException() != null) {
                        if (answerMsg.getException() instanceof RPCServiceException) {
                            logger.error("Error from service ", answerMsg.getException());
                        } else {
                            Exception e = answerMsg.getException();
                            logger.debug("Error from service ", e);
                            throw e;
                        }
                    }
                    return answerMsg.getResult();
                });
    }


    private CompletableFuture<AnswerMsg> invokeAsync(Method method, Object... args) {
        RPCMessage message = prepareMsg(method, args);
        CompletableFuture<AnswerMsg> future = new CompletableFuture<>();
        Request request = new Request(message, future);
        requestQueue.add(request);
        return future;
    }

    private RPCMessage prepareMsg(Method method, Object... args) {
        RPCMessage rpcMessage = new RPCMessage();
        rpcMessage.setArgs(args);
        rpcMessage.setMethodName(method.getName());
        rpcMessage.setReturnType(method.getReturnType());
        Class<?>[] paramTypes = new Class[0];
        if (args != null) paramTypes = method.getParameterTypes();
        rpcMessage.setParameterTypes(paramTypes);
        return rpcMessage;
    }

    public void shutdown() {
        executorService.shutdownNow();
        logger.info("Shutdown");
    }
}
