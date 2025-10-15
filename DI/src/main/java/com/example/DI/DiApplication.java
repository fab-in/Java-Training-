package com.example.DI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class DiApplication {

	public static void main(String[] args) {

		ApplicationContext context = SpringApplication.run(DiApplication.class, args);

		Dev obj = context.getBean(Dev.class);

		obj.build();
	}

}
