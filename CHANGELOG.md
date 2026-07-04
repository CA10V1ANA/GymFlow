# Changelog

Todas as mudanças notáveis deste projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/),
e este projeto segue [Versionamento Semântico](https://semver.org/lang/pt-BR/).

## [Unreleased]

### Added
- Arquitetura completa do projeto (backend Spring Boot 3 + frontend React/TypeScript).
- Modelagem do banco de dados PostgreSQL com Flyway (usuários, alunos, planos, matrículas, treinos, exercícios, presença, funcionários, financeiro, produtos, auditoria).
- Autenticação JWT com refresh token e controle de perfis (Administrador, Recepcionista, Instrutor, Aluno).
- Módulos de Alunos, Planos, Matrículas, Treinos, Exercícios, Presença, Funcionários, Financeiro, Produtos e Relatórios.
- Dashboard com indicadores e gráficos (receita, frequência, alunos por plano).
- Frontend em React + TypeScript com Tailwind CSS, shadcn/ui, dark/light mode e componentes reutilizáveis.
- Dockerfiles para backend e frontend e orquestração via docker-compose com PostgreSQL.
- Pipeline de CI no GitHub Actions (build, lint e testes).
- Suíte de testes do backend: unitários (JUnit 5 + Mockito) para todos os services, testes de controller (MockMvc) com verificação de autorização por perfil, testes de mapper, segurança e testes de integração com Testcontainers. Cobertura de linha ~85%, acima da meta de 80% (JaCoCo).

### Fixed
- Hash bcrypt do usuário administrador da seed (`V7__seed_data.sql`) não correspondia à senha documentada; gerado hash válido para `Admin@123`.
- Ambiguidade de overload do Mockito (`delete(any())`) em `ExerciseServiceImplTest` ao usar um repositório que estende tanto `JpaRepository` quanto `JpaSpecificationExecutor`.
- Adicionado `docker-java.properties` (test classpath) fixando `api.version=1.41` para o Testcontainers funcionar com engines Docker recentes que recusam a versão de API padrão do cliente (mínimo exigido é 1.40).

## [1.0.0] - TBD
- Primeira versão publicada do GymFlow Pro.
