# Barbearia de Hilzer

Implementação em Java do Problema da Barbearia de Ralph Hilzer (3 barbeiros, 3 cadeiras, sofá com 4 lugares, capacidade total de 20 clientes, 1 POS).

## Estrutura

- `Cliente.java` — thread do cliente (chegada, espera, sofá, atendimento, saída)
- `Barbeiro.java` — thread do barbeiro (chamar cliente, cortar, receber pagamento)
- `Barbearia.java` — monitor central com toda a sincronização (capacidade total, sofá, fila em pé, POS)
- `Main.java` — monta a simulação e inicia as threads
- `LogAuditoria.java` - logs cronometrados