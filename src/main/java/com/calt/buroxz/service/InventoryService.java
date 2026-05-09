package com.calt.buroxz.service;

import com.calt.buroxz.domain.Order;
import com.calt.buroxz.domain.Product;
import com.calt.buroxz.service.dto.CustomizedCartItemDTO;
import com.calt.buroxz.service.dto.response.CartResponse;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

    @Transactional(readOnly = true)
    public boolean validateStock(CustomizedCartItemDTO item, Product readyProduct) {
        Integer expectQuantity = item.getQuantity();
        Integer readyQuanity = readyProduct.getQuantity();
        if (expectQuantity > readyQuanity) {
            return false;
        }
        return true;
    }

    public BigDecimal calculatePrice(CartResponse cartResponse) {
        //        return cartResponse.getCartItems().stream().mapToDouble(item -> item.getPrice()).sum();
        return cartResponse
            .getCartItems()
            .stream()
            .map(item -> {
                BigDecimal qty = BigDecimal.valueOf(item.getQuantity());
                return item.getPrice().multiply(qty);
            }) // Or however you get the value
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculatePrice(Order order) {
        //        return cartResponse.getCartItems().stream().mapToDouble(item -> item.getPrice()).sum();
        return order
            .getOrderItems()
            .stream()
            .map(item -> {
                BigDecimal qty = BigDecimal.valueOf(item.getQuantity());
                return item.getPriceAtPurchase().multiply(qty);
            }) // Or however you get the value
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
