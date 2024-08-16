package com.morris.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.mongodb.client.model.Aggregates;
import com.morris.lambda.configurations.SpringMongoConfig;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

/**
 * This Lambda Function will run on a daily schedule, triggered by CloudWatch on AWS. The purpose
 * of this function is to aggregate the top prompts from each user and feed this to the quizly
 * SystemAI to produce a list of recommended prompts based on the users top prompts.
 */
public class PromptRecommendationLambda implements RequestHandler<Object, String> {

    private static final ApplicationContext appContext;

    static {
        appContext = new AnnotationConfigApplicationContext(SpringMongoConfig.class);
    }

    @Override
    public String handleRequest(Object input, Context context) {
        MongoTemplate mongoTemplate = appContext.getBean(MongoTemplate.class);
        processAggregation(mongoTemplate);
        return "Prompt Recommendation Aggregation completed successfully";
    }

    private void processAggregation(MongoTemplate mongoTemplate) {
        List<String> userIds = getAllUserIds(mongoTemplate);
        for (String userId : userIds) {
            List<String> recommendations = generateRecommendationsForUser(mongoTemplate, userId);
            storeRecommendations(mongoTemplate, userId, recommendations);
        }
    }

    private List<String> getAllUserIds(MongoTemplate mongoTemplate) {
        return mongoTemplate.query(Document.class)
                .inCollection("quizzes")
                .distinct("userId")
                .as(String.class)
                .all();
    }

    private List<String> generateRecommendationsForUser(MongoTemplate mongoTemplate, String userId) {
        Aggregation aggregation = Aggregation.newAggregation(
                (AggregationOperation) Aggregates.match((Bson) Criteria.where("userId").is(userId)),
                Aggregation.project("prompt"),
                Aggregation.unwind("prompt"),
                Aggregation.group("prompt").count().as("count"),
                Aggregation.sort(Sort.Direction.DESC, "count"),
                Aggregation.limit(10)
        );
        return mongoTemplate.aggregate(aggregation, "quizzes", String.class)
                .getMappedResults();
    }

    private void storeRecommendations(MongoTemplate mongoTemplate, String userId, List<String> recommendations) {
        // Store recommendations back into MongoDB
        mongoTemplate.updateFirst(
                Query.query(Criteria.where("userId").is(userId)),
                Update.update("recommendations", recommendations),
                "quizzes"
        );
    }
}
