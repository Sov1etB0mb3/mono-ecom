package com.calt.buroxz.service;

import com.calt.buroxz.domain.Order;
import com.calt.buroxz.domain.OrderItem;
import com.calt.buroxz.domain.Product;
import com.calt.buroxz.domain.enumeration.OrderStatus;
import com.calt.buroxz.repository.OrderRepository;
import com.calt.buroxz.repository.ProductRepository;
import com.calt.buroxz.repository.search.OrderSearchRepository;
import com.calt.buroxz.service.dto.CustomizedCartItemDTO;
import com.calt.buroxz.service.dto.OrderDTO;
import com.calt.buroxz.service.dto.request.CartRequest;
import com.calt.buroxz.service.dto.response.OrderResponse;
import com.calt.buroxz.service.mapper.CustomizedCartItemMapper;
import com.calt.buroxz.service.mapper.CustomizedOrderMapper;
import com.calt.buroxz.service.mapper.OrderMapper;
import com.calt.buroxz.web.rest.errors.BadRequestAlertException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link Order}.
 */
@Service
@Transactional
@Primary
public class CustomizedOrderService extends OrderService {

    private static final Logger LOG = LoggerFactory.getLogger(CustomizedOrderService.class);

    private final OrderRepository orderRepository;

    private final OrderMapper orderMapper;
    private final CustomizedOrderMapper cOrderMapper;

    private final OrderSearchRepository orderSearchRepository;
    private final CustomizedCartItemMapper customizedCartItemMapper;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;

    public CustomizedOrderService(
        OrderRepository orderRepository,
        OrderMapper orderMapper,
        CustomizedOrderMapper cOrderMapper,
        OrderSearchRepository orderSearchRepository,
        CustomizedCartItemMapper customizedCartItemMapper,
        ProductRepository productRepository,
        InventoryService inventoryService
    ) {
        super(orderRepository, orderMapper, orderSearchRepository);
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.cOrderMapper = cOrderMapper;
        this.orderSearchRepository = orderSearchRepository;
        this.customizedCartItemMapper = customizedCartItemMapper;
        this.productRepository = productRepository;
        this.inventoryService = inventoryService;
    }

    public OrderResponse checkOut(CartRequest cartRequest) {
        Order order = new Order();
        //    Set<OrderItem> orderItemSet = customizedCartItemMapper.toEntity(cartRequest.getCartItems())
        //        .stream()
        //        .map(cartItem -> {
        //            Product product = cartItem.getProduct();
        //            product.setQuantity(product.getQuantity()-cartItem.getQuantity());
        //            OrderItem orderItem = new OrderItem();
        //            orderItem.setProduct(cartItem.getProduct());
        //            orderItem.setQuantity(cartItem.getQuantity());
        //            orderItem.setPriceAtPurchase(cartItem.getPrice());
        //
        //            return orderItem;
        //        }).collect(Collectors.toSet());
        //        order.setOrderItems(orderItemSet);
        Set<Long> productIds = cartRequest
            .getCartItems()
            .stream()
            .map(cartItem -> cartItem.getProduct().getId())
            .collect(Collectors.toSet());
        List<Product> lsProduct = productRepository.findProductsByIdIn(productIds);
        Map<Long, Product> productMap = lsProduct.stream().collect(Collectors.toMap(Product::getId, p -> p));
        for (CustomizedCartItemDTO itemDTO : cartRequest.getCartItems()) {
            Long pid = itemDTO.getProduct().getId();

            Product readyProduct = productMap.get(pid);
            if (inventoryService.validateStock(itemDTO, readyProduct)) {
                OrderItem orderItem = new OrderItem();
                orderItem.setProduct(readyProduct);
                orderItem.setQuantity(itemDTO.getQuantity());
                orderItem.setPriceAtPurchase(readyProduct.getPrice());
                order.addOrderItem(orderItem);
                readyProduct.setQuantity(readyProduct.getQuantity() - itemDTO.getQuantity());
            }
        }
        order.setTotal(inventoryService.calculatePrice(order));
        order.setStatus(OrderStatus.PENDING); // You should probably set an initial status
        Order savedOrder = orderRepository.save(order);
        return cOrderMapper.toDto(savedOrder);
    }

    public void payment(Long orderId) {
        Order order = orderRepository
            .findById(orderId)
            .orElseThrow(() -> new BadRequestAlertException("ORDER NOTFOUND", orderId.toString(), "nonorder"));
    }

    /**
     * Update a order.
     *
     * @param orderDTO the entity to save.
     * @return the persisted entity.
     */
    public OrderDTO update(OrderDTO orderDTO) {
        LOG.debug("Request to update Order : {}", orderDTO);
        Order order = orderMapper.toEntity(orderDTO);
        order.setIsPersisted();
        order = orderRepository.save(order);
        orderSearchRepository.index(order);
        return orderMapper.toDto(order);
    }

    /**
     * Partially update a order.
     *
     * @param orderDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<OrderDTO> partialUpdate(OrderDTO orderDTO) {
        LOG.debug("Request to partially update Order : {}", orderDTO);

        return orderRepository
            .findById(orderDTO.getId())
            .map(existingOrder -> {
                orderMapper.partialUpdate(existingOrder, orderDTO);

                return existingOrder;
            })
            .map(orderRepository::save)
            .map(savedOrder -> {
                orderSearchRepository.index(savedOrder);
                return savedOrder;
            })
            .map(orderMapper::toDto);
    }

    /**
     * Get all the orders.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<OrderDTO> findAll() {
        LOG.debug("Request to get all Orders");
        return orderRepository.findAll().stream().map(orderMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Get one order by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<OrderDTO> findOne(Long id) {
        LOG.debug("Request to get Order : {}", id);
        return orderRepository.findById(id).map(orderMapper::toDto);
    }

    /**
     * Delete the order by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Order : {}", id);
        orderRepository.deleteById(id);
        orderSearchRepository.deleteFromIndexById(id);
    }

    /**
     * Search for the order corresponding to the query.
     *
     * @param query the query of the search.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<OrderDTO> search(String query) {
        LOG.debug("Request to search Orders for query {}", query);
        try {
            return StreamSupport.stream(orderSearchRepository.search(query).spliterator(), false).map(orderMapper::toDto).toList();
        } catch (RuntimeException e) {
            throw e;
        }
    }
}
