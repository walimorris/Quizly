package com.morris.quizly.services.impl;

import com.morris.quizly.models.security.UserDetails;
import com.morris.quizly.repositories.UserRepository;
import com.morris.quizly.services.QuizlyUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class QuizlyUserDetailsServiceImpl implements QuizlyUserDetailsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuizlyUserDetailsServiceImpl.class);

    private final UserRepository userRepository;

    @Autowired
    public QuizlyUserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Override
    public UserDetails save(UserDetails userDetails) {
        return userRepository.save(userDetails);
    }

    @Override
    public boolean isUserNameInUse(String userName) {
        return userRepository.findByUsername(userName).isPresent();
    }
}
