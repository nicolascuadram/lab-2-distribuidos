package problema_1;

// Clase principal que compara la suma secuencial y concurrente de una matriz grande.
public class Main {

    // Función que suma todos los elementos de la matriz de forma secuencial.
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

        // Definir tamaño de la matriz.
        int filas = 10000;
        int columnas = 10000;

        // Crear y llenar la matriz con valores conocidos (todos 1).
        int[][] matriz = new int[filas][columnas];
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                matriz[i][j] = 1;
            }
        }

        /* Suma Secuencial. */

        // Se inicia a medir tiempo de la suma secuencial.
        long inicioSec = System.currentTimeMillis();

        // Realizar la suma secuencial.
        long resultadoSecuencial = sumaSecuencial(matriz);

        // Se termina de medir tiempo de la suma secuencial.
        long finSec = System.currentTimeMillis();

        // Se muestran los resultados de la suma secuencial.
        System.out.println("Suma secuencial: " + resultadoSecuencial);
        System.out.println("Tiempo secuencial: " + (finSec - inicioSec) + " ms");
        System.out.println("");

        /* Suma Concurrente. */

        // Crear objeto para almacenar el resultado compartido.
        ResultadoCompartido resultado = new ResultadoCompartido();

        // Definir número de hilos según núcleos disponibles o filas.
        int numHilos = Math.min(filas, Runtime.getRuntime().availableProcessors());

        // Crear los hilos y definir el bloque de filas que cada hilo procesará.
        SumaParcial[] hilos = new SumaParcial[numHilos];
        int bloque = filas / numHilos;

        // Se inicia a medir tiempo de la suma concurrente.
        long inicioConc = System.currentTimeMillis();

        // Crear y lanzar los hilos, cada uno suma un bloque de filas.
        for (int i = 0; i < numHilos; i++) {
            int inicio = i * bloque;
            int fin = (i == numHilos - 1) ? filas : inicio + bloque;
            hilos[i] = new SumaParcial(matriz, inicio, fin, resultado);
            hilos[i].start();
        }

        // Esperar a que todos los hilos terminen.
        for (int i = 0; i < numHilos; i++) {
            try {
                hilos[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Se termina de medir tiempo de la suma concurrente.
        long finConc = System.currentTimeMillis();

        // Se muestran los resultados de la suma concurrente.
        System.out.println("");
        System.out.println("Suma concurrente: " + resultado.getSumaTotal());
        System.out.println("Tiempo concurrente: " + (finConc - inicioConc) + " ms");
    }

}
