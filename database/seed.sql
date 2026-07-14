USE dje_airlines;

-- Limpeza da base
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE etickets;
TRUNCATE TABLE vendas;
TRUNCATE TABLE pagamentos;
TRUNCATE TABLE passageiros;
TRUNCATE TABLE assentos;
TRUNCATE TABLE voo_trechos;
TRUNCATE TABLE voos;
TRUNCATE TABLE rotas;
TRUNCATE TABLE funcionarios;
TRUNCATE TABLE aeroportos;
SET FOREIGN_KEY_CHECKS = 1;

-- Aeroportos
INSERT INTO aeroportos (codigo_iata, cidade, nome) VALUES
('POA', 'Porto Alegre', 'Aeroporto Internacional Salgado Filho'),
('CGH', 'São Paulo', 'Aeroporto de Congonhas'),
('GIG', 'Rio de Janeiro', 'Aeroporto Internacional Tom Jobim'),
('FOR', 'Fortaleza', 'Aeroporto Internacional de Fortaleza'),
('BSB', 'Brasília', 'Aeroporto Internacional de Brasília'),
('FLN', 'Florianópolis', 'Aeroporto Internacional de Florianópolis'),
('CNF', 'Belo Horizonte', 'Aeroporto Internacional de Confins'),
('VIX', 'Vitória', 'Aeroporto de Vitória');

-- Distâncias do enunciado
INSERT INTO rotas (origem_id, destino_id, distancia_milhas)
SELECT origem.id, destino.id, dados.milhas
FROM (
    SELECT 'FLN' origem, 'CGH' destino, 304 milhas UNION ALL
    SELECT 'CGH', 'FLN', 304 UNION ALL
    SELECT 'CGH', 'CNF', 305 UNION ALL
    SELECT 'CNF', 'CGH', 305 UNION ALL
    SELECT 'CGH', 'VIX', 464 UNION ALL
    SELECT 'VIX', 'CGH', 464 UNION ALL
    SELECT 'CGH', 'FOR', 464 UNION ALL
    SELECT 'FOR', 'CGH', 464 UNION ALL
    SELECT 'CGH', 'BSB', 541 UNION ALL
    SELECT 'BSB', 'CGH', 541 UNION ALL
    SELECT 'FLN', 'BSB', 816 UNION ALL
    SELECT 'BSB', 'FLN', 816
) dados
JOIN aeroportos origem ON origem.codigo_iata = dados.origem
JOIN aeroportos destino ON destino.codigo_iata = dados.destino;

INSERT INTO funcionarios (matricula, nome, ativo)
VALUES ('1001', 'Atendente 1001', TRUE);

-- Itinerários diretos e com conexão
CREATE TEMPORARY TABLE itinerarios (
    id TINYINT UNSIGNED PRIMARY KEY,
    origem CHAR(3) NOT NULL,
    destino CHAR(3) NOT NULL,
    trecho1_origem CHAR(3) NOT NULL,
    trecho1_destino CHAR(3) NOT NULL,
    trecho2_origem CHAR(3),
    trecho2_destino CHAR(3)
);

