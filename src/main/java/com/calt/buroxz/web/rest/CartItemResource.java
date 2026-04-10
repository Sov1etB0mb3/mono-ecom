package com.calt.buroxz.web.rest;

import com.calt.buroxz.repository.CartItemRepository;
import com.calt.buroxz.service.CartItemService;
import com.calt.buroxz.service.dto.CartItemDTO;
import com.calt.buroxz.web.rest.errors.BadRequestAlertException;
import com.calt.buroxz.web.rest.errors.ElasticsearchExceptionMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.calt.buroxz.domain.CartItem}.
 */
@RestController
@RequestMapping("/api/cart-items")
public class CartItemResource {

    private static final Logger LOG = LoggerFactory.getLogger(CartItemResource.class);

    private static final String ENTITY_NAME = "cartItem";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CartItemService cartItemService;

    private final CartItemRepository cartItemRepository;

    public CartItemResource(CartItemService cartItemService, CartItemRepository cartItemRepository) {
        this.cartItemService = cartItemService;
        this.cartItemRepository = cartItemRepository;
    }

    /**
     * {@code POST  /cart-items} : Create a new cartItem.
     *
     * @param cartItemDTO the cartItemDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new cartItemDTO, or with status {@code 400 (Bad Request)} if the cartItem has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<CartItemDTO> createCartItem(@Valid @RequestBody CartItemDTO cartItemDTO) throws URISyntaxException {
        LOG.debug("REST request to save CartItem : {}", cartItemDTO);
        if (cartItemDTO.getId() != null) {
            throw new BadRequestAlertException("A new cartItem cannot already have an ID", ENTITY_NAME, "idexists");
        }
        cartItemDTO = cartItemService.save(cartItemDTO);
        return ResponseEntity.created(new URI("/api/cart-items/" + cartItemDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, cartItemDTO.getId().toString()))
            .body(cartItemDTO);
    }

    /**
     * {@code PUT  /cart-items/:id} : Updates an existing cartItem.
     *
     * @param id the id of the cartItemDTO to save.
     * @param cartItemDTO the cartItemDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated cartItemDTO,
     * or with status {@code 400 (Bad Request)} if the cartItemDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the cartItemDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<CartItemDTO> updateCartItem(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody CartItemDTO cartItemDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update CartItem : {}, {}", id, cartItemDTO);
        if (cartItemDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, cartItemDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!cartItemRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        cartItemDTO = cartItemService.update(cartItemDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, cartItemDTO.getId().toString()))
            .body(cartItemDTO);
    }

    /**
     * {@code PATCH  /cart-items/:id} : Partial updates given fields of an existing cartItem, field will ignore if it is null
     *
     * @param id the id of the cartItemDTO to save.
     * @param cartItemDTO the cartItemDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated cartItemDTO,
     * or with status {@code 400 (Bad Request)} if the cartItemDTO is not valid,
     * or with status {@code 404 (Not Found)} if the cartItemDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the cartItemDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<CartItemDTO> partialUpdateCartItem(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody CartItemDTO cartItemDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update CartItem partially : {}, {}", id, cartItemDTO);
        if (cartItemDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, cartItemDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!cartItemRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<CartItemDTO> result = cartItemService.partialUpdate(cartItemDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, cartItemDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /cart-items} : get all the cartItems.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of cartItems in body.
     */
    @GetMapping("")
    public List<CartItemDTO> getAllCartItems() {
        LOG.debug("REST request to get all CartItems");
        return cartItemService.findAll();
    }

    /**
     * {@code GET  /cart-items/:id} : get the "id" cartItem.
     *
     * @param id the id of the cartItemDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the cartItemDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CartItemDTO> getCartItem(@PathVariable("id") Long id) {
        LOG.debug("REST request to get CartItem : {}", id);
        Optional<CartItemDTO> cartItemDTO = cartItemService.findOne(id);
        return ResponseUtil.wrapOrNotFound(cartItemDTO);
    }

    /**
     * {@code DELETE  /cart-items/:id} : delete the "id" cartItem.
     *
     * @param id the id of the cartItemDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCartItem(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete CartItem : {}", id);
        cartItemService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /cart-items/_search?query=:query} : search for the cartItem corresponding
     * to the query.
     *
     * @param query the query of the cartItem search.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public List<CartItemDTO> searchCartItems(@RequestParam("query") String query) {
        LOG.debug("REST request to search CartItems for query {}", query);
        try {
            return cartItemService.search(query);
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }
}
