package com.morris.quizly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;

@SpringBootApplication(exclude = {
		SecurityAutoConfiguration.class,
		SecurityFilterAutoConfiguration.class
})
public class QuizlyApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuizlyApplication.class, args);
	}

}
