package org.example;

import lombok.Data;

@Data
public class Confirmation {
    private int orderId;
    private int paymentId;
    private boolean completed;

}
