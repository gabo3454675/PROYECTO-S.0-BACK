# Arquitectura ÁvilaOS - Diagrama de Capas y Componentes

## 1. Vista de Capas (Layered Architecture)

```mermaid
graph TB
    subgraph GUI["Capa de Presentación (GUI)"]
        V1[Vista 1: Configuración]
        V2[Vista 2: Monitor]
        CH[Charts Panel]
        LOG[Event Log]
    end

    subgraph OBS["Capa de Observación (Listeners)"]
        CL[ClockListener]
        SL[StateChangeListener]
        ML[MetricsListener]
    end

    subgraph KERNEL["Capa de Núcleo del SO (Kernel)"]
        SCH[Scheduler<br/>+ SchedulingPolicy]
        MM[MemoryManager]
        SYNC[SyncManager<br/>Semaphore, Buffer]
        PM[ProcessManager]
    end

    subgraph HW["Capa de Abstracción de Hardware"]
        CPU[CPU]
        RAM[RAM]
        DMA[DMA Controller]
        CLK[GlobalClock]
    end

    subgraph INFRA["Infraestructura Base"]
        DS[Estructuras Datos Propias]
        ENUMS[Enums]
        IFACES[Interfaces]
        IDG[IDGenerator]
    end

    GUI --> OBS
    OBS --> KERNEL
    KERNEL --> HW
    HW --> INFRA
    KERNEL --> INFRA
    GUI --> INFRA
```

**Regla de dependencia:** Cada capa solo conoce a la inmediatamente inferior. La GUI **nunca** llama directamente al Kernel; usa Listeners/Observers.

---

## 2. Diagrama de Clases Principal (Simplificado)

