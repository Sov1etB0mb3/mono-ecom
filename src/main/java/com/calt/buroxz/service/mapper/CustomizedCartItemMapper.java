package com.calt.buroxz.service.mapper;

import com.calt.buroxz.domain.Cart;
import com.calt.buroxz.domain.CartItem;
import com.calt.buroxz.domain.Product;
import com.calt.buroxz.service.dto.*;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link CartItem} and its DTO {@link CartItemDTO}.
 */
@Mapper(componentModel = "spring")
public interface CustomizedCartItemMapper extends EntityMapper<CustomizedCartItemDTO, CartItem> {
    @Mapping(target = "product", source = "product", qualifiedByName = "productDetail")
    @Mapping(target = "cart", ignore = true)
    @Mapping(target = "availableStock", source = "product.quantity")
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

    @AfterMapping
    default void setAvailability(@MappingTarget CustomizedCartItemDTO dto) {
        if (dto.getAvailableStock() != null) dto.setAvailable(dto.getAvailableStock() > 0 && dto.getQuantity() <= dto.getAvailableStock());
    }
}
