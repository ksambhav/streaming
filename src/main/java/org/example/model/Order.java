package org.example.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class Order {
    private int id;
    private Instant createdOn;
    private String itemDescription;
    private BigDecimal amount;
    private Integer paymentId;
    private boolean paymentSuccess;
}
