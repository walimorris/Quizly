package com.morris.quizly.services.impl;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.search.FieldSearchPath;
import com.mongodb.client.model.search.SearchPath;
import com.morris.quizly.models.locales.Language;
import com.morris.quizly.models.quiz.Quiz;
import com.morris.quizly.models.security.ConfigurationComponent;
import com.morris.quizly.models.system.SystemAi;
import com.morris.quizly.services.OpenAiService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModelName;
import dev.langchain4j.service.AiServices;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Aggregates.project;
import static com.mongodb.client.model.Aggregates.vectorSearch;
import static com.mongodb.client.model.Projections.*;
import static java.util.Arrays.asList;

@Service
public class OpenAiServiceImpl implements OpenAiService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenAiServiceImpl.class);

    private static final String GPT_4o = "gpt-4o";

    private static final String QUIZ_GENERATION_ERROR = "Error generating quiz response: {}";
    private static final String UNSUCCESSFUL = "Unsuccessful";

    private final ConfigurationComponent configurationComponent;;
    private final MongoTemplate mongoTemplate;
    private final CodecRegistry codecRegistry;

    @Autowired
    public OpenAiServiceImpl(ConfigurationComponent configurationComponent,
                             MongoTemplate mongoTemplate,
                             CodecRegistry codecRegistry) {
        this.configurationComponent = configurationComponent;
        this.mongoTemplate = mongoTemplate;
        this.codecRegistry = codecRegistry;
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
    public String generateQuizResponseWithDocumentContext(String prompt, Language language) {
        EmbeddingModel embeddingModel = new OpenAiEmbeddingModel.OpenAiEmbeddingModelBuilder()
                .modelName(OpenAiEmbeddingModelName.TEXT_EMBEDDING_ADA_002)
                .apiKey(configurationComponent.getOpenAiApiKey())
                .maxRetries(2)
                .build();

        // We can get the matching documents in our vector from the prompt and collect
        // the questionsGroups and pass this as context to our AI system. This ensures
        // we generate unique quizzes
        List<Quiz> quizMatches = getMatchingQuizDocuments(embeddingModel, prompt);
        List<String> questionsGroups = new ArrayList<>();
        quizMatches.forEach(quiz -> {
            if (quiz.getLanguage().equals(language)) {
                LOGGER.info("{}", quiz.getQuizTitle());
                LOGGER.info("{}", quiz.getQuestionsGroup().toString());
                questionsGroups.add(quiz.getQuestionsGroup().toString());
            }
        });
        ChatLanguageModel chatLanguageModel = OpenAiChatModel.builder()
                .apiKey(configurationComponent.getOpenAiApiKey())
                .modelName(GPT_4o)
                .timeout(Duration.ofSeconds(30))
                .build();

        SystemAi systemAi = AiServices.builder(SystemAi.class)
                .chatLanguageModel(chatLanguageModel)
                .build();

        String systemAiResponse = null;
        switch(language) {
            case EN -> systemAiResponse = systemAi.openAiWithContextQuizPromptEN(
                    prompt,
                    questionsGroups.toString()
            );
            case BG -> systemAiResponse = systemAi.openAiWithContextQuizPromptBG(
                    prompt,
                    questionsGroups.toString()
            );
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

    // For now, we must roll our own search results for matching documents
    // until langchain4j supports correct mongodb atlas dependencies that
    // will enable us to use the MongoDBEmbeddingStore in our project.
    @Override
    public List<Quiz> getMatchingQuizDocuments(EmbeddingModel embeddingModel, String prompt) {
        List<Quiz> quizResults  = new ArrayList<>();
        if (!prompt.isEmpty()) {
            List<Float> promptVectorEmbedding = embeddingModel.embed(prompt).content().vectorAsList();
            List<Double> promptEmbedding = promptVectorEmbedding.stream()
                    .mapToDouble(f -> f)
                    .boxed()
                    .toList();
            if (!promptEmbedding.isEmpty()) {
                MongoDatabase database = mongoTemplate.getDb().withCodecRegistry(codecRegistry);
                MongoCollection<Quiz> collection = database.getCollection("quizzes", Quiz.class);
                FieldSearchPath fieldSearchPath = SearchPath.fieldPath("pdfEmbeddings");

                int candidates = 200;
                int limit = 10;

                List<Bson> pipeline = asList(
                        vectorSearch(
                                fieldSearchPath,
                                promptEmbedding,
                                "quiz_pdf_vector_index",
                                candidates,
                                limit
                        ),
                        project(
                               fields(metaVectorSearchScore("score"),
                                       include("_id"),
                                       include("userId"),
                                       include("quizTitle"),
                                       include("questionsGroup"),
                                       include("pdfContent"),
                                       include("pdfEmbeddings"),
                                       include("pdfImage"),
                                       include("language")
                               )
                        )
                );
                // run query and marshall results
                collection.aggregate(pipeline).forEach(quizResults::add);
            }
        }
        return quizResults;
    }
}
