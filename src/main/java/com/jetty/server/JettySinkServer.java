package com.jetty.server;

import org.eclipse.jetty.server.Server;

public class JettySinkServer {
 
    public static void main(String[] args) throws Exception
    {
//        processArgs(args);
        Server server = new Server(9080);
        server.setHandler(new JettyHandler());
        server.start();
        server.join();
    }

    private static void processArgs(String... args) {
        String brokers = new String();
        int retries = 0;
        for ( String arg : args) {
            String[] keyValue = arg.split("=", 2);
            String key = keyValue[0], value = keyValue[1];
            if (key.equalsIgnoreCase("brokers"))
                brokers = value;
            if (key.equalsIgnoreCase("retries"))
                retries = Integer.parseInt(value);

        }
        KafkaProducerSingleton._INSTANCE.setKafkaProducerProps(brokers, retries);
    }
}
