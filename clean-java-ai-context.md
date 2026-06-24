# Clean Java Code AI Context

Use this document as standing context whenever you generate, review, refactor, or document Java code.

The goal is to produce Java code that is clean, readable, maintainable, testable, and safe to evolve. Prefer simple, explicit code over clever abstractions. Optimize first for correctness and clarity, then for performance only when there is evidence or a clear requirement.

---

## 1. Core Principles

### Write code for humans first

Code should be easy to read, reason about, debug, and modify. Assume the next developer will not have the full context in their head.

Prefer:

- Clear names.
- Small methods.
- Explicit control flow.
- Local reasoning.
- Predictable behavior.
- Minimal hidden side effects.

Avoid:

- Clever one-liners that hide intent.
- Deep nesting.
- Overly generic abstractions.
- Premature frameworks or patterns.
- Magic values.
- Boolean parameters that make call sites unclear.

### Make illegal states hard to represent

Use types, constructors, validation, enums, and value objects to prevent invalid combinations of data.

Prefer this:

```java
public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    DONE,
    CANCELLED
}
```

Over this:

```java
String status;
```

### Keep behavior close to the data it owns

A class should be responsible for the rules related to its own state. Avoid spreading business rules across unrelated utility classes unless the behavior is truly stateless and reusable.

---

## 2. Naming Guidelines

### General naming

Use names that describe intent, not implementation details.

Good examples:

```java
calculateTotalPrice()
findActiveUsers()
markInvoiceAsPaid()
isExpired()
```

Weak examples:

```java
doStuff()
process()
handle()
check()
manager()
data()
```

Names like `process`, `handle`, and `execute` are acceptable only when the surrounding type gives strong meaning, such as `PaymentProcessor.process(payment)`.

### Class names

Use nouns or noun phrases.

Examples:

```java
Order
Invoice
CustomerRepository
UserRegistrationService
DateRange
Money
```

Avoid vague suffixes unless meaningful:

```java
OrderHelper
CommonUtils
DataManager
GeneralService
```

### Method names

Use verbs or verb phrases.

Examples:

```java
createOrder()
validateRequest()
loadUserProfile()
removeExpiredSessions()
```

Boolean-returning methods should read naturally:

```java
isVisible()
hasPermission()
canRetry()
shouldRefresh()
```

### Variable names

Use short names only for very small scopes.

Acceptable:

```java
for (User user : users) {
    // ...
}
```

Avoid meaningless names:

```java
var x = getData();
var temp = calculate();
```

---

## 3. Class Design

### Keep classes focused

Each class should have one clear responsibility. If a class becomes responsible for validation, persistence, formatting, authorization, and notification, split it.

A good class should be easy to describe in one sentence:

> `InvoiceCalculator` calculates invoice totals from invoice lines, tax rules, and discounts.

If the sentence needs multiple unrelated “and” clauses, the class probably does too much.

### Prefer composition over inheritance

Use inheritance only when there is a true “is-a” relationship and polymorphism is needed.

Prefer composition for reusable behavior:

```java
public final class OrderService {
    private final OrderRepository orderRepository;
    private final PaymentGateway paymentGateway;

    public OrderService(OrderRepository orderRepository, PaymentGateway paymentGateway) {
        this.orderRepository = orderRepository;
        this.paymentGateway = paymentGateway;
    }
}
```

### Use `final` intentionally

Use `final` for classes that are not designed for inheritance.
Use `final` fields for required dependencies and immutable state.

```java
public final class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository");
    }
}
```

### Minimize mutable state

Prefer immutable objects for values and configuration.

Use records when appropriate:

```java
public record DateRange(LocalDate startDate, LocalDate endDate) {
    public DateRange {
        Objects.requireNonNull(startDate, "startDate");
        Objects.requireNonNull(endDate, "endDate");

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate must not be before startDate");
        }
    }
}
```

Use regular classes when you need richer behavior, controlled construction, or compatibility with frameworks that do not work well with records.

---

## 4. Method Design

### Keep methods small and purposeful

A method should do one thing at one level of abstraction.

Avoid mixing high-level business flow with low-level formatting, parsing, SQL, or HTTP details in the same method.

Weak:

```java
public void registerUser(RegisterUserRequest request) {
    if (request.email() == null || !request.email().contains("@")) {
        throw new IllegalArgumentException("Invalid email");
    }

    String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
    String sql = "insert into users ...";
    // database logic
    // email template logic
    // notification sending logic
}
```

