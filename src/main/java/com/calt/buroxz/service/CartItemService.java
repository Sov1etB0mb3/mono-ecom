package com.calt.buroxz.service;

import com.calt.buroxz.domain.CartItem;
import com.calt.buroxz.repository.CartItemRepository;
import com.calt.buroxz.repository.search.CartItemSearchRepository;
import com.calt.buroxz.service.dto.CartItemDTO;
import com.calt.buroxz.service.mapper.CartItemMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.calt.buroxz.domain.CartItem}.
 */
@Service
@Transactional
public class CartItemService {

    private static final Logger LOG = LoggerFactory.getLogger(CartItemService.class);

    private final CartItemRepository cartItemRepository;

    private final CartItemMapper cartItemMapper;

    private final CartItemSearchRepository cartItemSearchRepository;

    public CartItemService(
        CartItemRepository cartItemRepository,
        CartItemMapper cartItemMapper,
        CartItemSearchRepository cartItemSearchRepository
    ) {
        this.cartItemRepository = cartItemRepository;
        this.cartItemMapper = cartItemMapper;
        this.cartItemSearchRepository = cartItemSearchRepository;
    }

    /**
     * Save a cartItem.
     *
     * @param cartItemDTO the entity to save.
     * @return the persisted entity.
     */
    public CartItemDTO save(CartItemDTO cartItemDTO) {
        LOG.debug("Request to save CartItem : {}", cartItemDTO);
        CartItem cartItem = cartItemMapper.toEntity(cartItemDTO);
        cartItem = cartItemRepository.save(cartItem);
        cartItemSearchRepository.index(cartItem);
        return cartItemMapper.toDto(cartItem);
    }

    /**
     * Update a cartItem.
     *
     * @param cartItemDTO the entity to save.
     * @return the persisted entity.
     */
    public CartItemDTO update(CartItemDTO cartItemDTO) {
        LOG.debug("Request to update CartItem : {}", cartItemDTO);
        CartItem cartItem = cartItemMapper.toEntity(cartItemDTO);
        cartItem = cartItemRepository.save(cartItem);
        cartItemSearchRepository.index(cartItem);
        return cartItemMapper.toDto(cartItem);
    }

    /**
     * Partially update a cartItem.
     *
     * @param cartItemDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<CartItemDTO> partialUpdate(CartItemDTO cartItemDTO) {
        LOG.debug("Request to partially update CartItem : {}", cartItemDTO);

        return cartItemRepository
            .findById(cartItemDTO.getId())
            .map(existingCartItem -> {
                cartItemMapper.partialUpdate(existingCartItem, cartItemDTO);

                return existingCartItem;
            })
            .map(cartItemRepository::save)
            .map(savedCartItem -> {
                cartItemSearchRepository.index(savedCartItem);
                return savedCartItem;
            })
            .map(cartItemMapper::toDto);
    }

    /**
     * Get all the cartItems.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<CartItemDTO> findAll() {
        LOG.debug("Request to get all CartItems");
        return cartItemRepository.findAll().stream().map(cartItemMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Get one cartItem by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<CartItemDTO> findOne(Long id) {
        LOG.debug("Request to get CartItem : {}", id);
        return cartItemRepository.findById(id).map(cartItemMapper::toDto);
    }

    /**
     * Delete the cartItem by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete CartItem : {}", id);
        cartItemRepository.deleteById(id);
        cartItemSearchRepository.deleteFromIndexById(id);
    }

    /**
     * Search for the cartItem corresponding to the query.
     *
     * @param query the query of the search.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<CartItemDTO> search(String query) {
        LOG.debug("Request to search CartItems for query {}", query);
        try {
            return StreamSupport.stream(cartItemSearchRepository.search(query).spliterator(), false).map(cartItemMapper::toDto).toList();
        } catch (RuntimeException e) {
            throw e;
        }
    }
}
