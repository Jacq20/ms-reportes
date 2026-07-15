# ms-reportes

**Perfulandia SPA — Microservicio de Reportes**
DSY1103 Desarrollo FullStack 1 · Evaluación Final Transversal · 2025

---

## Descripción del dominio

Genera reportes de negocio reales consultando otros microservicios vía RestTemplate. Según el tipo de reporte solicitado, llama a ms-ventas (VENTAS), ms-inventario (INVENTARIO) o ms-catalogo (RENDIMIENTO_SUCURSAL), procesa la respuesta y persiste el resumen junto con el periodo analizado.

---

## Tecnologías

- Java 25 · Spring Boot 4.0.7 · Maven · Packaging WAR
- Spring Data JPA + Hibernate · RestTemplate (comunicación inter-microservicio)
- SLF4J · springdoc-openapi 2.8.9
- JUnit 5 + Mockito · H2

---

## Puerto y base de datos

| Propiedad | Valor |
|---|---|
| Puerto | `8088` |
| Base de datos | `reportes_db` |
| Swagger UI | `http://localhost:8088/doc/swagger-ui.html` |

---

## Microservicios que consume

| TipoReporte | Microservicio consumido | Endpoint llamado |
|---|---|---|
| `VENTAS` | ms-ventas:8084 | `GET /api/v1/ventas/historial/{idSucursal}` |
| `INVENTARIO` | ms-inventario:8083 | `GET /api/v1/inventario/sucursal/{idSucursal}` |
| `RENDIMIENTO_SUCURSAL` | ms-catalogo:8082 | `GET /api/v1/productos` |

---

## Diagrama de entidades

```
┌───────────────────────────────────────────┐
│                  Reporte                  │
├───────────────────────────────────────────┤
│ id: Long (PK)                             │
│ tipo: TipoReporte (enum)                  │
│ idSucursal: String                        │
│ periodoInicio: DateTime                   │
│ periodoFin: DateTime                      │
│ fechaGeneracion: DateTime                 │
│ resumen: String (TEXT)                    │
│         ↑ datos reales del MS consultado  │
└───────────────────────────────────────────┘

TipoReporte: VENTAS | INVENTARIO | RENDIMIENTO_SUCURSAL

┌──────────────────────────────┐
│      PeriodoDTO (record)     │
├──────────────────────────────┤
│ periodoInicio: LocalDateTime │
│ periodoFin: LocalDateTime    │
└──────────────────────────────┘
```

---

## Estructura del proyecto (patrón CSR)

```
ms-reportes/
└── src/main/java/com/perfulandia/ms_reportes/
    ├── controller/   ReporteController
    ├── service/      ReporteService
    ├── repository/   ReporteRepository
    ├── model/        Reporte, TipoReporte
    ├── dto/          PeriodoDTO, VentaDTO, ItemInventarioDTO, ProductoDTO
    ├── config/       RestTemplateConfig
    └── exception/    GlobalExceptionHandler
```

---

## Endpoints REST

| Método | Ruta | Descripción | Código |
|--------|------|-------------|--------|
| `POST` | `/api/v1/reportes` | Generar reporte de negocio | 200 |

Parámetros query: `tipo` (TipoReporte) y `idSucursal` (String)
Body: `PeriodoDTO` con `periodoInicio` y `periodoFin`

### Ejemplo Request

```
POST /api/v1/reportes?tipo=INVENTARIO&idSucursal=1
Content-Type: application/json

{
  "periodoInicio": "2026-01-01T00:00:00",
  "periodoFin": "2026-01-31T23:59:59"
}
```

### Response 200

```json
{
  "id": 1,
  "tipo": "INVENTARIO",
  "idSucursal": "1",
  "periodoInicio": "2026-01-01T00:00:00",
  "periodoFin": "2026-01-31T23:59:59",
  "fechaGeneracion": "2026-07-13T10:30:00",
  "resumen": "REPORTE DE INVENTARIO | Sucursal: 1 | Total productos: 8 | Bajo stock: 2"
}
```

### Response 400 — periodo inválido

```
El periodo de inicio no puede ser posterior al periodo de fin
```

---

## Reglas de negocio

| Regla | Descripción |
|-------|-------------|
| Periodo obligatorio | `periodoInicio` y `periodoFin` no pueden ser nulos → HTTP 400 |
| Orden cronológico | `periodoInicio` no puede ser posterior a `periodoFin` → HTTP 400 |
| Resumen real | El campo `resumen` almacena datos obtenidos del MS correspondiente (totales, conteos, promedios) |
| Tolerancia a fallos | Si el MS origen no responde, el error queda registrado en `resumen` — el reporte igual se guarda |

---

## Contenido del campo `resumen` por tipo

| Tipo | Contenido del resumen |
|------|----------------------|
| `VENTAS` | Cantidad de ventas + monto total acumulado de la sucursal |
| `INVENTARIO` | Total de productos en inventario + cantidad con bajo stock |
| `RENDIMIENTO_SUCURSAL` | Productos en catálogo + precio promedio del catálogo completo |

---

## Manejo de errores

| Excepción | HTTP | Respuesta |
|-----------|------|-----------|
| `RuntimeException` (periodo inválido) | 400 | Mensaje en texto plano |
| Error de comunicación con MS externo | — | El error se registra en `resumen`, no falla el reporte |

---

## Pruebas unitarias

```bash
./mvnw clean test
```

| Clase | Casos cubiertos |
|-------|-----------------|
| `ReporteServiceTest` | Generar VENTAS OK (mock RestTemplate), Generar INVENTARIO OK, Sin periodo → error, Periodo inválido → error |
| `ReporteControllerTest` | POST 200 OK, POST 400 periodo inválido |

### Ejemplo de test con mock RestTemplate

```java
@Test
void testGenerarReporteInventario() {
    // Given
    ItemInventarioDTO[] items = {
        new ItemInventarioDTO(1L, 10L, 1L, 5, 10),  // bajo stock
        new ItemInventarioDTO(2L, 11L, 1L, 20, 5)   // stock normal
    };
    when(restTemplate.getForObject(contains("/inventario/sucursal/"), eq(ItemInventarioDTO[].class)))
        .thenReturn(items);
    when(reporteRepository.save(any())).thenReturn(guardado);

    // When
    Reporte resultado = reporteService.generarReporte(TipoReporte.INVENTARIO, "1", periodo);

    // Then
    assertNotNull(resultado);
    assertEquals(TipoReporte.INVENTARIO, resultado.getTipo());
    verify(reporteRepository, times(1)).save(any());
}
```

---

## Configuración

### `src/main/resources/application.properties`
```properties
spring.application.name=ms-reportes
server.port=8088

spring.datasource.url=jdbc:mysql://localhost:3306/reportes_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

springdoc.api-docs.enabled=true
springdoc.swagger-ui.enabled=true
springdoc.swagger-ui.path=/doc/swagger-ui.html

ms.inventario.url=http://localhost:8083
ms.catalogo.url=http://localhost:8082
ms.ventas.url=http://localhost:8084
```

---

## Ejecución local

```sql
CREATE DATABASE reportes_db;
```

```bash
cd ms-reportes
./mvnw spring-boot:run
./mvnw clean test
```
