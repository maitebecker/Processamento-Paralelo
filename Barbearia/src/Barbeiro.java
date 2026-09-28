public class Barbeiro implements Runnable {

    public final int id; // também é o número da cadeira
    private final Barbearia barbearia;
    private volatile boolean encerrar = false;

    public Barbeiro(int id, Barbearia barbearia) {
        this.id = id;
        this.barbearia = barbearia;
    }

    public void encerrar() {
        this.encerrar = true;
    }

    @Override
    public void run() {
        try {
            while (!encerrar) {
                Cliente c = barbearia.chamarProximoCliente(this); // dorme se sofá vazio
                barbearia.cortarCabelo(c, this);
                barbearia.pagar(c, this);
                c.atendimentoConcluido.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public String toString() {
        return "Barbeiro-" + String.format("%02d", id);
    }
}
