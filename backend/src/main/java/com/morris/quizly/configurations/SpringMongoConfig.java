package com.morris.quizly.configurations;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.morris.quizly.models.security.ConfigurationComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;

@Configuration
public class SpringMongoConfig {

    private final ConfigurationComponent configurationComponent;

    @Autowired
    public SpringMongoConfig(ConfigurationComponent configurationComponent) {
        this.configurationComponent = configurationComponent;
    }

    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create(configurationComponent.getMongoUri());
    }

    /**
     * By default, MongoDB saves documents with a _class field in the document. This bean
     * alters the MongoConverter and removes this default feature.
     *
     * @param databaseFactory {@link MongoDatabaseFactory}
     * @param converter       {@link MappingMongoConverter}
     *
     * @return {@link MongoTemplate}
     */
    @Bean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory databaseFactory, MappingMongoConverter converter) {
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        return new MongoTemplate(databaseFactory, converter);
    }
}
