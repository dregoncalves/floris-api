CREATE TABLE entrada (
     id BIGINT PRIMARY KEY AUTO_INCREMENT,
     user_id BIGINT NOT NULL,
     descricao VARCHAR(255) NOT NULL,
     valor DECIMAL(19,2) NOT NULL,
     data_recebimento DATE NOT NULL,
     tipo VARCHAR(20) NOT NULL,
     recorrente BOOLEAN NOT NULL,
     FOREIGN KEY (user_id) REFERENCES user(id)
);
