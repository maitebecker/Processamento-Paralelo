# Barbearia de Hilzer

Implementação em Java do Problema da Barbearia de Ralph Hilzer.

## Requisitos:

- Três cadeiras de barbeiro;
- Três barbeiros (Threads trabalhadoras);
- Uma sala de espera com um sofá de quatro lugares;
- O número total de clientes permitidos no recinto é rigorosamente 20;
- Nenhum cliente entrará se a capacidade máxima do local (20 clientes) estiver satisfeita;
- Se o cliente entrou e houver lugar no sofá, ele se senta; caso contrário, ele espera em pé;
- Quando um barbeiro está livre para atender, o cliente que está há mais tempo no sofá é atendido e o que está há mais tempo em pé se senta no sofá (disciplina FIFO em dois níveis);
- Qualquer barbeiro pode aceitar pagamento, mas somente um cliente pode pagar por vez, porque só há uma maquininha POS (exclusão mútua serializada);
- Os barbeiros dividem o seu tempo entre cortar cabelo, receber pagamento e dormir cooperativamente enquanto aguardam por clientes (bloqueio sem consumo de ciclos de CPU).

## Estrutura

- `Cliente.java` - Thread do cliente (chegada, espera, sofá, atendimento, saída)
- `Barbeiro.java` - Thread do barbeiro (chamar cliente, cortar, receber pagamento)
- `Barbearia.java` - Monitor central com toda a sincronização (capacidade total, sofá, fila em pé, POS)
- `App.java` - Monta a simulação e inicia as threads
- `LogAuditoria.java` - logs cronometrados
- `Metricas.java` - Coleta as métricas como tempo total, tempo médio de espera e taxa de utilização dos 3 barbeiros
