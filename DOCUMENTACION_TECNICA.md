# Documentación técnica – Alke Wallet

## 1. Resumen

Alke Wallet permite registrarse, iniciar sesión, ver el saldo y el perfil (con foto), consultar el historial de transacciones y enviar dinero. Los datos vienen de una API REST (Retrofit), se guardan en una base local (Room) para verlos sin conexión, y la foto de perfil se carga con Picasso.

| Capa / tema | Tecnología |
|---|---|
| Lenguaje / UI | Kotlin, XML + ViewBinding, Material 3 |
| Arquitectura | MVVM (View – ViewModel – Repository/Model) |
| API REST | Retrofit 2 + OkHttp + Gson |
| Base local | Room (KSP) |
| Imágenes | Picasso |
| Sesión | Token Bearer en `EncryptedSharedPreferences` |
| Asincronía | Coroutines + Flow + LiveData |
| Pruebas | JUnit4, coroutines-test, MockWebServer, Room in-memory |

## 2. Estructura del código

```
cl.alkewallet
├── WalletApp.kt              Application + AppContainer (inyección manual de dependencias)
├── data
│   ├── local                 MODELO – Room: UserEntity, TransactionEntity, UserDao, TransactionDao, AppDatabase
│   ├── remote                MODELO – Retrofit: WalletApi, Dto, AuthInterceptor, RetrofitClient
│   ├── session               TokenStore + SessionManager (token cifrado)
│   └── repository            AuthRepository(+Impl), WalletRepository(+Impl)
├── ui
│   ├── auth                  VISTA: LoginActivity, RegisterActivity · VIEWMODEL: AuthViewModel
│   ├── main                  VISTA: MainActivity, HomeFragment, TransactionsFragment, SendFragment, TransactionAdapter
│   │                         VIEWMODEL: WalletViewModel
│   ├── ViewModelFactory, ImageLoader (Picasso), CircleTransform
└── util                      AppResult, ErrorMapper, Messages, Validators, Formatters, Event
```

## 3. Arquitectura MVVM

```
 View (Activity/Fragment) ──observa LiveData──▶ ViewModel ──llama──▶ Repository ──▶ Retrofit (API)
        ▲  eventos de usuario                         │                      └────▶ Room (BD local)
        └─────────────────────────────────────────────┘ (la UI solo habla con el ViewModel)
```

- **Model**: entidades Room, DTO de la API y repositorios. Los repositorios son la única puerta de acceso a Retrofit y Room.
- **View**: Activities/Fragments con ViewBinding. No tienen lógica de negocio: capturan clics, leen campos y pintan lo que el ViewModel expone.
- **ViewModel**: valida formularios, coordina repositorios y expone estado (`LiveData`) y eventos de un solo uso (`Event`). No conoce clases de la vista.

**Decisiones de diseño**

- *Room como fuente de verdad de la UI*: el ViewModel expone `Flow` de Room como `LiveData`. Cada sincronización con la API reemplaza los datos locales y la pantalla se actualiza sola; sin conexión se muestran los últimos datos guardados.
- *Repositorios con interfaz* (`AuthRepository`, `WalletRepository`) para poder probar los ViewModels con repositorios falsos.
- *`AppResult`* en vez de excepciones: las capas inferiores devuelven `Success` o `Failure(mensaje, tipo)`, y el ViewModel decide qué mostrar.
- *Un `WalletViewModel` compartido* (scope de `MainActivity`) entre Inicio, Movimientos y Enviar, para que compartan datos y no repitan llamadas.
- *Inyección manual* (`AppContainer`): suficiente para el tamaño del proyecto y sin dependencias extra.

## 4. Base de datos local (Room)

- **Entidades**: `UserEntity` (perfil: id, username, email, avatarUrl, balance) y `TransactionEntity` (id, amount, description, date, type). No se guardan contraseñas ni tokens.
- **DAO** (CRUD completo): `UserDao` (`upsert`, `update`, `delete`, `observeCurrent`, `getCurrent`, `clear`) y `TransactionDao` (`insert`, `insertAll`, `update`, `delete`, `observeAll` ordenado por fecha descendente, `getById`, `clear`).
- **Sincronización**: `WalletRepositoryImpl.refresh()` descarga perfil e historial y los guarda en **una sola transacción** (`db.withTransaction`), así nunca queda la base a medias.
- **Cierre de sesión**: borra token y tablas locales.
- `fallbackToDestructiveMigration()`: como la API es la fuente de verdad, un cambio de esquema recrea la caché.

## 5. Retrofit y manejo de errores

