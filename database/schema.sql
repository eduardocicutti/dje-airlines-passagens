-- DJE Airlines - estrutura MySQL
-- Execute no MySQL Command Line Client com: SOURCE C:/caminho/database/schema.sql;

CREATE DATABASE IF NOT EXISTS dje_airlines
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE dje_airlines;

CREATE TABLE IF NOT EXISTS aeroportos (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    codigo_iata CHAR(3) NOT NULL,
    cidade VARCHAR(80) NOT NULL,
    nome VARCHAR(150) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_aeroporto_iata UNIQUE (codigo_iata),
    CONSTRAINT ck_aeroporto_iata CHECK (codigo_iata REGEXP '^[A-Z]{3}$')
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS rotas (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    origem_id BIGINT UNSIGNED NOT NULL,
    destino_id BIGINT UNSIGNED NOT NULL,
    distancia_milhas INT UNSIGNED NOT NULL,
    ativa BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_rota_origem FOREIGN KEY (origem_id) REFERENCES aeroportos(id),
    CONSTRAINT fk_rota_destino FOREIGN KEY (destino_id) REFERENCES aeroportos(id),
    CONSTRAINT uk_rota_sentido UNIQUE (origem_id, destino_id),
    CONSTRAINT ck_rota_aeroportos CHECK (origem_id <> destino_id),
    CONSTRAINT ck_rota_distancia CHECK (distancia_milhas > 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS voos (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL,
    data_voo DATE NOT NULL,
    horario TIME NOT NULL,
    portao VARCHAR(10) NOT NULL,
    capacidade SMALLINT UNSIGNED NOT NULL,
    estado ENUM('DISPONIVEL', 'LOTADO', 'CANCELADO', 'ENCERRADO') NOT NULL DEFAULT 'DISPONIVEL',
    CONSTRAINT uk_voo_codigo_data UNIQUE (codigo, data_voo),
    CONSTRAINT ck_voo_capacidade CHECK (capacidade > 0)
) ENGINE=InnoDB;

CREATE INDEX ix_voos_data_estado ON voos (data_voo, estado);

CREATE TABLE IF NOT EXISTS voo_trechos (
    voo_id BIGINT UNSIGNED NOT NULL,
    rota_id BIGINT UNSIGNED NOT NULL,
    ordem SMALLINT UNSIGNED NOT NULL,
    PRIMARY KEY (voo_id, ordem),
    CONSTRAINT uk_voo_rota UNIQUE (voo_id, rota_id, ordem),
    CONSTRAINT fk_voo_trecho_voo FOREIGN KEY (voo_id) REFERENCES voos(id) ON DELETE CASCADE,
    CONSTRAINT fk_voo_trecho_rota FOREIGN KEY (rota_id) REFERENCES rotas(id),
    CONSTRAINT ck_voo_trecho_ordem CHECK (ordem > 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS assentos (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    voo_id BIGINT UNSIGNED NOT NULL,
    codigo VARCHAR(4) NOT NULL,
    estado ENUM('DISPONIVEL', 'RESERVADO', 'OCUPADO') NOT NULL DEFAULT 'DISPONIVEL',
    reservado_ate DATETIME NULL,
    CONSTRAINT fk_assento_voo FOREIGN KEY (voo_id) REFERENCES voos(id) ON DELETE CASCADE,
    CONSTRAINT uk_voo_assento UNIQUE (voo_id, codigo),
    CONSTRAINT ck_assento_codigo CHECK (codigo REGEXP '^[A-Z][1-9][0-9]?$')
) ENGINE=InnoDB;

CREATE INDEX ix_assentos_voo_estado ON assentos (voo_id, estado);

CREATE TABLE IF NOT EXISTS funcionarios (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    matricula VARCHAR(30) NOT NULL,
    nome VARCHAR(120) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_funcionario_matricula UNIQUE (matricula)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS passageiros (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    documento VARCHAR(30) NOT NULL,
    criado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_passageiro_documento UNIQUE (documento)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS pagamentos (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    tipo ENUM('DINHEIRO', 'CREDITO', 'DEBITO') NOT NULL,
    valor DECIMAL(12,2) NOT NULL,
    estado ENUM('PENDENTE', 'APROVADO', 'RECUSADO', 'CANCELADO') NOT NULL,
    funcionario_id BIGINT UNSIGNED NULL,
    cartao_bandeira VARCHAR(30) NULL,
    cartao_final CHAR(4) NULL,
    parcelas TINYINT UNSIGNED NULL,
    processado_em DATETIME NULL,
    CONSTRAINT fk_pagamento_funcionario FOREIGN KEY (funcionario_id) REFERENCES funcionarios(id),
    CONSTRAINT ck_pagamento_valor CHECK (valor > 0),
    CONSTRAINT ck_pagamento_cartao_final CHECK (cartao_final IS NULL OR cartao_final REGEXP '^[0-9]{4}$'),
    CONSTRAINT ck_pagamento_parcelas CHECK (parcelas IS NULL OR parcelas BETWEEN 1 AND 12)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS vendas (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    passageiro_id BIGINT UNSIGNED NOT NULL,
    voo_id BIGINT UNSIGNED NOT NULL,
    assento_id BIGINT UNSIGNED NOT NULL,
    pagamento_id BIGINT UNSIGNED NOT NULL,
    valor_total DECIMAL(12,2) NOT NULL,
    estado ENUM('CONFIRMADA', 'CANCELADA') NOT NULL DEFAULT 'CONFIRMADA',
    criada_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_venda_passageiro FOREIGN KEY (passageiro_id) REFERENCES passageiros(id),
    CONSTRAINT fk_venda_voo FOREIGN KEY (voo_id) REFERENCES voos(id),
    CONSTRAINT fk_venda_assento FOREIGN KEY (assento_id) REFERENCES assentos(id),
    CONSTRAINT fk_venda_pagamento FOREIGN KEY (pagamento_id) REFERENCES pagamentos(id),
    CONSTRAINT uk_venda_assento UNIQUE (assento_id),
    CONSTRAINT uk_venda_pagamento UNIQUE (pagamento_id),
    CONSTRAINT ck_venda_valor CHECK (valor_total > 0)
) ENGINE=InnoDB;

CREATE INDEX ix_vendas_voo_estado ON vendas (voo_id, estado);
CREATE INDEX ix_vendas_criada_em ON vendas (criada_em);

CREATE TABLE IF NOT EXISTS etickets (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    venda_id BIGINT UNSIGNED NOT NULL,
    numero CHAR(12) NOT NULL,
    caminho_pdf VARCHAR(500) NULL,
    estado_geracao ENUM('PENDENTE', 'GERADO', 'FALHA') NOT NULL DEFAULT 'PENDENTE',
    estado_envio ENUM('PENDENTE', 'ENVIADO', 'FALHA') NOT NULL DEFAULT 'PENDENTE',
    gerado_em DATETIME NULL,
    enviado_em DATETIME NULL,
    mensagem_erro VARCHAR(500) NULL,
    CONSTRAINT fk_eticket_venda FOREIGN KEY (venda_id) REFERENCES vendas(id),
    CONSTRAINT uk_eticket_venda UNIQUE (venda_id),
    CONSTRAINT uk_eticket_numero UNIQUE (numero)
) ENGINE=InnoDB;
