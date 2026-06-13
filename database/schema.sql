DROP DATABASE IF EXISTS sistema_requerimento;

CREATE DATABASE sistema_requerimento;

-- Relacionamentos no Banco
/*

curso ──────┐
            ├──► aluno ──► requerimento ──► anexo
usuario ────┘                   ▲
                                │
                    tipo_requerimento

*/


\c sistema_requerimento_completo;
psql -h localhost -U postgres -- Abrir o PSQL no Terminal via LocalHost

CREATE TABLE curso(
    id SERIAL PRIMARY KEY,
    nome VARCHAR(200) NOT NULL,
    site VARCHAR(200) NOT NULL,
    turno VARCHAR(20) CHECK (turno IN ('noturno', 'diurno', 'vespertino')),
    duracao INTEGER CHECK (duracao > 0) -- Duração em Horas
);

CREATE TABLE usuario(
    id SERIAL PRIMARY KEY,
    nome VARCHAR(200) NOT NULL,
    email VARCHAR(200) UNIQUE,
    cpf VARCHAR(11) UNIQUE,
    data_nascimento DATE,
    cep VARCHAR(8),
    complemento TEXT,
    numero VARCHAR(100)
);

CREATE TABLE tipo_requerimento(
    id SERIAL PRIMARY KEY,
    descricao TEXT NOT NULL
);

CREATE TABLE aluno(
    matricula CHAR(10) PRIMARY KEY,
    usuario_id INTEGER REFERENCES usuario(id),
    curso_id INTEGER REFERENCES curso(id)
);

-- Nova Tabela
CREATE TABLE foto_aluno (
    id SERIAL PRIMARY KEY,
    aluno_matricula VARCHAR(20) NOT NULL UNIQUE, -- UNIQUE garante que cada aluno tenha apenas uma foto
    nome_arquivo VARCHAR(255) NOT NULL,
    conteudo_tipo VARCHAR(50) NOT NULL,          -- MIME-type (ex: image/png, image/jpeg)
    arquivo BYTEA NOT NULL,                     -- Os bytes da imagem
    FOREIGN KEY (aluno_matricula) REFERENCES aluno(matricula) ON DELETE CASCADE
);

CREATE TABLE requerimento(
    id SERIAL PRIMARY KEY,
    aluno_matricula CHAR(10) REFERENCES aluno(matricula),
    data_hora_abertura TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status TEXT CHECK (status IN ('em análise', 'indeferido', 'deferido')),
    tipo_requerimento_id INTEGER REFERENCES tipo_requerimento(id)
);

-- Criação da Tabela Filha Herdando de Requerimento
-- 1. Criação da tabela filha herdando de requerimento
CREATE TABLE requerimento_trancamento (
    semestre_retorno VARCHAR(20) NOT NULL
) INHERITS (requerimento);

CREATE TABLE anexo(
    id SERIAL PRIMARY KEY,
    descricao TEXT NOT NULL,
    arquivo BYTEA,
    requerimento_id INTEGER REFERENCES requerimento(id)
);

-- Substituição no banco
-- Trocando TEXT por JSONB
-- Aqui estamos convertendo o tipo TEXT para JSONB de forma definitiva
-- transformando todas as strings antigas que estavam lá em objetos JSON reais.
ALTER TABLE anexo
ALTER COLUMN descricao TYPE JSONB USING descricao::jsonb;

-- Correções de Bugs
-- 1. Garante a remoção da restrição na tabela filha de trancamento
ALTER TABLE requerimento DROP CONSTRAINT requerimento_status_check CASCADE;

-- ========================
-- DCL
-- =========================
CREATE USER usuario_admin WITH PASSWORD '123';
CREATE USER usuario_leitura WITH PASSWORD '123';

-- Admin ganha tudo
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO usuario_admin;

-- Leitura ganha apenas SELECT
GRANT SELECT ON ALL TABLES IN SCHEMA public TO usuario_leitura;

-- ========================
-- INSERÇÃO NAS TABELAS
-- =========================usua

-- =========================
-- CURSOS
-- =========================
INSERT INTO curso (nome, site, turno, duracao) VALUES
('Sistemas de Informação', 'https://si.exemplo.edu.br', 'noturno', 3000),
('Administração', 'https://adm.exemplo.edu.br', 'diurno', 2800),
('Direito', 'https://dir.exemplo.edu.br', 'vespertino', 4000);


-- =========================
-- USUÁRIOS
-- =========================
INSERT INTO usuario (nome, email, cpf, data_nascimento, cep, complemento, numero) VALUES
('João Silva', 'joao@email.com', '12345678901', '2000-05-10', '12345678', 'Apto 101', '100'), -- ID 1
('Maria Oliveira', 'maria@email.com', '98765432100', '1999-08-22', '87654321', 'Casa', '200'), -- ID 2
('Carlos Souza', 'carlos@email.com', '45678912300', '2001-01-15', '11223344', 'Bloco B', '300'); -- ID 3


-- =========================
-- TIPOS DE REQUERIMENTO
-- =========================
INSERT INTO tipo_requerimento (descricao) VALUES
('Declaração de Matrícula'),
('Aproveitamento de Disciplina'),
('Trancamento de Curso');


-- =========================
-- ALUNOS
-- =========================
INSERT INTO aluno (matricula, usuario_id, curso_id) VALUES
('2024000001', 1, 1), -- João
('2024000002', 2, 2), -- Maria
('2024000003', 3, 1); -- Carlos


-- =========================
-- REQUERIMENTOS
-- =========================
INSERT INTO requerimento (aluno_matricula, status, tipo_requerimento_id) VALUES
('2024000001', 'em análise', 1),
('2024000002', 'deferido', 2),
('2024000003', 'indeferido', 3);


-- =========================
-- ANEXOS
-- =========================
INSERT INTO anexo (descricao, arquivo, requerimento_id) VALUES
('Documento RG', NULL, 1),
('Histórico Escolar', NULL, 2),
('Comprovante de Pagamento', NULL, 3);


-- =========================
-- Consultas
-- =========================

-- Listar todos os requerimentos com nome do aluno
SELECT 
    usuario.nome AS aluno_nome,
    requerimento.id AS requerimento_id,
    requerimento.status,
    requerimento.data_hora_abertura
FROM requerimento
INNER JOIN aluno 
    ON aluno.matricula = requerimento.aluno_matricula
INNER JOIN usuario 
    ON usuario.id = aluno.usuario_id;

-- Listar requerimentos com descrição do tipo
SELECT 
    requerimento.id AS requerimento_codigo,
    tipo_requerimento.descricao AS descricao_requerimento
FROM requerimento
LEFT JOIN tipo_requerimento
    ON tipo_requerimento.id = requerimento.tipo_requerimento_id;


-- Listar requerimentos com nome do aluno e tipo
SELECT
    usuario.nome,
    tipo_requerimento.descricao
FROM usuario
INNER JOIN aluno
    ON usuario.id = aluno.usuario_id
INNER JOIN requerimento
    ON aluno.matricula = requerimento.aluno_matricula
INNER JOIN tipo_requerimento
    ON tipo_requerimento.id = requerimento.tipo_requerimento_id;