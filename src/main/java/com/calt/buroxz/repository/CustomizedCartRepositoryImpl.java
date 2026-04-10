package com.calt.buroxz.repository;

import com.calt.buroxz.domain.Cart;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class CustomizedCartRepositoryImpl implements CustomizedCartRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Cart getCartWithItem(String userName) {
        return entityManager
            .createQuery(
                "SELECT c FROM Cart c" + " LEFT JOIN FETCH c.user u" + " LEFT JOIN FETCH c.cartItems" + " WHERE c.user.login = :userName ",
                Cart.class
            )
            .setParameter("userName", userName)
            .getSingleResult();
    }
}
