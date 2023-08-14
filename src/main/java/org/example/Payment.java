package org.example;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Payment {
    private int id;
    private int orderId;
    private LocalDateTime createdOn;
    private boolean success;
    private BigDecimal amount;
}