Better:

```java
public void registerUser(RegisterUserRequest request) {
    RegisterUserCommand command = RegisterUserCommand.from(request);
    User user = userFactory.create(command);

    userRepository.save(user);
    welcomeEmailSender.sendTo(user);
}
```

### Prefer guard clauses

Use early returns to reduce nesting.

```java
public void sendReminder(User user) {
    if (!user.isActive()) {
        return;
    }

    if (!user.hasEmailAddress()) {
        return;
    }

    reminderSender.send(user);
}
```

Avoid unnecessary nesting:

```java
public void sendReminder(User user) {
    if (user.isActive()) {
        if (user.hasEmailAddress()) {
            reminderSender.send(user);
        }
    }
}
```

### Avoid boolean trap parameters

Avoid methods like:

```java
render(true);
createUser(request, false);
```

Prefer explicit methods or an options object:

```java
renderCompact();
renderFull();
```

Or:

```java
CreateUserOptions options = CreateUserOptions.withoutWelcomeEmail();
createUser(request, options);
```

### Keep parameter lists short

Three or fewer parameters is usually ideal. More parameters may indicate that a value object is needed.

Weak:

```java
createOrder(userId, productId, quantity, currency, discountCode, shippingAddress);
```

Better:

```java
createOrder(CreateOrderCommand command);
```

---

## 5. Null Handling

### Be explicit about nullability

Do not allow `null` accidentally. Decide whether `null` is allowed and make that clear.

For required constructor arguments:

```java
public UserService(UserRepository userRepository) {
    this.userRepository = Objects.requireNonNull(userRepository, "userRepository");
}
```

For required method parameters:

```java
public User findUser(UserId userId) {
    Objects.requireNonNull(userId, "userId");
    return userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
}
```

### Use `Optional` for possibly missing return values

Good:

```java
public Optional<User> findByEmail(String email) {
    // ...
}
```

Avoid using `Optional` for fields, method parameters, or collection elements unless there is a strong reason.

### Do not call methods after `orElseGet(() -> null)`

This can cause `NullPointerException`:

```java
return optional.orElseGet(() -> null).getValue();
```

Prefer:

```java
return optional
        .map(Item::getValue)
        .orElse(null);
```

Or better, return `Optional<V>`:

```java
public Optional<V> getValue() {
    return getItem().map(Item::getValue);
}
```

---

## 6. Exception Handling

### Use exceptions for exceptional cases

Do not use exceptions for normal control flow.

Good:

```java
public User getRequiredUser(UserId userId) {
    return userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
}
```

Good for expected absence:

```java
public Optional<User> findUser(UserId userId) {
    return userRepository.findById(userId);
}
```

### Preserve useful context

When wrapping exceptions, include meaningful context and keep the cause.

```java
try {
    paymentGateway.charge(payment);
} catch (PaymentGatewayException e) {
    throw new PaymentFailedException("Failed to charge payment " + payment.id(), e);
}
```

### Avoid swallowing exceptions

Bad:

```java
try {
    sendEmail();
} catch (Exception ignored) {
}
```

Better:

```java
try {
    sendEmail();
} catch (EmailException e) {
    logger.warn("Failed to send email for order {}", orderId, e);
}
```

Only catch broad exceptions at application boundaries where you can log, translate, or recover properly.

---

## 7. Collections and Streams

### Return empty collections, not null

Good:

```java
public List<Order> findOrders(UserId userId) {
    return orderRepository.findByUserId(userId);
}
```

Return `List.of()` when there are no results.

### Prefer readable streams

Streams are good for simple transformations.

```java
List<String> activeUserEmails = users.stream()
        .filter(User::isActive)
        .map(User::email)
        .toList();
```

Avoid long stream chains with complex side effects. Use a loop when it is clearer.

### Avoid mutating external state inside streams

Bad:

```java
List<String> emails = new ArrayList<>();
users.stream()
        .filter(User::isActive)
        .forEach(user -> emails.add(user.email()));
```

Better:

```java
List<String> emails = users.stream()
        .filter(User::isActive)
        .map(User::email)
        .toList();
```

---

## 8. Immutability and Value Objects

### Prefer value objects for domain concepts

Avoid passing raw strings and numbers for important business values.

Weak:

```java
sendPayment(String userId, BigDecimal amount, String currency);
```

Better:

```java
sendPayment(UserId userId, Money amount);
```

