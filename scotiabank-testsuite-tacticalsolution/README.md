# scotiabank-testsuite-tacticalsolution

Proyecto de pruebas de API con **REST Assured**, **JUnit 5**, **Allure** y **Java 17** para los servicios de Tactical Solution (IB BaaS).

Basado en la arquitectura de `testsuite-login-be`: escenarios en JSON, catálogo por HU y ejecución parametrizada.

## Ejecutar

```bash
mvn clean test
mvn test -Dgroups=smoke          # solo casos marcados smoke en catalog/*.txt
mvn allure:serve                # requiere Allure CLI
```

Con URL del ambiente y token automático vía Passport:

```bash
API_BASE_URI=https://ibbaas-customer-validation-mx.example.com \
PASSPORT_BASE_URL=https://passport.example.com \
./scripts/run-tests.sh
```

Passport responde con un GET a `/48cf7cec-2dfe-4695-a3b1-eb423fc6418c` (equivalente al curl de Postman):

```bash
curl --location --globoff '{{passport-base-url}}/48cf7cec-2dfe-4695-a3b1-eb423fc6418c'
```

Respuesta esperada (OAuth2):

```json
{
  "access_token": "eyJ...",
  "scope": "customer.validation.ib.bcs.read ...",
  "token_type": "Bearer",
  "expires_in": 3600
}
```

El framework lee `access_token` y envía `Authorization: Bearer <token>` en las 3 HU.

También puedes pasar el token manualmente (tiene prioridad sobre Passport):

```bash
API_BASE_URI=https://ibbaas-customer-validation-mx.example.com \
API_ACCESS_TOKEN=<tu-token> \
./scripts/run-tests.sh
```

Configura en `src/test/resources/application.properties`:

```properties
api.base.uri=http://localhost:8080
api.access.token=replace-with-valid-bearer-token

passport.base.uri=https://passport.example.com
passport.token.path=/48cf7cec-2dfe-4695-a3b1-eb423fc6418c
```

También puedes sobreescribir vía JVM: `-Dapi.base.uri=... -Dpassport.base.uri=... -Dapi.access.token=...`

## Historias de usuario

| Clase | HU | Endpoint |
|-------|-----|----------|
| `HUCurpValidation` | curp-validation | `POST /api/v1/customers/curp-validation` |
| `HUIsBlackListed` | is-black-listed | `POST /api/v1/customers/is-black-listed` |
| `HUIsPep` | is-pep | `POST /api/v1/customers/is-pep` |

## Dónde viven los datos

| Qué cambia | Dónde editar |
|------------|--------------|
| Headers/body comunes del curl | `scenarios/customer/<hu>/_base.json` |
| Un solo campo (ej. `curp`, `person_type`) | JSON con `"extends": "customer/<hu>/_base"` y solo el delta |
| Curl completo distinto | JSON nuevo **sin** `extends` |
| URL del servidor | `application.properties` o `API_BASE_URI` |
| Token manual | `api.access.token` o `API_ACCESS_TOKEN` |
| Token automático (Passport) | `passport.base.uri` o `PASSPORT_BASE_URL` |
| Aserciones / flujo | Clase de test (`HUCurpValidation`) |

## Escenarios JSON (HU curp-validation)

Ruta: `src/test/resources/scenarios/customer/curp-validation/`

```
_base.json                      # request base (headers + body del curl)
validacion-exitosa.json         # happy path — smoke
error-curp-formato-invalido.json # error E_CLV_VL_001 — regression
```

Catálogo: `scenarios/catalog/curp-validation-scenarios.txt`

## Escenarios JSON (HU is-black-listed)

Ruta: `src/test/resources/scenarios/customer/is-black-listed/`

```
_base.json                           # request base (headers + body del curl)
consulta-exitosa-no-en-lista-negra.json  # happy path — smoke
error-person-type-null.json          # error E_CLV_VL_001 — regression
```

Catálogo: `scenarios/catalog/is-black-listed-scenarios.txt`

