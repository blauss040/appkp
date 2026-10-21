// ============================================================================
// PROJETO K&P SAÚDE ANIMAL
// Arquivo: servidor/index.cjs
// Descrição: Servidor Express completo com rotas de autenticação, telemetria,
//            gestão de pets, ACL de compartilhamento e assistente Sayori.
// ============================================================================

const express = require('express');
const cors = require('cors');
const path = require('path');
const jwt = require('jsonwebtoken');
const bcrypt = require('bcryptjs');
require('dotenv').config();

const db = require('./db.js');

const app = express();
const PORT = process.env.PORT || 9000;
const JWT_SECRET = process.env.JWT_SECRET || 'kp_saude_animal_jwt_secret_token_chave_privada_2026';

// ----------------------------------------------------------------------------
// Middlewares Globais
// ----------------------------------------------------------------------------
app.use(cors());
app.use(express.json());

// Servir arquivos estáticos da pasta public (Frontend Web)
app.use(express.static(path.join(__dirname, '..', 'public')));

// ----------------------------------------------------------------------------
// Middleware de Autenticação JWT
// ----------------------------------------------------------------------------
const autenticarToken = (req, res, next) => {
    const authHeader = req.headers['authorization'];
    const token = authHeader && authHeader.split(' ')[1]; // Formato: Bearer <TOKEN>

    if (!token) {
        return res.status(401).json({
            sucesso: false,
            erro: 'Acesso negado. Token de autenticação não fornecido.'
        });
    }

    jwt.verify(token, JWT_SECRET, (err, usuarioDecodificado) => {
        if (err) {
            return res.status(403).json({
                sucesso: false,
                erro: 'Token inválido ou expirado. Faça login novamente.'
            });
        }
        req.usuario = usuarioDecodificado;
        next();
    });
};

// ----------------------------------------------------------------------------
// 1. ROTAS DE AUTENTICAÇÃO
// ----------------------------------------------------------------------------

/**
 * POST /api/auth/login
 * Realiza autenticação com e-mail e senha e retorna o token JWT
 */
app.post('/api/auth/login', async (req, res) => {
    try {
        const { email, senha } = req.body;

        if (!email || !senha) {
            return res.status(400).json({
                sucesso: false,
                erro: 'E-mail e senha são obrigatórios.'
            });
        }

        // Busca o usuário no banco de dados
        const resultado = await db.query(
            'SELECT id, nome, email, senha, telefone, endereco, perfil, ativo FROM usuarios WHERE email = $1',
            [email.toLowerCase().trim()]
        );

        if (resultado.rows.length === 0) {
            return res.status(401).json({
                sucesso: false,
                erro: 'Credenciais inválidas. Verifique seu e-mail e senha.'
            });
        }

        const usuario = resultado.rows[0];

        if (!usuario.ativo) {
            return res.status(403).json({
                sucesso: false,
                erro: 'Usuário desativado. Entre em contato com o suporte da K&P.'
            });
        }

        // Valida hash da senha com bcrypt
        const senhaValida = await bcrypt.compare(senha, usuario.senha);
        if (!senhaValida) {
            return res.status(401).json({
                sucesso: false,
                erro: 'Credenciais inválidas. Verifique seu e-mail e senha.'
            });
        }

        // Gera token JWT assinado
        const token = jwt.sign(
            {
                id: usuario.id,
                nome: usuario.nome,
                email: usuario.email,
                perfil: usuario.perfil
            },
            JWT_SECRET,
            { expiresIn: process.env.JWT_EXPIRES_IN || '7d' }
        );

        res.json({
            sucesso: true,
            mensagem: 'Login realizado com sucesso.',
            dados: {
                token,
                usuario: {
                    id: usuario.id,
                    nome: usuario.nome,
                    email: usuario.email,
                    telefone: usuario.telefone,
                    endereco: usuario.endereco,
                    perfil: usuario.perfil
                }
            }
        });
    } catch (err) {
        console.error('Erro no login:', err);
        res.status(500).json({
            sucesso: false,
            erro: 'Erro interno ao processar autenticação.'
        });
    }
});

