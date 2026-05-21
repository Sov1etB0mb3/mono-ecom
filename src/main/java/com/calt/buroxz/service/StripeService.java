package com.calt.buroxz.service;

import com.calt.buroxz.config.StripeConfig;
import com.calt.buroxz.domain.Order;
import com.calt.buroxz.domain.OrderItem;
import com.calt.buroxz.domain.enumeration.OrderStatus;
import com.calt.buroxz.repository.OrderRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StripeService {

    private static final Logger LOG = LoggerFactory.getLogger(StripeService.class);

    private final StripeConfig stripeConfig;
    private final OrderRepository orderRepository;

    @Value("${stripe.success-url}")
    private String successUrl;

    @Value("${stripe.cancel-url}")
    private String cancelUrl;

    public StripeService(StripeConfig stripeConfig, OrderRepository orderRepository) {
        this.stripeConfig = stripeConfig;
        this.orderRepository = orderRepository;
    }

    public String createCheckoutSession(Order order) {
        SessionCreateParams.LineItem[] lineItems = order
            .getOrderItems()
            .stream()
            .map(this::toLineItem)
            .toArray(SessionCreateParams.LineItem[]::new);

        SessionCreateParams params = SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.PAYMENT)
            .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
            .setCancelUrl(cancelUrl)
            .setClientReferenceId(order.getId().toString())
            .setCustomerEmail(order.getUser() != null ? order.getUser().getEmail() : null)
            .addAllLineItem(java.util.Arrays.asList(lineItems))
            .putMetadata("order_id", order.getId().toString())
            .build();

        try {
            Session session = Session.create(params);
            LOG.info("Created Stripe Checkout Session: {} for order {}", session.getId(), order.getId());
            return session.getUrl();
        } catch (StripeException e) {
            LOG.error("Failed to create Stripe Checkout Session for order {}", order.getId(), e);
            throw new RuntimeException("Failed to create payment session", e);
        }
    }

    private SessionCreateParams.LineItem toLineItem(OrderItem orderItem) {
        BigDecimal unitAmount = orderItem.getPriceAtPurchase().multiply(BigDecimal.valueOf(100));
        return SessionCreateParams.LineItem.builder()
            .setQuantity((long) orderItem.getQuantity())
            .setPriceData(
                SessionCreateParams.LineItem.PriceData.builder()
                    .setCurrency("usd")
                    .setUnitAmountDecimal(unitAmount)
                    .setProductData(
                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                            .setName(orderItem.getProduct() != null ? orderItem.getProduct().getName() : "Product")
                            .build()
                    )
                    .build()
            )
            .build();
    }

    @Transactional
    public String handleWebhookEvent(String payload, String sigHeader) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, stripeConfig.getWebhookSecret());
        } catch (Exception e) {
            LOG.error("Webhook signature verification failed", e);
            throw new RuntimeException("Webhook signature verification failed", e);
        }

        if ("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
            if (session == null) {
                LOG.warn("Webhook event missing session object");
                return "missing session";
            }

            String orderIdStr = session.getMetadata().get("order_id");
            if (orderIdStr == null) {
                orderIdStr = session.getClientReferenceId();
            }

            if (orderIdStr != null) {
                final String finalOrderId = orderIdStr;
                orderRepository
                    .findById(Long.parseLong(finalOrderId))
                    .ifPresent(order -> {
                        order.setStatus(OrderStatus.PAID);
                        orderRepository.save(order);
                        LOG.info("Order {} marked as PAID via Stripe webhook", finalOrderId);
                    });
            }
        }

        return "ok";
    }
}