## Escenarios JSON (HU is-pep)

Ruta: `src/test/resources/scenarios/customer/is-pep/`

```
_base.json                      # request base (headers + body del curl)
error-no-encontrado.json        # E_CLV_GN_002 — smoke (activo)
consulta-exitosa-pendiente.json # TODO: response de éxito aún no confirmado
```

Catálogo: `scenarios/catalog/is-pep-scenarios.txt`

> **TODO:** El escenario `consulta-exitosa-pendiente.json` existe con aserciones provisionales (`data` no vacío, `notifications` vacío). Está **comentado** en el catálogo hasta tener el response real de éxito. Cuando lo tengas, actualiza el JSON y descomenta la línea en `is-pep-scenarios.txt`.

Ejemplo de delta (solo cambia el body):

```json
{
  "extends": "customer/curp-validation/_base",
  "name": "error-curp-formato-invalido",
  "body": {
    "curp": "INVALID"
  },
  "expected": {
    "statusCode": 400,
    "body_assertions": [
      { "path": "notifications[0].code", "equals": "E_CLV_VL_001" }
    ]
  }
}
```

## curl de referencia (curp-validation)

```bash
curl --location --globoff '{{ibbaas_cusotmer_validation_mx_host}}/api/v1/customers/curp-validation' \
  --header 'Authorization: Bearer {{access_token}}' \
  --header 'x-b3-traceid: 1' \
  --header 'x-b3-spanid: 1' \
  --header 'x-channel-id: 1' \
  --header 'x-originating-appl-code: 1' \
  --header 'x-country-code: 1' \
  --header 'x-user-context: 1' \
  --header 'x-api-version: 1' \
  --header 'Content-Type: application/json' \
  --data '{
    "curp": "PEGC880313MDFRVR03",
    "request_id": "CUAC01726763563935"
  }'
```

## curl de referencia (is-black-listed)

```bash
curl --location --globoff '{{ibbaas_cusotmer_validation_mx_host}}/api/v1/customers/is-black-listed' \
  --header 'Authorization: Bearer {{access_token}}' \
  --header 'x-b3-traceid: 1' \
  --header 'x-b3-spanid: 1' \
  --header 'x-channel-id: 1' \
  --header 'x-originating-appl-code: 1' \
  --header 'x-country-code: 1' \
  --header 'x-user-context: 1' \
  --header 'x-api-version: 1' \
  --header 'Content-Type: application/json' \
  --data '{
    "person_type": "2",
    "names": "RUBEN",
    "first_surname": "ORTEGA",
    "second_surname": "TELLO",
    "birth_date": "1943-11-23",
    "rfc": "OETR250613BCC"
  }'
```

## curl de referencia (is-pep)

```bash
curl --location --globoff '{{ibbaas_cusotmer_validation_mx_host}}/api/v1/customers/is-pep' \
  --header 'Authorization: Bearer {{access_token}}' \
  --header 'x-b3-traceid: 1' \
  --header 'x-b3-spanid: 1' \
  --header 'x-channel-id: 1' \
  --header 'x-originating-appl-code: 1' \
  --header 'x-country-code: 1' \
  --header 'x-user-context: 1' \
  --header 'x-api-version: 1' \
  --header 'Content-Type: application/json' \
  --data '{
    "first_name": "ANDRES",
    "second_name": "MANUEL",
    "first_surname": "LOPEZ",
    "second_surname": "OBRADOR",
    "birthdate": "1953-11-13"
  }'
```

El header `Authorization` se inyecta automáticamente si no está definido en el JSON del escenario. Prioridad: `api.access.token` manual → token obtenido de Passport (`passport.base.uri` + `passport.token.path`).

## Ajustar códigos HTTP esperados

Los `statusCode` en los JSON son plantillas (200 éxito, 400 error de validación). Actualízalos si tu API responde con otro código (por ejemplo 422 o 200 con error en `notifications`).
