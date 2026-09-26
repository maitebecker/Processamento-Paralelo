import java.util.ArrayList;
import java.util.List;

public class App {

    private static final int NUM_BARBEIROS = 3;
    private static final int NUM_CLIENTES = 50;
    private static final int INTERVALO_CHEGADA_MIN_MS = 50;
    private static final int INTERVALO_CHEGADA_MAX_MS = 300;

    public static void main(String[] args) throws InterruptedException {
        int numClientes = NUM_CLIENTES;

        Barbearia barbearia = new Barbearia();

        List<Barbeiro> barbeiros = new ArrayList<>();
        List<Thread> threadsBarbeiros = new ArrayList<>();
        for (int i = 1; i <= NUM_BARBEIROS; i++) {
            Barbeiro b = new Barbeiro(i, barbearia);
            Thread t = new Thread(b, "Thread-Barbeiro-" + String.format("%02d", i));
            barbeiros.add(b);
            threadsBarbeiros.add(t);
            t.start();
        }

        List<Thread> threadsClientes = new ArrayList<>();
        for (int i = 1; i <= numClientes; i++) {
            Cliente c = new Cliente(i, barbearia);
            Thread t = new Thread(c, "Thread-Cliente-" + String.format("%02d", i));
            threadsClientes.add(t);
            t.start();
            Thread.sleep(randomEntre(INTERVALO_CHEGADA_MIN_MS, INTERVALO_CHEGADA_MAX_MS));
        }

        for (Thread t : threadsClientes) {
            t.join();
        }

        for (Barbeiro b : barbeiros) {
            b.encerrar();
        }

        for (Thread t : threadsBarbeiros) {
            t.interrupt();
        }

        for (Thread t : threadsBarbeiros) {
            t.join();
        }

        System.out.println("Simulacao encerrada.");
    }

    private static int randomEntre(int min, int max) {
        return min + (int) (Math.random() * (max - min + 1));
    }
}