/**
 * GET /api/auth/perfil
 * Retorna os dados completos do usuário autenticado
 */
app.get('/api/auth/perfil', autenticarToken, async (req, res) => {
    try {
        const resultado = await db.query(
            'SELECT id, nome, email, telefone, endereco, perfil, created_at FROM usuarios WHERE id = $1',
            [req.usuario.id]
        );

        if (resultado.rows.length === 0) {
            return res.status(404).json({
                sucesso: false,
                erro: 'Usuário não encontrado.'
            });
        }

        res.json({
            sucesso: true,
            dados: resultado.rows[0],
            mensagem: 'Perfil recuperado com sucesso.'
        });
    } catch (err) {
        console.error('Erro ao buscar perfil:', err);
        res.status(500).json({
            sucesso: false,
            erro: 'Erro interno ao recuperar dados do perfil.'
        });
    }
});

// ----------------------------------------------------------------------------
// 2. ROTAS DE COLEIRA E TELEMETRIA
// ----------------------------------------------------------------------------

/**
 * POST /api/coleira/dados
 * Endpoint de ingestão de telemetria das coleiras inteligentes K&P
 * Recebe batimento, SpO2, temperatura, aceleração, GPS e bateria
 */
app.post('/api/coleira/dados', async (req, res) => {
    try {
        const {
            coleira_chave,
            pet_id,
            batimento,
            spo2,
            temperatura,
            acel_x,
            acel_y,
            acel_z,
            latitude,
            longitude,
            bateria
        } = req.body;

        if (!batimento || !spo2 || !temperatura) {
            return res.status(400).json({
                sucesso: false,
                erro: 'Sinais vitais obrigatórios ausentes (batimento, spo2, temperatura).'
            });
        }

        // Identifica o pet por chave de coleira ou por ID
        let targetPetId = pet_id;
        if (!targetPetId && coleira_chave) {
            const petBusca = await db.query(
                'SELECT id FROM pets WHERE coleira_chave = $1',
                [coleira_chave]
            );
            if (petBusca.rows.length > 0) {
                targetPetId = petBusca.rows[0].id;
            }
        }

        if (!targetPetId) {
            return res.status(404).json({
                sucesso: false,
                erro: 'Pet não identificado para esta coleira ou identificador fornecido.'
            });
        }

        // Insere a leitura na série temporal
        const insereLeitura = await db.query(
            `INSERT INTO leituras 
                (pet_id, batimento, spo2, temperatura, acel_x, acel_y, acel_z, latitude, longitude, bateria, recebido_em)
             VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, CURRENT_TIMESTAMP)
             RETURNING id, pet_id, batimento, spo2, temperatura, latitude, longitude, bateria, recebido_em`,
            [
                targetPetId,
                parseInt(batimento, 10),
                parseInt(spo2, 10),
                parseFloat(temperatura),
                acel_x || 0,
                acel_y || 0,
                acel_z || 0,
                latitude || null,
                longitude || null,
                bateria !== undefined ? parseInt(bateria, 10) : null
            ]
        );

        // Análise de status automático do pet
        let novoStatus = 'normal';
        if (temperatura >= 39.5 || batimento > 140 || spo2 < 93) {
            novoStatus = 'alerta';
        } else if (temperatura >= 38.2 || batimento > 115 || spo2 < 96) {
            novoStatus = 'atencao';
        }

        await db.query(
            'UPDATE pets SET status = $1 WHERE id = $2',
            [novoStatus, targetPetId]
        );

        res.status(201).json({
            sucesso: true,
            mensagem: 'Telemetria da coleira registrada com sucesso.',
            dados: {
                leitura: insereLeitura.rows[0],
                status_pet: novoStatus
            }
        });
    } catch (err) {
        console.error('Erro ao registrar dados da coleira:', err);
        res.status(500).json({
            sucesso: false,
            erro: 'Erro interno ao salvar leitura da coleira.'
        });
    }
});

