package com.morris.quizly.controllers;

import com.morris.quizly.models.quiz.Quiz;
import com.morris.quizly.models.quiz.QuizRequest;
import com.morris.quizly.models.quiz.QuizlyQuestionGroup;
import com.morris.quizly.services.OpenAiService;
import com.morris.quizly.services.QuizlyDocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/quiz-generation")
public class QuizGenerationController {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuizGenerationController.class);

    private static final String UNSUCCESSFUL = "Unsuccessful";
    private static final String ACCESS_DENIED = "access denied";

    private final OpenAiService openAiService;
    private final QuizlyDocumentService quizlyDocumentService;

    @Autowired
    public QuizGenerationController(OpenAiService openAiService, QuizlyDocumentService quizlyDocumentService) {
        this.openAiService = openAiService;
        this.quizlyDocumentService = quizlyDocumentService;
    }

    /**
     * This method generates a basic {@link Quiz} based on the given QuizRequest.
     *
     * @param quizRequest {@link QuizRequest}
     * @return a newly generated quiz
     */
    @PostMapping("/basic")
    public ResponseEntity<?> generateBasicQuiz(@Validated @RequestBody QuizRequest quizRequest) {
        String questionsContext = String.format("Generate %d questions.", quizRequest.getNumberOfQuestions());
        String prompt = String.format("%s %s", questionsContext, quizRequest.getPrompt());
        String response = openAiService.generateBasicQuizResponse(prompt, quizRequest.getLanguage());

        if (response.equals(UNSUCCESSFUL)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(null);
        }

        // This will begin the flagging process based on user, agent, application, etc.
        // The flagging process is to track and handle unauthorized and/or malicious
        // prompting activities.
        if (response.equalsIgnoreCase(ACCESS_DENIED)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ACCESS_DENIED.toLowerCase());
        }
        List<QuizlyQuestionGroup> quiz = quizlyDocumentService.generateQuizlyPDF(response,  quizRequest);
        if (!quiz.isEmpty()) {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(quiz);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(null);
    }
}
