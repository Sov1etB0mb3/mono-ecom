package com.calt.buroxz.service.mapper;

import com.calt.buroxz.domain.Order;
import com.calt.buroxz.domain.User;
import com.calt.buroxz.service.dto.OrderDTO;
import com.calt.buroxz.service.dto.UserDTO;
import com.calt.buroxz.service.dto.response.OrderResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * Mapper for the entity {@link Order} and its DTO {@link OrderDTO}.
 */
@Mapper(componentModel = "spring", uses = { CustomizedOrderMapper.class })
public interface CustomizedOrderMapper extends EntityMapper<OrderResponse, Order> {
    @Mapping(target = "user", source = "user", qualifiedByName = "userId")
    OrderResponse toDto(Order s);

    @Named("userId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    UserDTO toDtoUserId(User user);
}
