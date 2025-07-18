package com.backendtrading212.model;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Holding {
    private int id;
    private int userId;
    private String cryptoSymbol;
    private BigDecimal quantity;
    private BigDecimal price;
}
