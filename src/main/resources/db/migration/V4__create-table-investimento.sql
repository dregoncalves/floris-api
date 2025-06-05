CREATE TABLE investimento (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  descricao VARCHAR(255),
  valor DECIMAL(19,2),
  data DATE,
  recorrente BOOLEAN,
  tipo_investimento VARCHAR(255)
);
