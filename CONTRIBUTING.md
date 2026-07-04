# Contribuindo com o GymFlow Pro

Obrigado por considerar contribuir com este projeto! Este é um projeto de portfólio, mas segue práticas de projetos open source reais.

## Como contribuir

1. Faça um fork do repositório.
2. Crie uma branch a partir de `main`: `git checkout -b feat/nome-da-feature`.
3. Siga o padrão [Conventional Commits](https://www.conventionalcommits.org/) nas mensagens de commit:
   - `feat:` nova funcionalidade
   - `fix:` correção de bug
   - `refactor:` refatoração sem mudança de comportamento
   - `docs:` documentação
   - `test:` testes
   - `chore:` tarefas de build/infra
4. Garanta que o backend compila e os testes passam: `cd backend && mvn clean verify`.
5. Garanta que o frontend builda sem erros: `cd frontend && npm run build`.
6. Abra um Pull Request descrevendo o que foi feito e por quê.

## Padrões de código

- **Backend**: Clean Code, SOLID, Service Layer, DTO Pattern, cobertura mínima de testes de 80%.
- **Frontend**: componentes e hooks reutilizáveis, tipagem estrita em TypeScript, sem `any`.

## Reportando bugs

Abra uma issue descrevendo:
- Passos para reproduzir
- Comportamento esperado vs. observado
- Ambiente (SO, versão do Java/Node)
