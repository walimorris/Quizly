package com.morris.quizly.services.impl;

import com.morris.quizly.models.ai.SystemAi;
import com.morris.quizly.models.locales.Language;
import com.morris.quizly.models.security.ConfigurationComponent;
import com.morris.quizly.services.OpenAiService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModelName;
import dev.langchain4j.service.AiServices;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class OpenAiServiceImpl implements OpenAiService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenAiServiceImpl.class);

    private static final String GPT_4o = "gpt-4o";

    private static final String QUIZ_GENERATION_ERROR = "Error generating quiz response: {}";
    private static final String UNSUCCESSFUL = "Unsuccessful";

    private final ConfigurationComponent configurationComponent;

    @Autowired
    public OpenAiServiceImpl(ConfigurationComponent configurationComponent) {
        this.configurationComponent = configurationComponent;
    }

    @Override
    public String generateBasicQuizResponse(String prompt, Language language) {
        String systemAiResponse = null;
        try {
            ChatLanguageModel chatLanguageModel = OpenAiChatModel.builder()
                    .apiKey(configurationComponent.getOpenAiApiKey())
                    .modelName(GPT_4o)
                    .timeout(Duration.ofSeconds(30))
                    .build();

            SystemAi systemAi = AiServices.builder(SystemAi.class)
                    .chatLanguageModel(chatLanguageModel)
                    .build();

            switch(language) {
                case EN -> {
                    systemAiResponse = systemAi.openAiQuizPromptEN(prompt);
                }
                case BG -> {
                    systemAiResponse = systemAi.openAiQuizPromptBG(prompt);
                }
            }
        } catch (Exception e) {
            LOGGER.error(QUIZ_GENERATION_ERROR, e.getMessage());
            return UNSUCCESSFUL;
        }
        return systemAiResponse;
    }

    @Override
    public List<Double> embedWithOpenAiAda002TextEmbeddings(byte[] pdfContent) {
        String pdfTextContent;

        try (PDDocument document = PDDocument.load(pdfContent)) {
            PDFTextStripper pdfTextStripper = new PDFTextStripper();
            pdfTextContent = pdfTextStripper.getText(document);
        } catch (IOException e) {
            LOGGER.info("Error extracting PDF content: {}", e.getMessage());
            return new ArrayList<>();
        }
        if (null != pdfTextContent && !pdfTextContent.isEmpty()) {
            EmbeddingModel embeddingModel = new OpenAiEmbeddingModel.OpenAiEmbeddingModelBuilder()
                    .modelName(OpenAiEmbeddingModelName.TEXT_EMBEDDING_ADA_002)
                    .apiKey(configurationComponent.getOpenAiApiKey())
                    .maxRetries(2)
                    .build();
            List<Float> embeddings = embeddingModel.embed(pdfTextContent).content().vectorAsList();
            return embeddings.stream().mapToDouble(f -> f).boxed().toList();
        }
        LOGGER.warn("No text extracted from PDF content.");
        return new ArrayList<>();
    }
}
