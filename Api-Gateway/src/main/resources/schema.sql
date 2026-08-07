CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL
);


INSERT INTO users (username, password, role) VALUES ('admin', 'senha123', 'ADMIN');
INSERT INTO users (username, password, role) VALUES ('user', 'senha123', 'USER');