Example:

```java
public record Money(BigDecimal amount, Currency currency) {
    public Money {
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(currency, "currency");

        if (amount.signum() < 0) {
            throw new IllegalArgumentException("amount must not be negative");
        }
    }
}
```

### Be careful with `BigDecimal`

Use `BigDecimal` for money. Avoid `double` and `float` for monetary values.

Prefer string construction for exact decimal values:

```java
BigDecimal price = new BigDecimal("12.50");
```

Avoid:

```java
BigDecimal price = new BigDecimal(12.50);
```

---

## 9. Service Layer Guidelines

### Services should express business use cases

A service method should read like a use case:

```java
public OrderId placeOrder(PlaceOrderCommand command) {
    Customer customer = customerRepository.getRequired(command.customerId());
    Cart cart = cartRepository.getRequired(command.cartId());

    Order order = orderFactory.create(customer, cart);
    paymentService.authorize(order.paymentRequest());
    orderRepository.save(order);

    return order.id();
}
```

### Keep services orchestration-focused

Services may coordinate repositories, domain objects, external clients, and transactions, but should not become dumping grounds for every rule.

Move reusable domain rules into domain objects or dedicated policy classes.

---

## 10. Repository and Persistence Guidelines

### Repositories should hide persistence details

Business code should not know about SQL joins, ORM sessions, or database-specific behavior unless it is infrastructure code.

Good:

```java
Optional<User> findByEmail(EmailAddress email);
void save(User user);
```

Avoid leaking persistence details into business methods:

```java
Result<Record7<UUID, String, String, Timestamp, Integer, String, Boolean>> findUsersWithRawJoin(...);
```

That kind of method may exist inside infrastructure, but it should be mapped before reaching domain or application layers.

### Use transactions at use-case boundaries

Prefer one clear transaction around one application use case.

Avoid starting transactions deep inside unrelated helper methods unless that method is explicitly a transactional boundary.

---

## 11. API and DTO Guidelines

### Separate external DTOs from internal domain models

Do not let API request/response classes become your core domain model.

Use mapping explicitly:

```java
public UserResponse toResponse(User user) {
    return new UserResponse(
            user.id().value(),
            user.name(),
            user.email().value()
    );
}
```

### Validate input at boundaries

Validate requests before they reach business logic.

Examples:

- Required fields.
- String length.
- Email format.
- Numeric ranges.
- Enum values.
- Authorization constraints.

Business logic should still protect important invariants.

---

## 12. Logging Guidelines

### Log useful events, not noise

Good logs explain what happened and include identifiers that help debugging.

```java
logger.info("Order {} was placed by customer {}", order.id(), customer.id());
```

Avoid excessive logs inside hot loops or logs that expose sensitive data.

### Use parameterized logging

Good:

```java
logger.debug("Loading user {}", userId);
```

Avoid:

```java
logger.debug("Loading user " + userId);
```

### Do not log secrets

Never log:

- Passwords.
- Tokens.
- API keys.
- Full payment details.
- Private personal information unless explicitly safe and necessary.

---

## 13. Testing Guidelines

### Write tests for behavior, not implementation

Tests should describe what the code does from the caller’s point of view.

Good test names:

```java
shouldRejectOrderWhenCartIsEmpty()
shouldApplyDiscountWhenCouponIsValid()
shouldReturnEmptyListWhenUserHasNoOrders()
```

Weak test names:

```java
test1()
testCreate()
testProcess()
```

### Use Arrange, Act, Assert

```java
@Test
public void shouldApplyDiscountWhenCouponIsValid() {
    // Arrange
    Cart cart = Cart.withItem(product, 2);
    Coupon coupon = Coupon.validPercentage("SAVE10", 10);

    // Act
    Money total = calculator.calculateTotal(cart, coupon);

    // Assert
    assertEquals(new Money(new BigDecimal("90.00"), Currency.getInstance("JOD")), total);
}
```

### Prefer simple test data builders

Use builders or factory methods to keep tests readable.

```java
User activeUser = userBuilder()
        .active()
        .withEmail("user@example.com")
        .build();
```

### Test edge cases

Include tests for:

- Empty input.
- Null input where applicable.
- Invalid values.
- Boundary values.
- Duplicate values.
- Permission failures.
- External service failures.
- Time-sensitive logic.

### Avoid over-mocking

Mock external systems and slow dependencies. Prefer real domain objects for business logic tests.

