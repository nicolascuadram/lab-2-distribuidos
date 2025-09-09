# lab-2-distribuidos

Repositorio del laboratorio N°2 del curso de Sistemas Distribuidos.

## Integrantes:
- Nicolás Cuadra
- Cristian Fuentes

---

## Descripción de la tarea

Este laboratorio aborda dos problemas clásicos de concurrencia y paralelismo, utilizando Java para demostrar el uso de hilos y sincronización.

### Problema 1: Suma de Matriz Secuencial y Concurrente

Se implementa una comparación entre el cálculo de la suma de todos los elementos de una matriz grande de manera secuencial y de manera concurrente (usando múltiples hilos).  
El objetivo es evidenciar la diferencia de rendimiento y la correcta sincronización de resultados cuando se utiliza concurrencia.

**Archivos involucrados:**
- `Main.java`: Clase principal que ejecuta la suma secuencial y concurrente.
- `SumaParcial.java`: Hilo que suma una parte de la matriz.
- `ResultadoCompartido.java`: Clase para almacenar la suma total de forma segura entre hilos.

### Problema 2: Carrera de Relevos con Hilos

Se simula una carrera de relevos donde varios equipos compiten en paralelo. Cada equipo tiene varios corredores que se relevan usando semáforos para sincronizar la entrega del testigo.
El programa muestra el orden de llegada de los equipos y la correcta coordinación entre los corredores de cada equipo.

**Archivo involucrado:**
- `CarreraRelevos.java`: Implementa la simulación completa de la carrera de relevos usando hilos y semáforos.

## Ejecución

1. Compila los archivos Java en la carpeta `src`.
2. Ejecuta la clase principal de cada problema:
   - Para el problema 1: `problema_1.Main`
   - Para el problema 2: `problema_2.CarreraRelevos`
