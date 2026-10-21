# K&P Saúde Animal 🐾

Sistema integrado de monitoramento contínuo de saúde, telemetria fisiológica e localização em tempo real para animais de estimação com coleira inteligente, cerca virtual e assistente veterinária IA (Sayori).

**Endereço Base Oficial:** [kpsaudeanimal.com.br](https://kpsaudeanimal.com.br)  
**Repositório:** [https://github.com/blauss040/appkp](https://github.com/blauss040/appkp)

---

## 🏗️ Estrutura do Projeto

O repositório está organizado como um monorepo contendo toda a base do sistema:

```
appkp/
├── banco/                      # Scripts e instruções do PostgreSQL
│   ├── 01_estrutura.sql        # Tabelas: usuarios, pets, leituras, prontuario, compartilhamentos (ACL)
│   ├── 02_dados_iniciais.sql   # Seed de admin e pets (Thor, Luna, Mel) com leituras
│   └── README.md               # Passo a passo de execução no banco
│
├── servidor/                   # Backend Node.js / Express
│   ├── index.cjs               # Rotas de Autenticação (JWT), Coleira, Pets, ACL e Sayori
│   ├── db.js                   # Pool de conexões PostgreSQL
│   ├── package.json            # Dependências: express, pg, jsonwebtoken, bcryptjs, cors, dotenv
│   └── .env.exemplo            # Modelo de variáveis de ambiente
│
├── public/                     # Frontend Web Base (Clean & Responsivo)
│   ├── index.html              # Tela de Login com validação
│   ├── principal.html          # Dashboard com cards rápidos, sinais vitais e status
│   └── diagnosticos.html       # Chat com a assistente veterinária Sayori
│
└── app/                        # Aplicativo Android Nativo (Kotlin + Jetpack Compose)
    └── src/main/java/com/example/ ...
```

---

## 🚀 Passo a Passo para Rodar o Sistema

Siga os 4 passos abaixo para executar a base completa do sistema:

### 1. Instalar as Dependências do Servidor

Abra o terminal e entre na pasta `servidor/`:

```bash
cd servidor
npm install
```

### 2. Configurar as Variáveis de Ambiente (.env)

Copie o arquivo de exemplo para `.env`:

```bash
cp .env.exemplo .env
```

Edite o arquivo `.env` com os dados do seu PostgreSQL local:

```env
PORT=9000
DB_HOST=localhost
DB_PORT=5432
DB_NAME=kpsaude_db
DB_USER=postgres
DB_PASSWORD=sua_senha_aqui
JWT_SECRET=kp_saude_animal_jwt_secret_token_chave_privada_2026
```

### 3. Criar e Inicializar o Banco de Dados (PostgreSQL)

Crie o banco de dados e execute os dois scripts SQL:

```bash
# Criar o banco no PostgreSQL
psql -U postgres -c "CREATE DATABASE kpsaude_db;"

# Executar a estrutura de tabelas e índices
psql -U postgres -d kpsaude_db -f ../banco/01_estrutura.sql

# Inserir o usuário administrador padrão e dados iniciais
psql -U postgres -d kpsaude_db -f ../banco/02_dados_iniciais.sql
```

> **Dica com Docker:**  
> `docker run --name kpsaude-db -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=kpsaude_db -p 5432:5432 -d postgres:16`

### 4. Iniciar o Servidor

Dentro do diretório `servidor/`, execute:

```bash
node index.cjs
```

Pronto! Acesse no navegador:
👉 **[http://localhost:9000](http://localhost:9000)**

---

## 🔐 Credenciais Padrão

- **Administrador:** `admin@kpsaudeanimal.com.br` / `admin123` *(trocar após o 1º acesso)*
- **Tutor de Teste:** `amanda.silva@email.com` / `tutor123`

---

## 📡 Endpoints da API REST

Todas as respostas seguem o padrão obrigatório:
- **Sucesso:** `{"sucesso": true, "dados": {...}, "mensagem": "..."}`
- **Erro:** `{"sucesso": false, "erro": "..."}`

| Método | Endpoint | Protegido | Descrição |
|---|---|:---:|---|
| `POST` | `/api/auth/login` | Não | Realiza login e retorna token JWT |
| `GET` | `/api/auth/perfil` | Sim | Retorna dados do usuário autenticado |
| `POST` | `/api/coleira/dados` | Não | Recebe telemetria da coleira IoT |
| `GET` | `/api/coleira/historico/:petId` | Sim | Histórico paginado de leituras |
| `GET` | `/api/coleira/ultimo/:petId` | Sim | Última leitura de telemetria |
| `GET` | `/api/pets` | Sim | Lista pets do tutor e compartilhados |
| `POST` | `/api/pets` | Sim | Cadastra novo pet |
| `GET` | `/api/pets/:id` | Sim | Detalhes e última leitura do pet |
| `PUT` | `/api/pets/:id` | Sim | Atualiza dados cadastrais do pet |
| `DELETE`| `/api/pets/:id` | Sim | Remove pet do sistema |
| `POST` | `/api/compartilhar` | Sim | Concede permissão ACL a outro usuário |
| `GET` | `/api/compartilhar/:petId` | Sim | Lista usuários com acesso |
| `DELETE`| `/api/compartilhar/:id` | Sim | Revoga permissão de acesso |
| `POST` | `/api/sayori/diagnostico` | Sim | Triagem e orientação veterinária com IA |

---

## 📱 Aplicativo Android

O projeto também conta com a aplicação móvel nativa em Jetpack Compose localizada em `app/`, pronta para compilar com o Gradle:

```bash
gradle :app:assembleDebug
```
