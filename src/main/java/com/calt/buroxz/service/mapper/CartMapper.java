package com.calt.buroxz.service.mapper;

import com.calt.buroxz.domain.Cart;
import com.calt.buroxz.domain.User;
import com.calt.buroxz.service.dto.CartDTO;
import com.calt.buroxz.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Cart} and its DTO {@link CartDTO}.
 */
@Mapper(componentModel = "spring")
public interface CartMapper extends EntityMapper<CartDTO, Cart> {
    @Mapping(target = "user", source = "user", qualifiedByName = "userLogin")
    CartDTO toDto(Cart s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
