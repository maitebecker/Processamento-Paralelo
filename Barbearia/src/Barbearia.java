import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Barbearia {

    private static final int CAPACIDADE_TOTAL = 20;
    private static final int VAGAS_SOFA = 4;

    private static final int TEMPO_CORTE_MIN_MS = 800;
    private static final int TEMPO_CORTE_MAX_MS = 1500;
    private static final int TEMPO_PAGAMENTO_MIN_MS = 200;
    private static final int TEMPO_PAGAMENTO_MAX_MS = 400;

    private final Semaphore capacidadeTotal = new Semaphore(CAPACIDADE_TOTAL, true);
    private final Semaphore pos = new Semaphore(1, true);

    private final ReentrantLock lock = new ReentrantLock(true);
    private final Condition sofaTemVaga = lock.newCondition();
    private final Condition sofaTemCliente = lock.newCondition();

    private final Queue<Cliente> filaEmPe = new LinkedList<>();
    private final Queue<Cliente> filaSofa = new LinkedList<>();

    private final AtomicInteger lotacaoAtual = new AtomicInteger(0);
    LogAuditoria auditoria = new LogAuditoria();

    boolean tentarEntrar(Cliente c) {
        if (!capacidadeTotal.tryAcquire()) {
            auditoria.log("RECUSA: " + c + " nao entrou - barbearia lotada (" + CAPACIDADE_TOTAL + "/"
                    + CAPACIDADE_TOTAL + ")");
            return false;
        }
        int lotacao = lotacaoAtual.incrementAndGet();
        auditoria.log("CHEGADA: " + c + " entrou na barbearia (Lotacao: " + lotacao + "/" + CAPACIDADE_TOTAL + ")");
        return true;
    }

    void entrarNaFila(Cliente c) throws InterruptedException {
        lock.lock();
        try {
            // Se tem vaga no sofá e ninguém na frente esperando em pé, o cliente
            // senta direto, sem passar pela fila em pé
            if (filaEmPe.isEmpty() && filaSofa.size() < VAGAS_SOFA) {
                filaSofa.add(c);
                auditoria.log("SOFA: " + c + " sentou direto no sofa (posicao " + filaSofa.size() + " do sofa)");
                sofaTemCliente.signalAll();
                return;
            }

            filaEmPe.add(c);
            auditoria.log("ESPERA: " + c + " aguardando em pe (posicao " + filaEmPe.size() + " da fila em pe)");

            // Só sai da fila em pé quando tiver vaga no sofá e for o cliente
            // mais antigo da fila em pé.
            while (filaSofa.size() >= VAGAS_SOFA || filaEmPe.peek() != c) {
                sofaTemVaga.await();
            }

            filaEmPe.poll();
            filaSofa.add(c);
            auditoria.log("PROMOCAO: " + c + " assumiu assento no sofa (posicao " + filaSofa.size() + " do sofa)");
            sofaTemCliente.signalAll();
        } finally {
            lock.unlock();
        }
    }

    // Barbeiro chama o cliente mais antigo do sofá
    Cliente chamarProximoCliente(Barbeiro b) throws InterruptedException {
        lock.lock();
        try {
            while (filaSofa.isEmpty()) {
                sofaTemCliente.await(); // barbeiro "dorme"
            }
            Cliente c = filaSofa.poll();
            sofaTemVaga.signalAll(); // libera vaga do sofá para o próximo em pé
            auditoria.log("ATENDIMENTO: " + b + " chamou " + c + " do sofa para a Cadeira " + b.id);
            return c;
        } finally {
            lock.unlock();
        }
    }

    void cortarCabelo(Cliente c, Barbeiro b) throws InterruptedException {
        int tempo = randomEntre(TEMPO_CORTE_MIN_MS, TEMPO_CORTE_MAX_MS);
        Thread.sleep(tempo);
        auditoria.log("CORTE: " + b + " concluiu o corte de " + c + " (" + tempo + "ms)");
    }

    void pagar(Cliente c, Barbeiro b) throws InterruptedException {
        auditoria.log("FILA-POS: " + c + " aguardando a maquina POS (atendido por " + b + ")");
        pos.acquire();
        try {
            int tempo = randomEntre(TEMPO_PAGAMENTO_MIN_MS, TEMPO_PAGAMENTO_MAX_MS);
            Thread.sleep(tempo);
            auditoria.log("PAGAMENTO: " + c + " concluiu pagamento com " + b + " (" + tempo + "ms)");
        } finally {
            pos.release();
        }
    }

    void sair(Cliente c) {
        int lotacao = lotacaoAtual.decrementAndGet();
        capacidadeTotal.release();
        auditoria.log("SAiDA: " + c + " saiu da barbearia (Lotacao: " + lotacao + "/" + CAPACIDADE_TOTAL + ")");
    }

    private int randomEntre(int min, int max) {
        return min + (int) (Math.random() * (max - min + 1));
    }
}
