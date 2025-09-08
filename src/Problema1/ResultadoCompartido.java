package Problema1;

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
