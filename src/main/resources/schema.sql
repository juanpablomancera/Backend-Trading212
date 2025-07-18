CREATE TABLE users (
                       id INT PRIMARY KEY AUTO_INCREMENT,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       balance DECIMAL(15, 2) DEFAULT 10000.00
);

CREATE TABLE holdings (
                          id INT PRIMARY KEY AUTO_INCREMENT,
                          user_id INT,
                          crypto_symbol VARCHAR(10),
                          quantity DECIMAL(20, 10),
                          price DECIMAL(15, 2),
                          FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE transactions (
                              id INT PRIMARY KEY AUTO_INCREMENT,
                              user_id INT,
                              crypto_symbol VARCHAR(10),
                              quantity DECIMAL(20, 10),
                              price DECIMAL(15, 2),
                              type ENUM('BUY', 'SELL'),
                              timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              FOREIGN KEY (user_id) REFERENCES users(id)
);
