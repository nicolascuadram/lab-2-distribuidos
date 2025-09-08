package problema_1;

// Hilo que suma una parte (bloque de filas) de la matriz y agrega el resultado al objeto compartido.
public class SumaParcial extends Thread {

    // Atributos del hilo.
    private int[][] matriz;
    private int filaInicio;
    private int filaFin;
    private long sumaLocal;
    private ResultadoCompartido resultado;

    /*
    Constructor del hilo.
    - "matriz" Matriz a sumar.
    - "filaInicio" Fila inicial (inclusive).
    - "filaFin" Fila final (exclusive).
    - "resultado" Objeto compartido para acumular la suma.
    */ 
    public SumaParcial(int[][] matriz, int filaInicio, int filaFin, ResultadoCompartido resultado) {
        this.matriz = matriz;
        this.filaInicio = filaInicio;
        this.filaFin = filaFin;
        this.resultado = resultado;
    }

    // Método que ejecuta el hilo, suma su bloque de filas y agrega la suma al resultado compartido.
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