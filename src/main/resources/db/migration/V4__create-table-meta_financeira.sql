CREATE TABLE meta_financeira (
                                 id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                 descricao VARCHAR(255),
                                 valor_objetivo DECIMAL(19,2),
                                 valor_atual DECIMAL(19,2),
                                 prazo_final DATE,
                                 concluida BOOLEAN,
                                 user_id BIGINT NOT NULL,
                                 CONSTRAINT fk_meta_user FOREIGN KEY (user_id) REFERENCES user(id)
);
