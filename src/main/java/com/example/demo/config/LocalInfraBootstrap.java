package com.example.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.test.EmbeddedKafkaKraftBroker;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import redis.embedded.RedisServer;

@Slf4j
public final class LocalInfraBootstrap {

	private static final String[] KAFKA_TOPICS = {
		"history-events", "order-events", "inventory-events",
		"shipment-events", "payment-events"
	};
	private static final int REDIS_PORT = 6379;
	private static EmbeddedKafkaKraftBroker embeddedKafkaBroker;
	private static RedisServer redisServer;

	private LocalInfraBootstrap() {
	}

	public static synchronized void start() {
		if (!Boolean.parseBoolean(System.getProperty("app.local-infra.enabled", "true"))) {
			log.info("Local embedded Kafka/Redis are disabled by app.local-infra.enabled=false");
			return;
		}

		startKafka();
		startRedis();
	}

	private static void startKafka() {
		if (embeddedKafkaBroker != null) {
			return;
		}

		embeddedKafkaBroker = new EmbeddedKafkaKraftBroker(1, 1, KAFKA_TOPICS);
		embeddedKafkaBroker.brokerListProperty("spring.kafka.bootstrap-servers");
		embeddedKafkaBroker.afterPropertiesSet();
		log.info("Embedded Kafka started at {}", embeddedKafkaBroker.getBrokersAsString());
	}

	private static void startRedis() {
		if (redisServer != null) {
			return;
		}

		if (isPortOpen("127.0.0.1", REDIS_PORT)) {
			log.info("Redis already reachable on port {}", REDIS_PORT);
			return;
		}

		try {
			redisServer = new RedisServer(REDIS_PORT);
			redisServer.start();
			log.info("Embedded Redis started on port {}", REDIS_PORT);
		} catch (Exception ex) {
			log.warn("Embedded Redis could not start on port {}. App will continue if an external Redis is available.", REDIS_PORT, ex);
		}
	}

	private static boolean isPortOpen(String host, int port) {
		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress(host, port), 500);
			return true;
		} catch (IOException ex) {
			return false;
		}
	}

	public static synchronized void stop() {
		if (embeddedKafkaBroker != null) {
			embeddedKafkaBroker.destroy();
			embeddedKafkaBroker = null;
		}
		if (redisServer != null) {
			redisServer.stop();
			redisServer = null;
		}
	}
}