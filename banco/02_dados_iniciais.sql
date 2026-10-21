-- ============================================================================
-- PROJETO K&P SAÚDE ANIMAL
-- Arquivo: banco/02_dados_iniciais.sql
-- Descrição: Dados iniciais para inicialização do sistema K&P
-- ============================================================================

-- ATENÇÃO DE SEGURANÇA:
-- O hash abaixo corresponde à senha temporária 'admin123' gerada com bcrypt (10 rounds).
-- ORIENTAÇÃO OBRIGATÓRIA: Altere imediatamente a senha do usuário administrador
-- em ambiente de produção após o primeiro login!

-- 1. Criação do Usuário Administrador padrão
INSERT INTO usuarios (id, nome, email, senha, telefone, endereco, perfil, ativo)
VALUES (
    'a0000000-0000-0000-0000-000000000001',
    'Administrador K&P',
    'admin@kpsaudeanimal.com.br',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- hash para 'admin123'
    '(11) 99999-0000',
    'Av. Paulista, 1000 - São Paulo, SP',
    'admin',
    TRUE
)
ON CONFLICT (email) DO NOTHING;

-- 2. Criação de um Tutor de Demonstração
-- Senha temporária: 'tutor123' ($2a$10$wE8wUuN431Qz09aT2B0iV.9Kiq7H9oH2uXy1q3dGkC3t9wS6j7l5m)
INSERT INTO usuarios (id, nome, email, senha, telefone, endereco, perfil, ativo)
VALUES (
    'b0000000-0000-0000-0000-000000000001',
    'Amanda Silva',
    'amanda.silva@email.com',
    '$2a$10$wE8wUuN431Qz09aT2B0iV.9Kiq7H9oH2uXy1q3dGkC3t9wS6j7l5m',
    '(11) 98765-4321',
    'Rua das Acácias, 240 - Jardim Europa, São Paulo - SP',
    'tutor',
    TRUE
)
ON CONFLICT (email) DO NOTHING;

-- 3. Cadastro dos Pets Iniciais vinculados à Amanda Silva
INSERT INTO pets (id, tutor_id, nome, raca, porte, idade, peso, cor, chip, coleira_chave, status)
VALUES
(
    'c0000000-0000-0000-0000-000000000001',
    'b0000000-0000-0000-0000-000000000001',
    'Thor',
    'Golden Retriever',
    'grande',
    '3 anos',
    28.50,
    'Dourado',
    '981098102938475',
    'KP-COLLAR-8921',
    'atencao'
),
(
    'c0000000-0000-0000-0000-000000000002',
    'b0000000-0000-0000-0000-000000000001',
    'Luna',
    'Siamês',
    'pequeno',
    '2 anos',
    4.20,
    'Branco e Cinza',
    '981098102938476',
    'KP-COLLAR-3341',
    'normal'
),
(
    'c0000000-0000-0000-0000-000000000003',
    'b0000000-0000-0000-0000-000000000001',
    'Mel',
    'Spitz Alemão',
    'pequeno',
    '4 anos',
    5.80,
    'Laranja',
    '981098102938477',
    'KP-COLLAR-7712',
    'normal'
)
ON CONFLICT DO NOTHING;

-- 4. Primeiras Leituras de Telemetria das Coleiras
INSERT INTO leituras (pet_id, batimento, spo2, temperatura, acel_x, acel_y, acel_z, latitude, longitude, bateria, recebido_em)
VALUES
(
    'c0000000-0000-0000-0000-000000000001', -- Thor
    72,
    98,
    38.2,
    0.012,
    0.045,
    0.981,
    -23.5616840,
    -46.6559810,
    88,
    CURRENT_TIMESTAMP - INTERVAL '2 minutes'
),
(
    'c0000000-0000-0000-0000-000000000002', -- Luna
    110,
    99,
    38.4,
    0.005,
    0.010,
    0.995,
    -23.5617200,
    -46.6559200,
    94,
    CURRENT_TIMESTAMP - INTERVAL '5 minutes'
),
(
    'c0000000-0000-0000-0000-000000000003', -- Mel
    92,
    98,
    38.1,
    0.020,
    0.030,
    0.975,
    -23.5616500,
    -46.6560100,
    64,
    CURRENT_TIMESTAMP - INTERVAL '8 minutes'
);
