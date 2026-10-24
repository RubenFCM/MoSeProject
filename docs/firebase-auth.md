# Autenticación de CineVerse

## Estado de la entrega

La rama `feature/firebase-auth` parte de `origin/master` (`a7cb9dd`). El árbol de trabajo estaba limpio antes de crearla. No se modifica `master`, no se fusiona y no se publica la rama.

Se implementan acceso con correo/contraseña, registro con confirmación, envío y reenvío de verificación, comprobación explícita de verificación, recuperación, restauración y cierre de sesión. El catálogo, la búsqueda y los detalles siguen siendo públicos. «Mi cuenta», en la cabecera, abre el flujo. La cuenta autenticada muestra únicamente el correo verificado y el cierre de sesión; las listas personales no se implementan.

No se encontró configuración Firebase, Firestore, Realtime Database, Storage, backend propio ni almacenamiento personal local. **No hay un `google-services.json` ni un proyecto Firebase inventados. Sin configuración real, la cuenta muestra un estado de indisponibilidad y el catálogo sigue funcionando.** Las pruebas con dobles no demuestran conexión con un proyecto Firebase real.

## Configuración manual obligatoria

1. Abre [Firebase Console](https://console.firebase.google.com/), selecciona tu proyecto real o crea el que quieras utilizar y registra una aplicación **Android**.
2. Usa exactamente el `applicationId` actual: **`com.example.moseproject`**. No uses el nombre visual CineVerse como identificador. No se ha cambiado el paquete. SHA-1/SHA-256 no son necesarios para este proveedor de correo/contraseña; Play Integrity sí requiere configuración de firma adicional.
3. Descarga su `google-services.json` y colócalo en **`app/google-services.json`**. Está ignorado por Git. El plugin Google Services se aplica al encontrar ese archivo (también admite archivos en `app/src/debug/` o `app/src/release/`; cada variante que construyas debe disponer de su configuración). Sin el archivo no se ejecuta el plugin y no se genera una configuración ficticia. Sincroniza Gradle y reinstala la app tras añadirlo. El JSON contiene identificadores públicos del cliente, **no** credenciales de servidor. Nunca añadas una cuenta de servicio ni Firebase Admin SDK a Android.
4. En **Authentication → Sign-in method**, habilita **Email/Password**, guarda y deja desactivado el acceso mediante enlace de correo si no lo necesitas. No se han añadido Google Sign-In, teléfono ni otros proveedores.
5. En **Authentication → Settings → Password policy**, configura tu política en modo **Require**. La política de Firebase es la autoridad; la validación local solo orienta al usuario. Antes de distribuir la app, alinea los valores públicos siguientes con la consola. No se conoce la política de un proyecto que todavía no has proporcionado.

   | Propiedad Gradle | Valor inicial (predeterminado de Firebase) |
   | --- | --- |
   | `authPasswordMinLength` | `6` (Firebase admite 6–30) |
   | `authPasswordMaxLength` | `4096` |
   | `authPasswordLowercase` | `false` |
   | `authPasswordUppercase` | `false` |
   | `authPasswordNumber` | `false` |
   | `authPasswordSymbol` | `false` |

   Configúralas en tu `~/.gradle/gradle.properties`, o pásalas con `-P`, por ejemplo `-PauthPasswordMinLength=12 -PauthPasswordUppercase=true -PauthPasswordNumber=true`. La longitud máxima debe ser mayor o igual que la mínima. El conjunto de símbolos locales coincide con el documentado por Firebase: `^ $ * . [ ] { } ( ) ? " ! @ # % & / \ , > < ' : ; | _ ~`. No se recortan ni transforman las contraseñas. El inicio de sesión no obliga a cumplir una nueva política que pueda bloquear contraseñas antiguas. Firebase puede rechazar una contraseña aunque pase la validación local si no has alineado las políticas: se muestra un error seguro, nunca se relaja la validación del servidor.
6. En **Authentication → Settings → User actions**, comprueba que **Email enumeration protection** está activada. Comprueba su estado real incluso si tu proyecto es reciente. El cliente no utiliza consultas de existencia de correos; acceso y registro tienen errores genéricos, y recuperación utiliza la misma confirmación para cuentas existentes e inexistentes. La comprobación de producción de esta protección debe hacerse con cuentas de prueba tuyas y las métricas de Firebase; las pruebas automáticas incluidas no usan cuentas ni correos reales.
7. En **Authentication → Templates**, configura las plantillas de **Email address verification** y **Password reset**, remitente, nombre de CineVerse y traducción al español. El SDK solicita `es`. Se utilizan las páginas de acción alojadas por Firebase; no se necesitan Dynamic Links ni un dominio o enlaces profundos inventados. Si personalizas los enlaces, configura también dominios autorizados y comprueba los flujos completos antes de distribuirlos.

## Dependencias y compatibilidad

Se conservan Kotlin/Compose Compiler **2.0.21**, AGP **8.5.2**, Gradle **8.8**, `compileSdk/targetSdk=34`, `minSdk=28` y el BoM de Compose **2023.08.00**. Se añade Firebase mediante el catálogo y BoM **34.1.0** (`firebase-auth` **24.0.1**), Google Services **4.4.3**, el puente de tareas de corrutinas **1.7.3** y Lifecycle Compose **2.7.0**, alineado con Lifecycle existente. No hay dependencias `firebase-*-ktx`. Se fija una versión compatible con las herramientas actuales, evitando Auth 24.0.0, que tiene una regresión documentada; no se hace una actualización general del proyecto. Las actualizaciones futuras deben volver a comprobar los requisitos de compilación y metadatos de Kotlin.

Espresso se ajusta de 3.5.1 a 3.7.0 únicamente para las nuevas pruebas de interfaz: la versión anterior falla antes de ejecutar las pantallas en el emulador Android 17/API 37 instalado, al buscar el método de plataforma eliminado `InputManager.getInstance`. No cambia las dependencias del APK de producción ni las herramientas de compilación.

## Arquitectura y comportamiento de sesión

- `AuthRepository` define el contrato; `FirebaseAuthRepository` es el único lugar que llama a Firebase Authentication. `CineVerseApplication` conserva una instancia por proceso. `MissingFirebaseConfiguration` comunica indisponibilidad explícita: no autentica de forma simulada.
- `AuthViewModel` publica `AuthUiState` y sesiones `Initializing`, `SignedOut`, `PendingVerification`, `Authenticated` o `Unavailable`. Las pantallas recogen el estado con `collectAsStateWithLifecycle`. La sesión inicial pasa por recarga del usuario y renovación forzada del ID token, antes de mostrar la cuenta verificada. Los fallos de red durante la restauración dejan la cuenta cerrada para navegación privada, con reintento; se puede continuar explorando.
- El listener de ID token solo proporciona señales de cambio de usuario. Se elimina mediante `awaitClose` al cancelar la colección. Una renovación del mismo usuario no inicia un bucle de peticiones porque los snapshots se deduplican. La verificación exige tanto el indicador del usuario recargado como `email_verified` de las claims renovadas. El token no se extrae, copia, registra ni guarda por la app.
- Firebase Tasks no permiten cancelar sus efectos. El repositorio termina cada operación del SDK bajo exclusión mutua incluso si se cancela su consumidor, para que un inicio de sesión tardío no revierta un cierre de sesión posterior. La cancelación de las corrutinas se propaga y no se transforma en un mensaje de error de usuario; no hay corrutinas independientes sin propietario.
- La navegación de cuenta se recrea al cambiar de UID o estado de seguridad. Al cerrar sesión se eliminan identidad, mensajes y tiempos de espera; se descarta el árbol de navegación de la cuenta, por lo que Atrás no recupera una cuenta privada. No existen todavía cachés de listas o datos personales que transferir entre cuentas. Al añadirlas, deben estar ligadas al UID y limpiarse también en el repositorio y al perder la sesión.
- Las contraseñas solo se introducen en estado `remember` de la pantalla, nunca `rememberSaveable` ni `SavedStateHandle`. Se limpian al enviar una solicitud aceptada, al pasar a segundo plano y al descartar la pantalla. No se desactiva pegar ni los gestores; se registran nodos de Autofill compatibles con Compose actual y se eliminan al salir. Correo y contraseña tienen etiquetas, teclado adecuado, acciones IME y mensajes accesibles; la visibilidad de la contraseña es opcional.
- El botón de envío se bloquea sincrónicamente antes de lanzar la corrutina. Reenvío y recuperación esperan **60 segundos** entre intentos; comprobación de verificación espera **5 segundos**. Los intentos fallidos también reservan el intervalo. Estos controles son de experiencia de usuario, no una defensa suficiente contra abuso y se reinician con el proceso; Firebase debe aplicar cuotas y controles del servidor.
- Firebase mantiene su propia sesión persistente. La app no copia contraseñas o tokens a preferencias, archivos, logs o DataStore. Las reglas de copia de seguridad excluyen preferencias tanto de copias en nube como de transferencias de dispositivo para no migrar sesiones de Firebase.

## Autorización de datos y App Check

La [tabla oficial de servicios compatibles](https://firebase.google.com/docs/app-check) identifica la integración con Firebase Authentication como **Preview**. Comprueba su disponibilidad y condiciones en tu proyecto antes de planificar su activación.

El repositorio solo consume la API pública de TMDB y Firebase Auth. No se ha creado una base de datos para replicar usuarios o guardar contraseñas. El estado de navegación **no es una frontera de autorización**. Si después añades datos personales, configura reglas que comprueben `request.auth.uid` y `request.auth.token.email_verified == true`, y vincula cada documento al propietario. En Realtime Database/Storage utiliza sus reglas equivalentes. Deben probarse accesos denegados entre UIDs y para correos no verificados. Si añades un backend propio, valida el ID token y sus claims en el servidor con un mecanismo oficial; un UID enviado por el cliente no demuestra identidad.

App Check con Play Integrity puede complementar la protección de Authentication **cuando esté disponible para la configuración de tu proyecto**, y de futuros servicios Firebase compatibles. No sustituye Firebase Auth ni las reglas por UID/correo verificado, y no protege por sí solo la API externa de TMDB. No se ha instalado ni activado enforcement sin un proyecto y métricas. Para evaluarlo: registra la app en App Check con su SHA-256 de firma, configura/vincula Play Integrity en Play Console y Firebase según la distribución de la app y su proyecto, revisa cuotas, facturación y requisitos del servicio, distribuye primero un cliente instrumentado y observa métricas antes de exigir tokens. Los veredictos `PLAY_RECOGNIZED`/`LICENSED` dependen de cómo se publica e instala la app; no los exijas a instalaciones legítimas que no puedan obtenerlos. En desarrollo/emulador se necesita el proveedor debug registrado (sin versionar su token); un emulador normal no equivale a un dispositivo validado por Play Integrity. App Check no se usa para simular una autenticación o eludir reglas.

## Probar en el emulador Android sin enviar correos reales

Requisitos: Android SDK/JDK existentes, Firebase CLI instalada, y un `google-services.json` real para la aplicación indicada. La configuración de emuladores en `firebase.json` solo define puertos locales, no un proyecto. Sustituye `TU_PROJECT_ID_REAL` por el `project_id` de tu archivo; no se proporciona un identificador de Firebase ficticio.

1. En la raíz, inicia el emulador de Authentication en tu PC:

   ```powershell
   firebase emulators:start --only auth --project TU_PROJECT_ID_REAL
   ```

   Usa únicamente cuentas de prueba con direcciones como `viewer@example.test`. La consola local estará en `http://127.0.0.1:4000`; Auth escucha en `127.0.0.1:9099`. El emulador no envía correos; muestra enlaces de verificación y recuperación en su terminal/interfaz. No uses cuentas de producción. El emulador no reproduce los controles antiabuso ni garantiza la política o protección contra enumeración de producción.

2. Compila e instala **debug**, explicitando el destino local:

   ```powershell
   .\gradlew.bat :app:installDebug -PfirebaseAuthEmulator=true
   & 'D:/PROGRAMACION/SDKandorid/platform-tools/adb.exe' shell am start -n com.example.moseproject/.ui.view.MainActivity
   ```

   El SDK usa `10.0.2.2:9099` (PC visto desde el emulador Android). El tráfico HTTP se permite solo para hosts locales en el manifiesto debug. Release siempre fija `AUTH_USE_EMULATOR=false` e incluye únicamente las reglas de red de producción. No cambies de entorno conservando sesiones: borra los datos de la app desde Ajustes de Android o usa un dispositivo de pruebas separado antes de alternar entre emulador y producción.

3. Comprueba que el catálogo sigue siendo público. Abre **Mi cuenta → Crear cuenta**, prueba confirmaciones distintas y luego registra una cuenta de prueba válida. Debe aparecer la pantalla pendiente, sin una cuenta privada verificada.
4. Abre en el PC el enlace local de verificación que muestra Auth Emulator. Vuelve a Android y pulsa **Ya he verificado mi correo**. La cuenta solo se muestra después de recargar usuario y token. Prueba reenviar y el tiempo de espera sin pulsaciones duplicadas.
5. Fuerza el cierre y vuelve a abrir la app: la sesión se restaura y valida antes de mostrar la cuenta. Desactiva la red o detén el emulador: debe aparecer un error recuperable, sin mostrar contenido privado durante la comprobación.
6. Pulsa **Cerrar sesión**, prueba Atrás y luego entra con otra cuenta de prueba: no debe reaparecer el correo anterior. Prueba **He olvidado mi contraseña** para direcciones de prueba existentes e inexistentes: la confirmación no debe revelar cuál existe. Usa el enlace de recuperación local y vuelve a iniciar sesión con la nueva contraseña de prueba.
7. Prueba teclado, desplazamiento, tamaños de fuente, lector de pantalla, pegado y el gestor de contraseñas del dispositivo. Las contraseñas se limpian al salir a segundo plano; vuelve a introducirlas si cambias al gestor fuera de la app.

Sin el JSON real, puedes compilar, explorar y comprobar la pantalla de cuenta no configurada, pero **no** probar autenticación funcional contra Firebase o Auth Emulator.

## Verificaciones automatizadas

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease :app:lintDebug
.\gradlew.bat :app:connectedDebugAndroidTest
```

Las pruebas unitarias usan un repositorio doble y Tasks/listeners del SDK doblados: cubren restauración sin acceso prematuro, errores, verificación, intervalos, duplicados, contraseñas exactas, cierre/cambio de cuenta, eliminación de listeners y serialización entre una Task de inicio cancelada y el cierre. Las pruebas de interfaz usan un repositorio doble, nunca un proyecto real. No se han automatizado envíos a cuentas reales. Consulta el resultado de ejecución de esta entrega; la existencia de un test o un comando no implica que se haya ejecutado. El flujo real de correo, la política y plantillas de tu proyecto, la configuración de App Check y la conexión al emulador Firebase quedan por comprobar tras facilitar la configuración externa.

## Documentación oficial consultada

Resultado de ejecución de esta entrega (sin configuración de proyecto Firebase): **27 pruebas unitarias correctas**, **4 pruebas instrumentadas correctas** en Pixel 8/API 37 (3 nuevas de autenticación y la existente), APK debug y release sin firmar compilados, y Lint debug con **0 errores y 87 advertencias**. No se han desactivado inspecciones para pasar los controles. Persisten avisos de versiones/recursos y del uso de Java 8 con el JDK 21 del entorno; no se ha actualizado el conjunto de herramientas para ocultarlos. No se ha ejecutado Auth Emulator ni autenticación real, porque falta el archivo del proyecto.

- [Configuración de Firebase Android y BoM](https://firebase.google.com/docs/android/setup).
- [Notas de versión Android (BoM 34.1.0 y corrección de Auth 24.0.1)](https://firebase.google.com/support/release-notes/android).
- [Correo y contraseña, política y protección contra enumeración](https://firebase.google.com/docs/auth/android/password-auth).
- [Gestión de usuarios y verificación](https://firebase.google.com/docs/auth/android/manage-users).
- [Authentication Emulator](https://firebase.google.com/docs/emulator-suite/connect_auth).
- [App Check con Play Integrity](https://firebase.google.com/docs/app-check/android/play-integrity-provider).
- [Verificación de ID tokens en un servidor](https://firebase.google.com/docs/auth/admin/verify-id-tokens).
