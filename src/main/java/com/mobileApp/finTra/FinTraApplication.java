package com.mobileApp.finTra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FinTraApplication {

	public static void main(String[] args) {

		SpringApplication.run(FinTraApplication.class, args);
		System.out.println("Backing is running...");
	}

}
