CREATE TABLE simulacao (
   id BIGINT PRIMARY KEY AUTO_INCREMENT,
   descricao VARCHAR(255),
   valor DECIMAL(19,2),
   data DATE,
   tipo VARCHAR(50),
   recorrente BOOLEAN,
   parcelas INT,
   aprovada BOOLEAN
);
