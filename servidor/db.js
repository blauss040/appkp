// ============================================================================
// PROJETO K&P SAÚDE ANIMAL
// Arquivo: servidor/db.js
// Descrição: Gerenciamento do Pool de Conexões com PostgreSQL
// ============================================================================

const { Pool } = require('pg');
require('dotenv').config();

// Configuração do pool de conexões utilizando variáveis de ambiente
const pool = new Pool({
    host: process.env.DB_HOST || 'localhost',
    port: parseInt(process.env.DB_PORT, 10) || 5432,
    database: process.env.DB_NAME || 'kpsaude_db',
    user: process.env.DB_USER || 'postgres',
    password: process.env.DB_PASSWORD || 'postgres',
    max: 20, // Máximo de conexões simultâneas
    idleTimeoutMillis: 30000,
    connectionTimeoutMillis: 5000,
});

// Eventos do Pool para monitoramento
pool.on('connect', () => {
    // Conexão criada com sucesso
});

pool.on('error', (err) => {
    console.error('❌ [K&P Banco] Erro inesperado no pool de conexões do PostgreSQL:', err.message);
});

/**
 * Executa uma query SQL com tratamento de parâmetros e medição de tempo
 * @param {string} text - Instrução SQL
 * @param {Array} params - Parâmetros da query
 * @returns {Promise<any>}
 */
const query = async (text, params) => {
    const start = Date.now();
    try {
        const res = await pool.query(text, params);
        const duration = Date.now() - start;
        if (process.env.NODE_ENV === 'development') {
            // Log opcional para auditoria de queries lentas
            if (duration > 500) {
                console.warn(`⚠️ [K&P Banco] Query lenta (${duration}ms):`, text);
            }
        }
        return res;
    } catch (error) {
        console.error('❌ [K&P Banco] Erro ao executar query:', error.message);
        throw error;
    }
};

module.exports = {
    pool,
    query
};