```mermaid
classDiagram
    %% Infraestructura
    class IDGenerator {
        +getNextId() long
    }
    class Node~E~
    class LinkedList~E~
    class Queue~E~
    class PriorityQueue~E~

    %% Enums
    class ProcessState {
        <<enumeration>>
        NEW, READY, RUNNING, BLOCKED, TERMINATED
    }
    class ProcessType {
        <<enumeration>>
        CPU_BOUND, IO_BOUND, PRODUCER, CONSUMER
    }
    class SchedulingPolicyType {
        <<enumeration>>
        FCFS, EDF, ROUND_ROBIN, PRIORITY_PREEMPTIVE
    }
    class ExecutionMode {
        <<enumeration>>
        USER, KERNEL
    }
    class BlockReason {
        <<enumeration>>
        SEMAPHORE_WAIT, SEMAPHORE_FULL, SEMAPHORE_EMPTY
        IO_WAIT, NETWORK_LATENCY, MEMORY_WAIT
    }

    %% Interfaces
    class SchedulingPolicy {
        <<interface>>
        +selectNext(Queue~PCB~) PCB
        +reorder(Queue~PCB~)
        +onQuantumExpired()
        +getType() SchedulingPolicyType
    }
    class ClockListener {
        <<interface>>
        +onTick(long cycle)
    }
    class StateChangeListener {
        <<interface>>
        +onStateChange(PCB, ProcessState, ProcessState)
    }

    %% Hardware
    class GlobalClock {
        -listeners: List~ClockListener~
        -cycleDurationMs: int
        -running: boolean
        +start()
        +stop()
        +addListener(ClockListener)
        +tick()
    }
    class Computer {
        -id: int
        -cpu: CPU
        -ram: RAM
        -kernel: Kernel
        -clock: GlobalClock
        +executeCycle()
    }
    class CPU {
        -currentProcess: PCB
        -mode: ExecutionMode
        +executeCycle()
        +contextSwitch(PCB)
        +interrupt()
    }
    class RAM {
        -totalSize: long
        -usedSize: long
        +allocate(size) boolean
        +free(size)
        +canAllocate(size) boolean
    }

    %% Kernel
    class Kernel {
        -scheduler: Scheduler
        -memoryManager: MemoryManager
        -syncManager: SyncManager
        -processManager: ProcessManager
        -newQueue: Queue~PCB~
        -readyQueue: Queue~PCB~
        -blockedQueues: Map~BlockReason, Queue~PCB~~
        -terminatedQueue: Queue~PCB~
        +admitProcess(PCB) boolean
        +executeCycle()
    }
    class Scheduler {
        -policy: SchedulingPolicy
        -readyQueue: Queue~PCB~
        +setPolicy(SchedulingPolicy)
        +selectNext() PCB
        +reorder()
    }
    class MemoryManager {
        -ram: RAM
        +admit(PCB) boolean
        +release(PCB)
        +checkNewQueue()
    }
    class SyncManager {
        -buffers: Map~String, Buffer~
        +createBuffer(id, capacity, hostComputerId)
        +getBuffer(id) Buffer
    }
    class ProcessManager {
        +createProcess(config) PCB
        +terminateProcess(PCB)
    }

    %% Procesos y Sincronización
    class PCB {
        -id: long
        -name: String
        -computerId: int
        -state: ProcessState
        -type: ProcessType
        -priority: int
        -memoryRequired: long
        -deadline: long
        -remainingTime: long
        -pc: int
        -mode: ExecutionMode
        -blockReason: BlockReason
    }
    class Process {
        -pcb: PCB
        -instructions: List~Instruction~
        +executeCycle() boolean
    }
    class Semaphore {
        -value: int
        -waitingQueue: Queue~PCB~
        +wait(PCB)
        +signal()
    }
    class Buffer {
        -id: String
        -capacity: int
        -hostComputerId: int
        -elements: Queue~Object~
        -semMutex: Semaphore
        -semEmpty: Semaphore
        -semFull: Semaphore
        +put(process, item)
        +get(process) Object
    }

    %% Políticas de Planificación
    class FCFSPolicy
    class RoundRobinPolicy
    class EDFPolicy
    class PriorityPreemptivePolicy

    %% Métricas
    class MetricsCollector
    class CPUMetricsChart

    %% Persistencia
    class ConfigSerializer
    class JsonConfigSerializer
    class CsvConfigSerializer

    %% Relaciones
    GlobalClock --> ClockListener
    Computer --> CPU
    Computer --> RAM
    Computer --> Kernel
    Computer --> GlobalClock
    CPU --> PCB
    Kernel --> Scheduler
    Kernel --> MemoryManager
    Kernel --> SyncManager
    Kernel --> ProcessManager
    Scheduler --> SchedulingPolicy
    Scheduler --> Queue~PCB~
    FCFSPolicy ..|> SchedulingPolicy
    RoundRobinPolicy ..|> SchedulingPolicy
    EDFPolicy ..|> SchedulingPolicy
    PriorityPreemptivePolicy ..|> SchedulingPolicy
    PCB --> ProcessState
    PCB --> ProcessType
    PCB --> ExecutionMode
    PCB --> BlockReason
    SyncManager --> Buffer
    Buffer --> Semaphore
    Semaphore --> Queue~PCB~
    MetricsCollector --> Computer
    ConfigSerializer <|.. JsonConfigSerializer
    ConfigSerializer <|.. CsvConfigSerializer
```

---

## 3. Diagrama de Secuencia: Ciclo de Reloj Global

```mermaid
sequenceDiagram
    participant GC as GlobalClock
    participant C1 as Computer 1
    participant C2 as Computer 2
    participant CPU1 as CPU 1
    participant SCH1 as Scheduler 1
    participant RAM1 as RAM 1
    participant GUI as GUI (Observers)

    loop Cada ciclo (tick)
        GC->>C1: onTick(cycle)
        GC->>C2: onTick(cycle)
        
        par Computer 1
            C1->>CPU1: executeCycle()
            CPU1->>CPU1: fetch-decode-execute
            alt Instrucción USER
                CPU1->>CPU1: decrement remainingTime
                CPU1->>CPU1: pc++
            else Instrucción KERNEL (semWait, I/O, etc.)
                CPU1->>SCH1: blockCurrentProcess(reason)
                SCH1->>SCH1: move RUNNING->BLOCKED
                SCH1->>SCH1: selectNext()
                SCH1-->>CPU1: next PCB
                CPU1->>CPU1: contextSwitch()
            end
        and Computer 2
            C2->>CPU2: executeCycle()
            Note right of CPU2: Misma lógica
        end

        C1-->>GUI: StateChangeEvent (colas, CPU, RAM)
        C2-->>GUI: StateChangeEvent
        GC-->>GUI: ClockTickEvent(cycle)
    end
```

