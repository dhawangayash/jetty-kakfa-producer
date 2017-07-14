package com.jetty.server;

import org.apache.kafka.clients.producer.ProducerRecord;

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
}
