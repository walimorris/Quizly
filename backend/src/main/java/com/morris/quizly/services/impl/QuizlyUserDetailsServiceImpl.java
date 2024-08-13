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

    private static final String _ID = "_id";
    private static final String ENABLED = "enabled";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String EMAIL_ADDRESS = "emailAddress";
    private static final String PASSWORD_RESET_TOKEN = "passwordResetToken";
    private static final String PASSWORD_RESET_TOKEN_EXPIRY = "passwordResetTokenExpiry";
    private static final String USER_NOT_FOUND_ERROR = "User not found: ";
    private static final String SIGNUP_TOKEN_NOT_FOUND = "signup confirmation token not found: ";
    private static final String PASSWORD_RESET_TOKEN_NOT_FOUND = "password reset token not found: ";

    @Autowired
    public QuizlyUserDetailsServiceImpl(UserRepository userRepository, MongoTemplate mongoTemplate) {
        this.userRepository = userRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_ERROR + username));
    }

    @Override
    public UserDetails findBySignupToken(String token) {
        return userRepository.findBySignupToken(token)
                .orElseThrow(() -> new UsernameNotFoundException(SIGNUP_TOKEN_NOT_FOUND + token));
    }

    @Override
    public UserDetails findByPasswordResetToken(String token) {
        return userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new UsernameNotFoundException(PASSWORD_RESET_TOKEN_NOT_FOUND + token));
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
        Query query = new Query(Criteria.where(USERNAME).is(user.getUsername()));
        Update update = new Update().set(PASSWORD, password);
        mongoTemplate.updateFirst(query, update, UserDetails.class);
    }

    @Override
    public void updateEnabledStatus(UserDetails user, boolean enabled) {
        Query query = new Query(Criteria.where(_ID).is(user.getId()));
        Update update = new Update().set(ENABLED, enabled);
        mongoTemplate.updateFirst(query, update, UserDetails.class);
    }

    @Override
    public void updateAndRemovePasswordResetToken(String token) {
        Query query = new Query(Criteria.where(PASSWORD_RESET_TOKEN).is(token));
        Update update = new Update().unset(PASSWORD_RESET_TOKEN);
        update.unset(PASSWORD_RESET_TOKEN_EXPIRY);
        mongoTemplate.updateFirst(query, update, UserDetails.class);
    }

    // TODO: add a counter here, users make mistakes such as email deletes but we need to protect from spam
    @Override
    public void updateAndSetPasswordResetToken(UserDetails user, String token) {
        Query query = new Query(Criteria.where(EMAIL_ADDRESS).is(user.getEmailAddress()));
        Update update = new Update().set(PASSWORD_RESET_TOKEN, token);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 1);
        Date expire = calendar.getTime();
        update.set(PASSWORD_RESET_TOKEN_EXPIRY, expire);

        mongoTemplate.updateFirst(query, update, UserDetails.class);
    }
}
