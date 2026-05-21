# Order & Payment (Stripe) Implementation

## Overview

This document explains the complete order/checkout/payment flow implemented for MrX SHop using Stripe Checkout Sessions. The flow is:

```
Cart → Checkout → Order created → Stripe Checkout Session → Redirect → Stripe hosted payment → Redirect back → Webhook updates order status
```

---

## Backend Files

### 1. Stripe Configuration

**`src/main/java/com/calt/buroxz/config/StripeConfig.java`**

Reads `stripe.api-key` from `application.yml` and initializes the Stripe SDK on startup:

```java
@Configuration
public class StripeConfig {
    @Value("${stripe.api-key}")
    private String apiKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = apiKey;
    }

    public String getWebhookSecret() { ... }
}
```

### 2. Stripe Service

**`src/main/java/com/calt/buroxz/service/StripeService.java`**

Handles two operations:

**`createCheckoutSession(Order order)`** — Creates a Stripe Checkout Session for payment:

- Converts each `OrderItem` into a Stripe line item with product name, quantity, and unit amount (in USD cents)
- Sets success URL (`/payment/success?session_id={CHECKOUT_SESSION_ID}`) and cancel URL (`/payment/cancel`)
- Stores the order ID in metadata for webhook processing
- Returns the redirect URL to the frontend

**`handleWebhookEvent(payload, sigHeader)`** — Verifies and processes Stripe webhooks:

- Verifies the webhook signature using the `whsec_...` secret
- On `checkout.session.completed` event, looks up the order via metadata `order_id`
- Updates the order status from `PENDING` to `PAID`

### 3. Fixed Order Service

**`src/main/java/com/calt/buroxz/service/CustomizedOrderService.java`**

**`checkOut()`** — Creates an Order from the current user's cart:

- Fetches the authenticated user
- Fetches the user's Cart (with L2 cache bypass to avoid stale data)
- Validates stock for each item (throws if insufficient)
- Creates `OrderItem` for each `CartItem`, deducts stock from Product
- Calculates `subTotal` and `total`
- Saves the Order (with `cascade = CascadeType.ALL` so OrderItems are saved automatically)
- Does NOT clear the cart (clearing happens after Stripe session is created)

**`clearCart()`** — Deletes all CartItems for the current user

**`payment(orderId)`** — Creates a Stripe Checkout Session for the given order

### 4. Order REST Controller

**`src/main/java/com/calt/buroxz/web/rest/CustomizedOrderResource.java`**

| Endpoint               | Method | Auth     | Purpose                                                                        |
| ---------------------- | ------ | -------- | ------------------------------------------------------------------------------ |
| `/api/orders/checkout` | POST   | Required | Creates order + Stripe session, clears cart, returns `{ sessionUrl, orderId }` |
| `/api/orders/webhook`  | POST   | None     | Receives Stripe webhook events (signature verified)                            |

### 5. Security Exceptions

**`src/main/java/com/calt/buroxz/config/SecurityConfiguration.java`**

- `/api/orders/webhook` is excluded from authentication (`.permitAll()`)
- `/api/orders/webhook` is excluded from CSRF protection (`.ignoringRequestMatchers(...)`)

**`src/main/java/com/calt/buroxz/security/authorization/ScopeAspect.java`**

- `CustomizedOrderService` and `StripeService` are excluded from the scope-checking aspect pointcut

### 6. Domain Changes

**`src/main/java/com/calt/buroxz/domain/Order.java`**

Added `cascade = CascadeType.ALL, orphanRemoval = true` to the `@OneToMany` on `orderItems` so that OrderItems are persisted/deleted along with the Order.

### 7. Repository Changes

**`src/main/java/com/calt/buroxz/repository/CustomizedCartRepositoryImpl.java`**

Added `findCartByUserLogin(userName)` method with L2 cache bypass hints to avoid stale cart data from Hibernate second-level cache.

**`src/main/java/com/calt/buroxz/repository/CustomizedCartRepository.java`**

Added `findCartByUserLogin(String userName)` method declaration.

### 8. application.yml

Added Stripe configuration:

```yaml
stripe:
  api-key: ${STRIPE_API_KEY:sk_test_placeholder}
  webhook-secret: ${STRIPE_WEBHOOK_SECRET:whsec_placeholder}
  success-url: 'http://localhost:8080/payment/success'
  cancel-url: 'http://localhost:8080/payment/cancel'
```

---

## Frontend Files

### 1. Types

**`src/main/webapp/app/order/order.model.ts`**

```typescript
export interface ICheckoutResponse {
  sessionUrl: string;
  orderId: number;
}
```

### 2. Order Service

**`src/main/webapp/app/order/order.service.ts`**

Single method `checkout()` that posts to `POST /api/orders/checkout` and returns the session URL.

### 3. Checkout Component

