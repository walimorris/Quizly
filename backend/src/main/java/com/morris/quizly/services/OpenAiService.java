package com.morris.quizly.services;

import com.morris.quizly.models.locales.Language;
import dev.langchain4j.rag.content.retriever.ContentRetriever;

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

    /**
     * Get OpenAI quiz {@link ContentRetriever}. This method is specific
     * to OpenAI by supplying the ContentRetriever with OpenAI Embedding
     * Model. Furthermore, this specific ContentRetriever is responsible
     * for supplying content from MongoDB vector database.
     *
     * @return {@link ContentRetriever}
     */
    ContentRetriever getOpenAiQuizContentRetriever();
}