/**
 * GET /api/coleira/historico/:petId
 * Histórico paginado de leituras de um pet
 */
app.get('/api/coleira/historico/:petId', autenticarToken, async (req, res) => {
    try {
        const { petId } = req.params;
        const limit = Math.min(parseInt(req.query.limit, 10) || 50, 200);
        const offset = parseInt(req.query.offset, 10) || 0;

        const resultado = await db.query(
            `SELECT id, batimento, spo2, temperatura, latitude, longitude, bateria, recebido_em
             FROM leituras
             WHERE pet_id = $1
             ORDER BY recebido_em DESC
             LIMIT $2 OFFSET $3`,
            [petId, limit, offset]
        );

        const totalQuery = await db.query(
            'SELECT COUNT(*) FROM leituras WHERE pet_id = $1',
            [petId]
        );
        const total = parseInt(totalQuery.rows[0].count, 10);

        res.json({
            sucesso: true,
            mensagem: 'Histórico de telemetria recuperado com sucesso.',
            dados: {
                leituras: resultado.rows,
                paginacao: {
                    total,
                    limit,
                    offset
                }
            }
        });
    } catch (err) {
        console.error('Erro ao buscar histórico:', err);
        res.status(500).json({
            sucesso: false,
            erro: 'Erro interno ao consultar histórico da coleira.'
        });
    }
});

/**
 * GET /api/coleira/ultimo/:petId
 * Retorna a leitura mais recente de um pet
 */
app.get('/api/coleira/ultimo/:petId', autenticarToken, async (req, res) => {
    try {
        const { petId } = req.params;

        const resultado = await db.query(
            `SELECT id, batimento, spo2, temperatura, acel_x, acel_y, acel_z, latitude, longitude, bateria, recebido_em
             FROM leituras
             WHERE pet_id = $1
             ORDER BY recebido_em DESC
             LIMIT 1`,
            [petId]
        );

        if (resultado.rows.length === 0) {
            return res.json({
                sucesso: true,
                dados: null,
                mensagem: 'Nenhuma leitura registrada para este pet até o momento.'
            });
        }

        res.json({
            sucesso: true,
            dados: resultado.rows[0],
            mensagem: 'Última telemetria encontrada.'
        });
    } catch (err) {
        console.error('Erro ao buscar última leitura:', err);
        res.status(500).json({
            sucesso: false,
            erro: 'Erro interno ao buscar última leitura da coleira.'
        });
    }
});

// ----------------------------------------------------------------------------
// 3. ROTAS DE GESTÃO DE PETS
// ----------------------------------------------------------------------------

/**
 * GET /api/pets
 * Lista todos os pets do tutor logado ou compartilhados com ele
 */
app.get('/api/pets', autenticarToken, async (req, res) => {
    try {
        const usuarioId = req.usuario.id;

        const resultado = await db.query(
            `SELECT DISTINCT p.*, 
                    COALESCE(c.permissao, 'proprietario') as permissao_acesso,
                    (
                        SELECT json_build_object(
                            'batimento', l.batimento,
                            'spo2', l.spo2,
                            'temperatura', l.temperatura,
                            'bateria', l.bateria,
                            'latitude', l.latitude,
                            'longitude', l.longitude,
                            'recebido_em', l.recebido_em
                        )
                        FROM leituras l
                        WHERE l.pet_id = p.id
                        ORDER BY l.recebido_em DESC
                        LIMIT 1
                    ) as ultima_leitura
             FROM pets p
             LEFT JOIN compartilhamentos c ON c.pet_id = p.id AND c.usuario_id = $1 AND c.ativo = TRUE
             WHERE p.tutor_id = $1 OR c.usuario_id = $1
             ORDER BY p.created_at DESC`,
            [usuarioId]
        );

        res.json({
            sucesso: true,
            dados: resultado.rows,
            mensagem: 'Lista de pets recuperada com sucesso.'
        });
    } catch (err) {
        console.error('Erro ao listar pets:', err);
        res.status(500).json({
            sucesso: false,
            erro: 'Erro interno ao consultar lista de pets.'
        });
    }
});

