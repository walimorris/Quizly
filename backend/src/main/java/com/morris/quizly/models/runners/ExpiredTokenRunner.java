package com.morris.quizly.models.runners;

import com.morris.quizly.models.security.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class ExpiredTokenRunner {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public ExpiredTokenRunner(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Scheduled(fixedRate = 3600000)
    public void runExpiredTokens() {
        Query query = new Query(Criteria.where("passwordResetTokenExpiry").lt(new Date()));
        Update update = new Update().unset("passwordResetToken").unset("passwordResetTokenExpiry");
        mongoTemplate.updateMulti(query, update, UserDetails.class);
    }
}
