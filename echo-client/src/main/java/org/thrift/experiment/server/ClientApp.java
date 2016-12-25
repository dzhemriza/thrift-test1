package org.thrift.experiment.server;

import org.thrift.experiment.echo.stubs.Echo;
import org.apache.log4j.Logger;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientApp {

    private static Logger LOG = Logger.getLogger(ClientApp.class);

    private static AtomicInteger messagesPerSecond = new AtomicInteger();
    private static final int NUMBER_OF_MESSAGES = 1000000;
    private static final int NUMBER_OF_CLIENTS = 1;

    private static void runImpl() throws Exception {
        TTransport transport;

        transport = new TSocket("localhost", 8124); // NOT A THREAD SAFE
        transport.open();

        TProtocol protocol = new TBinaryProtocol(transport);
        Echo.Client echoClient = new Echo.Client(protocol);

        for (int i = 0; i < NUMBER_OF_MESSAGES; ++i) {

            //final String MSG = "test data ... data test";
            final String MSG = "TEST DATA ... DATA TESTTEST DATA ... DATA TESTTEST DATA ... DATA TESTTEST DATA ... DATA TESTTEST DATA ... DATA TESTTEST DATA ... DATA TESTTEST DATA ... DATA TEST";
            String msg = echoClient.echo(MSG);
            if (MSG.equals(msg)) {
                // do nothing it works
                //LOG.info("It works: " + msg);
            } else {
                LOG.error("Invalid message received: " + msg);
            }
            messagesPerSecond.incrementAndGet();
        }

        transport.close();
    }

    private static void run() throws Exception {
        messagesPerSecond.set(0);
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

        scheduledExecutorService.scheduleAtFixedRate((Runnable) () -> trackTime(), 1, 1, TimeUnit.SECONDS);

        ArrayList<Future<?>> concurrentClients = new ArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_CLIENTS);

        for (int i = 0; i < NUMBER_OF_CLIENTS; ++i) {
            concurrentClients.add(executorService.submit((Runnable)() -> {
                try {
                    runImpl();
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            }));
        }

        for (Future<?> task : concurrentClients) {
            task.get();
        }

        scheduledExecutorService.shutdown();
        executorService.shutdown();
        trackTime();
    }

    private static void trackTime() {
        int rate = messagesPerSecond.getAndSet(0);
        LOG.info("Messages per second: " + rate);
    }

    public static void main(String[] args) {
        try {
            run();
        } catch (Exception e) {
            LOG.fatal(e.getMessage(), e);
        }
    }
}
