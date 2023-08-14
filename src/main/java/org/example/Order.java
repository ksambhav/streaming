package org.example;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Order {
    private int id;
    private LocalDateTime createdOn;
    private String description;
    private BigDecimal amount;
}
