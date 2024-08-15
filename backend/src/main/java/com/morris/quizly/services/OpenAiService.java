package com.morris.quizly.services;

import com.morris.quizly.models.locales.Language;
import com.morris.quizly.models.quiz.Quiz;
import dev.langchain4j.model.embedding.EmbeddingModel;

import java.util.List;

/**
 * Interface to provide different functionality for the OpenAI service.
 */
public interface OpenAiService {

    /**
     * Generate basic quiz generation response.
     *
     * @param prompt   {@link String} user input to AI system interface
     * @param language {@link Language} response language
     *
     * @return {@link String} quiz response
     */
    String generateBasicQuizResponse(String prompt, Language language);

    /**
     * Generate quiz generation response with given context. This ensures that
     * generated quizzes are unique for users.
     *
     * @param userId   {@link String} userId
     * @param prompt   {@link String} user prompt
     * @param language {@link Language} language
     *
     * @return {@link String}
     */
    String generateQuizResponseWithDocumentContext(String userId, String prompt, Language language);

    /**
     * Creates embeddings using OpenAi's Ada002TextEmbeddings model.
     *
     * @param pdfContent byte[] pdfContent
     * @return {@link List<Double>}
     */
    List<Double> embedWithOpenAiAda002TextEmbeddings(byte[] pdfContent);

    /**
     * Get {@link List} of matching quizzes based on given user prompt.
     * For now, we must roll our own search results for matching documents
     * until langchain4j supports correct mongodb atlas dependencies that
     * will enable us to use the MongoDBEmbeddingStore in our project.
     *
     * @param userId         {@link String} userId
     * @param language       {@link Language} language
     * @param embeddingModel {@link EmbeddingModel} embedding model
     * @param prompt         {@link String} user prompt
     *
     * @return {@link List<Quiz>}
     */
    List<Quiz> getMatchingQuizDocuments(String userId, Language language, String prompt, EmbeddingModel embeddingModel);
}
