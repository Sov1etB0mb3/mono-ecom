package com.calt.buroxz.web.rest;

import com.calt.buroxz.domain.Cart;
import com.calt.buroxz.repository.CartRepository;
import com.calt.buroxz.service.CartService;
import com.calt.buroxz.service.CustomizedCartService;
import com.calt.buroxz.service.dto.CartDTO;
import com.calt.buroxz.service.dto.CartItemDTO;
import com.calt.buroxz.service.dto.request.CartRequest;
import com.calt.buroxz.service.dto.response.CartResponse;
import com.calt.buroxz.web.rest.errors.BadRequestAlertException;
import com.calt.buroxz.web.rest.errors.ElasticsearchExceptionMapper;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.calt.buroxz.domain.Cart}.
 */
@RestController
@RequestMapping("/api/cart")
public class CustomizedCartResource {

    private static final Logger LOG = LoggerFactory.getLogger(CustomizedCartResource.class);

    private static final String ENTITY_NAME = "cart";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CartService cartService;

    private final CartRepository cartRepository;

    private final CustomizedCartService customizedCartService;

    public CustomizedCartResource(CartService cartService, CartRepository cartRepository, CustomizedCartService customizedCartService) {
        this.cartService = cartService;
        this.cartRepository = cartRepository;
        this.customizedCartService = customizedCartService;
    }

    @PostMapping("")
    public ResponseEntity<CartResponse> addItemToCart(@RequestBody CartItemDTO cartItemDTO) throws URISyntaxException {
        LOG.debug("REST request to save Cart : {}", cartItemDTO);
        CartResponse cartResponse = customizedCartService.addCartItem(cartItemDTO);
        return ResponseEntity.created(new URI("/api/cart/"))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, cartResponse.getUser().getLogin().toString()))
            .body(cartResponse);
    }

    @PutMapping("")
    public ResponseEntity<CartDTO> updateCart(@RequestBody CartRequest cartRequest) throws URISyntaxException {
        //        LOG.debug("REST request to update Cart : {}, {}", id, cartRequest);
        LOG.debug("REST request to update Cart : {}, {}", cartRequest);

        if (cartRequest.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        //        if (!Objects.equals(id, cartRequest.getId())) {
        //            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        //        }
        //
        //        if (!cartRepository.existsById(id)) {
        //            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        //        }

        CartDTO result = customizedCartService.updateCartItemInCart(cartRequest);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    @GetMapping("/{id}/items")
    //    public ResponseEntity<List<CartItemDTO>> getCart(@PathVariable("id") Long id) {
    public ResponseEntity<CartResponse> getCart(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Cart : {}", id);
        //        List<CartItemDTO> cartItems = customizedCartService.findCartItems();
        //        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(cartItems));
        CartResponse cart = customizedCartService.findCartWithItems();
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(cart));
    }
}