/**
 * POST /api/pets
 * Cadastra um novo pet vinculado ao tutor autenticado
 */
app.post('/api/pets', autenticarToken, async (req, res) => {
    try {
        const tutorId = req.usuario.id;
        const { nome, raca, porte, idade, peso, cor, chip, coleira_chave } = req.body;

        if (!nome) {
            return res.status(400).json({
                sucesso: false,
                erro: 'O nome do pet é obrigatório.'
            });
        }

        const resultado = await db.query(
            `INSERT INTO pets (tutor_id, nome, raca, porte, idade, peso, cor, chip, coleira_chave, status)
             VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, 'normal')
             RETURNING *`,
            [tutorId, nome, raca || null, porte || 'medio', idade || null, peso || null, cor || null, chip || null, coleira_chave || null]
        );

        res.status(201).json({
            sucesso: true,
            mensagem: 'Pet cadastrado com sucesso!',
            dados: resultado.rows[0]
        });
    } catch (err) {
        if (err.code === '23505' && err.constraint === 'pets_coleira_chave_key') {
            return res.status(409).json({
                sucesso: false,
                erro: 'Esta chave de coleira já está vinculada a outro pet.'
            });
        }
        console.error('Erro ao cadastrar pet:', err);
        res.status(500).json({
            sucesso: false,
            erro: 'Erro interno ao cadastrar pet.'
        });
    }
});

/**
 * GET /api/pets/:id
 * Detalhes completos de um pet específico
 */
app.get('/api/pets/:id', autenticarToken, async (req, res) => {
    try {
        const { id } = req.params;
        const usuarioId = req.usuario.id;

        const resultado = await db.query(
            `SELECT p.*, u.nome as tutor_nome, u.telefone as tutor_telefone
             FROM pets p
             JOIN usuarios u ON u.id = p.tutor_id
             LEFT JOIN compartilhamentos c ON c.pet_id = p.id AND c.usuario_id = $2 AND c.ativo = TRUE
             WHERE p.id = $1 AND (p.tutor_id = $2 OR c.usuario_id = $2)`,
            [id, usuarioId]
        );

        if (resultado.rows.length === 0) {
            return res.status(404).json({
                sucesso: false,
                erro: 'Pet não encontrado ou você não possui permissão de acesso.'
            });
        }

        // Busca última leitura
        const leituraQuery = await db.query(
            'SELECT * FROM leituras WHERE pet_id = $1 ORDER BY recebido_em DESC LIMIT 1',
            [id]
        );

        res.json({
            sucesso: true,
            dados: {
                ...resultado.rows[0],
                ultima_leitura: leituraQuery.rows[0] || null
            },
            mensagem: 'Detalhes do pet recuperados com sucesso.'
        });
    } catch (err) {
        console.error('Erro ao buscar pet:', err);
        res.status(500).json({
            sucesso: false,
            erro: 'Erro interno ao recuperar detalhes do pet.'
        });
    }
});

/**
 * PUT /api/pets/:id
 * Atualiza os dados de um pet
 */
app.put('/api/pets/:id', autenticarToken, async (req, res) => {
    try {
        const { id } = req.params;
        const tutorId = req.usuario.id;
        const { nome, raca, porte, idade, peso, cor, chip, coleira_chave, status } = req.body;

        const resultado = await db.query(
            `UPDATE pets 
             SET nome = COALESCE($1, nome),
                 raca = COALESCE($2, raca),
                 porte = COALESCE($3, porte),
                 idade = COALESCE($4, idade),
                 peso = COALESCE($5, peso),
                 cor = COALESCE($6, cor),
                 chip = COALESCE($7, chip),
                 coleira_chave = COALESCE($8, coleira_chave),
                 status = COALESCE($9, status)
             WHERE id = $10 AND tutor_id = $11
             RETURNING *`,
            [nome, raca, porte, idade, peso, cor, chip, coleira_chave, status, id, tutorId]
        );

        if (resultado.rows.length === 0) {
            return res.status(404).json({
                sucesso: false,
                erro: 'Pet não encontrado ou permissão insuficiente para alteração.'
            });
        }

        res.json({
            sucesso: true,
            mensagem: 'Dados do pet atualizados com sucesso.',
            dados: resultado.rows[0]
        });
    } catch (err) {
        console.error('Erro ao atualizar pet:', err);
        res.status(500).json({
            sucesso: false,
            erro: 'Erro interno ao atualizar dados do pet.'
        });
    }
});

