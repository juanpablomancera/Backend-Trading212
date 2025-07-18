package com.backendtrading212.repository;

import com.backendtrading212.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public class TransactionRepository {

    @Autowired
    private JdbcTemplate jdbc;

    public List<Transaction> getTransactionsByUsername(String username) {
        return jdbc.query(
                "SELECT * FROM transactions WHERE user_id = (SELECT id FROM users WHERE username = ?) ORDER BY timestamp DESC",
                new BeanPropertyRowMapper<>(Transaction.class),
                username
        );
    }

    public void insertTransaction(Long userId, String symbol, BigDecimal qty, BigDecimal price, String type) {
        jdbc.update(
                "INSERT INTO transactions (user_id, crypto_symbol, quantity, price, type, timestamp) VALUES (?, ?, ?, ?, ?, NOW())",
                userId, symbol, qty, price, type
        );
    }

    public List<Transaction> getTransactionsByUserId(Long userId) {
        return jdbc.query("SELECT * FROM transactions WHERE user_id = ?",
                new BeanPropertyRowMapper<>(Transaction.class), userId);
    }

    public BigDecimal getLatestPriceForSymbol(String symbol) {
        List<Transaction> tx = jdbc.query(
                "SELECT * FROM transactions WHERE crypto_symbol = ? ORDER BY timestamp DESC LIMIT 1",
                new BeanPropertyRowMapper<>(Transaction.class), symbol
        );
        return tx.isEmpty() ? null : tx.get(0).getPrice();
    }
}
