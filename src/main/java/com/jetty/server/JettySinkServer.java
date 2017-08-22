package com.jetty.server;

import io.prometheus.client.exporter.MetricsServlet;
import io.prometheus.client.hotspot.DefaultExports;
import io.prometheus.client.jetty.JettyStatisticsCollector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JettySinkServer {
    private static final Logger LOG = LoggerFactory.getLogger(JettySinkServer.class);

    public static void main(String[] args) throws Exception {
        // processArgs(args);
        Server server = new Server(9080);
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        server.setHandler(context);
        context.addServlet(new ServletHolder(new JettyServletHandler()), "/jetty/*");
        context.addServlet(new ServletHolder(new MetricsServlet()), "/metrics");
        DefaultExports.initialize();

        // Add metrics about CPU, JVM memory
        StatisticsHandler stats = new StatisticsHandler();
        stats.setHandler(server.getHandler());
        server.setHandler(stats);

        new JettyStatisticsCollector(stats).register();

        server.start();
        server.join();
    }
/*
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
        PostToKafka.KafkaProducerSingleton._INSTANCE.setKafkaProducerProps(brokers, retries);
    }
    */
}
