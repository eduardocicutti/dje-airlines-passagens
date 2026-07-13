-- DJE Airlines - dados iniciais fornecidos pelo professor
-- Execute depois de schema.sql.

USE dje_airlines;

INSERT INTO aeroportos (codigo_iata, cidade, nome) VALUES
('POA', 'Porto Alegre', 'Aeroporto Internacional Salgado Filho'),
('CGH', 'São Paulo', 'Aeroporto de Congonhas'),
('GIG', 'Rio de Janeiro', 'Aeroporto Internacional Tom Jobim'),
('FOR', 'Fortaleza', 'Aeroporto Internacional de Fortaleza'),
('BSB', 'Brasília', 'Aeroporto Internacional de Brasília'),
('FLN', 'Florianópolis', 'Aeroporto Internacional de Florianópolis'),
('CNF', 'Belo Horizonte', 'Aeroporto Internacional de Confins'),
('VIX', 'Vitória', 'Aeroporto de Vitória')
ON DUPLICATE KEY UPDATE cidade = VALUES(cidade), nome = VALUES(nome), ativo = TRUE;

-- As seis distâncias abaixo reproduzem exatamente o PDF do professor.
-- Cada trecho é cadastrado nos dois sentidos.
INSERT INTO rotas (origem_id, destino_id, distancia_milhas)
SELECT o.id, d.id, dados.milhas
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
JOIN aeroportos o ON o.codigo_iata = dados.origem
JOIN aeroportos d ON d.codigo_iata = dados.destino
ON DUPLICATE KEY UPDATE distancia_milhas = VALUES(distancia_milhas), ativa = TRUE;

INSERT INTO funcionarios (matricula, nome, ativo) VALUES
('1001', 'Funcionário de Demonstração', TRUE)
ON DUPLICATE KEY UPDATE nome = VALUES(nome), ativo = TRUE;

-- Voos futuros de demonstração. As datas são relativas para o seed continuar utilizável.
INSERT INTO voos (codigo, data_voo, horario, portao, capacidade, estado) VALUES
('DJE101', DATE_ADD(CURDATE(), INTERVAL 7 DAY), '08:30:00', 'A1', 60, 'DISPONIVEL'),
('DJE202', DATE_ADD(CURDATE(), INTERVAL 12 DAY), '14:00:00', 'B3', 60, 'DISPONIVEL'),
('DJE303', DATE_ADD(CURDATE(), INTERVAL 21 DAY), '18:30:00', 'C2', 60, 'DISPONIVEL')
ON DUPLICATE KEY UPDATE horario = VALUES(horario), portao = VALUES(portao), capacidade = VALUES(capacidade);

-- DJE101: FLN -> CGH -> BSB (com conexão em São Paulo).
INSERT INTO voo_trechos (voo_id, rota_id, ordem)
SELECT v.id, r.id, dados.ordem
FROM (
    SELECT 'DJE101' codigo, 'FLN' origem, 'CGH' destino, 1 ordem UNION ALL
    SELECT 'DJE101', 'CGH', 'BSB', 2 UNION ALL
    SELECT 'DJE202', 'CGH', 'VIX', 1 UNION ALL
    SELECT 'DJE303', 'CGH', 'FOR', 1
) dados
JOIN voos v ON v.codigo = dados.codigo
JOIN aeroportos o ON o.codigo_iata = dados.origem
JOIN aeroportos d ON d.codigo_iata = dados.destino
JOIN rotas r ON r.origem_id = o.id AND r.destino_id = d.id
ON DUPLICATE KEY UPDATE rota_id = VALUES(rota_id);

-- Gera A1 até J6 para cada voo de 60 lugares, sem duplicar em nova execução.
INSERT INTO assentos (voo_id, codigo, estado)
SELECT v.id,
       CONCAT(CHAR(65 + FLOOR(n.valor / 6)), 1 + MOD(n.valor, 6)),
       'DISPONIVEL'
FROM voos v
JOIN (
    SELECT unidade.n + dezena.n * 10 AS valor
    FROM
      (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
       UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) unidade
    CROSS JOIN
      (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
       UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) dezena
) n ON n.valor < v.capacidade
ON DUPLICATE KEY UPDATE codigo = VALUES(codigo);
