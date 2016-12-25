package org.thrift.experiment.server;

import org.thrift.experiment.echo.stubs.Echo;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerApp {

    private static Logger LOG = Logger.getLogger(ServerApp.class);

    private static class EchoHandler implements Echo.Iface {

        @Override
        public String echo(String msg) throws TException {
            return msg;
        }
    }

    private static void run() throws Exception {

        EchoHandler echoHandler = new EchoHandler();
        Echo.Processor echoProcessor = new Echo.Processor(echoHandler);

        TServerTransport serverTransport = new TServerSocket(8124);

        ExecutorService threadPool = Executors.newFixedThreadPool(12);

        TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).
                processor(echoProcessor).executorService(threadPool));

        //TServer server = new TSimpleServer(new TServer.Args(serverTransport).processor(echoProcessor));

        LOG.info("Server started...");
        server.serve();
    }

    public static void main(String[] args) {
        try {
            run();
        } catch (Exception e) {
            LOG.fatal(e.getMessage(), e);
        }
    }
}
