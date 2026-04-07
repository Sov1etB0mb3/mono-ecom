package com.calt.buroxz.service.mapper;

import com.calt.buroxz.domain.Order;
import com.calt.buroxz.domain.OrderItem;
import com.calt.buroxz.domain.Product;
import com.calt.buroxz.service.dto.OrderDTO;
import com.calt.buroxz.service.dto.OrderItemDTO;
import com.calt.buroxz.service.dto.ProductDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link OrderItem} and its DTO {@link OrderItemDTO}.
 */
@Mapper(componentModel = "spring")
public interface OrderItemMapper extends EntityMapper<OrderItemDTO, OrderItem> {
    @Mapping(target = "product", source = "product", qualifiedByName = "productId")
    @Mapping(target = "order", source = "order", qualifiedByName = "orderId")
    OrderItemDTO toDto(OrderItem s);

    @Named("productId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    ProductDTO toDtoProductId(Product product);

    @Named("orderId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    OrderDTO toDtoOrderId(Order order);
}
