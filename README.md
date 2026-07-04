# 🏋️ GymFlow Pro

Sistema completo de gerenciamento de academias, construído como projeto de portfólio para demonstrar práticas modernas de desenvolvimento Full Stack com **Java + Spring Boot** e **React + TypeScript**.

> Projeto de portfólio — não é um CRUD simples. Foca em arquitetura em camadas, segurança real (JWT + refresh token + RBAC), modelagem de domínio consistente e uma interface que se propõe a parecer um produto comercial.

---

## Sumário

- [Objetivo](#objetivo)
- [Tecnologias](#tecnologias)
- [Arquitetura](#arquitetura)
- [Estrutura de Pastas](#estrutura-de-pastas)
- [Modelo do Banco de Dados](#modelo-do-banco-de-dados)
- [Funcionalidades](#funcionalidades)
- [Como Executar](#como-executar)
- [Docker](#docker)
- [Testes](#testes)
- [Screenshots](#screenshots)
- [Roadmap](#roadmap)
- [Licença](#licença)

---

## Objetivo

Centralizar a gestão operacional de uma academia — alunos, planos, matrículas, treinos, frequência, financeiro e estoque — em um único sistema, com:

- Autenticação e controle de acesso por perfil (Administrador, Recepcionista, Instrutor, Aluno).
- Dashboard gerencial com indicadores e gráficos em tempo real.
- API REST documentada (OpenAPI/Swagger), versionável e testável.
- Interface responsiva com suporte a tema claro/escuro.
- Infraestrutura reprodutível via Docker.

## Tecnologias

### Backend
Java 21 · Spring Boot 3 · Spring Security · JWT (access + refresh token) · Spring Data JPA / Hibernate · PostgreSQL · Flyway · Lombok · MapStruct · Bean Validation · springdoc-openapi (Swagger) · Maven · JUnit 5 · Mockito · Testcontainers

### Frontend
React 18 · TypeScript · Vite · Tailwind CSS · shadcn/ui (Radix) · React Router · Axios · TanStack Query · React Hook Form · Zod · Framer Motion · Recharts · Lucide Icons

### Infraestrutura
Docker · Docker Compose · GitHub Actions (CI)

## Arquitetura

O backend segue uma arquitetura em camadas inspirada em Clean Architecture, com **Repository Pattern**, **DTO Pattern** e **Service Layer**:

```
Controller  →  Service (interface)  →  ServiceImpl  →  Repository  →  Entity
     ↑                                                        ↓
   DTO (request/response)  ←────── Mapper (MapStruct) ───────┘
```

- **Controllers**: expõem a API REST, validam entrada (`@Valid`) e aplicam autorização por perfil (`@PreAuthorize`).
- **Services**: concentram as regras de negócio (ex.: cálculo de vencimento de matrícula, geração automática de cobrança, controle de estoque atômico).
- **Repositories**: acesso a dados via Spring Data JPA, com `JpaSpecificationExecutor` para filtros dinâmicos.
- **Mappers**: conversão Entity ↔ DTO via MapStruct, sem lógica de negócio.
- **Exception Handling**: `@RestControllerAdvice` global, com respostas de erro padronizadas.
- **Security**: filtro JWT stateless, refresh token persistido (hash SHA-256), RBAC via `@PreAuthorize`.

O frontend segue composição por camadas: `pages` consomem `hooks` (TanStack Query) que chamam `services` (Axios), com estado de autenticação e tema em `contexts`, e componentes de UI reutilizáveis e desacoplados de regra de negócio.

## Estrutura de Pastas

```
GymFlow/
├── backend/
│   └── src/main/java/com/gymflow/pro/
│       ├── controller/       # Endpoints REST
│       ├── service/          # Interfaces de regra de negócio
│       │   └── impl/         # Implementações
│       ├── repository/       # Spring Data JPA + Specifications
│       ├── entity/           # Entidades JPA + enums de domínio
│       ├── dto/
│       │   ├── request/      # DTOs de entrada com Bean Validation
│       │   └── response/     # DTOs de saída
│       ├── mapper/           # MapStruct
│       ├── exception/        # Exceptions de domínio + handler global
│       ├── security/         # JWT, filtros, UserDetailsService
│       ├── config/           # Security, CORS, OpenAPI
│       ├── validation/       # Validadores customizados (ex.: CPF)
│       ├── scheduled/        # Jobs agendados (ex.: mensalidades vencidas)
│       └── util/
│   └── src/main/resources/
│       └── db/migration/     # Migrations Flyway (V1...V7)
│
├── frontend/
│   └── src/
│       ├── components/       # UI (shadcn/ui) + componentes compartilhados
│       ├── pages/             # Uma pasta por módulo de tela
│       ├── layouts/           # AuthLayout, DashboardLayout
│       ├── hooks/             # TanStack Query hooks por recurso
│       ├── contexts/          # AuthContext, ThemeContext
│       ├── services/          # Clientes Axios por recurso
│       ├── routes/            # Rotas + proteção por perfil
│       ├── types/             # Tipos espelhando os DTOs da API
│       └── utils/
│
├── docker-compose.yml
└── .github/workflows/ci.yml
```

## Modelo do Banco de Dados

Principais entidades e relacionamentos (schema completo em `backend/src/main/resources/db/migration`):

```
users ──< refresh_tokens
users ──< audit_logs
users ──< employees (1:1)
users ──< students (1:1, opcional — aluno pode ter login próprio)

plans ──< enrollments >── students
students ──< workouts >── workout_exercises >── exercises
students ──< attendances
students ──< financial_transactions >── enrollments

suppliers ──< products >── product_categories
products ──< stock_movements >── students (venda, opcional)
```

- **users**: conta única para autenticação, com `role` (ADMIN, RECEPTIONIST, INSTRUCTOR, STUDENT).
- **students**: cadastro completo (dados pessoais, endereço, contato de emergência, status).
- **plans / enrollments**: planos com duração/desconto; matrícula com vigência, congelamento e cancelamento.
- **workouts / exercises / workout_exercises**: banco de exercícios e montagem de treinos (séries, repetições, carga, descanso).
- **attendances**: check-in/check-out e cálculo de permanência.
- **financial_transactions**: lançamentos de receita/despesa com status (pendente, pago, vencido, cancelado).
- **products / stock_movements**: estoque de produtos com entrada/saída e alerta de estoque mínimo.
- **audit_logs**: trilha de auditoria (usuário, ação, entidade, IP, data/hora).

## Funcionalidades

- ✅ Autenticação JWT com refresh token e logout, perfis de acesso (Admin, Recepcionista, Instrutor, Aluno).
- ✅ Dashboard com indicadores (alunos ativos/inativos, novos alunos, mensalidades vencidas, receita mensal/anual) e gráficos (receita, frequência, alunos por plano).
- ✅ Cadastro completo de alunos com busca, filtros e paginação.
- ✅ Planos (mensal, trimestral, semestral, anual, personalizado).
- ✅ Matrículas: nova matrícula, renovação, cancelamento, congelamento e reativação.
- ✅ Treinos (A/B/C/D...) vinculados a um banco de exercícios com séries, repetições, carga e descanso.
- ✅ Controle de presença (check-in/check-out, frequência diária/semanal/mensal).
- ✅ Gestão de funcionários (instrutores, recepcionistas, administradores).
- ✅ Financeiro: receitas, despesas, fluxo de caixa, múltiplos meios de pagamento.
- ✅ Produtos e estoque (suplementos, acessórios, fornecedores, entrada/saída).
- ✅ Relatórios agregados (alunos, financeiro, frequência, planos, produtos).
- ✅ Auditoria de ações do sistema.

## Como Executar

### Pré-requisitos
- Java 21 e Maven 3.9+
- Node.js 20+
- PostgreSQL 16 (ou Docker)

### Backend
```bash
cd backend
# configure as variáveis de ambiente (ou exporte as do .env.example na raiz)
mvn spring-boot:run
```
A API sobe em `http://localhost:8080`, com documentação Swagger em `http://localhost:8080/swagger-ui.html`.

Usuário administrador padrão (seed): `admin@gymflow.com` / `Admin@123` — **troque a senha após o primeiro login**.

### Frontend
```bash
cd frontend
cp .env.example .env
npm install
npm run dev
```
A aplicação sobe em `http://localhost:5173`.

## Docker

Suba toda a stack (PostgreSQL + backend + frontend) com um único comando:

```bash
cp .env.example .env
docker compose up --build
```

- Frontend: `http://localhost:5173`
- Backend: `http://localhost:8080`
- PostgreSQL: `localhost:5432`

Os dados do banco persistem no volume `gymflow-postgres-data`.

## Testes

```bash
cd backend
mvn clean verify
```

Executa testes unitários (JUnit 5 + Mockito), testes de integração com banco real via **Testcontainers**, e gera relatório de cobertura (JaCoCo) com meta mínima de 80% de linhas cobertas nos módulos implementados.

## Screenshots

> Adicionar aqui screenshots do Dashboard, cadastro de alunos, matrículas e demais telas, além de um GIF demonstrando o fluxo principal do sistema.

## Roadmap

- [ ] Exportação de relatórios em PDF e Excel
- [ ] Check-in por QR Code (leitura via câmera no frontend)
- [ ] Notificações por e-mail/WhatsApp de mensalidades vencidas
- [ ] App mobile para instrutores e alunos
- [ ] Multi-tenant (mais de uma unidade/franquia por conta)

## Licença

Distribuído sob a licença [MIT](LICENSE).
