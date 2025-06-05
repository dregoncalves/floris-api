CREATE TABLE meta_financeira (
     id BIGINT PRIMARY KEY AUTO_INCREMENT,
     descricao VARCHAR(255),
     valor_objetivo DECIMAL(19,2),
     valor_atual DECIMAL(19,2),
     prazo_final DATE,
     concluida BOOLEAN
);
