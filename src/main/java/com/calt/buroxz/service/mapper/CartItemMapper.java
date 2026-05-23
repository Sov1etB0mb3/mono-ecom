package com.calt.buroxz.service.mapper;

import com.calt.buroxz.domain.Cart;
import com.calt.buroxz.domain.CartItem;
import com.calt.buroxz.domain.Product;
import com.calt.buroxz.service.dto.CartDTO;
import com.calt.buroxz.service.dto.CartItemDTO;
import com.calt.buroxz.service.dto.ProductDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link CartItem} and its DTO {@link CartItemDTO}.
 */
@Mapper(componentModel = "spring")
public interface CartItemMapper extends EntityMapper<CartItemDTO, CartItem> {
    @Mapping(target = "product", source = "product", qualifiedByName = "productId")
    @Mapping(target = "cart", source = "cart", qualifiedByName = "cartId")
    CartItemDTO toDto(CartItem s);

    @Named("productId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    ProductDTO toDtoProductId(Product product);

    @Named("cartId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    CartDTO toDtoCartId(Cart cart);
}
