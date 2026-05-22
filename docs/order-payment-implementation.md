# Order / Payment / Checkout Implementation

## Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│                    Frontend (Angular)                     │
│                                                         │
│  Cart Page ──> Checkout ──> Stripe Checkout Session     │
│  (checkout)    Component      (Redirect)                │
│                   │                                      │
│                   │ POST /api/orders/checkout            │
│                   ▼                                      │
│  Success/Cancel Pages  <── Stripe redirects back         │
│                                                         │
│  My Orders Page ──> GET /api/orders/my-orders            │
└──────────────────────────┬──────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────┐
│                    Backend (Spring Boot)                  │
│                                                         │
│  CustomizedOrderResource                                 │
│    │  POST /checkout  ──> CustomizedOrderService         │
│    │                        .checkOut() ──> Create Order │
│    │                        .payment()  ──> Stripe       │
│    │                        .clearCart()                 │
│    │                                                     │
│    │  POST /webhook  ──> StripeService                   │
│    │                       .handleWebhookEvent()         │
│    │                       (verifies sig, marks PAID)    │
│    │                                                     │
│    │  GET /my-orders ──> CustomizedOrderService          │
│    │                       .getOrdersForCurrentUser()    │
│    │                                                     │
│  StripeService                                           │
│    .createCheckoutSession() ──> Stripe API (SDK)         │
│    .handleWebhookEvent()   <── Stripe Webhook            │
└─────────────────────────────────────────────────────────┘
```

## Backend Components

### 1. Domain Entity: `Order.java`

**File:** `src/main/java/com/calt/buroxz/domain/Order.java`

The `Order` entity maps to the `jhi_order` table. Key fields:

```java
@Entity
@Table(name = "jhi_order")
public class Order extends AbstractAuditingEntity<Long> implements Serializable, Persistable<Long> {

    @Id @GeneratedValue(...)
    private Long id;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;              // PENDING, PAID, CANCELLED, REFUNDED

    @Column(precision = 21, scale = 2)
    private BigDecimal subTotal;             // Sum of item line totals at time of order

    @Column(precision = 21, scale = 2)
    private BigDecimal total;                // Same as subTotal (no tax/shipping yet)

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderItem> orderItems = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;                       // The customer who placed the order
}
```

**Key design decisions:**

- `cascade = CascadeType.ALL, orphanRemoval = true` on `orderItems` ensures when an `Order` is saved, all its `OrderItem` children are automatically persisted/removed.
- `addOrderItem(OrderItem)` is the bidirectional helper (sets `orderItem.setOrder(this)`).
- Status transitions: `PENDING` (initial) → `PAID` (webhook) | `CANCELLED` (cancel).

### 2. Configuration: `StripeConfig.java`

**File:** `src/main/java/com/calt/buroxz/config/StripeConfig.java`

```java
@Configuration
public class StripeConfig {

  @Value("${stripe.api-key}")
  private String apiKey;

  @Value("${stripe.webhook-secret}")
  private String webhookSecret;

  @PostConstruct
  public void init() {
    Stripe.apiKey = apiKey; // Initialize Stripe SDK globally
  }
}

```

- Reads API key and webhook secret from `application.yml`.
- `@PostConstruct init()` sets the static `Stripe.apiKey` once at startup.

### 3. Application YAML Configuration

**File:** `src/main/resources/config/application.yml`

```yaml
stripe:
  api-key: ${STRIPE_API_KEY:sk_test_...}
  webhook-secret: ${STRIPE_WEBHOOK_SECRET:whsec_...}
  success-url: 'http://localhost:8080/payment/success'
  cancel-url: 'http://localhost:8080/payment/cancel'