- `RetrofitClient` crea `WalletApi` con timeouts de 15 s, `AuthInterceptor` (agrega `Authorization: Bearer <token>`) y, solo en *debug*, un logger de nivel BASIC que oculta la cabecera `Authorization`.
- Las funciones `suspend` de `WalletApi` usan GET (`users/me`, `transactions`) y POST (`auth/login`, `auth/register`, `transactions`).
- `safeApiCall` / `safeDbCall` capturan fallos y `ErrorMapper` los convierte en mensajes claros:

| Situación | Tipo | Mensaje al usuario |
|---|---|---|
| Sin internet / servidor inalcanzable | NETWORK | "No pudimos conectarnos al servidor…" |
| Tiempo de espera agotado | NETWORK | "El servidor tardó demasiado…" |
| 401 / 403 | UNAUTHORIZED | "Correo o contraseña incorrectos, o tu sesión expiró." |
| 400 / 422 | VALIDATION | "Los datos enviados no son válidos…" |
| 409 | CONFLICT | "Ya existe una cuenta registrada…" |
| 5xx | SERVER | "El servicio no está disponible…" |
| JSON inválido o campos obligatorios ausentes | SERVER | "Recibimos una respuesta inesperada…" |
| Fallo en Room | LOCAL | "No pudimos acceder a los datos guardados…" |

- Sin conexión, la pantalla no muestra error: aparece un aviso "Mostrando la última información guardada". Si la API responde 401 con una sesión activa, se cierra la sesión y se vuelve al login.

## 6. Contrato con la API REST (¡ajustar!)

El enunciado indica usar "la API REST externa" de la documentación oficial, pero la URL no lleva a la ruta.:

| Método | Ruta | Cuerpo | Respuesta |
|---|---|---|---|
| POST | `auth/register` | `{username, email, password}` | cualquiera (luego se hace login) |
| POST | `auth/login` | `{email, password}` | `{token, user?: {id, username, email, avatarUrl, balance}}` |
| GET | `users/me` | – | `{id, username, email, avatarUrl, balance}` |
| GET | `transactions` | – | `[{id, amount, description, date, type}]` |
| POST | `transactions` | `{amount, description?}` | `{id, amount, description, date, type}` |

Un movimiento se muestra como egreso si `amount < 0` o si `type` es `expense`, `sent`, `send`, `debit`, `egreso` o `envio` (`TransactionEntity.isExpense()`).

## 7. Pantallas de registro e inicio de sesión

- **Registro**: usuario (3–30 caracteres), correo válido, contraseña (mínimo 8, con letra y número) y confirmación. Los errores aparecen bajo cada campo.
- **Login**: correo válido y contraseña no vacía.
- **Sesión**: al autenticarse se guarda el token en `EncryptedSharedPreferences` (AES-256, clave en Android Keystore). `LoginActivity` es la pantalla de entrada: si ya hay token, salta a `MainActivity`.
- **Envío de dinero**: monto > 0 con hasta 2 decimales (acepta coma o punto) y descripción opcional de hasta 100 caracteres.

## 8. Seguridad

- **En tránsito**: `network_security_config.xml` bloquea tráfico HTTP sin cifrar (solo HTTPS).
- **Token**: guardado cifrado; se envía solo en la cabecera `Authorization`; se elimina al cerrar sesión o al fallar el login.
- **Contraseñas**: nunca se guardan en el dispositivo.
- **En reposo**: la base Room vive en el almacenamiento privado de la app, con `allowBackup="false"`, y contiene solo perfil e historial (sin credenciales). Room no cifra el archivo por sí mismo; si el curso exige cifrado de la base, se puede añadir SQLCipher (`SupportOpenHelperFactory`) sin cambiar el resto de la arquitectura.
- **Logs**: el logger HTTP solo existe en *debug*, sin cuerpos y con `Authorization` oculto.

## 9. Pruebas

| Archivo | Tipo | Qué verifica |
|---|---|---|
| `AuthViewModelTest` | Unitaria (JVM) | Validaciones de login/registro, éxito, credenciales inválidas, correo duplicado |
| `WalletViewModelTest` | Unitaria (JVM) | Sincronización, modo sin conexión, sesión expirada, envío válido/ inválido, errores, logout |
| `ValidatorsTest` | Unitaria (JVM) | Reglas de correo, contraseña, usuario, monto y descripción |
| `WalletApiTest` | Integración (MockWebServer) | Rutas, métodos, cuerpo JSON, cabecera Bearer, parseo y traducción de errores 401/409/500, JSON inválido y falta de red |
| `RoomDaoTest` | Instrumentada (Room en memoria) | CRUD de ambos DAO, orden por fecha, reemplazo por id y `clear` |

## 10. Limitaciones conocidas

- La URL base (`API_BASE_URL`) es un marcador de posición y debe reemplazarse.
- El ícono de la app es un vector simple; puedes generar uno propio con *New > Image Asset*.
- No hay paginación del historial ni refresco automático del token (si expira, se pide iniciar sesión de nuevo).
