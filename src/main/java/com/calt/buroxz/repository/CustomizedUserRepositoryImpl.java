package com.calt.buroxz.repository;

import com.calt.buroxz.domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the {@link User} entity.
 */
@Repository
public class CustomizedUserRepositoryImpl implements CustomizedUserRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<User> getUserWithAuthById(String id) {
        return entityManager
            .createQuery("SELECT u FROM User u" + " LEFT JOIN FETCH u.authorities a" + " WHERE u.id = :id", User.class)
            .setParameter("id", id)
            .getResultList()
            .stream()
            .findFirst();
    }

    @Override
    public Optional<User> getUserWithAuthByUserName(String username) {
        return entityManager
            .createQuery("SELECT u FROM User u" + " LEFT JOIN FETCH u.authorities a" + " WHERE u.login = :username", User.class)
            .setParameter("username", username)
            .getResultList()
            .stream()
            .findFirst();
    }
}
