CREATE TABLE reserva_emergencia (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    valor_objetivo DECIMAL(19,2),
    valor_atual DECIMAL(19,2),
    ativa BOOLEAN,
    data_criacao DATE,
    ultima_atualizacao DATE,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_reserva_emergencia_user FOREIGN KEY (user_id) REFERENCES user(id)
);
