public final class Metricas {
    private final long inicioSimulacao = System.nanoTime();
    private final long[] chegada;
    private final long[] entradaSofa;
    private final long[] chamada;
    private final long[] solicitacaoPos;
    private final long[] entradaPos;
    private final long[] saida;
    private final long[] inicioAtendimento = new long[4];
    private final long[] tempoAtendimento = new long[4];
    private long fimSimulacao;

    public Metricas(int quantidadeClientes) {
        int tamanho = quantidadeClientes + 1;
        chegada = new long[tamanho];
        entradaSofa = new long[tamanho];
        chamada = new long[tamanho];
        solicitacaoPos = new long[tamanho];
        entradaPos = new long[tamanho];
        saida = new long[tamanho];
    }

    public synchronized void chegada(Cliente cliente) {
        chegada[cliente.id] = System.nanoTime();
    }

    public synchronized void entradaSofa(Cliente cliente) {
        entradaSofa[cliente.id] = System.nanoTime();
    }

    public synchronized void chamada(Cliente cliente, Barbeiro barbeiro) {
        long agora = System.nanoTime();
        chamada[cliente.id] = agora;
        inicioAtendimento[barbeiro.id] = agora;
    }

    public synchronized void solicitacaoPos(Cliente cliente) {
        solicitacaoPos[cliente.id] = System.nanoTime();
    }

    public synchronized void entradaPos(Cliente cliente) {
        entradaPos[cliente.id] = System.nanoTime();
    }

    public synchronized void saida(Cliente cliente) {
        saida[cliente.id] = System.nanoTime();
    }

    public synchronized void finalizarAtendimento(Barbeiro barbeiro) {
        if (inicioAtendimento[barbeiro.id] != 0) {
            tempoAtendimento[barbeiro.id] += System.nanoTime() - inicioAtendimento[barbeiro.id];
            inicioAtendimento[barbeiro.id] = 0;
        }
    }

    public synchronized String resumo() {
        fimSimulacao = System.nanoTime();
        long totalEsperaEmPe = 0;
        long totalEsperaSofa = 0;
        long totalEsperaPos = 0;
        int clientesConcluidos = 0;

        for (int id = 1; id < saida.length; id++) {
            if (saida[id] == 0) { // o cliente ainda não saiu ou foi recusado
                continue;
            }
            clientesConcluidos++;
            totalEsperaEmPe += entradaSofa[id] - chegada[id];
            totalEsperaSofa += chamada[id] - entradaSofa[id];
            totalEsperaPos += entradaPos[id] - solicitacaoPos[id];
        }

        double divisor = clientesConcluidos == 0 ? 1 : clientesConcluidos;
        double totalMs = (fimSimulacao - inicioSimulacao) / 1_000_000.0;
        StringBuilder resultado = new StringBuilder();
        resultado.append("METRICAS clientes_concluidos=").append(clientesConcluidos)
                .append(" tempo_total_ms=").append(formatar(totalMs))
                .append(" espera_media_em_pe_ms=").append(formatar(totalEsperaEmPe / divisor / 1_000_000.0))
                .append(" espera_media_sofa_ms=").append(formatar(totalEsperaSofa / divisor / 1_000_000.0))
                .append(" espera_media_pos_ms=").append(formatar(totalEsperaPos / divisor / 1_000_000.0));

        for (int barbeiroId = 1; barbeiroId <= 3; barbeiroId++) {
            double utilizacao = totalMs == 0 ? 0
                    : tempoAtendimento[barbeiroId] / 1_000_000.0 / totalMs * 100.0;
            resultado.append(" taxa_utilizacao_barbeiro").append(barbeiroId).append("_percentual=").append(formatar(utilizacao));
        }
        return resultado.toString();
    }

    private String formatar(double valor) {
        return String.format(java.util.Locale.forLanguageTag("pt-BR"), "%.2f", valor);
    }
}
