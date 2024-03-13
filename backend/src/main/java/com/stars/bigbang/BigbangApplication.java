package com.stars.bigbang;

import com.stars.bigbang.rest.RawgApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(clients = RawgApi.class)
public class BigbangApplication {

	public static void main(String[] args) {
		SpringApplication.run(BigbangApplication.class, args);
	}

}
