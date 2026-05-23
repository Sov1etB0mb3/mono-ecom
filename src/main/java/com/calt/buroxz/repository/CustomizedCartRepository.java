package com.calt.buroxz.repository;

import com.calt.buroxz.domain.Cart;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomizedCartRepository {
    public Cart getCartWithItem(String userName);

    public Optional<Cart> findCartByUserLogin(String userName);
}
