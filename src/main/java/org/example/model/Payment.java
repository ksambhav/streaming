package org.example.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class Payment {
    private int id;
    private int orderId;
    private Instant createdOn;
    private boolean success;
    private BigDecimal amount;
}
