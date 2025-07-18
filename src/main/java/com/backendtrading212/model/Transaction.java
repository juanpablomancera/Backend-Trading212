package com.backendtrading212.model;

import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    private int id;
    private int userId;
    private String cryptoSymbol;
    private BigDecimal quantity;
    private BigDecimal price;
    private String type;
    private Timestamp timestamp;
}