/**
 * DELETE /api/pets/:id
 * Remove um pet do sistema
 */
app.delete('/api/pets/:id', autenticarToken, async (req, res) => {
    try {
        const { id } = req.params;
        const tutorId = req.usuario.id;

        const resultado = await db.query(
            'DELETE FROM pets WHERE id = $1 AND tutor_id = $2 RETURNING id',
            [id, tutorId]
        );

        if (resultado.rows.length === 0) {
            return res.status(404).json({
                sucesso: false,
                erro: 'Pet não encontrado ou permissão insuficiente para exclusão.'
            });
        }

        res.json({
            sucesso: true,
            mensagem: 'Pet removido com sucesso.',
            dados: { id: resultado.rows[0].id }
        });
    } catch (err) {
        console.error('Erro ao remover pet:', err);
        res.status(500).json({
            sucesso: false,
            erro: 'Erro interno ao excluir pet.'
        });
    }
});

// ----------------------------------------------------------------------------
// 4. ROTAS DE COMPARTILHAMENTO / ACL
// ----------------------------------------------------------------------------

/**
 * POST /api/compartilhar
 * Concede acesso aos dados do pet para outro usuário (veterinário, cuidador, familiar)
 */
app.post('/api/compartilhar', autenticarToken, async (req, res) => {
    try {
        const tutorId = req.usuario.id;
        const { pet_id, email_destino, permissao } = req.body;

        if (!pet_id || !email_destino) {
            return res.status(400).json({
                sucesso: false,
                erro: 'ID do pet e e-mail do destinatário são obrigatórios.'
            });
        }

        // Valida se o pet pertence ao tutor autenticado
        const petCheck = await db.query(
            'SELECT id FROM pets WHERE id = $1 AND tutor_id = $2',
            [pet_id, tutorId]
        );
        if (petCheck.rows.length === 0) {
            return res.status(403).json({
                sucesso: false,
                erro: 'Você só pode compartilhar pets dos quais é tutor.'
            });
        }

        // Busca o usuário destinatário pelo e-mail
        const userDestino = await db.query(
            'SELECT id, nome, email, perfil FROM usuarios WHERE email = $1',
            [email_destino.toLowerCase().trim()]
        );
        if (userDestino.rows.length === 0) {
            return res.status(404).json({
                sucesso: false,
                erro: 'Nenhum usuário encontrado com o e-mail informado.'
            });
        }

        const destinatarioId = userDestino.rows[0].id;
        const nivelPermissao = permissao || 'leitura';

        const resultado = await db.query(
            `INSERT INTO compartilhamentos (pet_id, usuario_id, permissao, ativo)
             VALUES ($1, $2, $3, TRUE)
             ON CONFLICT (pet_id, usuario_id) 
             DO UPDATE SET permissao = EXCLUDED.permissao, ativo = TRUE, created_at = CURRENT_TIMESTAMP
             RETURNING *`,
            [pet_id, destinatarioId, nivelPermissao]
        );

        res.status(201).json({
            sucesso: true,
            mensagem: `Acesso compartilhado com sucesso para ${userDestino.rows[0].nome}.`,
            dados: {
                compartilhamento: resultado.rows[0],
                usuario_compartilhado: userDestino.rows[0]
            }
        });
    } catch (err) {
        console.error('Erro ao compartilhar pet:', err);
        res.status(500).json({
            sucesso: false,
            erro: 'Erro interno ao registrar compartilhamento.'
        });
    }
});

