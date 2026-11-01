# Mi lista

## Rama y alcance

`feature/watchlist` parte de `origin/feature/firebase-auth` (`adb3955`): al iniciar el trabajo, `origin/master` seguía en `a7cb9dd` y no contenía la autenticación. Se comprobó un árbol limpio, se actualizó origin y se conservó el `app/google-services.json` local ignorado. No se modifica master, no se hace merge/push ni se despliega a Firebase.

Se añade «Mi lista» a la navegación inferior y a la cuenta verificada, y «Añadir a mi lista» a fichas públicas de películas y series. No se añaden reproductores, listas compartidas, recomendaciones ni bases de datos de contraseñas. Se conservan las versiones de herramientas y el Firebase BoM existente; solo se añade `firebase-firestore` (sin módulo KTX). Las imágenes siguen usando Coil y TMDB; no hay Firebase Storage.

## Modelo y progreso

Cada documento se guarda en `users/{uid}/watchlist/{mediaType_tmdbId}`. `movie_123` y `tv_123` son entradas diferentes.

| Campo | Contenido |
| --- | --- |
| `tmdbId`, `mediaType` | ID positivo y `movie`/`tv` |
| `catalog` | `title`, `posterPath`, `releaseDate`, `runtimeMinutes`, `providers` |
| `status` | `pending`, `in_progress`, `watched` |
| `progress` | Modelo específico, siempre con `unit: minutes` |
| `progressVersion` | Control de concurrencia del progreso, empieza en 0 |
| `createdAt`, `updatedAt` | Timestamps del servidor |

Póster, estreno, duración y plataformas desconocidos se guardan como `null`; no se convierten en cero. Para series, `catalog.runtimeMinutes` siempre es `null`: una duración de episodio no se presenta como duración total. No se hace una consulta por ficha al renderizar cada fila: se muestran los metadatos copiados en Firestore.

Película: `progress = {kind: movie, unit: minutes, positionMinutes: null|entero}`. Minuto positivo implica En curso; cero vuelve a Pendiente; llegar a una duración conocida marca Visto. No se admite una posición negativa ni superior a la duración conocida (límite adicional de 100000 minutos para entradas). Duración desconocida admite posición sin porcentaje. También se puede marcar En curso sin conocer el minuto o Visto manualmente sin inventarlo.

Serie: `progress = {kind: tv, unit: minutes, current: null|{episode, positionMinutes, mode}, lastCompleted: null|episode}`. El snapshot de episodio contiene ID, temporada, número, título, fecha y duración nullable. `mode` diferencia `watching` de `next`. Las temporadas y episodios se obtienen de TMDB; temporada **0** significa especiales. No se permite seleccionar un episodio con fecha futura conocida; una fecha desconocida permite selección manual, pero nunca avance automático por suposición. Los episodios de una temporada se descargan al abrir/seleccionar esa temporada, con una caché pública RAM acotada a 40 temporadas; «Reintentar» fuerza su actualización. Al terminar un episodio se refresca esa temporada y se busca el siguiente con fecha de emisión conocida no posterior al día actual, primero en la misma temporada y después en temporadas posteriores emitidas. Puede consultar temporadas adicionales durante esa acción explícita; nunca se descargan todas al añadir la serie.

«Estoy viendo este episodio» guarda la posición manual. «Marcar episodio como visto» guarda `lastCompleted` y, si existe, selecciona el siguiente emitido con minuto 0 y `mode: next`. Si no hay siguiente emitido conocido, `current` queda null y se conserva el último terminado. **No marca toda la serie Vista**, no presupone vistos episodios intermedios ni calcula un porcentaje total. La serie solo pasa a Vista por elección manual y las nuevas temporadas no cambian ese estado.

Cambiar a Pendiente elimina la posición y el último episodio terminado. Marcar Visto manualmente también elimina esa posición parcial; el propio estado registra la finalización. Volver desde Visto a En curso permite introducir una nueva posición, sin reutilizar automáticamente un final de película. Si el cambio de estado reinicia progreso existente, el editor pide confirmación. Los cambios se envían al confirmar, no por cada pulsación. Eliminar requiere confirmación separada.