Good mock targets:

- HTTP clients.
- Email senders.
- Payment gateways.
- Repositories in service unit tests.

Avoid mocking simple value objects or the class under test.

---

## 14. Documentation and Javadocs

### Document public APIs when intent is not obvious

Javadocs should explain why and how to use something, not repeat the method name.

Weak:

```java
/**
 * Gets the name.
 */
public String getName() {
    return name;
}
```

Better:

```java
/**
 * Returns the display name shown to users in the account menu.
 */
public String getDisplayName() {
    return displayName;
}
```

### Include `{@inheritDoc}` for inherited methods when documenting overrides

```java
/**
 * {@inheritDoc}
 */
@Override
public void close() {
    connection.close();
}
```

### Keep examples useful and small

Use examples when the API is not obvious.

```java
/**
 * Parses a comma-separated list of tags.
 *
 * <p>Example:</p>
 *
 * <pre>{@code
 * List<String> tags = TagParser.parse("java, clean-code, testing");
 * }</pre>
 */
public static List<String> parse(String value) {
    // ...
}
```

Avoid Markdown-specific formatting inside Javadocs unless the project explicitly supports it.

---

## 15. Formatting and Style

### Prefer consistent formatting over personal preference

Follow the existing project style. If no style exists, use conventional Java formatting:

- Four spaces for indentation.
- Opening braces on the same line.
- One statement per line.
- Meaningful blank lines between logical sections.
- No trailing whitespace.
- Keep imports organized.

### Keep line length reasonable

Prefer readable wrapping over very long lines.

```java
OrderSummary summary = orderSummaryFactory.create(
        customer,
        order,
        pricingRules,
        clock
);
```

### Avoid unnecessary comments

Do not comment what the code already says.

Bad:

```java
// Increment count by one
count++;
```

Good comments explain non-obvious decisions:

```java
// The provider may retry the callback, so this operation must be idempotent.
markPaymentAsCompleted(paymentId);
```

---

## 16. Concurrency and Thread Safety

### Do not assume classes are thread-safe

Document thread-safety expectations when relevant.

Use immutable objects where possible. For shared mutable state, use proper synchronization or concurrent collections.

```java
private final ConcurrentMap<UserId, Session> sessions = new ConcurrentHashMap<>();
```

### Avoid exposing mutable internals

Bad:

```java
public List<Item> getItems() {
    return items;
}
```

Better:

```java
public List<Item> getItems() {
    return List.copyOf(items);
}
```

---

## 17. Time and Date Guidelines

### Use `java.time`

Prefer:

```java
Instant
LocalDate
LocalDateTime
ZonedDateTime
Duration
Period
Clock
```

Avoid old date/time APIs unless required by legacy code:

```java
Date
Calendar
SimpleDateFormat
```

### Inject `Clock` for testable time logic

```java
public final class SubscriptionService {
    private final Clock clock;

    public SubscriptionService(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public boolean isExpired(Subscription subscription) {
        return subscription.expiresAt().isBefore(Instant.now(clock));
    }
}
```

---

## 18. Security and Validation

### Treat all external input as untrusted

External input includes:

- HTTP requests.
- Files.
- Environment variables.
- Database values from untrusted sources.
- Queue messages.
- Webhook payloads.

Validate before use.

### Avoid SQL injection

Use parameterized queries or safe query builders. Never concatenate untrusted values into SQL.

Bad:

```java
String sql = "select * from users where email = '" + email + "'";
```

Good:

```java
DSL.using(configuration)
        .selectFrom(USERS)
        .where(USERS.EMAIL.eq(email))
        .fetch();
```

### Avoid path traversal

Normalize and validate file paths before reading or writing files.

---

## 19. Performance Guidelines

### Do not optimize without reason

Start with clear code. Optimize only when:

- There is a measured bottleneck.
- The data size clearly requires it.
- The code is in a known hot path.
- The algorithmic complexity is obviously poor.

### Prefer better algorithms over micro-optimizations

Avoid nested loops over large collections when a map or set would make the intent and performance better.

Weak:

```java
for (Order order : orders) {
    for (Customer customer : customers) {
        if (order.customerId().equals(customer.id())) {
            // ...
        }
    }
}
```

Better:

```java
Map<CustomerId, Customer> customersById = customers.stream()
        .collect(Collectors.toMap(Customer::id, Function.identity()));

for (Order order : orders) {
    Customer customer = customersById.get(order.customerId());
    // ...
}
```

