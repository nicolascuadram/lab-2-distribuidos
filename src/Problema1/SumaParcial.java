package Problema1;

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