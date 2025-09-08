import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class RelayRace {

    static final int largoPista = 400;
    static final int pistas = 5;
    static final int corredoresPorPistas = 4; // 20 competitors total
    static final int totalCorredores = pistas * corredoresPorPistas;

    // Compartimos las posiciones finales para imprimir al final
    static final int[] posicionesFinales = new int[totalCorredores];
    static final List<Integer> resultadosEquipos = Collections.synchronizedList(new ArrayList<>());

    static class Competidor implements Runnable {
        private final int id;           // identificador 1..20
        private final int pista;         // 0..4
        private final int indexTestigo;  // 0..3 (0 es el que parte con el testigo)
        private int posicion;           // posición actual
        private final int target;       // posición objetivo a la que corre (siguiente compañero o meta)
        private final Semaphore miTurno; // semáforo propio: permiso para correr
        private final Semaphore siguienteTurno; // semáforo del siguiente competidor (null si es el último)
        private final Random rnd;

        public Competidor(int id, int pista, int indexTestigo, int startPos, int target, Semaphore miTurno, Semaphore siguienteTurno) {
            this.id = id;
            this.pista = pista;
            this.indexTestigo = indexTestigo;
            this.posicion = startPos;
            this.target = target;
            this.miTurno = miTurno;
            this.siguienteTurno= siguienteTurno;
            this.rnd = new Random(System.nanoTime() + id);
        }

        @Override
        public void run() {
            try {
                // Espera hasta que tenga el testigo
                miTurno.acquire();

                // Mientras no alcance su objetivo (siguiente relevo o meta)
                while (posicion < target) {
                    int paso = rnd.nextInt(2) + 1; // 1 o 2
                    posicion += paso;
                    if (posicion > target) posicion = target;

                    // pequeño retardo para que las salidas sean legibles (opcional)
                    try { Thread.sleep(10); } catch (InterruptedException ignored) {}
                }

                // Llegó al objetivo -> entregar testigo o terminar si fue el último
                if (miTurno != null) {
                    // Imprimir entrega de testigo
                    synchronized (System.out) {
                        System.out.printf("Competidor %d (pista %d) entrega testigo a Competidor %d (pista %d)%n",
                                id, pista + 1, id + 1, pista+ 1);
                    }
                    // registrar su posición final
                    posicionesFinales[id - 1] = posicion;
                    // liberar al siguiente para que comience
                    siguienteTurno.release();
                } else {
                    // Este era el último competidor de la pista -> cruzó la meta
                    synchronized (System.out) {
                        System.out.printf("Competidor %d (pista %d) cruzó la meta en posición %d%n",
                                id, pista + 1, posicion);
                    }
                    posicionesFinales[id - 1] = posicion;
                    resultadosEquipos.add(pista + 1); // registrar qué pista llegó
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // Crear semáforos: uno por competidor
        Semaphore[] sems = new Semaphore[totalCorredores];
        for (int i = 0; i < totalCorredores; i++) sems[i] = new Semaphore(0);

        Thread[] threads = new Thread[totalCorredores];

        // Asignaciones iniciales (posiciones): para cada pista, competidores en 0,100,200,300
        System.out.println("Asignaciones iniciales:");
        int idContador = 1;
        for (int pista = 0; pista < pistas; pista++) {
            for (int idx = 0; idx < corredoresPorPistas; idx++) {
                int startPos = idx * 100; // 0,100,200,300
                System.out.printf("Competidor %d -> pista %d, posición inicial %d%n", idContador, pista + 1, startPos);
                idContador++;
            }
        }
        System.out.println();

        // Crear hilos/competidores y determinamos sus targets (siguiente relevo o meta)
        idContador = 1;
        for (int pista = 0; pista < pistas; pista++) {
            for (int idx = 0; idx < corredoresPorPistas; idx++) {
                int startPos = idx * 100;
                int target = (idx < corredoresPorPistas - 1) ? (idx + 1) * 100 : largoPista;
                /* int target;
                if (idx < corredoresPorPistas - 1) {
                    target = (idx + 1) * 100; // entregar en la posición del siguiente compañero
                } else {
                    target = largoPista; // último corre hasta la meta (400)
                } */

                Semaphore actual = sems[idContador - 1];
                Semaphore next = (idx < corredoresPorPistas - 1) ? sems[idContador] : null;

                Competidor c = new Competidor(idContador, pista, idx, startPos, target, actual, next);
                Thread t = new Thread(c, "C" + idContador);
                threads[idContador - 1] = t;
                idContador++;
            }
        }

        // Iniciar todos los hilos (todos threads creados). Solo los que partan con testigo tendrán permiso.
        for (Thread t : threads) t.start();

        // Dar testigo a los competidores en posición 0 (idx == 0 de cada pista)
        // Sus IDs: 1, 1+4, 1+8, 1+12, 1+16 => equivalen a 1,5,9,13,17
        for (int pista = 0; pista< pistas; pista++) {
            int starterId = pista * corredoresPorPistas + 1;
            sems[starterId - 1].release(); // liberamos a los de inicio
        }

        // Esperar a que todos lleguen a terminar (todos los hilos finalizan cuando pasan su target)
        for (Thread t : threads) t.join();

        // Imprimir posiciones finales por pista (cada equipo)
        /* System.out.println("\nPosiciones finales de cada competidor (por pista):");
        idCounter = 1;
        for (int pista = 0; pista< pistas; pista++) {
            System.out.printf("Pista %d: ", pista + 1);
            for (int idx = 0; idx < corredoresPorPistas; idx++) {
                System.out.printf("C%d=%d  ", idCounter, finalPositions[idCounter - 1]);
                idCounter++;
            }
            System.out.println();
        } */

        // Ranking de equipos
        System.out.println("\nResultados finales de la carrera:");
        for (int i = 0; i < resultadosEquipos.size(); i++) {
            int pista = resultadosEquipos.get(i);
            System.out.printf("%d° lugar: Equipo de la pista %d%n", i + 1, pista);
        }

        System.out.println("\nCarrera finalizada.");
    }
}
