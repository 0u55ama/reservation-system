package com.SchedularApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class SchedularAppApplication {


	public static void main(String[] args) {
		SpringApplication.run(SchedularAppApplication.class, args);
	}

}
