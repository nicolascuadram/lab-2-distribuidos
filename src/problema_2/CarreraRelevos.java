package problema_2;

import java.util.*;
import java.util.concurrent.Semaphore;

// Simulación de una carrera de relevos con varios equipos y corredores.
// Cada equipo tiene 4 corredores que se relevan usando semáforos para sincronizar la entrega del testigo.
public class CarreraRelevos {

    // Constantes de la pista y equipos.
    static final int LONGITUD_PISTA = 400;
    static final int CANTIDAD_PISTAS = 5;
    static final int COMPETIDORES_POR_PISTA = 4;
    static final int TOTAL_COMPETIDORES = CANTIDAD_PISTAS * COMPETIDORES_POR_PISTA;

    // Arreglo para almacenar posiciones finales de cada competidor.
    static final int[] posicionesFinales = new int[TOTAL_COMPETIDORES];

    // Lista sincronizada para registrar el orden de llegada de los equipos.
    static final List<Integer> resultadosEquipos = Collections.synchronizedList(new ArrayList<>());

    // Clase interna que representa a cada competidor/corredor.
    // Implementa Runnable para ser ejecutado en un hilo.
    static class Competidor implements Runnable {
        private final int id;
        private final int pista;
        private int posicion;
        private final int objetivo;
        private final Semaphore miTurno;
        private final Semaphore siguienteTurno;
        private final Random random;

        /*
        Constructor del competidor.
        - "id" Identificador único.
        - "pista" Número de pista/equipo.
        - "posicion" Posición actual.
        - "objetivo" Posición objetivo.
        - "miTurno" Semáforo propio.
        - "siguienteTurno" Semáforo del siguiente corredor (null si es el último).
        - "random" Generador de números aleatorios para simular avance.
        */
        public Competidor(int id, int pista, int posicionInicial, int objetivo, Semaphore miTurno, Semaphore siguienteTurno) {
            this.id = id;
            this.pista = pista;
            this.posicion = posicionInicial;
            this.objetivo = objetivo;
            this.miTurno = miTurno;
            this.siguienteTurno = siguienteTurno;
            this.random = new Random(System.nanoTime() + id);
        }

        // Método principal del hilo: espera el testigo, avanza hasta el objetivo y entrega el testigo.
        @Override
        public void run() {
            try {
                // Espera hasta que tenga el testigo (permiso para correr).
                miTurno.acquire();

                // Avanza hasta alcanzar el objetivo (siguiente relevo o meta).
                while (posicion < objetivo) {
                    // Avanza 1 o 2 posiciones de manera aleatoria.
                    int paso = random.nextInt(2) + 1; // Avanza 1 o 2 unidades
                    posicion += paso;
                    if (posicion > objetivo) posicion = objetivo;

                    // Pequeño retardo para la simulación.
                    try { Thread.sleep(10); } catch (InterruptedException ignored) {}
                }

                // Al llegar al objetivo, entrega el testigo o termina si es el último.
                if (siguienteTurno != null) {
                    // Imprime la entrega del testigo al siguiente corredor.
                    synchronized (System.out) {
                        System.out.println("Competidor " + id + " (pista " + (pista + 1) + ") entrega testigo a Competidor " + (id + 1) + " (pista " + (pista + 1) + ")");
                    }

                    // Registra la posición final del corredor.
                    posicionesFinales[id - 1] = posicion;

                    // Libera al siguiente corredor para que comience.
                    siguienteTurno.release();
                } else {
                    // Último competidor cruza la meta.
                    synchronized (System.out) {
                        System.out.println("Competidor " + id + " (pista " + (pista + 1) + ") cruzó la meta en posición " + posicion);
                    }

                    // Registra la posición final del corredor.
                    posicionesFinales[id - 1] = posicion;

                    // Registra el equipo que llegó.
                    resultadosEquipos.add(pista + 1);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // Método principal: inicializa la carrera, crea los corredores y gestiona la simulación.
    public static void main(String[] args) throws InterruptedException {

        // Crear semáforos para controlar el turno de cada competidor.
        Semaphore[] semaforos = new Semaphore[TOTAL_COMPETIDORES];
        for (int i = 0; i < TOTAL_COMPETIDORES; i++) semaforos[i] = new Semaphore(0);

        // Crear los hilos de los corredores.
        Thread[] hilos = new Thread[TOTAL_COMPETIDORES];

        // Mostrar asignaciones iniciales de los corredores.
        System.out.println("Asignaciones iniciales:");
        int contadorId = 1;
        for (int pista = 0; pista < CANTIDAD_PISTAS; pista++) {
            for (int idx = 0; idx < COMPETIDORES_POR_PISTA; idx++) {
                // Posiciones iniciales: 0, 100, 200, 300.
                int posicionInicial = idx * 100;
                System.out.println("Competidor " + contadorId + " -> pista " + (pista + 1) + ", posición inicial " + posicionInicial);
                contadorId++;
            }
        }
        System.out.println();

        // Crear los hilos de los competidores y definir sus objetivos.
        contadorId = 1;
        for (int pista = 0; pista < CANTIDAD_PISTAS; pista++) {
            for (int idx = 0; idx < COMPETIDORES_POR_PISTA; idx++) {
                int posicionInicial = idx * 100;
                int objetivo = (idx < COMPETIDORES_POR_PISTA - 1) ? (idx + 1) * 100 : LONGITUD_PISTA;

                Semaphore miSemaforo = semaforos[contadorId - 1];
                Semaphore siguienteSemaforo = (idx < COMPETIDORES_POR_PISTA - 1) ? semaforos[contadorId] : null;

                Competidor competidor = new Competidor(contadorId, pista, posicionInicial, objetivo, miSemaforo, siguienteSemaforo);
                Thread hilo = new Thread(competidor, "C" + contadorId);
                hilos[contadorId - 1] = hilo;
                contadorId++;
            }
        }

        // Iniciar todos los hilos (corredores). Solo los primeros de cada pista tienen permiso inicial.
        for (Thread hilo : hilos) hilo.start();

        // Liberar el turno de los primeros competidores de cada pista.
        for (int pista = 0; pista < CANTIDAD_PISTAS; pista++) {
            int idInicial = pista * COMPETIDORES_POR_PISTA + 1;
            semaforos[idInicial - 1].release();
        }

        // Esperar a que todos los corredores (hilos) terminen.
        for (Thread hilo : hilos) hilo.join();

        // Mostrar el ranking final de los equipos según orden de llegada.
        System.out.println("\nResultados finales de la carrera:");
        for (int i = 0; i < resultadosEquipos.size(); i++) {
            int pista = resultadosEquipos.get(i);
            System.out.println((i + 1) + "° lugar: Equipo de la pista " + pista);
        }

        System.out.println("\nCarrera finalizada.");
    }
}
