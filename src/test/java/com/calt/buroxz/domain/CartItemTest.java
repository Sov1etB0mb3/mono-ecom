package com.calt.buroxz.domain;

import static com.calt.buroxz.domain.CartItemTestSamples.*;
import static com.calt.buroxz.domain.CartTestSamples.*;
import static com.calt.buroxz.domain.ProductTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.calt.buroxz.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CartItemTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(CartItem.class);
        CartItem cartItem1 = getCartItemSample1();
        CartItem cartItem2 = new CartItem();
        assertThat(cartItem1).isNotEqualTo(cartItem2);

        cartItem2.setId(cartItem1.getId());
        assertThat(cartItem1).isEqualTo(cartItem2);

        cartItem2 = getCartItemSample2();
        assertThat(cartItem1).isNotEqualTo(cartItem2);
    }

    @Test
    void productTest() {
        CartItem cartItem = getCartItemRandomSampleGenerator();
        Product productBack = getProductRandomSampleGenerator();

        cartItem.setProduct(productBack);
        assertThat(cartItem.getProduct()).isEqualTo(productBack);

        cartItem.product(null);
        assertThat(cartItem.getProduct()).isNull();
    }

    @Test
    void cartTest() {
        CartItem cartItem = getCartItemRandomSampleGenerator();
        Cart cartBack = getCartRandomSampleGenerator();

        cartItem.setCart(cartBack);
        assertThat(cartItem.getCart()).isEqualTo(cartBack);

        cartItem.cart(null);
        assertThat(cartItem.getCart()).isNull();
    }
}
