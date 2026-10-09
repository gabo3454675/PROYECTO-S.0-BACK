# ÁvilaOS · BACK

Simulador de un sistema operativo distribuido escrito en Java para la
asignatura de Sistemas Operativos (trimestre 2627-1).

Cada nodo del clúster es un `Computer` con su propia CPU, RAM y núcleo del
sistema operativo (planificador, colas y sincización). Todos los nodos avanzan
un ciclo por tick del reloj global.

## Requisitos

- JDK 21 o superior
- Apache Maven 3.9+
- NetBeans (IDE requerido por el enunciado; abrir `pom.xml` como proyecto Maven)

## Compilar y ejecutar pruebas

```bash
mvn clean test
```

## Estructura

```
src/main/java/avilaos/
├── model/       enums, interfaces, PCB y DTOs de configuración
├── util/        estructuras de datos propias (listas, colas, mapa, prioridad)
├── core/        hardware simulado: reloj global, Computer, CPU, RAM
├── kernel/      núcleo del SO: planificador, memoria, semáforos y buffers
└── metrics/     snapshot de métricas por computador
src/test/java/   pruebas unitarias de estructuras, kernel y sincronización
docs/            plan del proyecto y arquitectura por capas
```

## Estados del proyecto

- [x] **Fase A — base**: modelo, estructuras propias, hardware y kernel con
  4 políticas de planificación (FCFS, Round Robin, EDF, Prioridades),
  admisión por memoria y productor-consumidor con semáforos.
- [ ] **Fase B**: servicio de simulación, API REST y persistencia JSON/CSV.
- [ ] **Fase C**: interfaz gráfica (Vista 1 configuración, Vista 2 monitor).
- [ ] **Fase D**: dashboard de métricas y gráfica de utilización de CPU.

## Convención de trabajo

- `main`: solo recibe cambios mediante Pull Requests desde `develop`.
- `develop`: rama de integración.
- `feat/...`, `fix/...`: ramas por funcionalidad, siempre con su Issue.
- Commits convencionales: `feat:`, `fix:`, `docs:`, `test:`, `refactor:`.