/**
 * GET /api/compartilhar/:petId
 * Lista todos os usuários que possuem acesso compartilhado a um pet
 */
app.get('/api/compartilhar/:petId', autenticarToken, async (req, res) => {
    try {
        const { petId } = req.params;
        const usuarioId = req.usuario.id;

        const resultado = await db.query(
            `SELECT c.id, c.pet_id, c.usuario_id, c.permissao, c.ativo, c.created_at,
                    u.nome, u.email, u.perfil, u.telefone
             FROM compartilhamentos c
             JOIN usuarios u ON u.id = c.usuario_id
             JOIN pets p ON p.id = c.pet_id
             WHERE c.pet_id = $1 AND (p.tutor_id = $2 OR c.usuario_id = $2)
             ORDER BY c.created_at DESC`,
            [petId, usuarioId]
        );

        res.json({
            sucesso: true,
            dados: resultado.rows,
            mensagem: 'Lista de acessos compartilhados recuperada com sucesso.'
        });
    } catch (err) {
        console.error('Erro ao consultar compartilhamentos:', err);
        res.status(500).json({
            sucesso: false,
            erro: 'Erro interno ao consultar permissões do pet.'
        });
    }
});

/**
 * DELETE /api/compartilhar/:id
 * Revoga um compartilhamento ativo
 */
app.delete('/api/compartilhar/:id', autenticarToken, async (req, res) => {
    try {
        const { id } = req.params;
        const tutorId = req.usuario.id;

        // Permite revogação se for o tutor dono do pet ou o próprio usuário que recebeu o acesso
        const resultado = await db.query(
            `DELETE FROM compartilhamentos c
             USING pets p
             WHERE c.id = $1 AND c.pet_id = p.id AND (p.tutor_id = $2 OR c.usuario_id = $2)
             RETURNING c.id`,
            [id, tutorId]
        );

        if (resultado.rows.length === 0) {
            return res.status(404).json({
                sucesso: false,
                erro: 'Compartilhamento não encontrado ou permissão insuficiente para revogar.'
            });
        }

        res.json({
            sucesso: true,
            mensagem: 'Permissão de acesso revogada com sucesso.',
            dados: { id: resultado.rows[0].id }
        });
    } catch (err) {
        console.error('Erro ao revogar compartilhamento:', err);
        res.status(500).json({
            sucesso: false,
            erro: 'Erro interno ao revogar compartilhamento.'
        });
    }
});

// ----------------------------------------------------------------------------
// 5. ROTA SAYORI — DIAGNÓSTICO E ORIENTAÇÃO VETERINÁRIA INTELIGENTE
// ----------------------------------------------------------------------------

/**
 * POST /api/sayori/diagnostico
 * Recebe petId, sinais vitais ou descrição de sintomas e gera uma orientação
 * clara, carinhosa e fundamentada em cuidados veterinários.
 */
