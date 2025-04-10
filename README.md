# Integração OAuth com HubSpot

Este projeto implementa uma API REST em Java utilizando o Spring Boot para integrar com a API do HubSpot. A integração abrange o fluxo de autenticação OAuth 2.0 (Authorization Code Flow), a criação de contatos no CRM e o recebimento de notificações via webhooks para eventos de criação de contatos.

## Funcionalidades Implementadas

- **Geração da Authorization URL**: Endpoint que gera a URL de autorização para iniciar o fluxo OAuth com o HubSpot.
- **Processamento do Callback OAuth**: Endpoint que recebe o código de autorização e o troca pelo token de acesso.
- **Criação de Contatos**: Endpoint para criar contatos no CRM do HubSpot.
- **Recebimento de Webhook**: Endpoint que processa eventos do tipo `contact.creation` enviados pelo HubSpot, com validação de assinatura para segurança.

## Tecnologias Utilizadas

- **Java 21**: Versão moderna do Java para aproveitar as últimas funcionalidades.
- **Spring Boot 3.4.4**: Framework para criação de APIs REST com configuração simplificada.
- **Spring Data JPA**: Para persistência de dados com o banco H2 em memória.
- **H2 Database**: Banco em memória usado para desenvolvimento e testes.
- **Jackson Databind**: Para manipulação de JSON, essencial para processar payloads do HubSpot.
- **Lombok**: Reduz boilerplate code, aumentando a legibilidade.
- **Commons Codec**: Para cálculos de hash na validação de assinatura do webhook.
- **Commons Lang3**: Utilitários gerais para manipulação de strings e objetos.
- **Ngrok**: Ferramenta para expor a aplicação local e testar webhooks.

## Como Executar o Projeto

### Pré-requisitos

- Java 21 instalado.
- Maven instalado.
- Ngrok instalado (para testes de webhooks).
- Conta de Desenvolvedor no HubSpot com um aplicativo configurado (Client ID, Client Secret, Redirect URI).

### Passos para Execução

1. **Clone o Repositório**:
   ```bash
   git clone https://github.com/seu-usuario/seu-repositorio.git
   cd seu-repositorio
   
2. **Configurar o application.properties**:
Edite o arquivo src/main/resources/application.properties com as credenciais do HubSpot fornecidas ou substitua pelas suas próprias:
```bash
# Configurações do HubSpot OAuth
hubspot.contacts-url=https://api.hubapi.com/crm/v3/objects/contacts
hubspot.authorize-url=https://app.hubspot.com/oauth/authorize
hubspot.token-url=https://api.hubapi.com/oauth/v1/token
hubspot.redirect-uri=http://localhost:8080/api/hubspot/oauth/callback
hubspot.client-secret=72605e33-14c7-4230-9e41-036401b31ad1
hubspot.client-id=d918de25-5023-47f2-b003-f451a5bfeb24
hubspot.scopes=crm.objects.line_items.read, oauth, conversations.read, tickets, crm.objects.contacts.write, e-commerce, crm.objects.companies.read, crm.objects.deals.read, crm.objects.contacts.read

# Configurações do H2
spring.datasource.url=jdbc:h2:mem:db_oauth
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=clear
spring.datasource.password=clear
spring.h2.console.path=/h2
spring.h2.console.enabled=true
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.defer-datasource-initialization=true
spring.jpa.show-sql=true

```
3. **Executar a Aplicação**:
```bash
mvn spring-boot:run
```

4. **Acessar o Console H2 (Opcional)**:
```bash
Abra http://localhost:8080/h2 no navegador para visualizar o banco de dados em memória.
```

5. **Configurar Ngrok para Webhooks**:
- Inicie o Ngrok em um terminal:
```bash
ngrok http 8080
```
- Copie a URL pública gerada (ex.: https://abc123.ngrok.io).
-No portal do HubSpot, configure o webhook apontando para https://abc123.ngrok.io/api/hubspot/webhook.

- Dica: Para entender melhor como configurar e usar o Ngrok, recomendo assistir ao vídeo da Algaworks sobre o assunto que explica de forma clara como expor sua aplicação local para testes com webhooks [Como usar Ngrok para expor sua aplicação local](https://www.youtube.com/watch?v=aHtCPkIxS-c).

6. **Testar os Endpoints**:
- Autorização: Acesse GET http://localhost:8080/api/hubspot/authorize para obter a URL de autenticação.
- Callback OAuth: Após autorizar, o HubSpot redirecionará para http://localhost:8080/api/hubspot/oauth/callback?code=.
- Criar Contato: Envie uma requisição POST para http://localhost:8080/api/hubspot/contacts com o seguinte body:
```bash
{
  "firstname": "Maria",
  "lastname": "Silva",
  "email": "maria.silva@example.com"
}
```
- Webhook: Crie um contato diretamente no HubSpot e verifique os logs da aplicação para o processamento do evento.

# Documentação Técnica
## Decisões Técnicas e Motivações
### Escolha das Dependências
- Spring Boot Starter Web: Fornece uma base sólida para APIs REST, com servidor embutido (Tomcat) e configuração mínima.
- Spring Boot Starter Data JPA: Simplifica a persistência de dados com abstrações sobre o Hibernate, ideal para o H2.
- H2 Database: Banco em memória leve e rápido, perfeito para desenvolvimento e testes locais.
- Jackson Databind: Biblioteca padrão para parsing de JSON, necessária para processar requisições e respostas do HubSpot.
- Lombok: Reduz a verbosidade do código (ex.: getters, setters), melhorando a manutenção.
- Commons Codec: Utilizado para gerar hashes SHA-256 na validação de assinaturas de webhooks, conforme exigido pelo HubSpot.
- Commons Lang3: Oferece utilitários que facilitam manipulações comuns, como validação de strings.
- Ngrok: Essencial para expor a aplicação local, já que o HubSpot não aceita URLs locais para webhooks.

### Abordagem para Webhooks
- O Ngrok foi escolhido para testes locais, pois o HubSpot exige URLs públicas. Isso permitiu simular um ambiente de produção sem a necessidade de deploy.

### Persistência
- O H2 foi usado por sua simplicidade e ausência de dependências externas, mas pode ser substituído por bancos como PostgreSQL em produção.

### Estrutura do Projeto
- Controllers: Gerenciam os endpoints da API (ex.: autorização, callback, criação de contatos, webhook).
- Services: Contêm a lógica de negócio, como autenticação OAuth e processamento de webhooks.
- Entities: Representam os dados persistidos, como tokens e contatos.
- Repositories: Interfaces JPA para operações no banco de dados.

### Melhorias Futuras
- Refresh Token: Adicionar suporte à renovação automática do token de acesso.
- Mais Eventos: Expandir o webhook para suportar outros eventos, como atualizações de contatos.
- Testes Automatizados: Incluir testes de integração para maior robustez.

### Considerações Finais
O projeto foi desenvolvido com foco em simplicidade, boas práticas e alinhamento com os requisitos do desafio proposto. 

### Informações de Contato
Caso tenha interesse em discutir mais sobre o projeto, oportunidades ou sugestões de melhorias, sinta-se à vontade para entrar em contato:

- [**Joao Paulo Oliveira**]
- [**LinkedIn**](https://www.linkedin.com/in/seu-perfil)
- [**GitHub** ](https://github.com/Jp0liveira)
- [**Contato**](https://linktr.ee/jpoliveiraweb)
