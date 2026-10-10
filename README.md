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

## Como compilar

No PowerShell, entre na pasta `src` do projeto, que contém os arquivos `.java`, e compile:

```powershell
cd barbearia\src
javac App.java
```

Todos os comandos seguintes (execução, geração do `.jar` e experimento) também devem ser rodados a partir dessa pasta.

## Como executar

```powershell
java App [numClientes] [seed]
```

Os dois argumentos são opcionais:

| Argumento     | Padrão    | Descrição                                                                                                          |
| ------------- | --------- | ------------------------------------------------------------------------------------------------------------------ |
| `numClientes` | 50        | Quantidade de clientes gerados                                                                                     |
| `seed`        | aleatória | Semente do gerador de números aleatórios. Com a mesma seed, os intervalos de chegada se repetem entre as execuções |

Exemplos:

```powershell
java App                # 50 clientes, seed aleatória
java App 50 12345       # 50 clientes, seed 12345
java App 80 12345       # 80 clientes, seed 12345
```

## Como gerar o `Barbearia.jar`

```powershell
javac -d out App.java Barbearia.java Barbeiro.java Cliente.java Metricas.java LogAuditoria.java
jar cfe Barbearia.jar App -C out .
jar tf Barbearia.jar
java -jar Barbearia.jar 50 12345
```

## Experimento: 1 núcleo vs. multicore (Windows)

No PowerShell, na pasta que contém o `Barbearia.jar`. Use a **mesma seed** nos dois cenários.

**Cenário A — 1 único núcleo lógico de CPU (afinidade 1)**

```powershell
Start-Process cmd -ArgumentList '/k start /affinity 1 /b /wait java -jar Barbearia.jar 50 12345'
```

**Cenário B — multicore livre**

```powershell
java -jar Processamento-Paralelo.jar 50 12345
```

Para conferir a afinidade, abra o Gerenciador de Tarefas, vá em **Detalhes**, clique com o botão direito em `java.exe` e escolha **Definir afinidade**. No Cenário A deve estar marcado apenas o CPU 0.

## Saída

O log de auditoria é impresso no console, uma linha por evento:

```powershell
[00:01.120] [Thread-Cliente-04] CHEGADA: Cliente-04 entrou na barbearia (Lotacao: 12/20)
```

Eventos registrados: `CHEGADA`, `RECUSA`, `SOFA` (sentou direto), `ESPERA` (em pé), `PROMOCAO` (em pé → sofá), `ATENDIMENTO`, `CORTE`, `FILA-POS`, `PAGAMENTO` e `SAIDA`.

No final aparecem `Simulacao encerrada.` e uma linha `METRICAS`, com:

- `clientes_concluidos` (clientes atendidos até a saída; confira este valor, pois clientes recusados não entram)
- `tempo_total_ms`
- `espera_media_em_pe_ms`, `espera_media_sofa_ms` e `espera_media_pos_ms`
- `taxa_utilizacao_barbeiro1_percentual`, `..2_..` e `..3_..`
