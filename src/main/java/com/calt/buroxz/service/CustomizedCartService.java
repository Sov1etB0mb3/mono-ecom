package com.calt.buroxz.service;

import com.calt.buroxz.domain.Cart;
import com.calt.buroxz.domain.CartItem;
import com.calt.buroxz.domain.Product;
import com.calt.buroxz.domain.User;
import com.calt.buroxz.repository.*;
import com.calt.buroxz.repository.search.CartSearchRepository;
import com.calt.buroxz.service.dto.CartDTO;
import com.calt.buroxz.service.dto.CartItemDTO;
import com.calt.buroxz.service.dto.request.CartRequest;
import com.calt.buroxz.service.dto.response.CartResponse;
import com.calt.buroxz.service.mapper.CartItemMapper;
import com.calt.buroxz.service.mapper.CartMapper;
import com.calt.buroxz.service.mapper.CustomizedCartItemMapper;
import com.calt.buroxz.service.mapper.CustomizedCartMapper;
import com.calt.buroxz.web.rest.errors.BadRequestAlertException;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link Cart}.
 */
@Service
@Transactional
@Primary
public class CustomizedCartService extends CartService {

    private static final Logger LOG = LoggerFactory.getLogger(CustomizedCartService.class);
    private final String ENTITY_NAME = "cartItem";
    private final CartRepository cartRepository;

    private final CartMapper cartMapper;

    private final CartItemMapper cartItemMapper;

    private final CartSearchRepository cartSearchRepository;
    private final CustomizedCartItemMapper customizedCartItemMapper;
    private final UserRepository userRepository;
    private final CustomizedCartMapper customizedCartMapper;
    private final CartItemRepository cartItemRepository;
    private final CustomizedCartRepository customizedCartRepository;
    private final ProductRepository productRepository;

    public CustomizedCartService(
        CartRepository cartRepository,
        CartMapper cartMapper,
        CartSearchRepository cartSearchRepository,
        CartItemMapper cartItemMapper,
        UserRepository userRepository,
        CartItemRepository cartItemRepository,
        CustomizedCartRepository customizedCartRepository,
        CustomizedCartMapper customizedCartMapper,
        CustomizedCartItemMapper customizedCartItemMapper,
        ProductRepository productRepository
    ) {
        super(cartRepository, cartMapper, cartSearchRepository);
        this.cartRepository = cartRepository;
        this.cartMapper = cartMapper;
        this.cartItemMapper = cartItemMapper;
        this.cartSearchRepository = cartSearchRepository;
        this.userRepository = userRepository;
        this.cartItemRepository = cartItemRepository;
        this.customizedCartRepository = customizedCartRepository;
        this.customizedCartMapper = customizedCartMapper;
        this.customizedCartItemMapper = customizedCartItemMapper;
        this.productRepository = productRepository;
    }

    //    public CartResponse addCartItem(CartRequest cartRequest){
    //        CartItemDTO item= cartRequest.getCartItems().iterator().next();
    //        if(!validateStock(item)){
    //            throw new BadRequestAlertException("Out of stocks", ENTITY_NAME, "quantityinvalid");
    //        }
    //        item.setPrice(item.getProduct().getPrice()*item.getQuantity());
    //
    //        Cart newCart= customizedCartMapper.toEntity(cartRequest);
    //        CartResponse cartResponse = customizedCartMapper.toDto(cartRepository.save(newCart));
    //        return cartResponse;
    //    }
    public CartResponse addCartItem(CartItemDTO cartItemDTO) {
        LOG.debug("Request to save CartItem : {}", cartItemDTO);
        Product readyProduct = productRepository.findProductById(cartItemDTO.getProduct().getId());
        if (!validateStock(cartItemDTO, readyProduct)) {
            throw new BadRequestAlertException("Out of stocks", ENTITY_NAME, "quantityinvalid");
        }
        CartItem cartItem = cartItemMapper.toEntity(cartItemDTO);
        cartItem.setPrice(readyProduct.getPrice() * cartItem.getQuantity());
        cartItem.setProduct(readyProduct);
        cartItemRepository.save(cartItem);
        Cart cart = cartRepository.findById(cartItemDTO.getCart().getId()).orElseThrow();
        cart.addCartItem(cartItem); // IMPORTANT: This updates the List in memory
        CartResponse cartResponse = customizedCartMapper.toDto(cart);
        double total = calculatePrice(cartResponse);
        cartResponse.setTotalPrice(total);
        //        cartItemSearchRepository.index(cartItem);
        return cartResponse;
    }

