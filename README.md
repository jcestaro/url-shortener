# Encurtador de URLs - Template Inicial

Este repositório serve como o template inicial para um projeto de Encurtador de URLs. A intenção é explorar e implementar diferentes abordagens e tecnologias para construir essa funcionalidade, incluindo processamento assíncrono de mensagens.

## Estrutura do Projeto e Ramificações (Branches)

Este branch (`main` ou `master`, dependendo da sua configuração padrão) contém a estrutura base e a configuração inicial do projeto.

Futuras explorações e implementações utilizando tecnologias ou abordagens alternativas serão desenvolvidas em **branches separadas**. Isso permitirá uma comparação e aprendizado mais claros sobre as diferentes ferramentas e técnicas. Fique atento aos nomes dos branches para identificar a tecnologia ou o foco específico de cada um.

## Tecnologias Utilizadas Neste Template Inicial

A configuração atual neste branch (`main`/`master`) utiliza as seguintes tecnologias:

* **Backend Framework:**
    * **Spring Boot (Java):** Um framework robusto e popular para a criação de aplicações Java, incluindo APIs RESTful. Ele foi escolhido pela sua produtividade, ecossistema maduro e facilidade de configuração.
        * *Dependência principal utilizada:* `spring-boot-starter-data-mongodb` para integração com o MongoDB.
        * *Dependência principal utilizada:* `spring-boot-starter-web` para funcionalidades web e criação de APIs REST.
        * *Dependência principal utilizada:* `spring-kafka` para integração com Apache Kafka.

* **Banco de Dados:**
    * **MongoDB:** Um banco de dados NoSQL orientado a documentos, escolhido por sua flexibilidade de esquema e escalabilidade, que podem ser vantajosas para um encurtador de URLs.

* **Mensageria Assíncrona:**
    * **Apache Kafka:** Uma plataforma de streaming distribuído utilizada para construir pipelines de dados em tempo real e aplicações de streaming. Neste projeto, é usado para processar o encurtamento de URLs de forma assíncrona, permitindo um padrão de request-reply.
    * **Apache Zookeeper:** (Frequentemente usado com Kafka) Um serviço de coordenação distribuído. O Kafka o utiliza para gerenciar e coordenar os brokers Kafka. Em configurações mais recentes do Kafka (com KRaft), o Zookeeper pode não ser necessário, mas muitas imagens Docker ainda o incluem ou o exigem para setups mais simples.

* **Containerização (para Banco de Dados e Mensageria):**
    * **Docker:** Utilizado para rodar as instâncias do MongoDB, Kafka e Zookeeper em containers isolados. Isso simplifica a configuração do ambiente de desenvolvimento, garante consistência e facilita o deploy.
        * Imagens oficiais ou populares do Docker Hub são utilizadas (ex: `mongo`, `confluentinc/cp-kafka`, `confluentinc/cp-zookeeper`).
        * Volumes Docker são configurados para persistência dos dados.

* **Testes de Integração:**
    * **Testcontainers:** Uma biblioteca Java que facilita o uso de instâncias Docker descartáveis para testes de integração, garantindo um ambiente de teste confiável e reproduzível para MongoDB e Kafka.

## Como Rodar (Configuração Atual)

1.  **Pré-requisitos:**
    * JDK 17 ou superior instalado.
    * Maven ou Gradle instalado.
    * Docker e Docker Compose instalados e rodando.

2.  **Configurar Variáveis de Ambiente (se necessário):**
    * Verifique o arquivo `application.yml` (ou `application.properties`) para quaisquer variáveis de ambiente que precisem ser configuradas (ex: credenciais, se não estiverem no compose).

