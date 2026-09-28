public class LogAuditoria {
    private final long inicioNs = System.nanoTime();

    public synchronized void log(String mensagem) {
        long decorridoMs = (System.nanoTime() - inicioNs) / 1_000_000;
        long minutos = decorridoMs / 60000;
        long segundos = (decorridoMs % 60000) / 1000;
        long millis = decorridoMs % 1000;
        String timestamp = String.format("[%02d:%02d.%03d]", minutos, segundos, millis);
        System.out.println(timestamp + " [" + Thread.currentThread().getName() + "] " + mensagem);
    }
}