# Plan de Proyecto ÁvilaOS - Simulador de SO Distribuido

## Cronograma General (7 Semanas)

| Semana | Enfoque Principal | Entregables Clave |
|--------|-------------------|-------------------|
| 1 | Fundación: Arquitectura, Estructuras de datos, Enums/Interfaces | Repo GitHub, Estructuras propias, Core types |
| 2 | Núcleo del SO: Clock, Computer, CPU, RAM, PCB, Process | Un computador funcional ejecutando procesos simples |
| 3 | Planificación: Políticas intercambiables (FCFS, EDF, RR, Prioridades) | 3+ policies, cambio runtime por computador |
| 4 | Sincronización: Semáforos propios + Productor-Consumidor (local/remoto) | Buffers, semáforos, latencia red, bloqueo correcto |
| 5 | Memoria + GUI Vista 1 (Config) + Vista 2 (Monitor) | Admisión RAM, colas estados, interfaz completa |
| 6 | Métricas, Gráficas, Persistencia CSV/JSON, Integración multi-computador | Throughput, utilización, deadlines, gráficas, export/import |
| 7 | Testing exhaustivo, Pulido, Informe, Defensa | Código limpio, Informe PDF, Repo listo, Demo funcionando |

---

## Hitos Detallados por Semana

### SEMANA 1: Fundación (Días 1-7)
**Objetivo**: Base sólida, sin lógica de SO aún

#### Día 1-2: Setup y Arquitectura
- [ ] Crear repositorio GitHub con rama `develop` y `main`
- [ ] Configurar NetBeans + Maven/Gradle (Java 21+)
- [ ] Definir estructura de paquetes:
  ```
  avilaos/
  ├── core/           # Reloj, Computer, CPU, RAM
  ├── kernel/         # Scheduler, PCB, Process, Queues
  ├── sync/           # Semaphore, Buffer, ProducerConsumer
  ├── memory/         # MemoryManager
  ├── gui/            # Vista1, Vista2, Components
  ├── metrics/        # MetricsCollector, Charts
  ├── persistence/    # ConfigLoader, ConfigSaver (CSV/JSON)
  ├── model/          # Enums, Interfaces, DTOs
  └── util/           # Estructuras de datos propias
  ```
- [ ] Crear `ARCHITECTURE.md` con diagrama de capas (PlantUML/Mermaid)

#### Día 3-4: Estructuras de Datos Propias (OBLIGATORIO - sin java.util)
- [ ] `Node<E>` - Nodo genérico
- [ ] `LinkedList<E>` - add, remove, get, size, iterator, clear
- [ ] `Queue<E>` - enqueue, dequeue, peek, isEmpty, size (basada en LinkedList)
- [ ] `Stack<E>` - push, pop, peek, isEmpty, size
- [ ] `PriorityQueue<E>` - con Comparator personalizado (para EDF/Prioridades)
- [ ] Tests unitarios básicos para cada estructura

#### Día 5-6: Enums e Interfaces Base
**Enums (mínimo obligatorio):**
- [ ] `ProcessState`: NEW, READY, RUNNING, BLOCKED, TERMINATED
- [ ] `ProcessType`: CPU_BOUND, IO_BOUND, PRODUCER, CONSUMER
- [ ] `SchedulingPolicyType`: FCFS, EDF, ROUND_ROBIN, PRIORITY_PREEMPTIVE
- [ ] `ExecutionMode`: USER, KERNEL
- [ ] `BlockReason`: SEMAPHORE_WAIT, SEMAPHORE_FULL, SEMAPHORE_EMPTY, IO_WAIT, NETWORK_LATENCY, MEMORY_WAIT

**Interfaces (mínimo obligatorio):**
- [ ] `SchedulingPolicy` - `selectNext(Queue<PCB>)`, `reorder(Queue<PCB>)`, `onQuantumExpired()`, `getType()`
- [ ] `ClockListener` - `onTick(long cycle)` (CPU, DMA, etc.)
- [ ] `ProcessFactory` - para creación de procesos (opcional pero recomendado)
- [ ] `MetricsObserver` - para suscripción a métricas (opcional)

#### Día 7: Documentación y Commit
- [ ] Commit: `feat: foundation - data structures, enums, interfaces`
- [ ] Push a rama `feat/foundation`
- [ ] PR a `develop` con descripción

---

### SEMANA 2: Núcleo del SO (Días 8-14)
**Objetivo**: Un computador ejecutando procesos CPU-bound/I/O-bound