---

## 20. Refactoring Guidelines

### Preserve behavior first

When refactoring existing code:

1. Understand current behavior.
2. Add or update tests if possible.
3. Refactor in small steps.
4. Keep public APIs stable unless the change is intentional.
5. Avoid mixing refactoring with unrelated feature changes.

### Prefer small safe improvements

Good refactoring targets:

- Extract method.
- Rename unclear variables.
- Replace magic values with constants.
- Replace duplicate logic with a focused helper.
- Introduce value objects for repeated primitive groups.
- Reduce nesting with guard clauses.

### Do not rewrite everything unnecessarily

Do not replace working code with a completely different design unless there is a clear reason.

---

## 21. Code Review Checklist

Before considering Java code complete, verify:

- The code solves the requested problem.
- The design is simple enough.
- Names clearly express intent.
- Methods are focused and readable.
- Null handling is explicit.
- Exceptions include useful context.
- No sensitive values are logged.
- Collections are not returned as `null`.
- Public APIs are documented when needed.
- Tests cover normal, edge, and failure cases.
- Existing behavior is preserved unless intentionally changed.
- No unrelated formatting or structural changes were introduced.
- No hidden side effects were added.
- No unnecessary abstractions were introduced.

---

## 22. AI-Specific Instructions for Code Generation

When generating Java code, follow these rules:

1. Prefer clear, boring, maintainable Java.
2. Preserve existing method order and class structure unless asked to reorganize.
3. Do not remove existing behavior unless explicitly requested.
4. Do not silently change public APIs.
5. Keep changes minimal and focused.
6. Include imports when providing full files.
7. Avoid placeholder code unless clearly marked.
8. Avoid unnecessary dependencies.
9. Use standard Java APIs where possible.
10. Explain important tradeoffs briefly after the code.
11. Add tests when the change affects logic.
12. Prefer JUnit 4 when the existing module uses JUnit 4.
13. Use `Objects.requireNonNull` for required dependencies.
14. Prefer `Optional` for missing return values, not for fields or parameters.
15. Use `BigDecimal` for money.
16. Use `java.time` for date and time.
17. Use parameterized logging.
18. Avoid broad `catch (Exception)` unless at an application boundary.
19. Avoid mutable static state.
20. Avoid global utility dumping grounds.

---

## 23. Preferred Response Format for AI Coding Tasks

When asked to modify or generate Java code, respond with:

1. A brief summary of the change.
2. The code or patch.
3. Any important notes about compatibility, behavior, or assumptions.
4. Tests or suggested tests when applicable.

Do not over-explain obvious syntax. Focus explanations on design decisions, risks, and usage.

---

## 24. Example Clean Java Class

```java
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final TaxCalculator taxCalculator;
    private final Clock clock;

    public InvoiceService(
            InvoiceRepository invoiceRepository,
            TaxCalculator taxCalculator,
            Clock clock
    ) {
        this.invoiceRepository = Objects.requireNonNull(invoiceRepository, "invoiceRepository");
        this.taxCalculator = Objects.requireNonNull(taxCalculator, "taxCalculator");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public InvoiceId createInvoice(CreateInvoiceCommand command) {
        Objects.requireNonNull(command, "command");

        if (command.lines().isEmpty()) {
            throw new IllegalArgumentException("Invoice must contain at least one line");
        }

        BigDecimal subtotal = calculateSubtotal(command);
        BigDecimal tax = taxCalculator.calculateTax(subtotal, command.taxRegion());

        Invoice invoice = new Invoice(
                InvoiceId.newId(),
                command.customerId(),
                command.lines(),
                subtotal,
                tax,
                Instant.now(clock)
        );

        invoiceRepository.save(invoice);
        return invoice.id();
    }

    private BigDecimal calculateSubtotal(CreateInvoiceCommand command) {
        return command.lines().stream()
                .map(InvoiceLine::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
```

This example demonstrates:

- Constructor dependency injection.
- `final` dependencies.
- Null checks for required values.
- Small focused methods.
- Guard clause validation.
- `BigDecimal` for money.
- `Clock` for testable time logic.
- Clear names.
- No hidden global state.

---

## 25. Final Reminder

Clean Java code should feel obvious after reading it. It should avoid surprise, reduce cognitive load, and make future changes safer.

When in doubt, choose the design that is easiest to explain, easiest to test, and hardest to misuse.
