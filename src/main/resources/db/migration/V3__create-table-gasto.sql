CREATE TABLE gasto (
   id BIGINT PRIMARY KEY AUTO_INCREMENT,
   usuario_id BIGINT NOT NULL,
   descricao VARCHAR(255) NOT NULL,
   valor DECIMAL(19,2) NOT NULL,
   tipo VARCHAR(20) NOT NULL,         -- Enum: FIXO, VARIAVEL, PARCELADO
   data_vencimento DATE NOT NULL,
   numero_parcela_atual INT,          -- se parcelado
   total_parcelas INT,                -- se parcelado
   gasto_cartao BOOLEAN,
   pago BOOLEAN,
   FOREIGN KEY (usuario_id) REFERENCES users(id)
);
