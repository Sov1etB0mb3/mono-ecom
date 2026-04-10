package com.calt.buroxz.service.dto.response;

import com.calt.buroxz.domain.CartItem;
import com.calt.buroxz.service.dto.CartItemDTO;
import com.calt.buroxz.service.dto.CustomizedCartItemDTO;
import com.calt.buroxz.service.dto.UserDTO;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A DTO for the {@link com.calt.buroxz.domain.Cart} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CartResponse implements Serializable {

    private Long id;

    private UserDTO user;
    private Set<CustomizedCartItemDTO> cartItems = new HashSet<>();

    private double totalPrice;

    public Set<CustomizedCartItemDTO> getCartItems() {
        return cartItems;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setCartItems(Set<CustomizedCartItemDTO> cartItems) {
        this.cartItems = cartItems;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CartResponse)) {
            return false;
        }

        CartResponse cartDTO = (CartResponse) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, cartDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CartDTO{" +
            "id=" + getId() +
            ", user=" + getUser() +
            "}";
    }
}
