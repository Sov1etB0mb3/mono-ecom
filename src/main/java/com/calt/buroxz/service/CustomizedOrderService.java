//package com.calt.buroxz.service;
//
//import com.calt.buroxz.domain.Order;
//import com.calt.buroxz.domain.OrderItem;
//import com.calt.buroxz.repository.OrderRepository;
//import com.calt.buroxz.repository.search.OrderSearchRepository;
//import com.calt.buroxz.service.dto.OrderDTO;
//import com.calt.buroxz.service.mapper.OrderMapper;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.context.annotation.Primary;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//import java.util.stream.StreamSupport;
//
///**
// * Service Implementation for managing {@link Order}.
// */
//@Service
//@Transactional
//@Primary
//public class CustomizedOrderService extends OrderService{
//
//    private static final Logger LOG = LoggerFactory.getLogger(CustomizedOrderService.class);
//
//    private final OrderRepository orderRepository;
//
//    private final OrderMapper orderMapper;
//
//    private final OrderSearchRepository orderSearchRepository;
//
//    public CustomizedOrderService(OrderRepository orderRepository, OrderMapper orderMapper, OrderSearchRepository orderSearchRepository) {
//        super(orderRepository,orderMapper,orderSearchRepository);
//        this.orderRepository = orderRepository;
//        this.orderMapper = orderMapper;
//        this.orderSearchRepository = orderSearchRepository;
//    }
//    @
//    public OrderDTO createOrder(){
//        OrderItem orderItem = new
//
//    }
//}
