# K&P Saúde Animal — Banco de Dados (PostgreSQL)

Este diretório contém os scripts SQL para provisionamento e inicialização do banco de dados relacional do **K&P Saúde Animal**.

---

## 📋 Pré-requisitos

1. **PostgreSQL 14+** instalado e rodando em seu ambiente ou container Docker.
2. Cliente `psql` disponível no terminal ou ferramenta com interface gráfica (ex: pgAdmin, DBeaver).

---

## 🚀 Passo a Passo para Criar e Inicializar o Banco

### 1. Criar o Banco de Dados no PostgreSQL

Abra o terminal e conecte-se ao PostgreSQL como superusuário:

```bash
psql -U postgres
```

Dentro do prompt do PostgreSQL, execute:

```sql
CREATE DATABASE kpsaude_db WITH OWNER postgres ENCODING 'UTF8';
\q
```

---

### 2. Executar os Scripts de Estrutura e Dados Iniciais

Navegue até a raiz do projeto ou diretório `banco/` e execute:

```bash
# 1. Cria as tabelas, tipos, chaves estrangeiras e índices otimizados
psql -U postgres -d kpsaude_db -f banco/01_estrutura.sql

# 2. Insere os dados iniciais (administrador padrão e pets de exemplo)
psql -U postgres -d kpsaude_db -f banco/02_dados_iniciais.sql
```

Se preferir usar Docker para rodar um container rápido com PostgreSQL:

```bash
docker run --name kpsaude-postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=kpsaude_db -p 5432:5432 -d postgres:16

# Copiar e executar os scripts no container:
docker cp banco/01_estrutura.sql kpsaude-postgres:/01_estrutura.sql
docker cp banco/02_dados_iniciais.sql kpsaude-postgres:/02_dados_iniciais.sql
docker exec -it kpsaude-postgres psql -U postgres -d kpsaude_db -f /01_estrutura.sql
docker exec -it kpsaude-postgres psql -U postgres -d kpsaude_db -f /02_dados_iniciais.sql
```

---

## 🔐 Contas Padrão Criadas

| Perfil | E-mail | Senha Padrão | Observação |
|---|---|---|---|
| **Administrador** | `admin@kpsaudeanimal.com.br` | `admin123` | **Obrigatório alterar a senha no primeiro acesso!** |
| **Tutor de Teste** | `amanda.silva@email.com` | `tutor123` | Possui 3 pets cadastrados (Thor, Luna e Mel) |

---

## 🗃️ Estrutura das Tabelas

- **`usuarios`**: Gestão de tutores, veterinários e administradores com hash bcrypt.
- **`pets`**: Dados biométricos, raça, porte, microchip e chave única da coleira K&P.
- **`leituras`**: Série temporal de frequência cardíaca, SpO₂, temperatura, aceleração 3D, GPS e bateria.
- **`prontuario`**: Histórico veterinário, vacinas e exames anexados.
- **`compartilhamentos`**: ACL granular permitindo acesso seguro a cuidadores e clínicas.
