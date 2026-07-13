# DJE Airlines - Venda de Passagens Aereas

Sistema desktop em Java para venda de passagens aereas, desenvolvido como trabalho final da disciplina de Programacao Orientada a Objetos.

O projeto usa Java Swing para a interface, MySQL para persistencia, Maven para gerenciamento de dependencias e OpenPDF para geracao do e-ticket.

## Autores

- Eduardo Cicutti
- Daniel Mendes
- José Antônio dos Santos

## Funcionalidades

- Cadastro inicial de aeroportos, rotas, voos, conexoes e assentos via script SQL.
- Consulta de voos disponiveis por origem e destino.
- Fluxo Swing em cinco etapas:
  - origem e destino;
  - voo e horario;
  - assento;
  - passageiro e pagamento;
  - e-ticket.
- Calculo de preco com base nas regras fornecidas no enunciado.
- Pagamento em dinheiro, credito ou debito, com heranca e polimorfismo.
- Gravacao completa da venda em banco MySQL.
- Bloqueio de venda duplicada para o mesmo assento.
- Geracao de PDF do e-ticket.
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

As principais tabelas sao:

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

## Configuracao Local

Copie o arquivo de exemplo:

```text
src/main/resources/application.properties.example
```

para:

```text
src/main/resources/application.properties
```

Depois ajuste os dados de conexao:

```properties
db.url=jdbc:mysql://localhost:3306/dje_airlines?useSSL=false&serverTimezone=America/Sao_Paulo&allowPublicKeyRetrieval=true
db.user=root
db.password=sua-senha
```

O arquivo `application.properties` fica fora do Git por conter senha local.

## Execucao

Para compilar:

```powershell
mvn clean package
```

Para abrir a aplicacao:

```powershell
mvn exec:java "-Dexec.mainClass=br.ufes.App"
```

Tambem e possivel executar a classe `br.ufes.App` diretamente pelo NetBeans.

## Rotas de Demonstracao

O `seed.sql` inclui os seguintes voos:

- `DJE101`: `FLN -> CGH -> BSB`
- `DJE202`: `CGH -> VIX`
- `DJE303`: `CGH -> FOR`

Para pagamento em dinheiro, a matricula inicial cadastrada e `1001`.

## Testes

Para executar os testes unitarios:

```powershell
mvn test
```

Classes auxiliares para validacao manual:

- `TesteConexao`
- `TesteConsultaVoos`
- `TesteVendaCompleta`
- `TesteResumoBanco`

## Observacoes

- As distancias seguem os valores fornecidos no enunciado.
- Porto Alegre e Rio de Janeiro estao cadastrados, mas nao possuem rotas comerciais iniciais porque o material base nao fornece suas distancias.
- O sistema nao salva CVV nem numero completo de cartao.
- Os PDFs gerados ficam em `output/etickets/`.
- O envio de e-ticket usa `DJE_SERVER_DIR` quando configurado; caso contrario usa `output/servidor/`.