    public CartResponse findCartWithItems() {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        Cart cart = customizedCartRepository.getCartWithItem(userName);
        CartResponse cartResponse = customizedCartMapper.toDto(cart);
        double total = calculatePrice(cartResponse);
        cartResponse.setTotalPrice(total);
        return cartResponse;
    }

    public CartDTO updateCartItemInCart(CartRequest cartRequest) {
        if (!Objects.equals(cartRequest.getId(), cartRequest.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!cartRepository.existsById(cartRequest.getId())) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }
        final Cart cart = cartRepository.findCartById(cartRequest.getId());

        cart.getCartItems().clear();
        cartRequest
            .getCartItems()
            .stream()
            .map(cartItemMapper::toEntity)
            .forEach(cartItem -> {
                cartItem.setCart(cart);
                cart.getCartItems().add(cartItem);
            });

        Cart newCart = cartRepository.save(cart);
        cartSearchRepository.index(cart);
        return cartMapper.toDto(cart);
    }

    /**
     * Update a cart.
     *
     * @param cartDTO the entity to save.
     * @return the persisted entity.
     */
    public CartDTO update(CartDTO cartDTO) {
        LOG.debug("Request to update Cart : {}", cartDTO);
        Cart cart = cartMapper.toEntity(cartDTO);
        cart = cartRepository.save(cart);
        cartSearchRepository.index(cart);
        return cartMapper.toDto(cart);
    }

    /**
     * Partially update a cart.
     *
     * @param cartDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<CartDTO> partialUpdate(CartDTO cartDTO) {
        LOG.debug("Request to partially update Cart : {}", cartDTO);

        return cartRepository
            .findById(cartDTO.getId())
            .map(existingCart -> {
                cartMapper.partialUpdate(existingCart, cartDTO);

                return existingCart;
            })
            .map(cartRepository::save)
            .map(savedCart -> {
                cartSearchRepository.index(savedCart);
                return savedCart;
            })
            .map(cartMapper::toDto);
    }

    /**
     * Get all the carts.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<CartDTO> findAll() {
        LOG.debug("Request to get all Carts");
        return cartRepository.findAll().stream().map(cartMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Get all the carts with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<CartDTO> findAllWithEagerRelationships(Pageable pageable) {
        return cartRepository.findAllWithEagerRelationships(pageable).map(cartMapper::toDto);
    }

    /**
     * Get one cart by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<CartDTO> findOne(Long id) {
        LOG.debug("Request to get Cart : {}", id);
        return cartRepository.findOneWithEagerRelationships(id).map(cartMapper::toDto);
    }

    /**
     * Delete the cart by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Cart : {}", id);
        cartRepository.deleteById(id);
        cartSearchRepository.deleteFromIndexById(id);
    }

    /**
     * Search for the cart corresponding to the query.
     *
     * @param query the query of the search.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<CartDTO> search(String query) {
        LOG.debug("Request to search Carts for query {}", query);
        try {
            return StreamSupport.stream(cartSearchRepository.search(query).spliterator(), false).map(cartMapper::toDto).toList();
        } catch (RuntimeException e) {
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public boolean validateStock(CartItemDTO item, Product readyProduct) {
        Integer expectQuantity = item.getQuantity();
        Integer readyQuanity = readyProduct.getQuantity();
        if (expectQuantity > readyQuanity) {
            return false;
        }
        return true;
    }

    public double calculatePrice(CartResponse cartResponse) {
        return cartResponse.getCartItems().stream().mapToDouble(item -> item.getPrice()).sum();
    }
}
