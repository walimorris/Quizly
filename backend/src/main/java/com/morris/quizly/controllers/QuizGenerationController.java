package com.morris.quizly.controllers;

import com.morris.quizly.models.locales.Language;
import com.morris.quizly.models.quiz.Quiz;
import com.morris.quizly.models.quiz.QuizRequest;
import com.morris.quizly.models.quiz.QuizlyQuestionGroup;
import com.morris.quizly.models.system.Flag;
import com.morris.quizly.models.system.FlagType;
import com.morris.quizly.models.system.SystemFlag;
import com.morris.quizly.services.OpenAiService;
import com.morris.quizly.services.QuizlyDocumentService;
import com.morris.quizly.services.SystemFlaggingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/quiz-generation")
public class QuizGenerationController {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuizGenerationController.class);

    private static final String UNSUCCESSFUL = "Unsuccessful";
    private static final String ACCESS_DENIED = "access denied";

    private final OpenAiService openAiService;
    private final QuizlyDocumentService quizlyDocumentService;
    private final SystemFlaggingService systemFlaggingService;

    @Autowired
    public QuizGenerationController(OpenAiService openAiService, QuizlyDocumentService quizlyDocumentService,
                                    SystemFlaggingService systemFlaggingService) {
        this.openAiService = openAiService;
        this.quizlyDocumentService = quizlyDocumentService;
        this.systemFlaggingService = systemFlaggingService;
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

        // Flagging process begins
        if (response.equalsIgnoreCase(ACCESS_DENIED)) {
            SystemFlag systemFlag = SystemFlag.builder()
                    .flagType(FlagType.MALICIOUS_ACTIVITY)
                    .timestamp(Instant.now())
                    .build();
            Flag accountFlag = systemFlaggingService.insertFlag(quizRequest.getUserId(), systemFlag);
            LOGGER.info("Account flagged: {}, for user: {}", accountFlag, quizRequest.getUserId());

            // return lock status to client to begin lock procedure
            if (accountFlag.equals(Flag.FLAG_LOCK)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(accountFlag);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ACCESS_DENIED.toLowerCase() + " " + accountFlag);
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

    // TODO: integrate QuizRequest here into this process
    @PostMapping("/complex")
    public ResponseEntity<?> getDocumentMatches(@RequestParam String prompt, @RequestParam Language language) {
        String response = openAiService.generateQuizResponseWithDocumentContext(prompt, language);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }
}
