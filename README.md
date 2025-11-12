# PruebasRapiCredit

Este repositorio contiene las pruebas automatizadas (Serenity BDD + Screenplay + Selenium + Gradle) para el sitio Rapicredit.

Resumen rápido
- Lenguaje: Java 17
- Build: Gradle (wrapper incluido)
- Framework de pruebas: Serenity BDD (Screenplay) + Cucumber (v6) + JUnit 4
- Navegador objetivo para ejecución: Firefox (GeckoDriver)
- Reportes: Serenity single-page HTML en `target/site/serenity`

Contenido de este README
- Estructura del proyecto
- Requisitos previos
- Cómo ejecutar las pruebas (Windows, cmd.exe)
- Cómo regenerar y ver el reporte de Serenity correctamente
- Ejecutar pruebas específicas (feature, tag, runner)


1) Estructura importante del proyecto
- `build.gradle` / `settings.gradle` - configuración del build y dependencias.
- `serenity.properties` - configuración de Serenity (driver, report output, timeouts).
- `src/test/resources/features` - archivos Gherkin (.feature).
- `src/test/java` - código de tests, runners, stepdefinitions, tasks, interactions, questions, models.
- `target/site/serenity` - salida del reporte generado (`index.html`, assets, CSV/JSON/HTML por escenario).

Convecciones usadas (esperadas)
- Patrón Screenplay: `tasks/`, `interactions/`, `questions/`, `models/`.
- `runners` o `runners.*` contiene clases runner que ejecutan los features.
- `stepdefinitions` pueden existir si se usa Cucumber; con Serenity Screenplay puedes preferir Tasks/Interactions en vez de pasos largos.

2) Requisitos previos (Windows)
- Java 17 instalado y JAVA_HOME apuntando a JDK 17.
- Gradle wrapper incluido (usa `gradlew.bat` del repo).
- Firefox instalado.
- GeckoDriver: ya hay un binario en `src/test/resources/geckodriver/geckodriver.exe` (si no, descargar y actualizar ruta en `serenity.properties`).
- (Opcional) Python 3 o Node para servir el reporte localmente.

3) Ejecutar todas las pruebas y generar reporte (cmd.exe)
Abre un terminal cmd.exe en la raíz del proyecto y ejecuta:

```cmd
cd C:\Users\carol\Documentos\PruebasRapiCredit
gradlew.bat clean test aggregate --no-daemon
```

- `test` ejecuta las pruebas.
- `aggregate` genera el reporte de Serenity en `target/site/serenity` (el build.gradle está configurado para ejecutar `aggregate` tras `test`).

4) Ver el reporte correctamente (evitar errores de MIME/404)
Problema común: al abrir `target/site/serenity/index.html` con el servidor del IDE o con `file://` aparecen errores en consola: recursos .js/.css devuelven 404 o se sirven con `text/html` (X-Content-Type-Options: nosniff). Esto provoca que las gráficas y tablas no carguen.

Por qué ocurre: el servidor que está sirviendo los ficheros (por ejemplo, el servidor integrado del IDE) no mapea rutas relativas correctamente o responde con páginas HTML de error en vez de reenviar el contenido estático con los Content-Type correctos.

Recomendación (rápida y fiable): servir la carpeta `target/site/serenity` con un servidor HTTP simple que entregue los tipos MIME correctos.

Opción A — Python 3 (recomendado si tienes Python):

```cmd
cd C:\Users\carol\Documentos\PruebasRapiCredit\target\site\serenity
python -m http.server 8000
```
Abrir en el navegador: http://localhost:8000/index.html

Opción B — Node (http-server):

```cmd
npm install -g http-server
cd C:\Users\carol\Documentos\PruebasRapiCredit\target\site\serenity
http-server -p 8000
```

5) Ejecutar una única feature / un único escenario o tags
- Ejecutar un feature específico (usando Maven/Gradle runner depende de la configuración del runner). Ejemplo para ejecutar con JUnit serenty/cucumber runner (si está configurado):

```cmd
gradlew.bat -Dtest.single=YourRunnerClassName test
```

```cmd
gradlew.bat test -Dcucumber.filter.tags="@faq"
```

6) Comandos útiles (resumen)

```cmd
cd C:\Users\carol\Documentos\PruebasRapiCredit
gradlew.bat clean test aggregate --no-daemon

:: Servir reporte (Python)
cd target\site\serenity
python -m http.server 8000

:: Abrir en Windows
start http://localhost:8000/index.html
```