INSERT INTO itinerarios VALUES
(1,  'BSB', 'CNF', 'BSB', 'CGH', 'CGH', 'CNF'),
(2,  'BSB', 'CGH', 'BSB', 'CGH', NULL,  NULL),
(3,  'BSB', 'FLN', 'BSB', 'FLN', NULL,  NULL),
(4,  'BSB', 'FOR', 'BSB', 'CGH', 'CGH', 'FOR'),
(5,  'BSB', 'VIX', 'BSB', 'CGH', 'CGH', 'VIX'),
(6,  'CNF', 'BSB', 'CNF', 'CGH', 'CGH', 'BSB'),
(7,  'CNF', 'CGH', 'CNF', 'CGH', NULL,  NULL),
(8,  'CNF', 'FLN', 'CNF', 'CGH', 'CGH', 'FLN'),
(9,  'CNF', 'FOR', 'CNF', 'CGH', 'CGH', 'FOR'),
(10, 'CNF', 'VIX', 'CNF', 'CGH', 'CGH', 'VIX'),
(11, 'CGH', 'BSB', 'CGH', 'BSB', NULL,  NULL),
(12, 'CGH', 'CNF', 'CGH', 'CNF', NULL,  NULL),
(13, 'CGH', 'FLN', 'CGH', 'FLN', NULL,  NULL),
(14, 'CGH', 'FOR', 'CGH', 'FOR', NULL,  NULL),
(15, 'CGH', 'VIX', 'CGH', 'VIX', NULL,  NULL),
(16, 'FLN', 'BSB', 'FLN', 'BSB', NULL,  NULL),
(17, 'FLN', 'CNF', 'FLN', 'CGH', 'CGH', 'CNF'),
(18, 'FLN', 'CGH', 'FLN', 'CGH', NULL,  NULL),
(19, 'FLN', 'FOR', 'FLN', 'CGH', 'CGH', 'FOR'),
(20, 'FLN', 'VIX', 'FLN', 'CGH', 'CGH', 'VIX'),
(21, 'FOR', 'BSB', 'FOR', 'CGH', 'CGH', 'BSB'),
(22, 'FOR', 'CNF', 'FOR', 'CGH', 'CGH', 'CNF'),
(23, 'FOR', 'CGH', 'FOR', 'CGH', NULL,  NULL),
(24, 'FOR', 'FLN', 'FOR', 'CGH', 'CGH', 'FLN'),
(25, 'FOR', 'VIX', 'FOR', 'CGH', 'CGH', 'VIX'),
(26, 'VIX', 'BSB', 'VIX', 'CGH', 'CGH', 'BSB'),
(27, 'VIX', 'CNF', 'VIX', 'CGH', 'CGH', 'CNF'),
(28, 'VIX', 'CGH', 'VIX', 'CGH', NULL,  NULL),
(29, 'VIX', 'FLN', 'VIX', 'CGH', 'CGH', 'FLN'),
(30, 'VIX', 'FOR', 'VIX', 'CGH', 'CGH', 'FOR');

CREATE TEMPORARY TABLE horarios (
    serie TINYINT UNSIGNED PRIMARY KEY,
    dias_antecedencia TINYINT UNSIGNED NOT NULL,
    horario TIME NOT NULL
);

INSERT INTO horarios VALUES
(1, 7,  '08:00:00'),
(2, 22, '16:00:00');

-- Dois voos por origem e destino
INSERT INTO voos (codigo, data_voo, horario, portao, capacidade, estado)
SELECT CONCAT('DJE', LPAD(it.id, 2, '0'), h.serie),
       DATE_ADD(CURDATE(), INTERVAL (h.dias_antecedencia + MOD(it.id - 1, 4)) DAY),
       ADDTIME(h.horario, SEC_TO_TIME(MOD(it.id - 1, 3) * 1800)),
       CONCAT(CHAR(65 + MOD(it.id - 1, 3)), 1 + MOD(it.id - 1, 6)),
       60,
       'DISPONIVEL'
FROM itinerarios it
CROSS JOIN horarios h;

-- Primeiro trecho
INSERT INTO voo_trechos (voo_id, rota_id, ordem)
SELECT voo.id, rota.id, 1
FROM itinerarios it
CROSS JOIN horarios h
JOIN voos voo ON voo.codigo = CONCAT('DJE', LPAD(it.id, 2, '0'), h.serie)
JOIN aeroportos origem ON origem.codigo_iata = it.trecho1_origem
JOIN aeroportos destino ON destino.codigo_iata = it.trecho1_destino
JOIN rotas rota ON rota.origem_id = origem.id AND rota.destino_id = destino.id;

-- Segundo trecho
INSERT INTO voo_trechos (voo_id, rota_id, ordem)
SELECT voo.id, rota.id, 2
FROM itinerarios it
CROSS JOIN horarios h
JOIN voos voo ON voo.codigo = CONCAT('DJE', LPAD(it.id, 2, '0'), h.serie)
JOIN aeroportos origem ON origem.codigo_iata = it.trecho2_origem
JOIN aeroportos destino ON destino.codigo_iata = it.trecho2_destino
JOIN rotas rota ON rota.origem_id = origem.id AND rota.destino_id = destino.id
WHERE it.trecho2_origem IS NOT NULL;

-- Assentos A1 a J6
INSERT INTO assentos (voo_id, codigo, estado)
SELECT voo.id,
       CONCAT(CHAR(65 + FLOOR(numero.valor / 6)), 1 + MOD(numero.valor, 6)),
       'DISPONIVEL'
FROM voos voo
JOIN (
    SELECT unidade.n + dezena.n * 10 AS valor
    FROM
      (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
       UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) unidade
    CROSS JOIN
      (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
       UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) dezena
) numero ON numero.valor < voo.capacidade;

DROP TEMPORARY TABLE horarios;
DROP TEMPORARY TABLE itinerarios;