## Metadatos y disponibilidad

Se utilizan los endpoints oficiales `movie/{id}/watch/providers`, `tv/{id}/watch/providers`, detalles y `tv/{id}/season/{season}`. No existía una preferencia regional en el proyecto: se usa **ES** y se conserva la región guardada al actualizar. `providers` almacena región, enlace suministrado por TMDB, timestamp de consulta/actualización en servidor y listas distintas para `subscription` (flatrate), `free`, `ads`, `rent` y `buy`. Se admiten múltiples proveedores por grupo. Productoras y cadenas no se utilizan como plataformas.

Una respuesta sin disponibilidad para ES se presenta como «Sin información de disponibilidad». Un fallo de red al obtener proveedores no impide añadir metadatos básicos, pero se guarda providers=null sin una fecha inventada; se puede volver a consultar. Actualizar plataformas modifica únicamente `catalog.providers` y `updatedAt`, sin sobrescribir `status`, `progress`, `progressVersion` ni `createdAt`. Los metadatos del catálogo quedan separados del progreso; los snapshots de episodio del progreso no se sustituyen por una actualización de disponibilidad.

La UI atribuye disponibilidad a **JustWatch**, utiliza solo el enlace HTTPS recibido de TMDB y no fabrica enlaces de reproducción. La sección «Créditos» de Mi lista incluye la marca oficial de TMDB y su aviso requerido. El vector Android conserva geometría, proporción y colores del [SVG oficial de su documentación](https://files.readme.io/29c6fee-blue_short.svg); no se ha diseñado un logotipo alternativo.

## Persistencia, aislamiento y consultas

- `WatchlistRepository` separa persistencia y UI; `FirestoreWatchlistRepository` comprueba el UID/verificación locales y las reglas vuelven a comprobarlos en servidor. Se reutiliza el estado autenticado ya validado por `AuthViewModel`.
- Añadir es una transacción idempotente: si el documento existe no se escribe sobre él. Editar comprueba `progressVersion` dentro de una transacción para rechazar un editor obsoleto en vez de pisar cambios de otro dispositivo. Se conservan todos los metadatos. Las actualizaciones del SDK se vuelven a leer del servidor para mostrar datos confirmados.
- Se usa `MemoryCacheSettings`, sin persistencia Firestore en disco. Las lecturas visibles usan `Source.SERVER` y las mutaciones son transacciones, que no se encolan como escrituras offline. La UI muestra espera hasta confirmación y conserva los valores previos si falla. Si una respuesta se pierde tras un commit, no se anuncia éxito sin confirmación; actualizar permite recuperar el estado real y volver a añadir es seguro.
- No hay listeners de colecciones Firestore abiertos permanentemente. La lista consulta páginas de **30**, filtra por tipo y estado en servidor, ordena por incorporación/actualización y usa cursor de timestamp con precisión completa (segundos/nanosegundos) más ID. «Cargar más» solicita otra página. Tras una escritura se actualiza la ficha compartida y se refresca la primera página de la consulta activa para mantener el orden; se conservan filas mientras llega la respuesta y se pueden volver a cargar páginas adicionales. «Actualizar lista» recoge también cambios hechos desde otros dispositivos: no se promete sincronización push instantánea entre dispositivos.
- Al perder/cambiar la sesión se cancelan trabajos de consulta/edición y se borran entradas, membresías, errores, filtros, cursor y editor visibles. Se comprueba la identidad de cada respuesta tardía y la UI exige que el UID mostrado coincida con el de Auth. Los caches internos RAM del SDK quedan separados por rutas UID y no se usan como fuente visible offline; otro UID tampoco puede leerlos del servidor. La caché de temporadas es pública, sin progreso personal.
- Para retomar «Añadir» tras login/verificación solo se conservan en `SavedStateHandle` el tipo y el ID TMDB. No se conserva título, contraseña, token ni progreso privado. Se elimina la intención tras completarla o al abandonar una identidad ya autenticada. El retorno lleva a la ficha original y cualquier error de guardado queda visible allí.

## Reglas e índices

No había reglas Firestore locales previas. `firestore.rules` permite solo al propietario con `email_verified == true` leer, crear, editar o eliminar bajo su UID. Las consultas de lista requieren un límite máximo 30. Se validan campos permitidos, tipos, documento determinista, estados, timestamps de servidor, unidad, rangos de minutos, temporada/episodio, progreso coherente, identidad inmutable y versiones. Todo lo demás se deniega.

Limitaciones explícitas: las reglas no pueden consultar TMDB ni demostrar que una temporada/episodio existe o que la duración es correcta; eso se valida contra el catálogo en la aplicación. Las listas de proveedores tienen tipo y tamaño acotados, pero las reglas no iteran todos los elementos de una lista arbitraria; el mapeo tipado y la UI no tratan esos textos como HTML o autorización. Para aceptar catálogo como dato confiable de terceros frente a un cliente manipulado haría falta validación en un backend propio. El UID y la verificación, en cambio, se comprueban en servidor y no dependen de la navegación. No se guardan tokens ni contraseñas.

`firestore.indexes.json` contiene seis índices compuestos: tipo, estado o tipo+estado combinados con cada orden (`createdAt`/`updatedAt` descendente e ID descendente). Los casos sin filtros usan índices automáticos. El emulador comprueba reglas pero **no certifica que los índices compuestos estén desplegados en producción**.

## Configuración manual de Cloud Firestore

1. Selecciona en Firebase Console el mismo proyecto del `project_id` de `app/google-services.json` y confirma que Email/Password y la verificación funcionan.
2. En **Build → Firestore Database**, crea la base **`(default)`**, edición Standard/Native, y selecciona una ubicación apropiada antes de confirmar. Empieza con acceso de producción cerrado, no con reglas abiertas de prueba.
3. Revisa las reglas actuales de la consola **antes** de publicar. Si ya existen reglas para otras colecciones/apps, no reemplaces el archivo entero: integra las funciones y el match de watchlist y revisa permisos amplios existentes (los permisos coincidentes se combinan por OR). El archivo local deniega otras colecciones porque en este repositorio no había más usos; no implica autorización para eliminar reglas remotas previas.
4. Publica las reglas revisadas y crea/importa los índices de `firestore.indexes.json`. Solo si has revisado los archivos completos y el destino, puedes ejecutarlo tú manualmente: `firebase.cmd deploy --only "firestore:rules,firestore:indexes" --project TU_PROJECT_ID_REAL`. Espera a que los índices estén habilitados. **No se ha ejecutado este despliegue ni se ha creado una base remota durante la implementación.**
5. Los documentos se crean al añadir contenido; no hace falta crear perfiles de usuario ni subir imágenes a Storage. Revisa métricas/cuotas y App Check siguiendo `firebase-auth.md`; no se activa enforcement automáticamente.

## Emuladores y pruebas reproducibles

Requisitos: JDK del proyecto, Node 22+, Firebase CLI y un emulador Android arrancado para instrumentación. En PowerShell usa **`firebase.cmd`** si la política del equipo no permite el shim `firebase.ps1`; no es necesario cambiar esa política. `firebase.json` arranca Authentication 9099, Firestore 8080 y la UI 4000, ligados a localhost. Los tests se niegan a usar hosts externos o un proyecto sin prefijo demo.

Pruebas de reglas e integración REST, con registro y verificación reales **dentro de Auth Emulator**, sin correos ni cuentas reales y sin dependencias npm:

```powershell
firebase.cmd emulators:exec --only "auth,firestore" --project demo-cineverse-watchlist "node --test tests/emulators/watchlist.test.mjs"
```

Pruebas Android contra ambos emuladores, incluyendo las transacciones del SDK y aislamiento entre cuentas:

```powershell
firebase.cmd emulators:exec --only "auth,firestore" --project demo-cineverse-watchlist ".\gradlew.bat :app:connectedDebugAndroidTest -PfirebaseAuthEmulator=true --console=plain"
```

Las pruebas Android de repositorio usan una FirebaseApp **nombrada y exclusiva de test**, proyecto demo y endpoints `10.0.2.2`. No sustituyen el JSON de la app ni tocan datos de producción. Si se ejecutan sin la propiedad, esos tests se omiten; esto no debe confundirse con una integración aprobada. Las pruebas de UI usan dobles y no solicitan fichas reales a TMDB.

Compilación, pruebas unitarias y Lint:

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease :app:lintDebug -PfirebaseAuthEmulator=false
```

Para probar la **app manualmente**, usa el project_id real del JSON en lugar del proyecto demo: `firebase.cmd emulators:start --only "auth,firestore" --project TU_PROJECT_ID_REAL`. En otra terminal instala con `.\gradlew.bat :app:installDebug -PfirebaseAuthEmulator=true`, abre CineVerse y utiliza cuentas locales `@example.test`. El indicador existente `firebaseAuthEmulator` controla ahora ambos SDKs; release siempre lo fija a false. El catálogo sigue consultando TMDB; estos dos emuladores solo sustituyen Authentication y Firestore. Para alternar local/producción, usa un dispositivo de pruebas separado o borra tú los datos de la app desde Ajustes antes del cambio de entorno.

Comprueba: catálogo anónimo → añadir → login/registro local → enlace de verificación en terminal/UI local → volver a ficha → En mi lista; pulsaciones repetidas; filtros y paginación; minuto 45 de película y validaciones; especiales T0; episodio actual frente a terminado y avance; marcar serie Vista y actualizar plataformas; cancelar/confirmar eliminación; cerrar sesión y entrar con otra cuenta. Repite offline y verifica que una operación no confirmada no aparece guardada. Prueba también teclado, pegado y fuente grande.

## Resultado de esta entrega

- **46 pruebas unitarias correctas** (19 nuevas de lista, catálogo y progreso, más las 27 existentes).
- **9 pruebas Android correctas, 0 omitidas**, en Pixel 8/API 37: las 4 existentes y 5 nuevas (3 de interfaz, incluida fuente 1,6×, y 2 de repositorio con los SDK reales contra ambos emuladores).
- **6 pruebas REST de integración/reglas correctas** contra Authentication Emulator y Firestore Emulator, usando solo cuentas locales `@example.test` y el proyecto demo.
- APK debug en modo normal y APK release sin firmar compilados. Lint debug: **0 errores, 87 advertencias**; no se han desactivado inspecciones para pasar las comprobaciones. Persisten avisos de dependencias/recursos y de Java 8 con el JDK 21 del entorno.
- Se corrigió el área de desplazamiento de la lista para que sus botones no queden bajo la navegación flotante al usar fuente grande. El emulador bloqueado se reinició sin borrar datos antes de repetir satisfactoriamente la tanda completa.
- No se ha verificado ni modificado la base de producción: crearla si falta, revisar/publicar reglas e índices y comprobar su disponibilidad en el proyecto real son pasos manuales. El JSON existente se reutiliza y sigue fuera de Git. No hay commit, push ni merge automático.

## Fuentes oficiales

- [Caché en Firestore](https://firebase.google.com/docs/firestore/manage-data/enable-offline), [transacciones](https://firebase.google.com/docs/firestore/manage-data/transactions), [paginación](https://firebase.google.com/docs/firestore/query-data/query-cursors), [pruebas de reglas](https://firebase.google.com/docs/firestore/security/test-rules-emulator).
- [Proveedores de películas](https://developer.themoviedb.org/reference/movie-watch-providers), [proveedores de series](https://developer.themoviedb.org/reference/tv-series-watch-providers), [episodios por temporada](https://developer.themoviedb.org/reference/tv-season-details), [atribución](https://developer.themoviedb.org/docs/faq).
