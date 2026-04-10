package com.calt.buroxz.service.mapper;

import com.calt.buroxz.domain.Cart;
import com.calt.buroxz.domain.CartItem;
import com.calt.buroxz.domain.Product;
import com.calt.buroxz.service.dto.*;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * Mapper for the entity {@link CartItem} and its DTO {@link CartItemDTO}.
 */
@Mapper(componentModel = "spring")
public interface CustomizedCartItemMapper extends EntityMapper<CustomizedCartItemDTO, CartItem> {
    @Mapping(target = "product", source = "product", qualifiedByName = "productDetail")
    @Mapping(target = "cart", ignore = true)
    CustomizedCartItemDTO toDto(CartItem s);

    @Named("productDetail")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name") // Add this!
    @Mapping(target = "price", source = "price")
    CustomizedProductDTO toDtoProductDetail(Product product);

    @Named("cartId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    CartDTO toDtoCartId(Cart cart);
}
