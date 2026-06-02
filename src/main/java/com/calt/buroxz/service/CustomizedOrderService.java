package com.calt.buroxz.service;

import com.calt.buroxz.domain.Cart;
import com.calt.buroxz.domain.CartItem;
import com.calt.buroxz.domain.Order;
import com.calt.buroxz.domain.OrderItem;
import com.calt.buroxz.domain.Product;
import com.calt.buroxz.domain.User;
import com.calt.buroxz.domain.enumeration.OrderStatus;
import com.calt.buroxz.repository.CartItemRepository;
import com.calt.buroxz.repository.CustomizedCartRepository;
import com.calt.buroxz.repository.OrderItemRepository;
import com.calt.buroxz.repository.OrderRepository;
import com.calt.buroxz.repository.ProductRepository;
import com.calt.buroxz.repository.UserRepository;
import com.calt.buroxz.repository.search.OrderSearchRepository;
import com.calt.buroxz.service.dto.OrderDTO;
import com.calt.buroxz.service.dto.OrderItemDTO;
import com.calt.buroxz.service.dto.response.OrderResponse;
import com.calt.buroxz.service.mapper.CustomizedCartItemMapper;
import com.calt.buroxz.service.mapper.CustomizedOrderMapper;
import com.calt.buroxz.service.mapper.OrderItemMapper;
import com.calt.buroxz.service.mapper.OrderMapper;
import com.calt.buroxz.web.rest.errors.BadRequestAlertException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final CustomizedCartRepository customizedCartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final StripeService stripeService;
    private final OrderItemRepository orderItemRepository;
    private final OrderItemMapper orderItemMapper;

    public CustomizedOrderService(
        OrderRepository orderRepository,
        OrderMapper orderMapper,
        CustomizedOrderMapper cOrderMapper,
        OrderSearchRepository orderSearchRepository,
        CustomizedCartItemMapper customizedCartItemMapper,
        ProductRepository productRepository,
        InventoryService inventoryService,
        CustomizedCartRepository customizedCartRepository,
        CartItemRepository cartItemRepository,
        UserRepository userRepository,
        StripeService stripeService,
        OrderItemRepository orderItemRepository,
        OrderItemMapper orderItemMapper
    ) {
        super(orderRepository, orderMapper, orderSearchRepository);
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.cOrderMapper = cOrderMapper;
        this.orderSearchRepository = orderSearchRepository;
        this.customizedCartItemMapper = customizedCartItemMapper;
        this.productRepository = productRepository;
        this.inventoryService = inventoryService;
        this.customizedCartRepository = customizedCartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.stripeService = stripeService;
        this.orderItemRepository = orderItemRepository;
        this.orderItemMapper = orderItemMapper;
    }

    public OrderResponse checkOut() {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        LOG.debug("Checking out for user: {}", userName);
        User user = userRepository
            .findOneByLogin(userName)
            .orElseThrow(() -> new BadRequestAlertException("User not found", "user", "usernotfound"));

        Cart cart = customizedCartRepository
            .findCartByUserLogin(userName)
            .orElseThrow(() -> new BadRequestAlertException("Cart is empty", "order", "cartempty"));
        if (cart.getCartItems().isEmpty()) {
            throw new BadRequestAlertException("Cart is empty", "order", "cartempty");
        }

        Order order = new Order().user(user);

        Set<Long> productIds = cart.getCartItems().stream().map(cartItem -> cartItem.getProduct().getId()).collect(Collectors.toSet());
        List<Product> products = productRepository.findProductsByIdIn(productIds);
        Map<Long, Product> productMap = products.stream().collect(Collectors.toMap(Product::getId, p -> p));

        for (CartItem cartItem : cart.getCartItems()) {
            Product product = productMap.get(cartItem.getProduct().getId());
            if (product == null) {
                continue;
            }
            if (cartItem.getQuantity() > product.getQuantity()) {
                throw new BadRequestAlertException("Insufficient stock for " + product.getName(), "order", "insufficientstock");
            }
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceAtPurchase(product.getPrice());
            order.addOrderItem(orderItem);
            product.setQuantity(product.getQuantity() - cartItem.getQuantity());
        }

        if (order.getOrderItems().isEmpty()) {
            throw new BadRequestAlertException("No valid items to order", "order", "noitems");
        }

        BigDecimal subTotal = order
            .getOrderItems()
            .stream()
            .map(oi -> oi.getPriceAtPurchase().multiply(BigDecimal.valueOf(oi.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setSubTotal(subTotal);
        order.setTotal(subTotal);
        order.setStatus(OrderStatus.PENDING);

        Order savedOrder = orderRepository.save(order);

        return cOrderMapper.toDto(savedOrder);
    }

    public List<OrderDTO> getOrdersForCurrentUser() {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        LOG.debug("REST request to get all Orders for user: {}", userName);
        return orderRepository.findByUserIsCurrentUser().stream().map(orderMapper::toDto).toList();
    }

    public void cancelOrder(Long orderId) {
        Order order = orderRepository
            .findById(orderId)
            .orElseThrow(() -> new BadRequestAlertException("Order not found", "order", "ordernotfound"));
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        LOG.debug("Order {} cancelled", orderId);
    }

    public void clearCart() {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        Cart cart = customizedCartRepository.getCartWithItem(userName);
        if (cart != null && cart.getCartItems() != null && !cart.getCartItems().isEmpty()) {
            cartItemRepository.deleteAll(cart.getCartItems());
            cart.getCartItems().clear();
        }
    }

    public String payment(Long orderId) {
        Order order = orderRepository
            .findById(orderId)
            .orElseThrow(() -> new BadRequestAlertException("ORDER NOTFOUND", orderId.toString(), "nonorder"));
        return stripeService.createCheckoutSession(order);
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
     * Get all order items for a given order.
     *
     * @param orderId the id of the order.
     * @return the list of order items.
     */
    @Transactional(readOnly = true)
    public List<OrderItemDTO> findOrderItemsByOrderId(Long orderId) {
        LOG.debug("Request to get OrderItems for order : {}", orderId);
        return orderItemRepository.findByOrder_IdWithProduct(orderId).stream().map(orderItemMapper::toDto).toList();
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
