# Verificacao dos requisitos

| Requisito do professor | Implementação | Estado |
|---|---|---|
| Java e POO | `model/`, `service/`, `repository/`, `ui/` | Pronto |
| Encapsulamento | Modelos com atributos privados e validação no construtor | Pronto |
| Herança e polimorfismo | `Pagamento`, `PagamentoCartao`, `PagamentoCredito`, `PagamentoDebito`, `PagamentoDinheiro` | Pronto e testado |
| Algoritmo de preços | `CalculadoraPreco` com `BigDecimal` | Pronto e testado |
| MySQL e Connector/J | `pom.xml`, `config/`, `database/schema.sql`, `database/seed.sql` | Pronto e validado localmente |
| Métodos `Conectar`, `InserirDadosNoMySQL`, `Desconectar` | `ConexaoMySQL` | Pronto e testado por reflexão/conexão |
| Assento sem duplicidade | `uk_voo_assento`, `uk_venda_assento`, `SELECT ... FOR UPDATE` | Pronto |
| Aeroportos e conexões | `aeroportos`, `rotas`, `voo_trechos` | Pronto |
| Swing em cinco etapas | `TelaVendaPassagens` | Pronto |
| Transação integral da venda | `GerenciadorDados.confirmarVenda` | Pronto |
| E-ticket PDF | `GeradorPdf`, tabela `etickets` | Pronto |
| Threads | `TarefaEnvioServidor` iniciado após gerar PDF | Pronto |
| Persistência oficial em banco | Passageiros, pagamentos, vendas, assentos e e-tickets em MySQL | Pronto |
| NetBeans/Maven | Projeto Maven com dependências em `pom.xml` | Pronto |

## Comandos de validação executados

```powershell
mvn -q test
mvn -q -DskipTests package
mvn -q test-compile exec:java "-Dexec.mainClass=br.ufes.passagens.config.TesteConexao" "-Dexec.classpathScope=test"
mvn -q test-compile exec:java "-Dexec.mainClass=br.ufes.passagens.config.TesteConsultaVoos" "-Dexec.classpathScope=test"
mvn -q test-compile exec:java "-Dexec.mainClass=br.ufes.passagens.config.TesteVendaCompleta" "-Dexec.classpathScope=test"
mvn -q test-compile exec:java "-Dexec.mainClass=br.ufes.passagens.config.TesteResumoBanco" "-Dexec.classpathScope=test"
```

## Ambiguidades registradas

- O PDF de preços sobrepõe os dias 10 e 20 em duas faixas. A implementação usa faixas inteiras sem sobreposição: 7-10, 11-15, 16-20 e 21-30.
- Para uma viagem só de ida, `RET` é 1,00.
- Porto Alegre e Rio de Janeiro são obrigatórios no projeto final, mas não têm distâncias no PDF do algoritmo. Eles são cadastrados sem rotas comercializáveis; nenhuma distância é inventada.
- O enunciado menciona arquivo para assentos/vendas, mas conclui exigindo todas as operações em banco. MySQL é a persistência oficial e o PDF é o arquivo recuperável do e-ticket.
