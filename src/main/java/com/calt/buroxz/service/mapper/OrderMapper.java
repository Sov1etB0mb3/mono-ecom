package com.calt.buroxz.service.mapper;

import com.calt.buroxz.domain.Order;
import com.calt.buroxz.domain.User;
import com.calt.buroxz.service.dto.OrderDTO;
import com.calt.buroxz.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Order} and its DTO {@link OrderDTO}.
 */
@Mapper(componentModel = "spring")
public interface OrderMapper extends EntityMapper<OrderDTO, Order> {
    @Mapping(target = "user", source = "user", qualifiedByName = "userId")
    OrderDTO toDto(Order s);

    @Named("userId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    UserDTO toDtoUserId(User user);
}
