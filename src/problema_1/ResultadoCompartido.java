package problema_1;

// Clase para almacenar la suma total de forma segura entre hilos.
public class ResultadoCompartido {

    // Atributo para la suma total.
    private long sumaTotal = 0;

    // Método sincronizado para agregar una suma parcial a la suma total.
    public synchronized void agregarSuma(long valor, String hilo) {
        sumaTotal += valor;
        System.out.println("Hilo: " + hilo + " - Suma Parcial: " + valor + " - Suma Total: " + sumaTotal);
    }

    // Devuelve la suma total acumulada.
    public long getSumaTotal() {
        return sumaTotal;
    }
}