3.  **Iniciar os Serviços com Docker Compose:**
    * Navegue até o diretório raiz do projeto (onde está o `docker-compose.yml`).
    * Este arquivo deve definir os serviços para MongoDB, Kafka e Zookeeper.
      Exemplo de trecho do `docker-compose.yml` para Kafka e Zookeeper (adapte conforme sua imagem e configuração):
        ```yaml
        version: '3.8'
        services:
          zookeeper:
            image: confluentinc/cp-zookeeper:7.3.0 # Ou outra imagem de Zookeeper
            container_name: zookeeper
            ports:
              - "2181:2181"
            environment:
              ZOOKEEPER_CLIENT_PORT: 2181
              ZOOKEEPER_TICK_TIME: 2000

          kafka:
            image: confluentinc/cp-kafka:7.3.0 # Ou outra imagem de Kafka
            container_name: kafka
            ports:
              - "9092:9092" # Para clientes externos (como sua aplicação rodando localmente)
              # - "29092:29092" # Para comunicação dentro da rede Docker, se necessário
            depends_on:
              - zookeeper
            environment:
              KAFKA_BROKER_ID: 1
              KAFKA_ZOOKEEPER_CONNECT: 'zookeeper:2181'
              KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_INTERNAL:PLAINTEXT
              KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092,PLAINTEXT_INTERNAL://kafka:29092 # Ajuste 'localhost' se seu Docker estiver em outra máquina
              KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
              KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS: 0
              KAFKA_CONFLUENT_LICENSE_TOPIC_REPLICATION_FACTOR: 1 # Para imagens Confluent
              KAFKA_CONFLUENT_BALANCER_TOPIC_REPLICATION_FACTOR: 1 # Para imagens Confluent
              KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
              KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
            # volumes: # Para persistência de dados do Kafka
            #   - kafka_data:/var/lib/kafka/data

          mongodb:
            image: mongo:6.0
            container_name: meu-mongo-db-compose # Ou o nome que você usa
            ports:
              - "27017:27017"
            environment: # Exemplo com autenticação
              MONGO_INITDB_ROOT_USERNAME: admin
              MONGO_INITDB_ROOT_PASSWORD: admin
            volumes:
              - mongodata_compose:/data/db # Nome do seu volume MongoDB

        volumes:
          mongodata_compose:
          # kafka_data:
        ```
    * Execute o comando para iniciar todos os serviços definidos no seu `docker-compose.yml`:
        ```bash
        docker-compose up -d
        ```

4.  **Configurar a Aplicação Spring Boot:**
    * Verifique o arquivo `src/main/resources/application.yml` (ou `application.properties`).
    * Ajuste as configurações de conexão com o MongoDB (URI, usuário, senha, se aplicável).
      Exemplo para `application.yml` com autenticação no MongoDB:
        ```yaml
        spring:
          data:
            mongodb:
              uri: mongodb://admin:admin@localhost:27017/url_shortener_db?authSource=admin
        ```
    * Ajuste as configurações de conexão com o Kafka.
      Exemplo para `application.yml`:
        ```yaml
        spring:
          kafka:
            bootstrap-servers: localhost:9092
            consumer:
              group-id: url-shortener-group
            # ... outras propriedades de produtor/consumidor/admin se necessário ...

        kafka: # Suas propriedades personalizadas de tópicos
          topic:
            requestreply:
              request: url.shortener.request
              reply: url.shortener.reply
        ```

5.  **Rodar a Aplicação Spring Boot:**
    * Pela sua IDE (IntelliJ IDEA, Eclipse, VS Code): Encontre a classe principal com o método `main` (geralmente anotada com `@SpringBootApplication`) e execute-a.
    * Pelo terminal (usando Maven):
        ```bash
        ./mvnw spring-boot:run
        ```
    * Pelo terminal (usando Gradle):
        ```bash
        ./gradlew bootRun
        ```

A aplicação Spring Boot estará rodando (geralmente na porta `8080`, a menos que configurado de outra forma) e conectada ao MongoDB e Kafka.

## Testes

Os testes de integração utilizam **Testcontainers** para iniciar instâncias Docker do MongoDB e Kafka, garantindo um ambiente de teste isolado e consistente.
* Consulte a classe `IntegrationTestBase.java` para a configuração dos containers.
* As propriedades de conexão para os testes são definidas dinamicamente via `@DynamicPropertySource`.
* Um arquivo `src/test/resources/application-test.yml` pode ser usado para configurações adicionais específicas de teste.

## Contribuições

Como este é um projeto de aprendizado e exploração, sinta-se à vontade para criar seus próprios branches e experimentar diferentes tecnologias!

---

Lembre-se de salvar este conteúdo em um arquivo chamado `README.md` na raiz do seu projeto Git. Você pode ajustar os nomes dos branches, comandos e detalhes conforme a sua configuração específica.
