package core.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID eventId,
        Instant createdAt,
        Long orderId,
        String customerId,
        BigDecimal totalAmount,
        StatusEvent status,
        List<OrderItem> items
        ) {
    public record OrderItem(
            UUID productId,
            Integer quantity,
            BigDecimal price
    ) {}
}
