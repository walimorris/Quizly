package com.morris.quizly.services.impl;

import com.morris.quizly.models.security.UserDetails;
import com.morris.quizly.repositories.UserRepository;
import com.morris.quizly.services.QuizlyUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;

@Service
public class QuizlyUserDetailsServiceImpl implements QuizlyUserDetailsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuizlyUserDetailsServiceImpl.class);

    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public QuizlyUserDetailsServiceImpl(UserRepository userRepository, MongoTemplate mongoTemplate) {
        this.userRepository = userRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Override
    public UserDetails findBySignupToken(String token) {
        return userRepository.findBySignupToken(token)
                .orElseThrow(() -> new UsernameNotFoundException("signup confirmation token not found: " + token));
    }

    @Override
    public UserDetails findByPasswordResetToken(String token) {
        return userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new UsernameNotFoundException("password reset token not found: " + token));
    }

    @Override
    public UserDetails save(UserDetails userDetails) {
        return userRepository.save(userDetails);
    }

    @Override
    public boolean isUserNameInUse(String userName) {
        return userRepository.findByUsername(userName).isPresent();
    }

    @Override
    public void updateAndSetEncodedPassword(UserDetails user, String password) {
        Query query = new Query(Criteria.where("username").is(user.getUsername()));
        Update update = new Update().set("password", password);
        mongoTemplate.updateFirst(query, update, UserDetails.class);
    }

    @Override
    public void updateEnabledStatus(UserDetails user, boolean enabled) {
        Query query = new Query(Criteria.where("_id").is(user.getId()));
        Update update = new Update().set("enabled", enabled);
        mongoTemplate.updateFirst(query, update, UserDetails.class);
    }

    @Override
    public void updateAndRemovePasswordResetToken(String token) {
        Query query = new Query(Criteria.where("passwordResetToken").is(token));
        Update update = new Update().unset("passwordResetToken");
        update.unset("passwordResetTokenExpiry");
        mongoTemplate.updateFirst(query, update, UserDetails.class);
    }

    @Override
    public void updateAndSetPasswordResetToken(UserDetails user, String token) {
        Query query = new Query(Criteria.where("emailAddress").is(user.getEmailAddress()));
        Update update = new Update().set("passwordResetToken", token);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 1);
        Date expire = calendar.getTime();
        update.set("passwordResetTokenExpiry", expire);

        mongoTemplate.updateFirst(query, update, UserDetails.class);
    }
}
