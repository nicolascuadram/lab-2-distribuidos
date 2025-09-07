class SumaParcial extends Thread {
    private int[][] matriz;
    private int filaInicio;
    private int filaFin;
    private long sumaLocal;
    private ResultadoCompartido resultado;

    public SumaParcial(int[][] matriz, int filaInicio, int filaFin, ResultadoCompartido resultado) {
        this.matriz = matriz;
        this.filaInicio = filaInicio;
        this.filaFin = filaFin;
        this.resultado = resultado;
    }

    @Override
    public void run() {
        sumaLocal = 0;
        for (int i = filaInicio; i < filaFin; i++) {
            for (int j = 0; j < matriz[i].length; j++) {
                sumaLocal += matriz[i][j];
            }
        }
        resultado.agregarSuma(sumaLocal, Thread.currentThread().getName());
    }
}

class ResultadoCompartido {
    private long sumaTotal = 0;

    public synchronized void agregarSuma(long valor, String hilo) {
        sumaTotal += valor;
        System.out.println("Hilo: " + hilo + " - Suma Parcial: " + valor + " - Suma Total: " + sumaTotal);
    }

    public long getSumaTotal() {
        return sumaTotal;
    }
}

public class SumaMatrizConcurrente {

    // Método secuencial
    public static long sumaSecuencial(int[][] matriz) {
        long suma = 0;
        for (int i = 0; i < matriz.length; i++) {
            for (int j = 0; j < matriz[i].length; j++) {
                suma += matriz[i][j];
            }
        }
        return suma;
    }

    public static void main(String[] args) {
        int filas = 10000;
        int columnas = 10000;

        int[][] matriz = new int[filas][columnas];
        // Llenar matriz con valores conocidos (todos 1)
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                matriz[i][j] = 1;
            }
        }

        // --- Suma Secuencial ---
        long inicioSec = System.currentTimeMillis();
        long resultadoSecuencial = sumaSecuencial(matriz);
        long finSec = System.currentTimeMillis();
        System.out.println("Suma secuencial: " + resultadoSecuencial);
        System.out.println("Tiempo secuencial: " + (finSec - inicioSec) + " ms");
        System.out.println("");

        // --- Suma Concurrente ---
        ResultadoCompartido resultado = new ResultadoCompartido();

        // Definir número de hilos según número de filas o núcleos disponibles
        int numHilos = Math.min(filas, Runtime.getRuntime().availableProcessors());

        SumaParcial[] hilos = new SumaParcial[numHilos];
        int bloque = filas / numHilos;

        long inicioConc = System.currentTimeMillis();

        for (int i = 0; i < numHilos; i++) {
            int inicio = i * bloque;
            int fin = (i == numHilos - 1) ? filas : inicio + bloque;
            hilos[i] = new SumaParcial(matriz, inicio, fin, resultado);
            hilos[i].start();
        }

        for (int i = 0; i < numHilos; i++) {
            try {
                hilos[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        long finConc = System.currentTimeMillis();

        System.out.println("");
        System.out.println("Suma concurrente: " + resultado.getSumaTotal());
        System.out.println("Tiempo concurrente: " + (finConc - inicioConc) + " ms");
    }
}
