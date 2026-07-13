# Verificacao dos requisitos

| Requisito do professor | Implementacao | Estado |
|---|---|---|
| Java e POO | `model/`, `service/`, `repository/`, `ui/` | Pronto |
| Encapsulamento | Modelos com atributos privados e validacao no construtor | Pronto |
| Heranca e polimorfismo | `Pagamento`, `PagamentoCartao`, `PagamentoCredito`, `PagamentoDebito`, `PagamentoDinheiro` | Pronto e testado |
| Algoritmo de precos | `CalculadoraPreco` com `BigDecimal` | Pronto e testado |
| MySQL e Connector/J | `pom.xml`, `config/`, `database/schema.sql`, `database/seed.sql` | Pronto e validado localmente |
| Metodos `Conectar`, `InserirDadosNoMySQL`, `Desconectar` | `ConexaoMySQL` | Pronto e testado por reflexao/conexao |
| Assento sem duplicidade | `uk_voo_assento`, `uk_venda_assento`, `SELECT ... FOR UPDATE` | Pronto |
| Aeroportos e conexoes | `aeroportos`, `rotas`, `voo_trechos` | Pronto |
| Swing em cinco etapas | `TelaVendaPassagens` | Pronto |
| Transacao integral da venda | `GerenciadorDados.confirmarVenda` | Pronto |
| E-ticket PDF | `GeradorPdf`, tabela `etickets` | Pronto |
| Threads | `TarefaEnvioServidor` iniciado apos gerar PDF | Pronto |
| Persistencia oficial em banco | Passageiros, pagamentos, vendas, assentos e e-tickets em MySQL | Pronto |
| NetBeans/Maven | Projeto Maven com dependencias em `pom.xml` | Pronto |

## Comandos de validacao executados

```powershell
mvn -q test
mvn -q -DskipTests package
mvn -q exec:java "-Dexec.mainClass=br.ufes.passagens.config.TesteConexao"
mvn -q compile exec:java "-Dexec.mainClass=br.ufes.passagens.config.TesteConsultaVoos"
mvn -q compile exec:java "-Dexec.mainClass=br.ufes.passagens.config.TesteVendaCompleta"
mvn -q compile exec:java "-Dexec.mainClass=br.ufes.passagens.config.TesteResumoBanco"
```

## Ambiguidades registradas

- O PDF de precos sobrepoe os dias 10 e 20 em duas faixas. A implementacao usa faixas inteiras sem sobreposicao: 7-10, 11-15, 16-20 e 21-30.
- Para uma viagem so de ida, `RET` e 1,00.
- Porto Alegre e Rio de Janeiro sao obrigatorios no projeto final, mas nao tem distancias no PDF do algoritmo. Eles sao cadastrados sem rotas comercializaveis; nenhuma distancia e inventada.
- O enunciado menciona arquivo para assentos/vendas, mas conclui exigindo todas as operacoes em banco. MySQL e a persistencia oficial e o PDF e o arquivo recuperavel do e-ticket.
