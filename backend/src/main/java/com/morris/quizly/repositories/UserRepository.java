package com.morris.quizly.repositories;

import com.morris.quizly.models.security.UserDetails;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<UserDetails, String> {

    /**
     * Find user by username. Usernames in Quizly are also user email address.
     *
     * @param username {@link String} username
     * @return {@link UserDetails}
     */
    Optional<UserDetails> findByUsername(String username);
}
