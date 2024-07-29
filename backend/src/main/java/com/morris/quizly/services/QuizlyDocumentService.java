package com.morris.quizly.services;

import com.morris.quizly.models.quiz.Quiz;
import com.morris.quizly.models.quiz.QuizRequest;
import com.morris.quizly.models.quiz.QuizlyQuestionGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Interface provides different functionality for Quizly document and quiz processing.
 */
public interface QuizlyDocumentService {

    /**
     * Generates a PDF document for generated Quizzes.
     *
     * @param text        {@link String} document text
     * @param quizRequest {@link QuizRequest}
     *
     * @return {@link List<QuizlyQuestionGroup>}
     */
    List<QuizlyQuestionGroup> generateQuizlyPDF(String text, QuizRequest quizRequest);

    /**
     * Get quizzes by userId, returning most recent documents first.
     *
     * @param userId   {@link String} userId
     * @param pageable {@link Pageable} containing paging details (page, size)
     *
     * @return {@link Page<Quiz>}
     */
    Page<Quiz> getQuizzesByUserIdDescending(String userId, Pageable pageable);
}
