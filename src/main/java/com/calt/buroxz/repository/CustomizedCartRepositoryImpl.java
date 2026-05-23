package com.calt.buroxz.repository;

import com.calt.buroxz.domain.Cart;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class CustomizedCartRepositoryImpl implements CustomizedCartRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Cart getCartWithItem(String userName) {
        List<Cart> results = entityManager
            .createQuery(
                "SELECT c FROM Cart c" +
                " LEFT JOIN FETCH c.user u" +
                " LEFT JOIN FETCH c.cartItems ci" +
                " LEFT JOIN FETCH ci.product" +
                " WHERE c.user.login = :userName ",
                Cart.class
            )
            .setParameter("userName", userName)
            .setHint("org.hibernate.cacheMode", "IGNORE")
            .getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public Optional<Cart> findCartByUserLogin(String userName) {
        List<Cart> results = entityManager
            .createQuery(
                "SELECT c FROM Cart c LEFT JOIN FETCH c.cartItems ci LEFT JOIN FETCH ci.product WHERE c.user.login = :userName",
                Cart.class
            )
            .setParameter("userName", userName)
            .setHint("org.hibernate.cacheMode", "IGNORE")
            .getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}
