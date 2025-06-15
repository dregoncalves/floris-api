CREATE TABLE reserva_emergencia (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    valor_objetivo DECIMAL(19,2),
    valor_atual DECIMAL(19,2),
    ativa BOOLEAN
);
