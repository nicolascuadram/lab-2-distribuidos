import java.util.Random;
import java.util.concurrent.Semaphore;

public class RelayRace {

    static final int TRACK_LENGTH = 400;
    static final int LANES = 5;
    static final int COMPETITORS_PER_LANE = 4; // 20 competitors total
    static final int TOTAL_COMPETITORS = LANES * COMPETITORS_PER_LANE;

    // Compartimos las posiciones finales para imprimir al final
    static final int[] finalPositions = new int[TOTAL_COMPETITORS];

    static class Competitor implements Runnable {
        private final int id;           // identificador 1..20
        private final int lane;         // 0..4
        private final int indexInLane;  // 0..3 (0 es el que parte con el testigo)
        private int position;           // posición actual
        private final int target;       // posición objetivo a la que corre (siguiente compañero o meta)
        private final Semaphore myTurn; // semáforo propio: permiso para correr
        private final Semaphore nextTurn; // semáforo del siguiente competidor (null si es el último)
        private final Random rnd;

        public Competitor(int id, int lane, int indexInLane, int startPos, int target, Semaphore myTurn, Semaphore nextTurn) {
            this.id = id;
            this.lane = lane;
            this.indexInLane = indexInLane;
            this.position = startPos;
            this.target = target;
            this.myTurn = myTurn;
            this.nextTurn = nextTurn;
            this.rnd = new Random(System.nanoTime() + id);
        }

        @Override
        public void run() {
            try {
                // Espera hasta que tenga el testigo
                myTurn.acquire();

                // Mientras no alcance su objetivo (siguiente relevo o meta)
                while (position < target) {
                    int step = rnd.nextInt(2) + 1; // 1 o 2
                    position += step;
                    if (position > target) position = target;

                    // pequeño retardo para que las salidas sean legibles (opcional)
                    try { Thread.sleep(10); } catch (InterruptedException ignored) {}
                }

                // Llegó al objetivo -> entregar testigo o terminar si fue el último
                if (nextTurn != null) {
                    // Imprimir entrega de testigo
                    synchronized (System.out) {
                        System.out.printf("Competidor %d (pista %d) entrega testigo a Competidor %d (pista %d)%n",
                                id, lane + 1, id + 1, lane + 1);
                    }
                    // registrar su posición final
                    finalPositions[id - 1] = position;
                    // liberar al siguiente para que comience
                    nextTurn.release();
                } else {
                    // Este era el último competidor de la pista -> cruzó la meta
                    synchronized (System.out) {
                        System.out.printf("Competidor %d (pista %d) cruzó la meta en posición %d%n",
                                id, lane + 1, position);
                    }
                    finalPositions[id - 1] = position;
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // Crear semáforos: uno por competidor
        Semaphore[] sems = new Semaphore[TOTAL_COMPETITORS];
        for (int i = 0; i < TOTAL_COMPETITORS; i++) sems[i] = new Semaphore(0);

        Thread[] threads = new Thread[TOTAL_COMPETITORS];

        // Asignaciones iniciales (posiciones): para cada pista, competidores en 0,100,200,300
        System.out.println("Asignaciones iniciales:");
        int idCounter = 1;
        for (int lane = 0; lane < LANES; lane++) {
            for (int idx = 0; idx < COMPETITORS_PER_LANE; idx++) {
                int startPos = idx * 100; // 0,100,200,300
                System.out.printf("Competidor %d -> pista %d, posición inicial %d%n", idCounter, lane + 1, startPos);
                idCounter++;
            }
        }
        System.out.println();

        // Crear hilos/competidores y determinamos sus targets (siguiente relevo o meta)
        idCounter = 1;
        for (int lane = 0; lane < LANES; lane++) {
            for (int idx = 0; idx < COMPETITORS_PER_LANE; idx++) {
                int startPos = idx * 100;
                int target;
                if (idx < COMPETITORS_PER_LANE - 1) {
                    target = (idx + 1) * 100; // entregar en la posición del siguiente compañero
                } else {
                    target = TRACK_LENGTH; // último corre hasta la meta (400)
                }

                Semaphore my = sems[idCounter - 1];
                Semaphore next = (idx < COMPETITORS_PER_LANE - 1) ? sems[idCounter] : null;

                Competitor c = new Competitor(idCounter, lane, idx, startPos, target, my, next);
                Thread t = new Thread(c, "C" + idCounter);
                threads[idCounter - 1] = t;
                idCounter++;
            }
        }

        // Iniciar todos los hilos (todos threads creados). Solo los que partan con testigo tendrán permiso.
        for (Thread t : threads) t.start();

        // Dar testigo a los competidores en posición 0 (idx == 0 de cada pista)
        // Sus IDs: 1, 1+4, 1+8, 1+12, 1+16 => equivalen a 1,5,9,13,17
        for (int lane = 0; lane < LANES; lane++) {
            int starterId = lane * COMPETITORS_PER_LANE + 1;
            sems[starterId - 1].release(); // liberamos a los de inicio
        }

        // Esperar a que todos lleguen a terminar (todos los hilos finalizan cuando pasan su target)
        for (Thread t : threads) t.join();

        // Imprimir posiciones finales por pista (cada equipo)
        System.out.println("\nPosiciones finales de cada competidor (por pista):");
        idCounter = 1;
        for (int lane = 0; lane < LANES; lane++) {
            System.out.printf("Pista %d: ", lane + 1);
            for (int idx = 0; idx < COMPETITORS_PER_LANE; idx++) {
                System.out.printf("C%d=%d  ", idCounter, finalPositions[idCounter - 1]);
                idCounter++;
            }
            System.out.println();
        }

        System.out.println("\nCarrera finalizada.");
    }
}