```

- Values fall back to test keys (for development), override with env vars `STRIPE_API_KEY` / `STRIPE_WEBHOOK_SECRET` in production.

### 4. Service: `StripeService.java`

**File:** `src/main/java/com/calt/buroxz/service/StripeService.java`

Two main responsibilities:

#### `createCheckoutSession(Order)`

```java
public String createCheckoutSession(Order order) {
  // 1. Convert OrderItems to Stripe LineItems
  SessionCreateParams.LineItem[] lineItems = order
    .getOrderItems()
    .stream()
    .map(this::toLineItem)
    .toArray(SessionCreateParams.LineItem[]::new);

  // 2. Build session params
  SessionCreateParams params = SessionCreateParams.builder()
    .setMode(SessionCreateParams.Mode.PAYMENT)
    .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
    .setCancelUrl(cancelUrl)
    .setClientReferenceId(order.getId().toString())
    .setCustomerEmail(order.getUser().getEmail())
    .addAllLineItem(Arrays.asList(lineItems))
    .putMetadata("order_id", order.getId().toString())
    .build();

  // 3. Create session via Stripe API
  Session session = Session.create(params);
  return session.getUrl(); // Stripe Checkout URL to redirect user to
}

```

- `toLineItem(OrderItem)`: Converts each item to a Stripe line item with `unit_amount_decimal` (price × 100 for cents), quantity, and product name.
- `setClientReferenceId` and `putMetadata("order_id", ...)`: Both carry the internal order ID so the webhook can find it.
- Returns the Stripe Checkout Session URL (user gets redirected there).

#### `handleWebhookEvent(payload, sigHeader)`

```java
public String handleWebhookEvent(String payload, String sigHeader) {
  // 1. Verify webhook signature
  Event event = Webhook.constructEvent(payload, sigHeader, stripeConfig.getWebhookSecret());

  // 2. Handle checkout.session.completed
  if ("checkout.session.completed".equals(event.getType())) {
    Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
    String orderIdStr = session.getMetadata().get("order_id");
    // Fallback to client_reference_id
    if (orderIdStr == null) orderIdStr = session.getClientReferenceId();

    // 3. Mark order as PAID
    orderRepository
      .findById(Long.parseLong(orderIdStr))
      .ifPresent(order -> {
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);
      });
  }
  return "ok";
}

