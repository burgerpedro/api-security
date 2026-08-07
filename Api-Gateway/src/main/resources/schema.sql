CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL
);


INSERT INTO users (username, password, role) VALUES ('admin', '$2a$12$umfyJiA3PdioNIPMqpQFiucNw7qrNZVNTBAtf2ezN6p1HT2uF9A2W', 'ADMIN');
INSERT INTO users (username, password, role) VALUES ('user', '$2a$12$umfyJiA3PdioNIPMqpQFiucNw7qrNZVNTBAtf2ezN6p1HT2uF9A2W', 'USER');