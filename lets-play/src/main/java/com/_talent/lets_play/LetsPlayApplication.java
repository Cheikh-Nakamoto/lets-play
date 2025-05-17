package com._talent.lets_play;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class LetsPlayApplication {

	public static void main(String[] args) {
		SpringApplication.run(LetsPlayApplication.class, args);
	}

}
