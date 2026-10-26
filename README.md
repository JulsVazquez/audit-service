# Audit Service

Microservicio de auditoría para Cinema Ciencias. Registra logs de eventos
(`/api/logs`) en una base **MariaDB** independiente. Es parte del ecosistema de
microservicios de la tesis.

## Stack

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 25 |
| Framework | Spring Boot 4.0.2 (Spring Web MVC + Spring Data JPA) |
| Build | Gradle 9.3 (wrapper) |
| Base de datos | MariaDB 11.4 |
| Puerto | **8081** |

## Prerequisitos

- **JDK 25** instalado y disponible en `JAVA_HOME`
- **Docker** con Docker Compose (para levantar MariaDB)
- El repositorio raíz con su `docker-compose.yml` (los servicios de infraestructura
  viven ahí, no dentro de este repo)

## Requisitos

### 1. Levantar MariaDB (infraestructura)

Desde la raíz del proyecto `titulacion/`:

```bash
docker compose up -d mariadb-audit
```

Esto levanta **MariaDB 11.4** en `localhost:3307` con:

- Base de datos: `audit_db`
- Usuario: `root`
- Password: `root`

### 2. Compilar

```bash
./gradlew build
```

(Compila, corre los tests y genera el jar en `build/libs/audit-service-0.0.1-SNAPSHOT.jar`)

### 3. Ejecutar

Opción A — desde el código fuente:

```bash
./gradlew bootRun
```

Opción B — desde el jar compilado:

```bash
java -jar build/libs/audit-service-0.0.1-SNAPSHOT.jar
```

El servicio queda escuchando en **http://localhost:8081**.

## Verificación

El servicio expone un único endpoint (solo escritura):

```bash
curl -X POST http://localhost:8081/api/logs \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"action":"LOGIN","details":"user logged in"}'
```

Respuesta esperada: `200 OK` con la entidad guardada (incluye `id` y `timestamp`
generados). La tabla `audit_logs` se crea automáticamente (`ddl-auto: update`).

## Docker (opcional)

El `Dockerfile` copia el jar ya compilado, así que primero hay que hacer build:

```bash
./gradlew build
docker build -t audit-service .
docker run -p 8081:8081 audit-service
```

> **Ojo**: el datasource apunta a `localhost:3307`. Si corremos el servicio dentro
> de un contenedor, la base no queda accesible salvo que se use red del host
> (`--network host`) o se sobreescriba la URL del datasource con variables de
> entorno. El flujo más directo es correr el jar en el host contra la MariaDB
> contenerizada.

## Integración con el ecosistema

- **Cinema Ciencias** (puerto 8089) consume este servicio para registrar eventos
  de auditoría al crear tickets (`AuditClient` → `POST http://audit-service/api/logs`, un
  `RestClient` balanceado cuyo nombre se resuelve por Eureka).
- Se registra en **Eureka** como `AUDIT-SERVICE` y toma su configuración del Config Server
  (`spring.config.import`, opcional). No está enrutado por el gateway: lo usan los demás servicios.
- Solo expone `POST /api/logs`; para leer lo persistido hay que consultar MariaDB
  (`docker exec audit-mariadb mariadb -uroot -proot -e "select * from audit_db.audit_logs"`).

## Endpoints

| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| POST | `/api/logs` | Sin autenticación | Guarda un log de auditoría (`userId`, `action`, `details`) |