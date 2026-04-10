package com.calt.buroxz.service.mapper;

import com.calt.buroxz.domain.Cart;
import com.calt.buroxz.domain.User;
import com.calt.buroxz.service.dto.CartDTO;
import com.calt.buroxz.service.dto.UserDTO;
import com.calt.buroxz.service.dto.request.CartRequest;
import com.calt.buroxz.service.dto.response.CartResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * Mapper for the entity {@link Cart} and its DTO {@link CartDTO}.
 */
@Mapper(componentModel = "spring", uses = { CustomizedCartItemMapper.class })
public interface CustomizedCartMapper extends EntityMapper<CartResponse, Cart> {
    @Mapping(target = "user", source = "user", qualifiedByName = "userLogin")
    @Mapping(target = "cartItems", source = "cartItems")
    CartResponse toDto(Cart s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);

    Cart toEntity(CartRequest cartRequest);
}
