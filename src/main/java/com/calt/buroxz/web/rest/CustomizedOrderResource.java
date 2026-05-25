package com.calt.buroxz.web.rest;

import com.calt.buroxz.service.CustomizedOrderService;
import com.calt.buroxz.service.StripeService;
import com.calt.buroxz.service.dto.OrderDTO;
import com.calt.buroxz.service.dto.response.OrderResponse;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class CustomizedOrderResource {

    private static final Logger LOG = LoggerFactory.getLogger(CustomizedOrderResource.class);

    private final CustomizedOrderService customizedOrderService;
    private final StripeService stripeService;

    public CustomizedOrderResource(CustomizedOrderService customizedOrderService, StripeService stripeService) {
        this.customizedOrderService = customizedOrderService;
        this.stripeService = stripeService;
    }

    @GetMapping("/my-orders")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<OrderDTO>> getMyOrders() {
        LOG.debug("REST request to get current user's orders");
        return ResponseEntity.ok(customizedOrderService.getOrdersForCurrentUser());
    }

    @PostMapping("/checkout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> checkout() {
        LOG.debug("REST request to checkout");
        OrderResponse order = customizedOrderService.checkOut();
        String sessionUrl = customizedOrderService.payment(order.getId());
        customizedOrderService.clearCart();
        return ResponseEntity.ok(Map.of("sessionUrl", sessionUrl, "orderId", order.getId()));
    }

    @PostMapping("/pay/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> payOrder(@PathVariable Long id) {
        LOG.debug("REST request to pay order: {}", id);
        String sessionUrl = customizedOrderService.payment(id);
        return ResponseEntity.ok(Map.of("sessionUrl", sessionUrl));
    }

    @PostMapping("/cancel/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        LOG.debug("REST request to cancel order: {}", id);
        customizedOrderService.cancelOrder(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        LOG.debug("REST request to handle Stripe webhook");
        String result = stripeService.handleWebhookEvent(payload, sigHeader);
        return ResponseEntity.ok(result);
    }
}
