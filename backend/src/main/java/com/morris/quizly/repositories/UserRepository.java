package com.morris.quizly.repositories;

import com.morris.quizly.models.security.UserDetails;
import com.morris.quizly.models.system.SystemFlag;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    /**
     * Determines if an account is non-locked.
     *
     * @param userId {@link String} userId
     *
     * @return boolean
     */
    @Query(value = "{ '_id': ?0 }", fields = "{ 'isAccountNonLocked' : 1 }")
    Boolean isAccountNonLocked(String userId);

    /**
     * Get {@link SystemFlag} list of a user.
     *
     * @param userId {@link String} userId
     *
     * @return {@link List<SystemFlag>}
     */
    @Query(value = "{ '_id': ?0 }", fields = "{ 'flags' : 1 }")
    List<SystemFlag> getSystemFlagsByUserId(String userId);
}
