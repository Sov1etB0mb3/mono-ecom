package com.calt.buroxz.repository;

import com.calt.buroxz.domain.User;
import java.util.Optional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the {@link User} entity.
 */
@Repository
public interface CustomizedUserRepository {
    Optional<User> getUserWithAuthById(String id);
    Optional<User> getUserWithAuthByUserName(String username);
}