#### Día 8-9: PCB y Process
- [ ] `PCB` class: id (UUID global), name, computerId, state, type, priority, memoryRequired, deadline, remainingTime, pc, executionMode
- [ ] `Process` class: lógica de ejecución (run cycle), tipos de instrucción
- [ ] Instrucciones: CPU (decrementa remainingTime), I/O (bloquea), SEM_WAIT, SEM_SIGNAL, BUFFER_PUT, BUFFER_GET
- [ ] PC y MAR incrementan 1 por ciclo (lineal)

#### Día 10-11: CPU y RAM
- [ ] `CPU`: currentProcess, executeCycle(), interrupt(), contextSwitch()
- [ ] `RAM`: totalSize, usedSize, allocate(process), free(process), canAllocate(size)
- [ ] Modo ejecución: USER vs KERNEL (cada semWait/semSignal/buffer op = 1 ciclo KERNEL)

#### Día 12-13: Computer y GlobalClock
- [ ] `Computer`: id, cpu, ram, scheduler, queues (new, ready, blocked, terminated), buffers[], metrics
- [ ] `GlobalClock`: singleton, list<ClockListener>, start(), stop(), setCycleDuration(ms), tick()
- [ ] Cada tick: cada computer avanza 1 ciclo (CPU ejecuta, scheduler revisa colas, etc.)

#### Día 14: Integración 1 computador + Test
- [ ] Main simple: 1 computer, crear 3-4 procesos, correr 50 ciclos, log por consola
- [ ] Verificar: NEW->READY (si hay RAM), READY->RUNNING (scheduler), RUNNING->BLOCKED (I/O), RUNNING->TERMINATED (deadline/remainingTime=0)
- [ ] Commit: `feat: core - single computer execution`

---

### SEMANA 3: Planificación (Días 15-21)
**Objetivo**: 4 políticas intercambiables en runtime por computador

#### Día 15-16: Interfaz y FCFS
- [ ] Implementar `SchedulingPolicy` interface
- [ ] `FCFSPolicy`: cola FIFO simple, no reordena

#### Día 17-18: Round Robin
- [ ] `RoundRobinPolicy`: quantum configurable, reordena al final si quantum expira
- [ ] Manejo de `onQuantumExpired()` en interface

#### Día 19-20: EDF y Prioridades Apropiativas
- [ ] `EDFPolicy`: ordena por deadline ascendente (menor deadline = mayor prioridad)
- [ ] `PriorityPreemptivePolicy`: prioridad numérica (menor = mayor prioridad), desaloja si llega proceso de mayor prioridad
- [ ] Deadline = 0 => terminar proceso (métrica de cumplimiento)

#### Día 21: Selector GUI + Cambio Runtime
- [ ] ComboBox en Vista 2 por computador para cambiar policy
- [ ] Al cambiar: reordenar cola READY según nueva política
- [ ] Commit: `feat: scheduling - 4 policies runtime switchable`

---

### SEMANA 4: Sincronización Productor-Consumidor (Días 22-28)
**Objetivo**: Buffers, semáforos propios, acceso local/remoto

#### Día 22-23: Semáforo Propio
- [ ] `Semaphore`: value, queue<PCB> waiting, mutex interno (ReentrantLock o synchronized)
- [ ] `wait(PCB)`: decrementa, si < 0 => bloquea PCB, añade a waiting, cambia estado BLOCKED (reason SEMAPHORE_WAIT)
- [ ] `signal()`: incrementa, si <= 0 => despierta 1 PCB de waiting -> READY
- [ ] Tipos: counting (vacíos, llenos) y binary (mutex)

#### Día 24-25: Buffer
- [ ] `Buffer`: id, capacity, hostComputerId, elements[], semEmpty, semFull, semMutex
- [ ] Ocupa memoria en RAM del host (capacity * elementSize)
- [ ] `put(process, value)`: semWait(empty), semWait(mutex), add, semSignal(mutex), semSignal(full)
- [ ] `get(process)`: semWait(full), semWait(mutex), remove, semSignal(mutex), semSignal(empty)
- [ ] **Orden crítico**: vacío->mutex (productor), lleno->mutex (consumidor) - evitar deadlock

#### Día 26-27: Acceso Remoto y Latencia
- [ ] Si process.computerId != buffer.hostComputerId => acceso remoto
- [ ] Latencia configurable (ej. 3 ciclos): proceso se BLOQUEA razón NETWORK_LATENCY por N ciclos
- [ ] Después de latencia: ejecuta semWait reales
- [ ] Múltiples productores/consumidores por buffer

#### Día 28: Test Productor-Consumidor
- [ ] 1 buffer cap=5, 2 productores (producen cada 4 ciclos, 10 items), 2 consumidores (consumen cada 6 ciclos, 10 items)
- [ ] Verificar: no race conditions, bloqueo correcto, métricas items prod/cons
- [ ] Commit: `feat: sync - semaphores, buffers, producer-consumer local/remote`

