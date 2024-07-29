package com.morris.quizly.repositories;

import com.morris.quizly.models.quiz.Quiz;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizRepository extends MongoRepository<Quiz, String> {

    /**
     * Finds all quizzes by userId and return a {@link Page} of quizzes sorted by most recent documents.
     *
     * @param userId {@link String} userId
     * @param pageable {@link Page} details of page and document count
     *
     * @return {@link Page<Quiz>}
     */
    @Query(value = "{userId:  '?0'}", sort = "{createdDate:  -1}", fields = "{_id:  0, userId:  0, questionsGroup:  0}")
    Page<Quiz> findQuizzesByUserIdDescending(String userId, Pageable pageable);
}
