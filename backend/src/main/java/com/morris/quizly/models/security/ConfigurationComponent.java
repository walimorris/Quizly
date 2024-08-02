package com.morris.quizly.models.security;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The process of reading application environment variables have been delegated to Amazon SecretsManager.
 * The Secrets Manager contains the application variables. This process is autoconfigured in general
 * Spring fashion. The ConfigurationComponent will parse the variables and provide our application access
 * to bootstrap itself.
 */
@Component
@Getter
public class ConfigurationComponent {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database}")
    private String mongoDatabase;

    @Value("${data.mongodb.quiz.collection}")
    private String quizCollection;

    @Value("${data.mongodb.quiz.vector.index}")
    private String quizVectorIndex;

    @Value("${security.jwt.token.secret-key}")
    private String jwtSecretKey;

    @Value("${security.jwt.token.expire-length}")
    private long jwtExpireLength;

    @Value("${openai.api.key}")
    private String openAiApiKey;
}
