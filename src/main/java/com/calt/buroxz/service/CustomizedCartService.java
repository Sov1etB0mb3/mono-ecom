package com.calt.buroxz.service;

import com.calt.buroxz.domain.Cart;
import com.calt.buroxz.domain.CartItem;
import com.calt.buroxz.domain.Product;
import com.calt.buroxz.domain.User;
import com.calt.buroxz.repository.*;
import com.calt.buroxz.repository.search.CartSearchRepository;
import com.calt.buroxz.service.dto.*;
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
    private final String SUPER_ENTITY_NAME = "cart";
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
    public CartResponse addCartItem(CustomizedCartItemDTO cartItemDTO) {
        LOG.debug("Request to save CartItem : {}", cartItemDTO);
        Product readyProduct = productRepository.findProductById(cartItemDTO.getProduct().getId());
        if (!validateStock(cartItemDTO, readyProduct)) {
            throw new BadRequestAlertException("Out of stocks", cartItemDTO.getProduct().getName(), "qtyinvalid");
        }
        CartItem cartItem = customizedCartItemMapper.toEntity(cartItemDTO);
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

    @Transactional(readOnly = true)
    public CartResponse findCartWithItems() {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        Cart cart = customizedCartRepository.getCartWithItem(userName);
        CartResponse cartResponse = customizedCartMapper.toDto(cart);
        double total = calculatePrice(cartResponse);
        cartResponse.setTotalPrice(total);
        return cartResponse;
    }

    public CartResponse updateCartItemInCart(CartRequest cartRequest) {
        if (!cartRepository.existsById(cartRequest.getId())) {
            throw new BadRequestAlertException("Entity not found", SUPER_ENTITY_NAME, "idnotfound");
        }
        Cart cart = cartRepository.findCartById(cartRequest.getId());
        CartResponse response;
        cartRequest
            .getCartItems()
            .stream()
            .forEach(cartItemDTO -> {
                CustomizedProductDTO expectProduct = cartItemDTO.getProduct();
                //                Product readyProduct = productRepository.findProductById(expectProduct.getId());
                //                if(!validateStock(cartItemDTO,readyProduct)){
                ////                    cart.removeCartItem(cartItemRepository.findCartItemById(cartItemDTO.getId()));
                //                    cartItemDTO.setAvailable(false);
                ////                    throw new BadRequestAlertException("Out of stock", cartItemDTO.getProduct().getName(), "qtyinvalid");
                //                }
                //                else
                if (cartItemRepository.existsById(cartItemDTO.getId())) {
                    updateCartItemQuantity(cartItemDTO.getId(), cartItemDTO.getQuantity());
                }
            });
        Cart newCart = cartRepository.save(cart);
        cartSearchRepository.index(newCart);
        CartResponse cartResponse = customizedCartMapper.toDto(newCart);
        cartResponse.setTotalPrice(calculatePrice(cartResponse));
        //        cartResponse.getCartItems()
        //            .stream()
        //            .forEach(cartItem ->{
        //                    CustomizedProductDTO expectProduct= cartItem.getProduct();
        //                    Product readyProduct = productRepository.findProductById(expectProduct.getId());
        //                    if(!validateStock(cartItem,readyProduct)){
        //                        cartItem.setAvailable(false);
        //                    }
        //                }
        //                );
        return cartResponse;
    }

    public CartResponse updateCartItemQuantity(Long cartItemId, Integer newQuantity) {
        CartItem item = cartItemRepository
            .findById(cartItemId)
            .orElseThrow(() -> new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
        // 1. If quantity is 0, just delete it
        if (newQuantity <= 0) {
            return removeCartItem(cartItemId);
        }
        // 2. Check stock for the new specific number
        //        if (newQuantity > item.getProduct().getQuantity()) {
        ////            throw new BadRequestAlertException("Out of stock", item.getProduct().getName(), "qtyinvalid");
        //        }
        item.setQuantity(newQuantity);
        item.setPrice(item.getProduct().getPrice() * newQuantity);
        cartItemRepository.save(item);
        return findCartWithItems();
    }

    public CartResponse removeCartItem(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findCartItemById(cartItemId);
        Cart cart = cartItem.getCart();
        cart.removeCartItem(cartItem);
        cartItemRepository.delete(cartItem);
        CartResponse cartResponse = customizedCartMapper.toDto(cart);
        cartResponse.setTotalPrice(calculatePrice(cartResponse));
        return cartResponse;
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
    public boolean validateStock(CustomizedCartItemDTO item, Product readyProduct) {
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
