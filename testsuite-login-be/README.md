# testsuite-login-be

Proyecto de pruebas de API con **REST Assured**, **JUnit 5**, **Allure** y **Java 17**.

## Ejecutar

```bash
mvn clean test
mvn test -Dgroups=smoke          # solo casos marcados smoke en catalog/*.txt
mvn allure:serve                # requiere Allure CLI
```

Configura la URL en `src/test/resources/application.properties`:

```properties
api.base.uri=http://localhost:8081
```

## Dónde viven los datos (recomendación)

| Qué cambia | Dónde editar |
|------------|--------------|
| Headers/body comunes del curl | `scenarios/authentication/login/_base.json` |
| Un solo campo (ej. `user_id`) | Nuevo JSON con `"extends": "authentication/login/_base"` y solo el delta |
| Curl completo distinto | JSON nuevo **sin** `extends` (escenario autocontenido) |
| URL del servidor | `application.properties` |
| Aserciones / flujo | Clase de test (`LoginApiTest`) |

Los tests **no** duplican headers ni body: cargan un `ApiScenario` y ejecutan la petición.

## Escenarios JSON

Ruta: `src/test/resources/scenarios/`

```
scenarios/authentication/
  login/                  # HU-006101 (21 escenarios desde Postman)
  logout/                 # HU-006102 (14 escenarios)
  password/               # HU-006103 (11 escenarios)
scenarios/catalog/
  login-scenarios.txt     # índice: path|nombre|tag
```

Tests Java (`@DisplayName` = nombre HU):

| Clase | HU |
|-------|-----|
| `HU006101Login` | HU-006101 Login |
| `HU006102Logout` | HU-006102 Logout |
| `HU006103AutenticacionContrasena` | HU-006103_Autenticacion_Contraseña |

Regenerar escenarios desde Postman:

```bash
python3 scripts/import-postman.py
```

Ejemplo de delta (solo cambia el body):

```json
{
  "extends": "authentication/login/_base",
  "name": "login-invalid-user",
  "body": {
    "data": {
      "login": {
        "user_id": "otro@correo.com"
      }
    }
  },
  "expected": { "statusCode": 401 }
}
```

## Uso en tests

```java
ApiScenario scenario = ScenarioLoader.load("authentication/login/success");
RequestExecutor.execute(scenario)
    .statusCode(scenario.getExpected().getStatusCode());
```

Override puntual en código (sin nuevo archivo):

```java
ScenarioLoader.load("authentication/login/success")
    .withHeader("city", "Guadalajara");
```

## Ajustar códigos esperados

Los `statusCode` en los JSON de ejemplo son plantillas. Actualízalos según la respuesta real de tu API.
