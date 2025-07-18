package com.backendtrading212.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class TradeRequest {
    private String username;
    private String symbol;
    private BigDecimal price;
    private BigDecimal quantity;
}

