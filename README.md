# DJE Airlines - venda de passagens aéreas

Projeto Java 17 com Swing, Maven, MySQL e OpenPDF para o trabalho final de POO/PROG III da UFES.

## Autores

- Eduardo
- Daniel
- José

## Estado atual

O sistema já está integrado ao MySQL e possui:

- estrutura Maven em `src/main/java`;
- modelos encapsulados e hierarquia polimórfica de pagamentos;
- algoritmo de preços do professor com `BigDecimal`;
- Connector/J e OpenPDF configurados no Maven;
- configuração JDBC sem senha versionada;
- métodos `Conectar`, `InserirDadosNoMySQL` e `Desconectar`;
- schema e dados iniciais idempotentes;
- testes unitários das regras principais;
- envio de arquivo implementado como cópia real para diretório configurável;
- tela Swing inicial de venda em 5 etapas ligada ao MySQL.

O checkpoint atual é a validação final pelo NetBeans e por uma venda de demonstração na interface.

## Pré-requisitos pedidos pelo professor

1. Java JDK 17.
2. Apache Maven (ou o Maven integrado ao NetBeans).
3. NetBeans com suporte a projetos Maven.
4. Microsoft Visual C++ Redistributable compatível.
5. MySQL Server, usando a configuração **Server only** do instalador.
6. MySQL Command Line Client.
7. MySQL Connector/J.

O Connector/J está declarado no `pom.xml` como `com.mysql:mysql-connector-j:9.7.0`, conforme as coordenadas oficiais. Ao abrir o projeto Maven no NetBeans, ele aparecerá em Dependencies. Se for necessário demonstrar exatamente o procedimento do PDF do professor, baixe o Connector/J em <https://dev.mysql.com/downloads/connector/j/>, extraia o JAR e use `Libraries/Dependencies > Add JAR/Folder` no NetBeans. Não copie o JAR para este repositório.

Não use XAMPP ou phpMyAdmin como procedimento principal deste trabalho.

## Criar a base de dados

Abra o MySQL Command Line Client, autentique-se e execute, trocando o caminho conforme o local do projeto:

```sql
SOURCE C:/Users/eduardo/Desktop/passagens/database/schema.sql;
SOURCE C:/Users/eduardo/Desktop/passagens/database/seed.sql;
```

Os scripts podem ser executados novamente sem duplicar aeroportos, rotas, voos ou assentos.

O `seed.sql` contém somente as seis distâncias fornecidas pelo professor, nos dois sentidos. Porto Alegre e Rio de Janeiro são cadastrados, mas não recebem distâncias inventadas; uma venda sem distância conhecida deve ser bloqueada.

Para conferir a carga inicial no MySQL Command Line Client:

```sql
USE dje_airlines;
SHOW TABLES;
SELECT COUNT(*) AS aeroportos FROM aeroportos;
SELECT COUNT(*) AS rotas FROM rotas;
SELECT COUNT(*) AS voos FROM voos;
SELECT COUNT(*) AS assentos FROM assentos;
SELECT codigo_iata, nome, cidade FROM aeroportos ORDER BY codigo_iata;
```

Resultado esperado nesta versão inicial: 8 aeroportos, 12 rotas, 3 voos e 180 assentos.

## Configurar a conexão

A forma recomendada é usar variáveis de ambiente no PowerShell:

```powershell
$env:DJE_DB_URL='jdbc:mysql://localhost:3306/dje_airlines?useSSL=false&serverTimezone=America/Sao_Paulo&allowPublicKeyRetrieval=true'
$env:DJE_DB_USER='root'
$env:DJE_DB_PASSWORD='sua-senha'
```

Alternativamente, copie `src/main/resources/application.properties.example` para `src/main/resources/application.properties` e altere apenas a cópia local. Esse arquivo está ignorado e não deve ser compartilhado com senha real.

## Testar a conexão

Depois que o banco estiver criado:

```powershell
mvn -q -DskipTests package
mvn -q exec:java "-Dexec.mainClass=br.ufes.passagens.config.TesteConexao"
mvn -q exec:java "-Dexec.mainClass=br.ufes.passagens.config.TesteConsultaVoos"
mvn -q exec:java "-Dexec.mainClass=br.ufes.passagens.config.TesteVendaCompleta"
mvn -q exec:java "-Dexec.mainClass=br.ufes.passagens.config.TesteResumoBanco"
```

Se os comandos `exec:java` não estiverem disponíveis no NetBeans, execute as classes `TesteConexao`, `TesteConsultaVoos`, `TesteVendaCompleta` e `TesteResumoBanco` diretamente pela IDE.

Para abrir a aplicação Swing:

```powershell
mvn -q exec:java "-Dexec.mainClass=br.ufes.App"
```

Erros comuns:

- `Communications link failure`: serviço MySQL parado, host ou porta incorretos.
- `Access denied`: usuário ou senha incorretos.
- `Unknown database 'dje_airlines'`: `schema.sql` ainda não foi executado.
- `Public Key Retrieval is not allowed`: confirme que a URL é a do exemplo.
- caracteres sem acento: confirme UTF-8/`utf8mb4` e não altere a codificação dos scripts.

## Compilar e testar sem MySQL

Os testes unitários não exigem banco ativo:

```powershell
mvn clean test
mvn package
```

## Aplicação Swing

Abra a tela principal pelo NetBeans executando `br.ufes.App`, ou pelo PowerShell:

```powershell
mvn -q exec:java "-Dexec.mainClass=br.ufes.App"
```

Fluxos de demonstração já cadastrados:

- `FLN -> BSB`, voo `DJE101`, com conexão `FLN -> CGH -> BSB`;
- `CGH -> VIX`, voo `DJE202`;
- `CGH -> FOR`, voo `DJE303`.

Para pagamento em dinheiro, use a matrícula `1001`, previamente cadastrada em `seed.sql`.

## Servidor de e-tickets

Os PDFs são gerados em `output/etickets/`. Após a geração, uma thread copia o PDF para o destino configurado por `DJE_SERVER_DIR`. Se a variável não existir, o destino local de demonstração será `output/servidor/`.

`DJE_SERVER_DIR` pode ser uma pasta local de teste ou caminho UNC da rede:

```powershell
$env:DJE_SERVER_DIR='\\servidor\etickets'
```

O resultado do envio é persistido na tabela `etickets`, em `estado_envio` e `enviado_em`.

## Arquivos principais

- `database/schema.sql`: estrutura relacional e restrições.
- `database/seed.sql`: aeroportos, distâncias, voos, conexão e assentos de demonstração.
- `src/main/java/br/ufes/passagens/config`: propriedades e conexão JDBC.
- `src/main/java/br/ufes/passagens/model`: domínio e pagamentos.
- `src/main/java/br/ufes/passagens/service`: preço e validações.
- `PLANO_IMPLEMENTACAO_CODEX.md`: especificação integral das próximas etapas.
