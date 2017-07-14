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

/*
    curl -X POST -H "Content-Type: application/json"
            --data '{"name": "s3-sink-rt-4",
            "config": {
                "connector.class":"io.confluent.connect.s3.S3SinkConnector",
                "tasks.max":"1",
                "topics":"connect-distributed-realtime-jul12-1114",
                "storage.class": "io.confluent.connect.s3.storage.S3Storage",
                "partition.duration.ms": "100",
                "key.converter.schemas.enable": "false",
                "partitioner.class": "io.confluent.connect.storage.partitioner.DefaultPartitioner",
                "schema.generator.class": "io.confluent.connect.storage.hive.schema.DefaultSchemaGenerator",
                "s3.part.size": "5242880",
                "format.class": "io.confluent.connect.s3.format.json.JsonFormat", "timezone": "GMT", "locale": "US", "schema.compatibility": "NONE", "connector.class": "io.confluent.connect.s3.S3SinkConnector", "s3.region": "us-west-2", "value.converter.schemas.enable": "false", "flush.size": "3", "s3.bucket.name": "datavisor-dev"}}' http://localhost:8083/connectors
*/



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
