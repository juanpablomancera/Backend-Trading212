-- Insert a test user
INSERT INTO users (username, balance)
VALUES ('testuser', 9500.00);  -- assuming they spent $500

-- Insert holdings for testuser
INSERT INTO holdings (user_id, crypto_symbol, quantity)
VALUES
    ((SELECT id FROM users WHERE username = 'testuser'), 'BTC/USD', 0.01),
    ((SELECT id FROM users WHERE username = 'testuser'), 'ETH/USD', 0.5);

-- Insert some transactions for testuser
INSERT INTO transactions (user_id, crypto_symbol, quantity, price, type, timestamp)
VALUES
    ((SELECT id FROM users WHERE username = 'testuser'), 'BTC/USD', 0.01, 30000.00, 'BUY', NOW() - INTERVAL 2 DAY),
    ((SELECT id FROM users WHERE username = 'testuser'), 'ETH/USD', 0.5, 2000.00, 'BUY', NOW() - INTERVAL 1 DAY),
    ((SELECT id FROM users WHERE username = 'testuser'), 'ETH/USD', 0.2, 2200.00, 'SELL', NOW() - INTERVAL 12 HOUR);
