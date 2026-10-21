-- ============================================================================
-- PROJETO K&P SAÚDE ANIMAL
-- Arquivo: banco/01_estrutura.sql
-- Descrição: Estrutura completa do banco de dados relacional (PostgreSQL)
-- ============================================================================

-- Habilitar extensão para geração de UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ----------------------------------------------------------------------------
-- 1. TABELA: usuarios
-- Armazena tutores, veterinários, cuidadores e administradores
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuarios (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    telefone VARCHAR(30),
    endereco TEXT,
    perfil VARCHAR(30) NOT NULL DEFAULT 'tutor' CHECK (perfil IN ('admin', 'tutor', 'veterinario', 'cuidador')),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------------------------
-- 2. TABELA: pets
-- Animais de estimação monitorados pelo sistema e vinculados às coleiras
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS pets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tutor_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    nome VARCHAR(100) NOT NULL,
    raca VARCHAR(100),
    porte VARCHAR(30) CHECK (porte IN ('pequeno', 'medio', 'grande', 'gigante')),
    idade VARCHAR(50),
    peso NUMERIC(5,2),
    cor VARCHAR(60),
    chip VARCHAR(100),
    coleira_chave VARCHAR(100) UNIQUE,
    status VARCHAR(30) NOT NULL DEFAULT 'normal' CHECK (status IN ('normal', 'atencao', 'alerta')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------------------------
-- 3. TABELA: leituras
-- Telemetria em tempo real enviada pelas coleiras inteligentes K&P
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS leituras (
    id BIGSERIAL PRIMARY KEY,
    pet_id UUID NOT NULL REFERENCES pets(id) ON DELETE CASCADE,
    batimento INTEGER NOT NULL,
    spo2 INTEGER NOT NULL,
    temperatura NUMERIC(4,1) NOT NULL,
    acel_x NUMERIC(6,3),
    acel_y NUMERIC(6,3),
    acel_z NUMERIC(6,3),
    latitude NUMERIC(10,7),
    longitude NUMERIC(10,7),
    bateria INTEGER,
    recebido_em TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------------------------
-- 4. TABELA: prontuario
-- Histórico clínico veterinário, vacinas, exames e ocorrências
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS prontuario (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    pet_id UUID NOT NULL REFERENCES pets(id) ON DELETE CASCADE,
    tipo VARCHAR(50) NOT NULL, -- vacina, consulta, exame, cirurgia, observacao
    titulo VARCHAR(150) NOT NULL,
    descricao TEXT,
    data_evento DATE NOT NULL DEFAULT CURRENT_DATE,
    profissional_id UUID REFERENCES usuarios(id) ON DELETE SET NULL,
    anexo_url VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------------------------
-- 5. TABELA: compartilhamentos (Controle de Acesso - ACL)
-- Permite que tutores compartilhem dados do pet com veterinários ou cuidadores
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS compartilhamentos (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    pet_id UUID NOT NULL REFERENCES pets(id) ON DELETE CASCADE,
    usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    permissao VARCHAR(30) NOT NULL DEFAULT 'leitura' CHECK (permissao IN ('leitura', 'saude', 'completo')),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_pet_usuario UNIQUE (pet_id, usuario_id)
);

-- ----------------------------------------------------------------------------
-- ÍNDICES DE ALTA PERFORMANCE
-- Otimização para consultas de telemetria por pet/data e pets por tutor
-- ----------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_leituras_pet_data ON leituras(pet_id, recebido_em DESC);
CREATE INDEX IF NOT EXISTS idx_pets_tutor ON pets(tutor_id);
CREATE INDEX IF NOT EXISTS idx_pets_coleira_chave ON pets(coleira_chave);
CREATE INDEX IF NOT EXISTS idx_prontuario_pet ON prontuario(pet_id, data_evento DESC);
CREATE INDEX IF NOT EXISTS idx_compartilhamentos_usuario ON compartilhamentos(usuario_id);
