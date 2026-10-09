# ÁvilaOS · BACK

Simulador de un sistema operativo distribuido escrito en Java para la
asignatura de Sistemas Operativos (trimestre 2627-1).

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
├── core/        hardware simulado: reloj global, CPU, RAM
└── metrics/     snapshot de métricas por computador
src/test/java/   pruebas unitarias de las estructuras propias
docs/            plan del proyecto y arquitectura por capas
```

## Estado del proyecto

- [x] **Semana 1 — fundación**: estructuras de datos propias, enums,
  interfaces, PCB y DTOs.
- [x] **Semana 2 — hardware**: reloj global con thread propio, CPU y RAM.
- [ ] **Semanas 3-4**: kernel (planificador con políticas, memoria,
  productor-consumidor con semáforos).
- [ ] **Semana 5**: memoria principal y GUI (Vista 1 y Vista 2).
- [ ] **Semana 6**: servicio de simulación, API REST, persistencia y métricas.
- [ ] **Semana 7**: pruebas, pulido, informe y defensa.

## Convención de trabajo

- `main`: solo recibe cambios mediante Pull Requests desde `develop`.
- `develop`: rama de integración.
- `feat/...`, `fix/...`: ramas por funcionalidad, siempre con su Issue.
- Commits convencionales: `feat:`, `fix:`, `docs:`, `test:`, `refactor:`.
