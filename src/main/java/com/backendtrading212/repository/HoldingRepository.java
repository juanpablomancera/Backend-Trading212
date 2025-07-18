package com.backendtrading212.repository;

import com.backendtrading212.model.Holding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public class HoldingRepository {

    @Autowired
    private JdbcTemplate jdbc;

    public void upsertHolding(Long userId, String symbol, BigDecimal quantity, BigDecimal price) {
        jdbc.update("""
        INSERT INTO holdings(user_id, crypto_symbol, quantity, price)
        VALUES (?, ?, ?, ?)
    """, userId, symbol, quantity, price);
    }

    public Holding getHoldingByUserIdAndSymbol(Long userId, String symbol) {
        List<Holding> results = jdbc.query(
                "SELECT * FROM holdings WHERE user_id = ? AND crypto_symbol = ?",
                new BeanPropertyRowMapper<>(Holding.class),
                userId, symbol
        );
        return results.isEmpty() ? null : results.get(0);
    }

    public void updateHoldingQuantity(Long userId, String symbol, BigDecimal quantity) {
        jdbc.update("UPDATE holdings SET quantity = ? WHERE user_id = ? AND crypto_symbol = ?", quantity, userId, symbol);
    }

    public void deleteHolding(Long userId, String symbol) {
        jdbc.update("DELETE FROM holdings WHERE user_id = ? AND crypto_symbol = ?", userId, symbol);
    }

    public List<Holding> getHoldingsByUserId(Long userId) {
        return jdbc.query("SELECT * FROM holdings WHERE user_id = ?", new BeanPropertyRowMapper<>(Holding.class), userId);
    }

    public List<Holding> getHoldingsByUserIdAndSymbolOrderedByPrice(Long userId, String symbol) {
        return jdbc.query(
                "SELECT * FROM holdings WHERE user_id = ? AND crypto_symbol = ? ORDER BY price ASC",
                new BeanPropertyRowMapper<>(Holding.class),
                userId, symbol
        );
    }

    public void deleteHoldingById(Long holdingId) {
        jdbc.update("DELETE FROM holdings WHERE id = ?", holdingId);
    }

    public void updateHoldingQuantityById(Long holdingId, BigDecimal quantity) {
        jdbc.update("UPDATE holdings SET quantity = ? WHERE id = ?", quantity, holdingId);
    }

}

