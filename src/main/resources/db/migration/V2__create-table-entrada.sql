CREATE TABLE entrada (
     id BIGINT PRIMARY KEY AUTO_INCREMENT,
     usuario_id BIGINT NOT NULL,
     descricao VARCHAR(255) NOT NULL,
     valor DECIMAL(19,2) NOT NULL,
     data_recebimento DATE NOT NULL,
     tipo VARCHAR(20) NOT NULL,         -- Enum: SALARIO, FREELA, OUTROS
     recorrente BOOLEAN NOT NULL,
     FOREIGN KEY (usuario_id) REFERENCES users(id)
);
