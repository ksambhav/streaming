package org.example;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Data
public class Payment {
    private int id;
    private int orderId;
    private Instant createdOn;
    private boolean success;
    private BigDecimal amount;
}