---

### SEMANA 5: Memoria + GUI Completa (Días 29-35)
**Objetivo**: Admisión RAM + Interfaz gráfica funcional

#### Día 29-30: Gestión Memoria y Colas Estados
- [ ] `MemoryManager` en Computer: admitProcess(PCB) -> boolean
- [ ] Colas por estado: `newQueue`, `readyQueue`, `blockedQueue` (sub-colas por razón), `terminatedQueue`
- [ ] Transiciones: NEW -(RAM ok)-> READY, RUNNING -(I/O/semWait)-> BLOCKED, BLOCKED -(signal/timeout)-> READY, RUNNING -(done)-> TERMINATED
- [ ] Al TERMINATED: free RAM, revisar NEW queue para admitir siguientes

#### Día 31-33: GUI - Vista 1 (Configuración)
- [ ] JFrame principal con tabs: Configuración | Monitor
- [ ] Vista 1: Formulario crear proceso (nombre, instrucciones, memoria, prioridad, deadline, tipo, computerId, params tipo)
- [ ] Vista 1: Formulario crear buffer (capacidad, hostComputerId)
- [ ] Vista 1: Config global: num computadores, RAM cada uno, policy inicial, quantum, latencia red, ciclo duration
- [ ] Botón: Exportar config (JSON/CSV), Importar config
- [ ] Validaciones: tipos, rangos, campos obligatorios

#### Día 34-35: GUI - Vista 2 (Monitor Tiempo Real)
- [ ] Panel por computador (GridLayout 2xN):
  - CPU: proceso actual, PC, prioridad, deadline, modo (USER/KERNEL)
  - Colas: NEW, READY (ordenada), BLOCKED (agrupado por razón), TERMINATED
  - RAM: barra progreso usado/libre + MB
  - Policy actual + ComboBox cambio
- [ ] Panel global:
  - Reloj global (ciclo actual)
  - Tabla buffers: id, host, elementos/capacidad, semEmpty, semFull, semMutex, procesos bloqueados
  - Log eventos (JTextArea append-only con timestamp)
- [ ] Actualización: SwingWorker o Timer cada 100-200ms (no bloquear simulación)
- [ ] Commit: `feat: gui - vista1 config, vista2 monitor complete`

---

### SEMANA 6: Métricas, Gráficas, Persistencia, Multi-Computador (Días 36-42)