```

- Stripe signs every webhook with the webhook secret. `Webhook.constructEvent()` verifies the signature and throws if invalid.
- On `checkout.session.completed`, looks up the order by `order_id` metadata (fallback: `client_reference_id`) and sets status to `PAID`.

### 5. Service: `CustomizedOrderService.java`

**File:** `src/main/java/com/calt/buroxz/service/CustomizedOrderService.java`

Extends JHipster's generated `OrderService` with `@Primary` so it replaces the default bean.

#### `checkOut()` — The Order Creation Flow

```java
public OrderResponse checkOut() {
    // 1. Identify user
    String userName = SecurityContextHolder.getContext().getAuthentication().getName();
    User user = userRepository.findOneByLogin(userName)
        .orElseThrow(() -> new BadRequestAlertException("User not found", ...));

    // 2. Fetch the user's cart (bypass Hibernate L2 cache)
    Cart cart = customizedCartRepository.findCartByUserLogin(userName)
        .orElseThrow(() -> new BadRequestAlertException("Cart is empty", ...));
    if (cart.getCartItems().isEmpty()) throw ...;

    Order order = new Order().user(user);

    // 3. Validate stock for each item
    Set<Long> productIds = cart.getCartItems().stream()
        .map(ci -> ci.getProduct().getId()).collect(Collectors.toSet());
    Map<Long, Product> productMap = productRepository.findProductsByIdIn(productIds)
        .stream().collect(Collectors.toMap(Product::getId, p -> p));

    for (CartItem cartItem : cart.getCartItems()) {
        Product product = productMap.get(cartItem.getProduct().getId());
        if (cartItem.getQuantity() > product.getQuantity())
            throw new BadRequestAlertException("Insufficient stock for " + product.getName(), ...);
        // Create OrderItem and deplete stock
        OrderItem oi = new OrderItem();
        oi.setProduct(product);
        oi.setQuantity(cartItem.getQuantity());
        oi.setPriceAtPurchase(product.getPrice());
        order.addOrderItem(oi);
        product.setQuantity(product.getQuantity() - cartItem.getQuantity());
    }

    // 4. Calculate totals
    BigDecimal subTotal = order.getOrderItems().stream()
        .map(oi -> oi.getPriceAtPurchase().multiply(BigDecimal.valueOf(oi.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    order.setSubTotal(subTotal);
    order.setTotal(subTotal);
    order.setStatus(OrderStatus.PENDING);

    // 5. Save (cascade persists OrderItems)
    Order savedOrder = orderRepository.save(order);
    return cOrderMapper.toDto(savedOrder);
}
```

**Key design decisions:**

- **Cart is NOT cleared here** — clearing happens in the REST controller _after_ Stripe session creation, so if Stripe fails, the cart remains intact.
- Stock is validated AND decremented atomically in the same transaction.
- `priceAtPurchase` captures the current price, protecting against price changes after order placement.
- Cart queries use `org.hibernate.cacheMode=IGNORE` to bypass Hibernate L2 cache staleness.

#### `clearCart()` — Called After Stripe Session Creation

```java
public void clearCart() {
    String userName = ...;
    Cart cart = customizedCartRepository.getCartWithItem(userName);
    if (cart != null && cart.getCartItems() != null && !cart.getCartItems().isEmpty()) {
        cartItemRepository.deleteAll(cart.getCartItems());
        cart.getCartItems().clear();
    }
}
```

#### `getOrdersForCurrentUser()` — My Orders

```java
public List<OrderDTO> getOrdersForCurrentUser() {
    String userName = ...;
    return orderRepository.findByUserIsCurrentUser().stream()
        .map(orderMapper::toDto).toList();
}
```

Uses JHipster's built-in `OrderRepository.findByUserIsCurrentUser()` which uses SpEL `?#{authentication.name}` to filter by the current user automatically.

### 6. REST Controller: `CustomizedOrderResource.java`

**File:** `src/main/java/com/calt/buroxz/web/rest/CustomizedOrderResource.java`

Three endpoints under `/api/orders`:

| Method | Path         | Auth     | Description                                          |
| ------ | ------------ | -------- | ---------------------------------------------------- |
| `POST` | `/checkout`  | Required | Creates order, generates Stripe session, clears cart |
| `POST` | `/webhook`   | Public   | Stripe webhook — updates order to PAID               |
| `GET`  | `/my-orders` | Required | Returns current user's orders                        |

#### `POST /checkout` — Full Checkout Flow

```java
@PostMapping("/checkout")
public ResponseEntity<Map<String, Object>> checkout() {
  // 1. Create order from cart (stock validation, price capture)
  OrderResponse order = customizedOrderService.checkOut();
  // 2. Create Stripe Checkout Session, get redirect URL
  String sessionUrl = customizedOrderService.payment(order.getId());
  // 3. Clear the cart (only after Stripe succeeds)
  customizedOrderService.clearCart();
  return ResponseEntity.ok(Map.of("sessionUrl", sessionUrl, "orderId", order.getId()));
}

```

Order of operations is critical: `checkOut()` → `payment()` → `clearCart()`. If `payment()` throws (Stripe error), the order is rolled back and cart is untouched.

#### `POST /webhook` — Stripe Event Handler

```java
@PostMapping("/webhook")
public ResponseEntity<String> handleWebhook(
    @RequestBody String payload,
    @RequestHeader("Stripe-Signature") String sigHeader
) { ... }
```

- Excluded from authentication (`.permitAll()`) and CSRF protection (`.ignoringRequestMatchers`).
- Raw payload + signature header passed directly to Stripe SDK for verification.

### 7. Security Configuration

**File:** `src/main/java/com/calt/buroxz/config/SecurityConfiguration.java`

Key security rules relevant to orders/payments:

```java
// Webhook — no auth, no CSRF
.requestMatchers(mvc.pattern("/api/orders/webhook")).permitAll()

// Products & Categories — public GET for anonymous browsing
.requestMatchers(new AntPathRequestMatcher("/api/products", HttpMethod.GET.name())).permitAll()
.requestMatchers(new AntPathRequestMatcher("/api/products/**", HttpMethod.GET.name())).permitAll()
.requestMatchers(new AntPathRequestMatcher("/api/categories/**", HttpMethod.GET.name())).permitAll()

// Everything else — authenticated
.requestMatchers(mvc.pattern("/api/**")).authenticated()
```

CSRF also exempts webhook:

```java
.csrf(csrf -> csrf
    .ignoringRequestMatchers(mvc.pattern("/api/orders/webhook"))
)
```

### 8. ScopeAspect Exclusions

**File:** `src/main/java/com/calt/buroxz/security/authorization/ScopeAspect.java`

The custom `ScopeAspect` intercepts all `com.calt.buroxz.service.*Service` calls. To prevent "User not found" errors for anonymous users and avoid scope checks on checkout/cart/payment flows:

```java
@Before(
    "execution(* com.calt.buroxz.service.*Service.*(..))" +
    "&& !target(com.calt.buroxz.service.UserService)" +
    "&& !target(com.calt.buroxz.service.CustomizedCartService)" +
    "&& !target(com.calt.buroxz.service.CustomizedOrderService)" +
    "&& !target(com.calt.buroxz.service.StripeService)" +
    "&& !target(com.calt.buroxz.service.InventoryService)" +
    "&& !target(com.calt.buroxz.service.ProductService)"
)
```

### 9. Cart Services (Supporting Role)

**File:** `src/main/java/com/calt/buroxz/repository/CustomizedCartRepositoryImpl.java`

Both `getCartWithItem()` and `findCartByUserLogin()` use JPQL with `LEFT JOIN FETCH` and bypass Hibernate L2 cache:

```java
.setHint("org.hibernate.cacheMode", "IGNORE")
```

This prevents stale cached data where a cart appears empty or missing items after modifications.

**File:** `src/main/java/com/calt/buroxz/service/CustomizedCartService.java`

`addCartItem()` merges quantities for existing products instead of creating duplicates:

```java
for (CartItem existingItem : cart.getCartItems()) {
    if (existingItem.getProduct().getId().equals(cartItemDTO.getProduct().getId())) {
        existingItem.setQuantity(existingItem.getQuantity() + cartItemDTO.getQuantity());
        existingItem.setPrice(readyProduct.getPrice().multiply(BigDecimal.valueOf(existingItem.getQuantity())));
        cartItemRepository.save(existingItem);
        return findCartWithItems();
    }
}
```

---

## Frontend Components

### 1. Angular Route Configuration

**File:** `src/main/webapp/app/app.routes.ts`

```typescript
{
    path: '',
    loadChildren: () => import('./order/order.routes'),
}
```

**File:** `src/main/webapp/app/order/order.routes.ts`

| Path                | Component                 | Description                             |
| ------------------- | ------------------------- | --------------------------------------- |
| `/my-orders`        | `MyOrdersComponent`       | Order history for current user          |
| `/payment/checkout` | `CheckoutComponent`       | Initiates checkout, redirects to Stripe |
| `/payment/success`  | `PaymentSuccessComponent` | Shows after successful Stripe payment   |
| `/payment/cancel`   | `PaymentCancelComponent`  | Shows if user cancels on Stripe page    |

### 2. Order Service (Frontend)

**File:** `src/main/webapp/app/order/order.service.ts`

```typescript
export class OrderService {
  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/orders');

  checkout(): Observable<ICheckoutResponse> {
    return this.http.post<ICheckoutResponse>(`${this.resourceUrl}/checkout`, {});
  }

  getMyOrders(): Observable<IOrder[]> {
    return this.http.get<IOrder[]>(`${this.resourceUrl}/my-orders`);
  }
}
```

### 3. Checkout Flow (Frontend)

**File:** `src/main/webapp/app/order/checkout/checkout.component.ts`

```typescript
ngOnInit(): void {
    this.orderService.checkout().subscribe({
        next: res => {
            window.location.href = res.sessionUrl;  // Redirect to Stripe
        },
        error: () => {
            this.router.navigate(['/payment/cancel']); // Fallback on failure
        },
    });
}
```

The checkout component:

1. Calls `POST /api/orders/checkout`
2. On success, redirects the browser to the Stripe Checkout Session URL
3. On error, redirects to the cancel page

Template shows a spinner and "Redirecting to payment..." text.

### 4. Success Page

**File:** `src/main/webapp/app/order/success/success.component.html`

- Shows a green checkmark icon and "Payment Successful!" message.
- Reads `session_id` from query params (appended by Stripe after redirect).
- Link to return home.

### 5. Cancel Page

**File:** `src/main/webapp/app/order/cancel/cancel.component.html`

- Shows a yellow warning icon and "Payment Cancelled" message.
- Link to return to cart.

### 6. My Orders Page

**File:** `src/main/webapp/app/order/my-orders/my-orders.component.ts`

```typescript
ngOnInit(): void {
    this.loadOrders();
}

loadOrders(): void {
    this.isLoading.set(true);
    this.orderService.getMyOrders().subscribe(orders => {
        this.orders.set(orders);
        this.isLoading.set(false);
    });
}
```

**Template** (`my-orders.component.html`):

- Loading spinner while fetching.
- "No orders yet" alert when empty.
- Table with columns: ID, Status (badge: green PAID, yellow PENDING, gray other), Sub Total, Total, Date, Action (view detail button linking to `/order/:id/view`).
- Uses the JHipster entity's order detail component for the view link.

### 7. Cart Page Integration

**File:** `src/main/webapp/app/cart/cart.component.html`

A "Proceed to Checkout" button in the cart footer navigates to `/payment/checkout`, which triggers the checkout flow.

### 8. Home Page Integration

**File:** `src/main/webapp/app/home/home.component.html`

- Products are visible to all users (anonymous browsing).
- "Add to Cart" button only shown when logged in.
- "Login to buy" button shown for anonymous users, calling `loginService.login()`.
- Add-to-cart: confirmation modal (NgbModal) with product name/price, then API call.
- On success: green toast notification for 3 seconds.

### 9. Navbar

**File:** `src/main/webapp/app/layouts/navbar/navbar.component.html`

- "Cart" link with item count badge (authenticated only).
- "My Orders" link next to Cart (authenticated only), routes to `/my-orders`.
- "Entities" dropdown for admin CRUD.

### 10. Font Awesome Icons

**File:** `src/main/webapp/app/config/font-awesome-icons.ts`

Registered icons: `faCheckCircle`, `faTimesCircle`, `faCreditCard`, `faShoppingCart`, `faCartPlus`, `faClipboardList`, `faEye`, `faArrowLeft`, `faInfoCircle`.

---

## Stripe Integration Details

### Checkout Session Parameters

| Parameter             | Value                                                                    | Purpose                                 |
| --------------------- | ------------------------------------------------------------------------ | --------------------------------------- |
| `mode`                | `PAYMENT`                                                                | One-time payment (not subscription)     |
| `success_url`         | `http://localhost:8080/payment/success?session_id={CHECKOUT_SESSION_ID}` | Redirect after successful payment       |
| `cancel_url`          | `http://localhost:8080/payment/cancel`                                   | Redirect if user cancels                |
| `customer_email`      | `order.getUser().getEmail()`                                             | Pre-fills email on Stripe Checkout page |
| `client_reference_id` | `order.getId()`                                                          | Reference to our internal order ID      |
| `metadata.order_id`   | `order.getId()`                                                          | Same, in metadata for webhook           |
| `line_items`          | Order items with price, qty, name                                        | What the user sees on Stripe Checkout   |

### Price Calculation

- Stripe expects amounts in the smallest currency unit (cents for USD).
- `unit_amount_decimal = priceAtPurchase × 100` (e.g., $19.99 → 1999).
- The order's `subTotal` is the sum of `priceAtPurchase × quantity` for all items.

### Webhook Event Handling

- Only handles `checkout.session.completed`.
- Stripe can send the same event multiple times (idempotency), but the handler is idempotent since it overwrites `status = PAID` each time.
- The webhook URL must be configured in the Stripe Dashboard (Developers → Webhooks → Add endpoint → `http://your-host/api/orders/webhook`).
- Use the Stripe CLI for local testing: `stripe listen --forward-to localhost:8080/api/orders/webhook`.

---

## Security Notes

| Concern              | Solution                                                                          |
| -------------------- | --------------------------------------------------------------------------------- |
| Webhook authenticity | Stripe SDK `Webhook.constructEvent()` verifies HMAC signature with webhook secret |
| Order ownership      | `OrderRepository.findByUserIsCurrentUser()` uses SpEL `?#{authentication.name}`   |
| Cart isolation       | Cart queries filter by authenticated user's login                                 |
| Anonymous browsing   | Products/categories GET endpoints are `permitAll()`                               |
| CSRF on webhook      | Webhook explicitly excluded from CSRF protection                                  |
| Stripe API key       | Configured via environment variable `STRIPE_API_KEY` in production                |

## i18n Translations

**File:** `src/main/webapp/i18n/en/order.json`, `src/main/webapp/i18n/vi/order.json`

Translations cover:

- Checkout page title and redirect message
- Payment success/cancel titles and messages
- My Orders page title and empty state
- Order field labels (Status, Sub Total, Total, Created Date)
- Order entity CRUD messages (created, updated, deleted)

---

## Complete Checkout Flow (End to End)

```
1. User browses products (anonymous) ─────────────────────────────────┐
2. User clicks "Login to buy" ──> Keycloak login ──> Redirect back    │
3. User adds items to cart (confirmation modal, success toast)        │
4. User goes to Cart page ──> views items                             │
5. User clicks "Proceed to Checkout" ──> /payment/checkout            │
6. Angular CheckoutComponent calls POST /api/orders/checkout          │
                                                                      ▼
┌─────────────────── Backend ──────────────────────────────────────────┐
│ 7. CustomizedOrderService.checkOut()                                 │
│    a. Get authenticated user from SecurityContext                    │
│    b. Fetch cart by user login (Hibernate cache bypass)              │
│    c. Validate stock for every item                                  │
│    d. Create Order with OrderItems, capture current prices           │
│    e. Decrement product quantities                                   │
│    f. Calculate subTotal, set status = PENDING                       │
│    g. Save Order (cascade persists OrderItems)                       │
│    h. Return OrderResponse (id, totals, etc.)                        │
│ 8. CustomizedOrderService.payment(orderId)                            │
│    a. StripeService.createCheckoutSession(order)                     │
│      - Builds Stripe Session with line items, metadata, URLs         │
│      - Calls Stripe API via Java SDK                                 │
│      - Returns Checkout Session URL                                  │
│ 9. customizedOrderService.clearCart() ── deletes cart items          │
│ 10. Return { sessionUrl, orderId } to Angular                       │
└─────────────────────────────────────────────────────────────────────┘
                                                                      ▼
11. window.location.href = sessionUrl ──> Stripe Checkout Page
12. User enters card details on Stripe's hosted page
13. Stripe processes payment ──> redirects to /payment/success
14. Stripe sends webhook POST /api/orders/webhook
    a. Signature verification
    b. Lookup order by metadata.order_id
    c. Set order.status = PAID
    d. Save
15. User sees "Payment Successful!" page
16. User can visit /my-orders to see all their orders
```

## Troubleshooting

| Symptom                                 | Cause                                                                                          | Fix                                                               |
| --------------------------------------- | ---------------------------------------------------------------------------------------------- | ----------------------------------------------------------------- |
| "Cart is empty" when checking out       | Hibernate L2 cache returns stale empty cart                                                    | Cart queries use `cacheMode=IGNORE`; clear Redis cache or restart |
| "User not found" when browsing products | `ScopeAspect` intercepts `ProductService` and `AuthorizationService` can't find anonymous user | Exclude `ProductService` from ScopeAspect pointcut                |
| Stripe webhook returns 403              | CSRF protection blocks the POST                                                                | Exclude `/api/orders/webhook` from CSRF                           |
| Stripe webhook returns 401              | Authentication required                                                                        | Add `.permitAll()` for `/api/orders/webhook`                      |
| "Invalid email address" from Stripe     | OIDC username is not an email                                                                  | Use `getEmail()` instead of `getLogin()` for `customerEmail`      |
| Prices are doubled in Stripe            | `calculatePrice` was multiplying `unitPrice × qty²`                                            | Sum `lineTotal` directly instead of recalculating                 |
| Duplicate cart items                    | No merge logic for existing product in cart                                                    | `addCartItem()` now increments quantity for existing items        |
