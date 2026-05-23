package com.calt.buroxz.service.dto.request;

import com.calt.buroxz.service.dto.CartDTO;
import com.calt.buroxz.service.dto.ProductDTO;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.calt.buroxz.domain.CartItem} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CartItemQuantityRequest implements Serializable {

    private Long id;

    @NotNull
    @Min(value = 1)
    private Integer quantity;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CartItemQuantityRequest)) {
            return false;
        }

        CartItemQuantityRequest cartItemDTO = (CartItemQuantityRequest) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, cartItemDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore

    @Override
    public String toString() {
        return "CartItemQuantityRequest{" +
            "id=" + id +
            ", quantity=" + quantity +
            '}';
    }
}
