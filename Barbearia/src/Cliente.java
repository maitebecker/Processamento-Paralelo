import java.util.concurrent.Semaphore;

public class Cliente implements Runnable {

    public final int id;
    private final Barbearia barbearia;

    // Liberado pelo Barbeiro quando corte e o pagamento deste cliente terminam.
    final Semaphore atendimentoConcluido = new Semaphore(0);

    public Cliente(int id, Barbearia barbearia) {
        this.id = id;
        this.barbearia = barbearia;
    }

    @Override
    public void run() {
        if (!barbearia.tentarEntrar(this)) {
            return; // barbearia lotada: cliente desiste, não espera
        }

        try {
            barbearia.entrarNaFila(this);
            atendimentoConcluido.acquire(); // bloqueia até ser atendido
            barbearia.sair(this);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public String toString() {
        return "Cliente-" + String.format("%02d", id);
    }
}
