package com.morris.quizly;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
class QuizlyApplicationTests {
	private static final String SYSTEM_AI_BASIC_QUIZ_TEMPLATE = "/prompts/basic-quiz-format-en.txt";
	private static final String DEJAVU_SANS_FONT = "/fonts/DejaVuSans.ttf";

	@Test
	void resourceLoaderTest() {
		try {
			ClassPathResource basicQuizTemplate = new ClassPathResource(SYSTEM_AI_BASIC_QUIZ_TEMPLATE);
			ClassPathResource dejavu = new ClassPathResource(DEJAVU_SANS_FONT);
			Path path1 = basicQuizTemplate.getFile().toPath();
			Path path2 = dejavu.getFile().toPath();
			String content1 = new String(Files.readAllBytes(path1));
			String content2 = new String(Files.readAllBytes(path2));
			if (content1.isEmpty() || content2.isEmpty()) {
				fail();
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
}