---

## 4. Diagrama de Estados: Proceso (ProcessState)

```mermaid
stateDiagram-v2
    [*] --> NEW: Proceso creado
    NEW --> READY: RAM disponible (MemoryManager.admit)
    NEW --> [*]: RAM insuficiente (permanece en cola NEW)
    
    READY --> RUNNING: Scheduler.dispatch()
    
    RUNNING --> READY: Quantum expirado (RR)
    RUNNING --> READY: Preempted por prioridad mayor
    RUNNING --> BLOCKED: semWait fallido / I/O / Latencia red
    RUNNING --> TERMINATED: remainingTime == 0
    RUNNING --> TERMINATED: deadline == 0
    
    BLOCKED --> READY: semSignal / I/O complete / Latencia done
    BLOCKED --> TERMINATED: deadline == 0 (opcional)
    
    TERMINATED --> [*]: MemoryManager.release + checkNewQueue
```

---

## 5. Diagrama de Secuencia: Productor-Consumidor (Acceso Remoto)

```mermaid
sequenceDiagram
    participant P as Productor (Computer 1)
    participant B as Buffer (Computer 2)
    participant S1 as semEmpty
    participant S2 as semMutex
    participant S3 as semFull
    participant C as Consumidor (Computer 1)

    Note over P,B: Productor en C1, Buffer en C2 (remoto)
    
    P->>B: put(item)
    B->>P: BLOQUEAR por NETWORK_LATENCY (ej. 3 ciclos)
    Note right of P: Estado BLOCKED, reason=NETWORK_LATENCY
    ... 3 ciclos después ...
    P->>S1: semWait(empty)
    alt empty > 0
        S1-->>P: OK
        P->>S2: semWait(mutex)
        S2-->>P: OK
        P->>B: buffer.add(item)
        P->>S2: semSignal(mutex)
        P->>S3: semSignal(full)
    else empty == 0
        S1->>P: BLOQUEAR en cola semEmpty
        Note right of P: Estado BLOCKED, reason=SEMAPHORE_EMPTY
    end

    Note over C,B: Consumidor en C1, Buffer en C2 (remoto)
    C->>B: get()
    B->>C: BLOQUEAR por NETWORK_LATENCY
    ... latencia ...
    C->>S3: semWait(full)
    alt full > 0
        S3-->>C: OK
        C->>S2: semWait(mutex)
        S2-->>C: OK
        C->>B: item = buffer.remove()
        C->>S2: semSignal(mutex)
        C->>S1: semSignal(empty)
    else full == 0
        S3->>C: BLOQUEAR en cola semFull
    end
```

---

## 6. Estructura de Paquetes (Maven)

```
avilaos/
├── src/main/java/avilaos/
│   ├── model/
│   │   ├── enumeration/
│   │   │   ├── ProcessState.java
│   │   │   ├── ProcessType.java
│   │   │   ├── SchedulingPolicyType.java
│   │   │   ├── ExecutionMode.java
│   │   │   └── BlockReason.java
│   │   ├── interfaces/
│   │   │   ├── SchedulingPolicy.java
│   │   │   ├── ClockListener.java
│   │   │   ├── StateChangeListener.java
│   │   │   └── MetricsListener.java
│   │   ├── dto/
│   │   │   ├── ProcessConfig.java
│   │   │   ├── BufferConfig.java
│   │   │   ├── ComputerConfig.java
│   │   │   └── SimulationConfig.java
│   │   └── pcb/
│   │       └── PCB.java
│   ├── util/
│   │   ├── datastructures/
│   │   │   ├── Node.java
│   │   │   ├── LinkedList.java
│   │   │   ├── Queue.java
│   │   │   └── PriorityQueue.java
│   │   └── IDGenerator.java
│   ├── core/
│   │   ├── clock/
│   │   │   └── GlobalClock.java
│   │   ├── hardware/
│   │   │   ├── Computer.java
│   │   │   ├── CPU.java
│   │   │   └── RAM.java
│   │   └── process/
│   │       └── Process.java
│   ├── kernel/
│   │   ├── scheduler/
│   │   │   ├── Scheduler.java
│   │   │   ├── FCFSPolicy.java
│   │   │   ├── RoundRobinPolicy.java
│   │   │   ├── EDFPolicy.java
│   │   │   └── PriorityPreemptivePolicy.java
│   │   ├── memory/
│   │   │   └── MemoryManager.java
│   │   ├── sync/
│   │   │   ├── Semaphore.java
│   │   │   ├── Buffer.java
│   │   │   └── SyncManager.java
│   │   ├── process/
│   │   │   └── ProcessManager.java
│   │   └── Kernel.java
│   ├── metrics/
│   │   ├── MetricsCollector.java
│   │   └── CPUMetricsChart.java
│   ├── persistence/
│   │   ├── ConfigSerializer.java
│   │   ├── JsonConfigSerializer.java
│   │   └── CsvConfigSerializer.java
│   └── gui/
│       ├── Vista1Config.java
│       ├── Vista2Monitor.java
│       ├── components/
│       │   ├── ComputerPanel.java
│       │   ├── QueueTableModel.java
│       │   ├── BufferTableModel.java
│       │   └── MetricsChartPanel.java
│       └── controllers/
│           ├── ConfigController.java
│           └── MonitorController.java
└── src/test/java/avilaos/
    ├── util/datastructures/
    ├── kernel/scheduler/
    └── kernel/sync/
```

