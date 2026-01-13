# Floris API

API RESTful desenvolvida para gestão financeira pessoal, com foco em projeção de fluxo de caixa, organização de despesas e controle de metas.

## Tecnologias

* Java 17
* Spring Boot 3
* Spring Security 6 (JWT via HttpOnly Cookies)
* PostgreSQL
* Flyway
* Docker & Docker Compose
* GitHub Actions (CI)

## Como Rodar

O projeto utiliza Docker para orquestrar a aplicação e o banco de dados.

1. **Clone o repositório**
   ```
   git clone https://github.com/dregoncalves/floris-api.git
   
   cd floris-api
   ```
   

2. **Inicie a aplicação**
   ```
    docker-compose up --build
   ```

A API estará disponível em: http://localhost:8080

## Documentação e Testes

A coleção de requisições para testes manuais está disponível no arquivo:
`docs/floris-postman-import.json`

Importe este arquivo no Postman para testar os endpoints de Autenticação, Gastos, Entradas e Dashboard.
