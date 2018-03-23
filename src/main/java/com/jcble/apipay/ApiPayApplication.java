package com.jcble.apipay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@SpringBootApplication
@RestController
public class ApiPayApplication extends WebMvcConfigurerAdapter {
	
	@RequestMapping("/")
	public String home() {
		return "JCBLE API PAY";
	}
	
	/**
	 * 解决 Could not find acceptable representation
	 */
	@Override
	public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
		configurer.favorPathExtension(false);
	}
	
	public static void main(String[] args) {
		SpringApplication.run(ApiPayApplication.class, args);
	}
}