---

## 7. Flujo de Datos Principal

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│  GlobalClock │────▶│   Computer   │────▶│     CPU     │
│  (tick)      │     │  (execute)   │     │ (instr.)    │
└─────────────┘     └──────────────┘     └──────┬──────┘
                           ▲                    │
                           │                    ▼
                    ┌──────┴──────┐     ┌─────────────┐
                    │   Kernel    │     │  Scheduler  │
                    │  (coordinar)│     │ (selectNext)│
                    └──────┬──────┘     └──────┬──────┘
                           │                   │
              ┌────────────┼────────────┐      │
              ▼            ▼            ▼      ▼
         ┌─────────┐ ┌──────────┐ ┌──────────┐ ┌─────────┐
         │MemoryMgr│ │SyncManager│ │ProcessMgr│ │ Queues  │
         └─────────┘ └──────────┘ └──────────┘ └─────────┘
              │            │            │
              ▼            ▼            ▼
         ┌─────────┐ ┌──────────┐ ┌──────────┐
         │   RAM   │ │ Buffers  │ │  PCB     │
         └─────────┘ └──────────┘ └──────────┘
                           │
                           ▼
                    ┌──────────────┐
                    │  Semaphores  │
                    │ (wait/signal)│
                    └──────────────┘
```

---

## 8. Decisiones de Diseño Clave

| Decisión | Justificación |
|----------|---------------|
| **Computer como clase única, N instancias** | Requisito obligatorio: "agregar computador = nueva instancia" |
| **PCB = TCB (monohilo)** | Requisito: "un solo hilo por proceso, PCB integra TCB 1:1" |
| **Semáforos propios (no java.util.concurrent)** | Requisito: "implementación queda a su elección pero parte de ÁvilaOS" |
| **Estructuras de datos propias** | Requisito: "prohibido java.util.ArrayList, Queue, Stack, Vector" |
| **Interfaces para políticas** | Requisito: "agregar política nueva no requiere modificar planificador" |
| **GlobalClock como singleton con listeners** | Desacopla reloj de computadores; GUI se suscribe |
| **GUI como Observer pura** | "Interfaz debe observar al sistema, no ser el sistema" (Proyecto 2) |
| **Enums para conjuntos fijos** | Requisito obligatorio + switch exhaustivo compile-time |
| **DTOs records para persistencia** | Inmutables, serializables, menos boilerplate |

---

## 9. Puntos de Extensión (Proyecto 2)

| Componente | Preparación para Proyecto 2 |
|------------|----------------------------|
| `MemoryManager` | Añadir `VirtualMemoryManager` que herede/decore, agregar `suspendProcess()` |
| `Process` / `PCB` | Añadir `pageTable`, `diskBlocks`, `suspended` state |
| `SyncManager` | Añadir `DiskBuffer`, `FileSemaphore` |
| `GlobalClock` | Añadir `diskIOCycles`, `pageFaultHandler` |
| `GUI` | Nueva pestaña "Disco/Memoria Virtual", gráficas de page faults |

---

*Documento vivo - actualizar cuando cambie la arquitectura*