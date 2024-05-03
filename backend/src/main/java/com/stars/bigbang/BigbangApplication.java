package com.stars.bigbang;

import com.stars.bigbang.controller.rest.RawgApiRestController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableFeignClients(clients = RawgApiRestController.class)
@ComponentScan(basePackageClasses = RawgApiRestController.class)
public class BigbangApplication {

	public static void main(String[] args) {
		SpringApplication.run(BigbangApplication.class, args);
	}

}
