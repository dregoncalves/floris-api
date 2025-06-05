CREATE TABLE gasto (
   id BIGINT PRIMARY KEY AUTO_INCREMENT,
   descricao VARCHAR(255),
   valor DECIMAL(19,2),
   data DATE,
   fixo BOOLEAN,
   pago BOOLEAN,
   parcelas INT
);
