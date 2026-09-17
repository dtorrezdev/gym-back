package com.dtorrez.gym;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication(scanBasePackages = {"com.dtorrez.gym", "com.micrium.bd.access"})
public class ModulobaseApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(ModulobaseApplication.class, args);
	}

}
