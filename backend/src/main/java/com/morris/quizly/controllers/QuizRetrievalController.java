package com.morris.quizly.controllers;

import com.morris.quizly.models.quiz.Quiz;
import com.morris.quizly.services.QuizlyDocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quiz-retrieval")
public class QuizRetrievalController {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuizRetrievalController.class);

    private final QuizlyDocumentService quizlyDocumentService;
    private final PagedResourcesAssembler<Quiz> pagedResourcesAssembler;

    @Autowired
    public QuizRetrievalController(QuizlyDocumentService quizlyDocumentService, PagedResourcesAssembler<Quiz> pagedResourcesAssembler) {
        this.quizlyDocumentService = quizlyDocumentService;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    /**
     * Returns quizzes by a specified user in descending order (most recent first), paginated
     * in sizes of 9 documents per page.
     *
     * @param userId {@link String} user identification
     * @param page   int: page number
     * @param size   int: document count
     *
     * @return {@link  PagedModel} consisting of quiz documents
     */
    @GetMapping("/{userId}/quizzes")
    public PagedModel<?> getQuizzesByUserIdDescending(@PathVariable String userId,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "9") int size) {

        Page<Quiz> quizzes = quizlyDocumentService.getQuizzesByUserIdDescending(userId, PageRequest.of(page, size));
        return pagedResourcesAssembler.toModel(quizzes);
    }
}
