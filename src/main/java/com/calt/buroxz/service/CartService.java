package com.calt.buroxz.service;

import com.calt.buroxz.domain.Cart;
import com.calt.buroxz.repository.CartRepository;
import com.calt.buroxz.repository.search.CartSearchRepository;
import com.calt.buroxz.service.dto.CartDTO;
import com.calt.buroxz.service.mapper.CartMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.calt.buroxz.domain.Cart}.
 */
@Service
@Transactional
public class CartService {

    private static final Logger LOG = LoggerFactory.getLogger(CartService.class);

    private final CartRepository cartRepository;

    private final CartMapper cartMapper;

    private final CartSearchRepository cartSearchRepository;

    public CartService(CartRepository cartRepository, CartMapper cartMapper, CartSearchRepository cartSearchRepository) {
        this.cartRepository = cartRepository;
        this.cartMapper = cartMapper;
        this.cartSearchRepository = cartSearchRepository;
    }

    /**
     * Save a cart.
     *
     * @param cartDTO the entity to save.
     * @return the persisted entity.
     */
    public CartDTO save(CartDTO cartDTO) {
        LOG.debug("Request to save Cart : {}", cartDTO);
        Cart cart = cartMapper.toEntity(cartDTO);
        cart = cartRepository.save(cart);
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
}
