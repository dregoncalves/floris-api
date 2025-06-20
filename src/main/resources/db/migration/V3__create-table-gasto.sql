CREATE TABLE gasto (
   id BIGINT PRIMARY KEY AUTO_INCREMENT,
   user_id BIGINT NOT NULL,
   descricao VARCHAR(255) NOT NULL,
   valor DECIMAL(19,2) NOT NULL,
   tipo VARCHAR(20) NOT NULL,
   data_vencimento DATE NOT NULL,
   numero_parcela_atual INT,
   total_parcelas INT,
   gasto_cartao BOOLEAN,
   pago BOOLEAN,
   FOREIGN KEY (user_id) REFERENCES user(id)
);