**`src/main/webapp/app/order/checkout/checkout.component.ts`**

When mounted, immediately calls the checkout API and redirects the browser to the Stripe Checkout Session URL. Shows a loading spinner while redirecting.

### 4. Payment Success Page

**`src/main/webapp/app/order/success/success.component.ts`**

Displays a success message with the Stripe session ID from the query parameter. Has a "Back to Home" button.

Route: `/payment/success?session_id=cs_xxx`

### 5. Payment Cancel Page

**`src/main/webapp/app/order/cancel/cancel.component.ts`**

Shows a cancellation message with a "Back to Cart" link.

Route: `/payment/cancel`

### 6. Order Routes

**`src/main/webapp/app/order/order.routes.ts`**

| Route               | Component               |
| ------------------- | ----------------------- |
| `/payment/checkout` | CheckoutComponent       |
| `/payment/success`  | PaymentSuccessComponent |
| `/payment/cancel`   | PaymentCancelComponent  |

### 7. Cart Page Changes

**`src/main/webapp/app/cart/cart.component.ts`**

Added `checkout()` method that calls `OrderService.checkout()` and redirects to the returned Stripe URL on success.

**`src/main/webapp/app/cart/cart.component.html`**

Added a "Proceed to Checkout" button in the cart table footer (visible when cart has items).

### 8. App Routes

**`src/main/webapp/app/app.routes.ts`**

Added `loadChildren: () => import('./order/order.routes')` to include payment routes.

### 9. Font Awesome Icons

**`src/main/webapp/app/config/font-awesome-icons.ts`**

Added `faCheckCircle`, `faTimesCircle`, `faCreditCard`.

### 10. i18n

**`src/main/webapp/i18n/en/cart.json`** — `checkout: "Proceed to Checkout"`

**`src/main/webapp/i18n/vi/cart.json`** — `checkout: "Thanh toán"`

**`src/main/webapp/i18n/en/order.json`** — Added `checkout`, `payment.success`, `payment.cancel` sections

**`src/main/webapp/i18n/vi/order.json`** — Vietnamese translations for checkout/payment

---

## Stripe Setup

### Prerequisites

1. Create a Stripe account at https://dashboard.stripe.com/register
2. Get your **Secret key** (`sk_test_...`) from https://dashboard.stripe.com/apikeys

### Local Development

Install Stripe CLI (for webhook forwarding):

```bash
# Download from https://stripe.com/docs/stripe-cli
stripe login
stripe listen --forward-to localhost:8080/api/orders/webhook
```

This prints a webhook signing secret (`whsec_...`). Set environment variables:

```bash
$env:STRIPE_API_KEY = "sk_test_..."
$env:STRIPE_WEBHOOK_SECRET = "whsec_..."
```

Then restart the application.

### Running with Docker

Add to docker-compose environment or pass as JVM args:

```yaml
environment:
  - STRIPE_API_KEY=sk_test_...
  - STRIPE_WEBHOOK_SECRET=whsec_...
```

---

## Complete Flow (End to End)

1. User browses products, adds items to cart
2. User opens cart page (`/cart-page`) — sees items with quantities and total
3. User clicks **"Proceed to Checkout"**
4. Frontend calls `POST /api/orders/checkout` (loading spinner shown)
5. Backend:
   - Fetches user's cart from database (bypassing Hibernate L2 cache)
   - Validates stock for each item
   - Creates `Order` + `OrderItem` entities (status = `PENDING`)
   - Deducts stock from `Product.quantity`
   - Creates Stripe Checkout Session with line items
   - Clears the cart
   - Returns `{ sessionUrl: "https://checkout.stripe.com/..." }`
6. Browser redirects to Stripe Checkout
7. User pays on Stripe's hosted page (credit card, etc.)
8. On success, Stripe redirects to `http://localhost:8080/payment/success?session_id=cs_xxx`
9. Stripe also sends a webhook to `POST /api/orders/webhook`
10. Backend webhook handler verifies signature, updates order status to `PAID`
11. Success page shows "Payment Successful!" message

---

## Troubleshooting

### "Cart is empty" when clicking checkout

- Ensure the application is fully rebuilt and restarted after code changes
- Check server logs for the authenticated username (enable DEBUG logging)
- Verify the cart exists in the database via SQL query
- Check if Hibernate L2 cache is returning stale data (the implementation uses `BYPASS` mode)

### Stripe "Invalid email address" error

Fixed by using `order.getUser().getEmail()` instead of `order.getUser().getLogin()`.

### "Forbidden: order:unknown" error

Fixed by excluding `CustomizedOrderService` and `StripeService` from the `ScopeAspect` pointcut.

### Frontend build errors

- Add any new Font Awesome icons to `font-awesome-icons.ts`
- Ensure new routes are registered in `app.routes.ts`
