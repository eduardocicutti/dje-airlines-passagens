# DJE Airlines

Sistema desktop para venda de passagens aéreas em balcão, desenvolvido para o trabalho final de Programação Orientada a Objetos (PROG III) da Universidade Federal do Espírito Santo.

## Autores

- Eduardo Cicutti
- Daniel Mendes
- José Antônio dos Santos

## Funcionalidades

- fluxo de venda em cinco etapas;
- consulta de voos por origem e destino;
- itinerários diretos e com conexão;
- escolha visual de assentos;
- pagamento em dinheiro, crédito ou débito;
- cálculo dinâmico do valor da passagem;
- gravação da venda no MySQL;
- geração do e-ticket em PDF;
- envio do e-ticket em uma tarefa paralela.

O sistema usa herança e polimorfismo nas formas de pagamento, encapsulamento nos modelos e transação no banco para impedir a venda duplicada de um assento.

## Tecnologias

- Java 17
- Java Swing
- Maven
- MySQL 8
- MySQL Connector/J
- OpenPDF
- JUnit 5

## Estrutura

```text
database/
  schema.sql
  seed.sql
src/
  main/java/br/ufes/
  test/java/br/ufes/
pom.xml
```

## Banco de dados

O banco se chama `dje_airlines`. Os scripts estão na pasta `database`:

1. `schema.sql` recria a base e suas tabelas;
2. `seed.sql` limpa os registros e inclui os dados iniciais.

No MySQL Command Line Client:

```sql
SOURCE C:/caminho/do/projeto/database/schema.sql;
SOURCE C:/caminho/do/projeto/database/seed.sql;
```

Os scripts removem os dados anteriores. As tabelas de passageiros, pagamentos, vendas e e-tickets ficam vazias. Em seguida, o seed cadastra:

- 8 aeroportos;
- 12 rotas nos dois sentidos;
- 60 voos futuros;
- 96 trechos;
- 3.600 assentos disponíveis;
- 1 funcionário, matrícula `1001`.

Há duas opções de voo para cada uma das 30 combinações de origem e destino entre BSB, CGH, CNF, FLN, FOR e VIX. Quando não existe trecho direto, o itinerário usa conexão em São Paulo.

Porto Alegre e Rio de Janeiro permanecem cadastrados, mas sem voos no seed. O material da disciplina não informa distâncias envolvendo POA ou GIG, e o projeto não adota valores externos ao enunciado.

## Cálculo da passagem

O valor de cada trecho segue a expressão fornecida na disciplina:

```text
DIST x MILHA x PER x DUFFS x RET x PROC
```

O total é a soma dos trechos. A implementação considera distância, antecedência da compra, dia útil/feriado/fim de semana, intervalo de retorno e percentual de assentos disponíveis.

## Configuração

Crie o arquivo `src/main/resources/application.properties` a partir de `application.properties.example` e informe o acesso ao MySQL:

```properties
db.url=jdbc:mysql://localhost:3306/dje_airlines?useSSL=false&serverTimezone=America/Sao_Paulo&allowPublicKeyRetrieval=true
db.user=root
db.password=sua-senha
```

O arquivo com a senha local é ignorado pelo Git.

## Execução

Pelo terminal:

```powershell
mvn clean package
mvn exec:java "-Dexec.mainClass=br.ufes.App"
```

No NetBeans, abra a pasta como projeto Maven, aguarde o carregamento das dependências e execute a classe `br.ufes.App`.

## Testes

```powershell
mvn test
```

Os testes conferem as faixas do algoritmo de preços, as formas de pagamento, a geração do e-ticket e a configuração de conexão.

## Arquivos gerados

Os e-tickets ficam em `output/etickets`. O envio usa a pasta definida em `DJE_SERVER_DIR`; se a variável não estiver configurada, usa `output/servidor`.

## Versão

Versão atual: `1.1.0`.