#### Día 36-37: Métricas por Computador y Global
- [ ] `MetricsCollector` por Computer:
  - Throughput: terminados / tiempo total
  - CPU Utilization: ciclos USER / ciclos totales
  - Avg Response Time: (primer RUNNING - NEW) promedio
  - Deadline Compliance: terminados antes deadline / total terminados
  - Fairness: desviación estándar de tiempos de respuesta (o Jain's index)
- [ ] Productor-Consumidor: items produced/consumed por buffer, avg time blocked en semáforos

#### Día 38-39: Gráficas (JFreeChart)
- [ ] Dependency: `org.jfree:jfreechart:1.5.4`
- [ ] `CPUUtilizationChart`: TimeSeries por computador, actualización cada N ciclos
- [ ] Ventana separada o panel en Vista 2: "Métricas Históricas"
- [ ] Exportar gráfica como PNG (opcional)

#### Día 40-41: Persistencia CSV/JSON
- [ ] `ConfigDTO` con todos parámetros
- [ ] `JsonConfigSerializer` (Jackson/Gson) - guardar/cargar configuración completa
- [ ] `CsvConfigSerializer` - procesos y buffers en CSV
- [ ] Botones en Vista 1: Guardar Config, Cargar Config

#### Día 42: Integración Multi-Computador
- [ ] Config inicial: 3 computadores, RAM distinta, policies distintas
- [ ] Procesos distribuidos manualmente + auto (round-robin por carga)
- [ ] Buffers en distintos hosts, productores/consumidores cruzados
- [ ] Verificar: reloj global sincroniza todos, métricas globales agregan bien
- [ ] Commit: `feat: metrics, charts, persistence, multi-computer integration`

---

### SEMANA 7: Testing, Pulido, Informe, Defensa (Días 43-49)

#### Día 43-44: Testing Exhaustivo
- [ ] Casos borde: RAM llena, deadline 0, quantum 1, buffer capacity 1, latencia 0
- [ ] Stress: 100+ procesos, 10 buffers, 4 computadores, 10000 ciclos
- [ ] Race conditions: múltiples hilos accediendo colas/buffers (synchronized en estructuras)
- [ ] Memory leaks: limpiar referencias en terminated, buffers

#### Día 45-46: Pulido GUI y UX
- [ ] Colores por estado (verde=running, azul=ready, rojo=blocked, gris=terminated)
- [ ] Tooltips en colas (ver PCB completo on hover)
- [ ] Scroll en log, auto-scroll toggle
- [ ] Ventana redimensionable, layouts responsivos
- [ ] Manejo errores: try-catch en inputs, mensajes amigables

#### Día 47: Informe PDF (Obligatorio)
- [ ] Diagrama UML 1: Estático (Clases) - paquetes, herencia, interfaces, enums
- [ ] Diagrama UML 2: Dinámico (Secuencia/Estado) - ciclo reloj, transición estados, productor-consumidor
- [ ] Análisis comparativo políticas: tabla métricas + conclusiones
- [ ] Análisis productor-consumidor: local vs remoto, impacto latencia
- [ ] Estructura: Intro, Arquitectura, Implementación, Resultados, Conclusiones

#### Día 48-49: Preparación Defensa
- [ ] Demo script: escenario 1 (solo CPU-bound, comparar policies), escenario 2 (productor-consumidor remoto)
- [ ] Conocer cada módulo: poder explicar cualquier clase/método
- [ ] Verificar: repo GitHub limpio, commits descriptivos, ramas, PRs, issues
- [ ] Compilar en NetBeans limpio (Build -> Clean and Build)
- [ ] Entrega: PDF + Link GitHub + Spreadsheet antes 7:00 AM viernes

---

## Criterios de Calidad (No Negociables)

| Aspecto | Estándar |
|---------|----------|
| **Estructuras datos** | 0 uso de `java.util.*` collections |
| **Enums** | Estados, políticas, modo ejecución, tipos proceso, razones bloqueo |
| **Interfaces** | `SchedulingPolicy`, `ClockListener` mínimo |
| **Instancias Computer** | Una clase, N instancias (no copiar código) |
| **Hilos Java** | GlobalClock en hilo propio, GUI en EDT, sincronización con `Semaphore` Java |
| **Semáforos SO** | Implementación propia (no Java Semaphore para lógica buffer) |
| **GUI** | Funcional, tiempo real, validaciones, 0 consola-only |
| **GitHub** | Rama develop, feature branches, PRs comentados, issues, commits semánticos |
| **Java** | 21+, NetBeans compatible |
| **Defensa** | Todos integrantes presentes, conocen todo el código |

---

## Estimación de Esfuerzo (Horas/Persona)

| Componente | Horas |
|------------|-------|
| Estructuras datos | 8 |
| Enums/Interfaces/Core types | 6 |
| PCB/Process/CPU/RAM | 12 |
| GlobalClock/Computer | 8 |
| 4 Planificadores | 16 |
| Semáforos + Buffer + Prod/Cons | 20 |
| Memoria + Colas estados | 8 |
| GUI Vista 1 | 12 |
| GUI Vista 2 | 16 |
| Métricas + Gráficas | 12 |
| Persistencia JSON/CSV | 6 |
| Integración multi-computer | 8 |
| Testing + Pulido | 12 |
| Informe + Defensa prep | 10 |
| **Total** | **~154 horas** |
| **Por persona (equipo 3)** | **~51 horas** |

---

## Riesgos y Mitigación

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|-------------|---------|------------|
| GUI consume mucha lógica | Alta | Crítico | Separar estricto: GUI solo observa (Listeners), lógica en core |
| Deadlock en semáforos | Media | Alto | Revisiones de código en PR, test productores/consumidores exhaustivo |
| java.util collections por accidente | Media | Crítico (0 proyecto) | Linter/checkstyle, code review, buscar `import java.util` |
| No compila en NetBeans | Baja | Crítico | Compilar en NetBeans cada PR, CI opcional |
| Un integrante no aporta | Media | Alto | Issues asignados, commits obligatorios, revisión semanal |
| Tiempo insuficiente semana 7 | Alta | Alto | Front-load: core listo semana 3, GUI semana 5, métricas semana 6 |

---

## Definition of Done por Componente

- [ ] Compila sin warnings en NetBeans (Java 21)
- [ ] Tests unitarios pasan (estructuras, políticas, semáforos)
- [ ] Integración manual: escenario funcional end-to-end
- [ ] Código documentado (JavaDoc público)
- [ ] Commit en feature branch + PR aprobado + merge a develop
- [ ] Issue correspondiente cerrado

---

## Próximos Pasos Inmediatos

1. **Hoy**: Crear repo GitHub, invitar equipo, clonar en NetBeans
2. **Mañana**: Estructuras de datos + Enums + Interfaces (Semana 1 completa)
3. **Esta semana**: Núcleo 1 computador funcional (Semana 2)

---

*Documento vivo - actualizar al final de cada sprint semanal*