# Encurtador de URLs - Template Inicial

Este repositório serve como o template inicial para um projeto de Encurtador de URLs. A intenção é explorar e implementar diferentes abordagens e tecnologias para construir essa funcionalidade.

## Estrutura do Projeto e Ramificações (Branches)

Este branch (`main` ou `master`, dependendo da sua configuração padrão) contém a estrutura base e a configuração inicial do projeto.

Futuras explorações e implementações utilizando tecnologias ou abordagens alternativas serão desenvolvidas em **branches separadas**. Isso permitirá uma comparação e aprendizado mais claros sobre as diferentes ferramentas e técnicas. Fique atento aos nomes dos branches para identificar a tecnologia ou o foco específico de cada um.

## Tecnologias Utilizadas Neste Template Inicial

A configuração atual neste branch (`main`/`master`) utiliza as seguintes tecnologias:

* **Backend Framework:**
    * **Spring Boot (Java):** Um framework robusto e popular para a criação de aplicações Java, incluindo APIs RESTful. Ele foi escolhido pela sua produtividade, ecossistema maduro e facilidade de configuração.
        * *Dependência principal utilizada:* `spring-boot-starter-data-mongodb` para integração com o MongoDB.
        * *Dependência principal utilizada:* `spring-boot-starter-web` para funcionalidades web e criação de APIs REST.

* **Banco de Dados:**
    * **MongoDB:** Um banco de dados NoSQL orientado a documentos, escolhido por sua flexibilidade de esquema e escalabilidade, que podem ser vantajosas para um encurtador de URLs.

* **Containerização (para o Banco de Dados):**
    * **Docker:** Utilizado para rodar a instância do MongoDB em um container isolado. Isso simplifica a configuração do ambiente de desenvolvimento, garante consistência e facilita o deploy.
        * A imagem oficial `mongo` do Docker Hub é utilizada.
        * Um volume Docker (`mongodata` ou similar) é configurado para persistência dos dados do MongoDB.

## Próximos Passos (Neste Template)

1.  Implementar a lógica principal do encurtador de URLs:
    * Geração de códigos curtos (short codes) únicos.
    * Armazenamento do mapeamento entre URL original e short code no MongoDB.
    * Endpoint para encurtar uma nova URL.
    * Endpoint para redirecionar um short code para a URL original.
2.  Adicionar validações para as URLs de entrada.

## Como Rodar (Configuração Atual)

1.  **Pré-requisitos:**
    * JDK 17 ou superior instalado (verifique a versão do Java configurada no seu `pom.xml` ou `build.gradle`).
    * Maven ou Gradle instalado (conforme a configuração do seu projeto Spring Boot).
    * Docker e Docker Compose (opcional, mas recomendado para o `docker-compose.yml`) instalados e rodando.

2.  **Iniciar o Banco de Dados MongoDB com Docker:**
    * **Usando o comando `docker run` (conforme configurado anteriormente):**
        ```bash
        # Se ainda não criou o volume: docker volume create mongodata
        docker run -d \
          --name meu-mongo-db \
          -p 27017:27017 \
          -v mongodata:/data/db \
          # Descomente e ajuste se configurou autenticação:
          # -e MONGO_INITDB_ROOT_USERNAME=admin \
          # -e MONGO_INITDB_ROOT_PASSWORD=admin \
          mongo
        ```
    * **Usando Docker Compose (se você criou um `docker-compose.yml`):**
      Navegue até o diretório raiz do projeto (onde está o `docker-compose.yml`) e execute:
        ```bash
        docker-compose up -d # ou docker compose up -d
        ```

3.  **Configurar a Aplicação Spring Boot:**
    * Verifique o arquivo `src/main/resources/application.properties` (ou `application.yml`) e ajuste as configurações de conexão com o MongoDB, se necessário (especialmente se estiver usando autenticação).
      Exemplo para `application.properties` (sem autenticação):
        ```properties
        spring.data.mongodb.host=localhost
        spring.data.mongodb.port=27017
        spring.data.mongodb.database=encurtador_db # Ou o nome do banco que desejar
        ```
      Exemplo com autenticação:
        ```properties
        spring.data.mongodb.uri=mongodb://admin:admin@localhost:27017/encurtador_db?authSource=admin
        ```

4.  **Rodar a Aplicação Spring Boot:**
    * Pela sua IDE (IntelliJ IDEA, Eclipse, VS Code): Encontre a classe principal com o método `main` (geralmente anotada com `@SpringBootApplication`) e execute-a.
    * Pelo terminal (usando Maven):
        ```bash
        ./mvnw spring-boot:run
        ```
    * Pelo terminal (usando Gradle):
        ```bash
        ./gradlew bootRun
        ```

A aplicação Spring Boot estará rodando (geralmente na porta `8080`, a menos que configurado de outra forma).

## Contribuições

Como este é um projeto de aprendizado e exploração, sinta-se à vontade para criar seus próprios branches e experimentar diferentes tecnologias!

---

Lembre-se de salvar este conteúdo em um arquivo chamado `README.md` na raiz do seu projeto Git. Você pode ajustar os nomes dos branches, comandos e detalhes conforme a sua configuração específica.