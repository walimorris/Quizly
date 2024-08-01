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
                .orElseThrow(() -> new UsernameNotFoundException("token not found: " + token));
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
    public void updateEnabledStatus(UserDetails user, boolean enabled) {
        Query query = new Query(Criteria.where("_id").is(user.getId()));
        Update update = new Update().set("enabled", enabled);
        mongoTemplate.updateFirst(query, update, UserDetails.class);
    }
}
