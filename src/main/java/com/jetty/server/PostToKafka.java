package com.jetty.server;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class PostToKafka {
	final org.apache.kafka.clients.producer.Producer<String,String> _producer;

	public PostToKafka() {
		_producer = KafkaProducerSingleton._INSTANCE.getKafkaProducer();
	}

	public void write2Kafka( String request ) throws Exception {
		ProducerRecord<String, String> record = new ProducerRecord<>("sink_loadtest", request);
		_producer.send(record);
		_producer.flush();
		System.out.println(" Successfully Pushed to Kafka Topic ");
	}

	public static enum KafkaProducerSingleton {
        _INSTANCE;
        private Producer<String, String> producer = null;
        String bootstrapServers = null;
        int retries = 0;


        public Producer<String, String> getKafkaProducer() {
            if (producer == null) {
                System.out.println("bootstrapServers" + bootstrapServers);
                System.out.println("retries" + retries);
                Properties props = new Properties();
                props.put("bootstrap.servers", bootstrapServers);
                props.put("acks", "all");
                props.put("retries", retries);
                props.put("batch.size", 16384);
                props.put("linger.ms", 1);
                props.put("buffer.memory", 33554432);
                props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
                props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
                producer = new KafkaProducer<>(props);
            }
            return producer;
        }

        public void setKafkaProducerProps(String bootstrapServers, int retries) {
            this.bootstrapServers = bootstrapServers;
            this.retries = retries;
        }
    }
}
