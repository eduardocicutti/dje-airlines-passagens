# DJE Airlines - Venda de Passagens Aéreas

Sistema desktop em Java para venda de passagens aéreas, desenvolvido como trabalho final da disciplina de Programação Orientada a Objetos.

O projeto usa Java Swing para a interface, MySQL para persistência, Maven para gerenciamento de dependências e OpenPDF para geração do e-ticket.

## Autores

- Eduardo Cicutti
- Daniel Mendes
- José Antônio dos Santos

## Funcionalidades

- Cadastro inicial de aeroportos, rotas, voos, conexões e assentos via script SQL.
- Consulta de voos disponíveis por origem e destino.
- Fluxo Swing em cinco etapas:
  - origem e destino;
  - voo e horário;
  - assento;
  - passageiro e pagamento;
  - e-ticket.
- Cálculo de preço com base nas regras fornecidas no enunciado.
- Pagamento em dinheiro, crédito ou débito, com herança e polimorfismo.
- Gravação completa da venda em banco MySQL.
- Bloqueio de venda duplicada para o mesmo assento.
- Geração de PDF do e-ticket.
- Envio do e-ticket em uma tarefa paralela.

## Tecnologias

- Java 17
- Java Swing
- Maven
- MySQL
- MySQL Connector/J
- OpenPDF
- JUnit 5

## Estrutura

```text
database/
  schema.sql
  seed.sql
docs/
  verificacao-requisitos.md
src/main/java/br/ufes/passagens/
  config/
  model/
  repository/
  service/
  ui/
  util/
src/test/java/br/ufes/passagens/
```

## Banco de Dados

O banco usado pelo sistema se chama `dje_airlines`.

As principais tabelas são:

- `aeroportos`
- `rotas`
- `voos`
- `voo_trechos`
- `assentos`
- `funcionarios`
- `passageiros`
- `pagamentos`
- `vendas`
- `etickets`

Para criar e popular a base:

```sql
SOURCE caminho/do/projeto/database/schema.sql;
SOURCE caminho/do/projeto/database/seed.sql;
```

O script inicial cadastra 8 aeroportos, 12 rotas, 3 voos e 180 assentos.

## Configuração Local

Copie o arquivo de exemplo:

```text
src/main/resources/application.properties.example
```

para:

```text
src/main/resources/application.properties
```

Depois ajuste os dados de conexão:

```properties
db.url=jdbc:mysql://localhost:3306/dje_airlines?useSSL=false&serverTimezone=America/Sao_Paulo&allowPublicKeyRetrieval=true
db.user=root
db.password=sua-senha
```

O arquivo `application.properties` fica fora do Git por conter senha local.

## Execução

Para compilar:

```powershell
mvn clean package
```

Para abrir a aplicação:

```powershell
mvn exec:java "-Dexec.mainClass=br.ufes.App"
```

Também é possível executar a classe `br.ufes.App` diretamente pelo NetBeans.

## Rotas de Demonstração

O `seed.sql` inclui os seguintes voos:

- `DJE101`: `FLN -> CGH -> BSB`
- `DJE202`: `CGH -> VIX`
- `DJE303`: `CGH -> FOR`

Para pagamento em dinheiro, a matrícula inicial cadastrada é `1001`.

## Testes

Para executar os testes unitários:

```powershell
mvn test
```

Classes auxiliares para validação manual:

- `TesteConexao`
- `TesteConsultaVoos`
- `TesteVendaCompleta`
- `TesteResumoBanco`

Para executar uma validação manual pelo Maven:

```powershell
mvn test-compile exec:java "-Dexec.mainClass=br.ufes.passagens.config.TesteConexao" "-Dexec.classpathScope=test"
```

## Observações

- As distâncias seguem os valores fornecidos no enunciado.
- Porto Alegre e Rio de Janeiro estão cadastrados no banco, mas não aparecem na seleção comercial inicial porque o material base não fornece suas distâncias para cálculo de preço.
- O sistema não salva CVV nem número completo de cartão.
- Os PDFs gerados ficam em `output/etickets/`.
- O envio de e-ticket usa `DJE_SERVER_DIR` quando configurado; caso contrário usa `output/servidor/`.
