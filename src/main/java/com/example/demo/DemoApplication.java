package com.example.demo;

import com.example.demo.config.LocalInfraBootstrap;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		LocalInfraBootstrap.start();
		Runtime.getRuntime().addShutdownHook(new Thread(LocalInfraBootstrap::stop));
		SpringApplication.run(DemoApplication.class, args);
	}

}
