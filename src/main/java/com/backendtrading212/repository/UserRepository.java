package com.backendtrading212.repository;

import com.backendtrading212.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public class UserRepository {
    @Autowired
    private JdbcTemplate jdbc;

    public User findByUsername(String username) {
        return jdbc.queryForObject("SELECT * FROM users WHERE username = ?",
                new BeanPropertyRowMapper<>(User.class), username);
    }

    public void resetBalance(String username) {
        jdbc.update("UPDATE users SET balance = 10000.00 WHERE username = ?", username);
        jdbc.update("DELETE FROM holdings WHERE user_id = (SELECT id FROM users WHERE username = ?)", username);
        jdbc.update("DELETE FROM transactions WHERE user_id = (SELECT id FROM users WHERE username = ?)", username);
    }

    public void updateBalance(Long userId, BigDecimal amount) {
        jdbc.update("UPDATE users SET balance = ? WHERE id = ?", amount, userId);
    }
}
