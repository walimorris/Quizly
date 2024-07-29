package com.morris.quizly.services;

import com.morris.quizly.models.locales.Language;

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
     * Creates embeddings using OpenAi's Ada002TextEmbeddings model.
     *
     * @param pdfContent byte[] pdfContent
     * @return {@link List<Double>}
     */
    List<Double> embedWithOpenAiAda002TextEmbeddings(byte[] pdfContent);
}
