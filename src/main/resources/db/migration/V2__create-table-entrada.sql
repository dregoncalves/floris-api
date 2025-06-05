CREATE TABLE entrada (
     id BIGINT PRIMARY KEY AUTO_INCREMENT,
     descricao VARCHAR(255),
     valor DECIMAL(19,2),
     data_recebimento DATE,
     frequente BOOLEAN
);