app.post('/api/sayori/diagnostico', autenticarToken, async (req, res) => {
    try {
        const { pet_id, sintomas, batimento, temperatura, spo2 } = req.body;

        let petNome = 'seu pet';
        let dadosVitais = {
            batimento: batimento || null,
            temperatura: temperatura || null,
            spo2: spo2 || null
        };

        // Se pet_id for fornecido, busca dados recentes no banco
        if (pet_id) {
            const petQuery = await db.query('SELECT nome, raca, porte FROM pets WHERE id = $1', [pet_id]);
            if (petQuery.rows.length > 0) {
                petNome = petQuery.rows[0].nome;
            }

            const ultLeitura = await db.query(
                'SELECT batimento, temperatura, spo2 FROM leituras WHERE pet_id = $1 ORDER BY recebido_em DESC LIMIT 1',
                [pet_id]
            );
            if (ultLeitura.rows.length > 0) {
                dadosVitais.batimento = dadosVitais.batimento || ultLeitura.rows[0].batimento;
                dadosVitais.temperatura = dadosVitais.temperatura || ultLeitura.rows[0].temperatura;
                dadosVitais.spo2 = dadosVitais.spo2 || ultLeitura.rows[0].spo2;
            }
        }

        // Lógica de Triagem e Resposta da Sayori
        const s = (sintomas || '').toLowerCase();
        let nivelUrgencia = 'baixo'; // baixo, moderado, urgente
        let orientacao = '';

        if (dadosVitais.temperatura && dadosVitais.temperatura >= 39.5) {
            nivelUrgencia = 'urgente';
            orientacao = `Atenção com a temperatura de ${petNome} (${dadosVitais.temperatura}°C). Valores acima de 39.5°C caracterizam febre alta. Mantenha ${petNome} em local fresco, ofereça água fresca em pequenas doses e procure atendimento veterinário presencial para avaliar a causa. Evite qualquer medicação humana!`;
        } else if (dadosVitais.spo2 && dadosVitais.spo2 < 94) {
            nivelUrgencia = 'urgente';
            orientacao = `A oxigenação de ${petNome} registrou ${dadosVitais.spo2}%, abaixo do padrão seguro de 95%. Observe se há respiração ofegante, gengivas pálidas ou cansaço excessivo. Recomendamos avaliação clínica imediata.`;
        } else if (s.includes('vomito') || s.includes('vômito') || s.includes('diarreia') || s.includes('diarréia')) {
            nivelUrgencia = 'moderado';
            orientacao = `Entendo sua preocupação com ${petNome}. Vômitos ou alterações gastrointestinais exigem observação cuidadosa da hidratação. Mantenha água limpa e suspenda petiscos gordurosos. Se houver mais de 2 episódios no mesmo dia ou presença de sangue, consulte seu veterinário de confiança.`;
        } else if (dadosVitais.temperatura && dadosVitais.temperatura >= 38.2) {
            nivelUrgencia = 'moderado';
            orientacao = `Notei que ${petNome} está com temperatura em ${dadosVitais.temperatura}°C. É uma leve elevação que costuma ocorrer após brincadeiras, exercícios ou em dias mais quentes. Continue acompanhando os sinais vitais pela coleira K&P nas próximas horas.`;
        } else {
            orientacao = `Analisei os parâmetros de ${petNome} e os sinais vitais aparentam estabilidade. Batimentos: ${dadosVitais.batimento || '72'} BPM, SpO₂: ${dadosVitais.spo2 || '98'}%, Temperatura: ${dadosVitais.temperatura || '38.0'}°C. Lembre-se de manter a rotina de água fresca, passeios nos horários mais amenos e carinho constante! 🐾`;
        }

        res.json({
            sucesso: true,
            mensagem: 'Orientação gerada com sucesso pela Sayori.',
            dados: {
                pet: petNome,
                nivel_urgencia: nivelUrgencia,
                sinais_analisados: dadosVitais,
                orientacao,
                timestamp: new Date().toISOString()
            }
        });
    } catch (err) {
        console.error('Erro na Sayori:', err);
        res.status(500).json({
            sucesso: false,
            erro: 'Erro interno ao processar diagnóstico com a Sayori.'
        });
    }
});

// ----------------------------------------------------------------------------
// Healthcheck & Inicialização
// ----------------------------------------------------------------------------
app.get('/api/health', (req, res) => {
    res.json({
        sucesso: true,
        dados: {
            sistema: 'K&P Saúde Animal',
            versao: '1.0.0',
            status: 'online',
            porta: PORT,
            timestamp: new Date().toISOString()
        },
        mensagem: 'Servidor K&P Saúde Animal operacional.'
    });
});

// Inicialização do servidor HTTP
app.listen(PORT, () => {
    console.log(`====================================================`);
    console.log(`🐾 [K&P Saúde Animal] Servidor rodando na porta ${PORT}`);
    console.log(`🌐 Acesse o painel web em: http://localhost:${PORT}`);
    console.log(`🚀 API Base: http://localhost:${PORT}/api`);
    console.log(`====================================================`);
});
