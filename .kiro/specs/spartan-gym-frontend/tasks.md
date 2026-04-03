# Plan de Implementación: Spartan Golden Gym Frontend Web

## Descripción General

Implementación incremental de la aplicación web frontend de Spartan Golden Gym usando React 18 + TypeScript + Vite. El plan sigue un enfoque modular: primero la infraestructura base (store, API, rutas, i18n, tema), luego los módulos funcionales en orden de dependencia, y finalmente la integración y testing.

## Tareas

- [x] 1. Configurar estructura del proyecto y dependencias base
  - [x] 1.1 Inicializar proyecto Vite con React 18 + TypeScript y configurar `tsconfig.json`, `vite.config.ts`, `tailwind.config.ts`
    - Instalar dependencias: `@reduxjs/toolkit`, `react-redux`, `react-router-dom`, `@mui/material`, `tailwindcss`, `socket.io-client`, `react-i18next`, `i18next`, `react-hook-form`, `zod`, `recharts`, `victory`, `mapbox-gl`, `react-map-gl`, `video.js`, `react-window`, `@notistack/notistack`
    - Instalar dependencias de desarrollo: `vitest`, `@testing-library/react`, `msw`, `fast-check`, `axe-core`
    - Crear estructura de directorios según diseño: `src/app/`, `src/api/`, `src/features/`, `src/components/`, `src/hooks/`, `src/types/`, `src/utils/`, `src/i18n/`, `src/websocket/`, `src/theme/`, `src/routes/`
    - _Requisitos: 18.1, 18.2_

  - [x] 1.2 Configurar Redux Store, root reducer y hooks tipados (`useAppDispatch`, `useAppSelector`)
    - Crear `src/app/store.ts`, `src/app/rootReducer.ts`, `src/app/hooks.ts`
    - _Requisitos: 2.2, 2.8_

  - [x] 1.3 Configurar temas MUI (claro con dorado #D4AF37, oscuro con #FFD700) y integración Tailwind CSS
    - Crear `src/theme/muiTheme.ts` con `lightTheme` y `darkTheme`
    - Configurar `ThemeProvider` en `App.tsx` conectado al `uiSlice.theme`
    - _Requisitos: 21.3_

  - [x] 1.4 Configurar internacionalización (react-i18next) con carga bajo demanda por namespace
    - Crear `src/i18n/config.ts` con soporte para en, es, fr, de, ja
    - Configurar `i18next-http-backend` para cargar traducciones desde `/locales/{{lng}}/{{ns}}.json`
    - Crear archivos de traducción base para los 13 namespaces (common, auth, dashboard, training, nutrition, social, gym, payments, calendar, messages, trainer, analytics, settings) en los 5 idiomas
    - _Requisitos: 16.1, 16.2, 16.3_

  - [x] 1.5 Crear tipos TypeScript globales en `src/types/`
    - Definir interfaces: `User`, `AuthState`, `LoginRequest`, `LoginResponse`, `RegisterRequest`, `TrainingPlan`, `Routine`, `Exercise`, `RoutineExercise`, `WorkoutSession`, `WorkoutSet`, `WorkoutProgress`, `ProgressDataPoint`, `NutritionPlan`, `Food`, `MealLog`, `DailyBalance`, `Gym`, `GymEquipment`, `Challenge`, `Achievement`, `RankingEntry`, `Subscription`, `Transaction`, `Donation`, `Conversation`, `Message`, `CalendarEvent`, `GroupClass`, `ClassReservation`, `DashboardMetrics`, `AnalyticsReport`, `PagedResponse`, `ErrorResponse`
    - _Requisitos: 1.1, 2.1, 5.1, 6.1, 8.1, 9.1, 10.1, 11.1, 12.1, 13.1, 14.1, 15.1, 20.1_

- [x] 2. Implementar capa de API (RTK Query) y estado global
  - [x] 2.1 Crear `baseApi.ts` con `fetchBaseQuery`, inyección de JWT en headers, `Accept-Language`, y lógica de refresco automático de token (`baseQueryWithReauth`)
    - Implementar mutex para evitar refreshes concurrentes
    - En caso de 401: intentar refresh exactamente una vez; si falla, ejecutar logout
    - _Requisitos: 2.2, 2.5, 2.6, 2.7_

  - [ ]* 2.2 Escribir test de propiedad para refresco automático de token
    - **Propiedad 3: Refresco automático de token**
    - Verificar que para cualquier petición que recibe 401, el sistema intenta refrescar exactamente una vez; si exitoso, reintenta la petición original; si falla, ejecuta logout
    - **Valida: Requisitos 2.5, 2.6, 2.7**

  - [x] 2.3 Crear endpoints RTK Query para cada microservicio
    - `usersApi.ts`: login, register, getProfile, updateProfile, setupMfa, verifyMfa, submitOnboarding, requestDataExport, deleteAccount
    - `trainingApi.ts`: getPlans, getPlanById, createPlan, updatePlan, assignPlan, getExercises
    - `workoutsApi.ts`: startWorkout, addSet, completeWorkout, getWorkoutHistory, getProgress
    - `nutritionApi.ts`: getNutritionPlan, logMeal, getDailyBalance, searchFoods, getRecipes, getSupplements
    - `gymsApi.ts`: getNearbyGyms, getGymById, getGymEquipment, getGymOccupancy, checkin
    - `socialApi.ts`: getChallenges, getAchievements, getRankings, shareAchievement, getGroups
    - `paymentsApi.ts`: getSubscription, subscribe, getTransactions, requestRefund, donate, getPaymentMethods, addPaymentMethod
    - `messagesApi.ts`: getConversations, getMessages, sendMessage
    - `calendarApi.ts`: getEvents, createEvent, updateEvent, deleteEvent, syncExternalCalendar
    - `bookingsApi.ts`: getClasses, reserveClass, cancelReservation, getTrainerAvailability
    - `aiCoachApi.ts`: generatePlan, checkOvertraining, getAlternatives, getWarmup
    - `analyticsApi.ts`: getDashboardMetrics, getReports, getMetrics
    - `notificationsApi.ts`: getPreferences, updatePreferences, getHistory
    - Configurar `providesTags` e `invalidatesTags` según diseño
    - _Requisitos: 1.2, 2.1, 5.1, 6.1, 8.2, 9.1, 10.1, 11.1, 12.1, 13.2, 14.4, 15.1, 20.2, 21.2, 22.1_

  - [ ]* 2.4 Escribir test de propiedad para caché de RTK Query con invalidación
    - **Propiedad 4: Caché de RTK Query con invalidación**
    - Verificar que para cualquier mutación que invalida tags, las queries que proveen esos tags se re-ejecutan y los datos reflejan el estado actualizado
    - **Valida: Requisitos 4.4, 18.4**

  - [x] 2.5 Implementar slices de Redux: `authSlice`, `uiSlice`, `websocketSlice`
    - `authSlice`: acciones `setCredentials`, `setMfaPending`, `logout`
    - `uiSlice`: acciones `setLocale`, `toggleTheme`, `toggleSidebar`, `setMeasurementUnit`
    - `websocketSlice`: acciones `setConnected`, `addIncomingMessage`, `updateOccupancy`, `setTypingUsers`, `clearIncomingMessages`
    - _Requisitos: 2.2, 2.8, 16.2, 21.3, 23.5_

  - [ ]* 2.6 Escribir test de propiedad para limpieza completa de estado en logout
    - **Propiedad 15: Logout limpia todo el estado**
    - Verificar que al ejecutar logout: token es null, user es null, caché de RTK Query vacío, conexión WebSocket cerrada
    - **Valida: Requisitos 2.8**

- [x] 3. Implementar servicio WebSocket y hooks compartidos
  - [x] 3.1 Crear `src/websocket/socketService.ts` con Socket.io-client
    - Conexión autenticada con JWT, reconexión automática (máx 10 intentos, intervalo 1s)
    - Handlers para eventos: `message:new`, `message:read`, `message:typing`, `gym:occupancy`, `notification:new`
    - Métodos: `connect`, `disconnect`, `sendMessage`, `markAsRead`, `startTyping`, `joinGymOccupancy`, `leaveGymOccupancy`
    - Despachar acciones de Redux e invalidar caché de RTK Query al recibir eventos
    - _Requisitos: 23.1, 23.2, 23.3, 23.4, 23.5_

  - [x] 3.2 Crear hooks compartidos: `useAuth`, `useWorkout`, `useLocale`, `useRoleGuard`
    - `useAuth`: login, register, logout, estado de autenticación; conectar WebSocket al login exitoso
    - `useWorkout`: startWorkout, addSet, completeWorkout, sesión activa
    - `useLocale`: changeLocale, formatDate, formatCurrency, formatWeight, formatDistance
    - `useRoleGuard`: verificar acceso por rol
    - _Requisitos: 2.1, 2.8, 6.1, 16.4, 16.5_

  - [ ]* 3.3 Escribir test de propiedad para formato de unidades según configuración regional
    - **Propiedad 12: Formato de unidades según configuración regional**
    - Verificar que para cualquier valor de peso, si configuración imperial se muestra en libras (kg × 2.20462), si métrica en kilogramos; lo mismo para distancias (km/millas con factor 0.621371)
    - **Valida: Requisitos 16.5**

  - [x] 3.3b Crear hook `useGymOccupancy` para suscripción/desuscripción WebSocket de ocupación de gimnasios
    - Suscribirse al montar, desuscribirse al desmontar
    - _Requisitos: 9.2, 9.5_

- [ ] 4. Checkpoint — Verificar infraestructura base
  - Asegurar que todas las pruebas pasan, preguntar al usuario si surgen dudas.

- [x] 5. Implementar módulo de autenticación y RBAC
  - [x] 5.1 Crear componente `ProtectedRoute` con redirección según autenticación y rol
    - Usuario no autenticado → redirigir a `/login` preservando ruta original
    - Usuario con rol no autorizado → redirigir a `/unauthorized`
    - _Requisitos: 3.1, 3.2_

  - [ ]* 5.2 Escribir test de propiedad para protección de rutas por rol
    - **Propiedad 2: Protección de rutas por rol**
    - Verificar que para cualquier ruta protegida con `allowedRoles`, un usuario con rol no incluido es redirigido a `/unauthorized`, y un usuario no autenticado es redirigido a `/login`
    - **Valida: Requisitos 3.1, 3.2, 3.3, 3.4, 3.5**

  - [x] 5.3 Crear `LoginPage` con formulario email/password, validación con react-hook-form + zod
    - Manejar respuesta `mfaRequired: true` redirigiendo a `/mfa`
    - Almacenar JWT en memoria de Redux (no localStorage/sessionStorage)
    - _Requisitos: 2.1, 2.2, 2.3, 19.1_

  - [x] 5.4 Crear `RegisterPage` con formulario nombre, email, contraseña, fecha de nacimiento
    - Validación client-side con react-hook-form + zod (email válido, contraseña >= 8 caracteres, campos obligatorios)
    - Manejar error 409 (correo duplicado) mostrando mensaje en formulario
    - Redirigir a login tras registro exitoso
    - _Requisitos: 1.1, 1.2, 1.3, 1.4, 1.5_

  - [x] 5.5 Crear `MfaPage` con input de código TOTP de 6 dígitos
    - Verificar código y almacenar credenciales en Redux tras éxito
    - Redirigir al dashboard
    - _Requisitos: 2.3, 2.4_

  - [ ]* 5.6 Escribir test de propiedad para round-trip de autenticación
    - **Propiedad 1: Round-trip de autenticación**
    - Verificar que para cualquier credenciales válidas, el flujo login → obtener perfil retorna un usuario con el mismo email, y el token JWT está presente en Redux
    - **Valida: Requisitos 2.1, 2.2, 2.4**

- [ ] 6. Implementar layout, navegación y manejo de errores global
  - [ ] 6.1 Crear `AppLayout` con sidebar, topbar y `Suspense` con fallback de carga
    - Sidebar con navegación según rol del usuario
    - _Requisitos: 18.1, 18.2_

  - [ ] 6.2 Configurar rutas con React Router v6 y lazy loading por módulo
    - Rutas públicas: `/login`, `/register`, `/mfa`
    - Rutas protegidas comunes: `/dashboard`, `/training/*`, `/nutrition/*`, `/social/*`, `/gym/*`, `/payments/*`, `/calendar`, `/messages/*`, `/settings/*`
    - Rutas restringidas: `/trainer/*` (solo trainer), `/analytics/*` (solo admin)
    - _Requisitos: 3.3, 3.4, 3.5, 18.1_

  - [ ]* 6.3 Escribir test de propiedad para lazy loading y navegación
    - **Propiedad 14: Lazy loading no rompe navegación**
    - Verificar que para cualquier ruta definida, navegar a ella carga el módulo correspondiente y renderiza el componente correcto; durante la carga se muestra el fallback de Suspense
    - **Valida: Requisitos 18.1, 18.2**

  - [ ] 6.4 Implementar `ErrorBoundary` y manejo global de errores HTTP
    - ErrorBoundary: capturar errores de renderizado, mostrar interfaz de recuperación con botón reintentar, registrar en servicio de monitoreo
    - Middleware/hook para errores HTTP: 403 → `/unauthorized`, 404 → componente NotFound, 429 → toast "Demasiadas solicitudes", 500 → error genérico con reintentar, 503 → banner "Servicio no disponible"
    - Banner de modo offline al perder conexión a internet
    - Indicador visual de estado de conexión WebSocket con reconexión automática
    - _Requisitos: 17.1, 17.2, 17.3, 17.4, 17.5, 17.6, 17.7, 17.8_

  - [ ]* 6.5 Escribir tests unitarios para ErrorBoundary y manejo de errores HTTP
    - Verificar redirección correcta por código de error
    - Verificar que ErrorBoundary captura errores y muestra interfaz de recuperación
    - _Requisitos: 17.1, 17.2, 17.3, 17.4, 17.5, 17.6, 17.7_

- [ ] 7. Checkpoint — Verificar autenticación, RBAC y manejo de errores
  - Asegurar que todas las pruebas pasan, preguntar al usuario si surgen dudas.

- [x] 8. Implementar módulo de dashboard personalizado
  - [x] 8.1 Crear `DashboardPage` con vista personalizada según rol
    - Client: widgets de resumen de entrenamiento, próximas clases, balance nutricional, logros recientes
    - Trainer: lista de clientes asignados, sesiones del día, alertas de progreso
    - Admin: métricas clave de negocio, ocupación de gimnasios, ingresos
    - Obtener datos mediante RTK Query con caché automático y re-fetch al montar
    - _Requisitos: 4.1, 4.2, 4.3, 4.4_

  - [ ]* 8.2 Escribir tests unitarios para DashboardPage
    - Verificar renderizado correcto de widgets según rol (client, trainer, admin)
    - _Requisitos: 4.1, 4.2, 4.3_

- [x] 9. Implementar módulo de entrenamiento
  - [x] 9.1 Crear `PlansListPage` con lista de planes y filtros por estado (activo, completado, pausado)
    - _Requisitos: 5.1_

  - [x] 9.2 Crear `PlanDetailPage` con detalle de rutinas, ejercicios, series, repeticiones, descanso
    - Integrar reproductor Video.js con soporte HLS, controles de velocidad y repetición para videos tutoriales
    - Botón para consultar ejercicios alternativos (`/api/ai/alternatives`)
    - Mostrar ejercicios de calentamiento recomendados (`/api/ai/warmup`)
    - _Requisitos: 5.2, 5.3, 5.4, 5.5_

  - [x] 9.3 Crear `WorkoutLivePage` para seguimiento de entrenamientos en tiempo real
    - Iniciar sesión (POST `/api/workouts/start`), crear sesión activa en Redux
    - Mostrar ejercicio actual, series completadas, temporizador de descanso, próximo ejercicio
    - Registrar series (peso, repeticiones) con POST a `/api/workouts/{id}/sets`, actualizar estado local
    - Finalizar entrenamiento (POST `/api/workouts/{id}/complete`), mostrar resumen con duración, volumen, calorías
    - Manejar fallo de registro de serie: mostrar error, permitir reintentar sin perder datos
    - _Requisitos: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6_

  - [ ]* 9.4 Escribir test de propiedad para consistencia de sesión de entrenamiento
    - **Propiedad 5: Consistencia de sesión de entrenamiento**
    - Verificar que para cualquier sesión iniciada, el número de series en estado local es igual al número de series registradas exitosamente en el backend; al completar, el resumen contiene todas las series
    - **Valida: Requisitos 6.3, 6.5**

  - [x] 9.5 Crear `ProgressPage` con gráficos de volumen, duración y calorías usando Recharts
    - Filtros por período: día, semana, mes, año, rango personalizado
    - Comparativas de objetivos semanales y mensuales con indicadores visuales
    - _Requisitos: 7.1, 7.2, 7.3_

  - [ ]* 9.6 Escribir tests unitarios para WorkoutLivePage y ProgressPage
    - Verificar flujo completo de entrenamiento en vivo con MSW
    - Verificar renderizado de gráficos con datos mock
    - _Requisitos: 6.1, 6.4, 7.1_

- [x] 10. Implementar módulo de nutrición
  - [x] 10.1 Crear `NutritionDashboardPage` con balance diario de macros (gráfico circular) e historial de comidas
    - Gráfico circular de macronutrientes (proteínas, carbohidratos, grasas) con Recharts
    - Balance diario = suma de valores nutricionales de todas las comidas del día vs objetivos del plan
    - _Requisitos: 8.1, 8.6_

  - [x] 10.2 Crear `MealLogPage` con formulario de registro de comida
    - Búsqueda de alimentos con información nutricional por 100g
    - Registro con alimento, cantidad y tipo de comida; actualizar balance diario automáticamente
    - _Requisitos: 8.2, 8.3_

  - [ ]* 10.3 Escribir test de propiedad para balance nutricional
    - **Propiedad 6: Balance nutricional es suma de comidas**
    - Verificar que para cualquier conjunto de comidas registradas en un día, el balance diario mostrado coincide con la suma de valores nutricionales de cada comida
    - **Valida: Requisitos 8.1, 8.6**

  - [x] 10.4 Crear `RecipesPage` y `SupplementsPage`
    - Recetas recomendadas según objetivos nutricionales
    - Suplementos con información de dosificación y beneficios
    - _Requisitos: 8.4, 8.5_

- [-] 11. Implementar módulo de gimnasios con mapa y ocupación en tiempo real
  - [x] 11.1 Crear `GymMapPage` con mapa interactivo Mapbox GL JS
    - Marcadores de gimnasios cercanos con indicador de ocupación en tiempo real (WebSocket)
    - Suscribirse a `gym:occupancy` al montar, desuscribirse al desmontar
    - _Requisitos: 9.1, 9.2, 9.5_

  - [ ] 11.2 Crear `GymDetailPage` con horarios, equipamiento y ocupación actual
    - Indicador visual de ocupación (barra de progreso con colores), valor siempre >= 0 y <= capacidad máxima
    - _Requisitos: 9.3, 9.4_

  - [ ]* 11.3 Escribir test de propiedad para ocupación de gimnasios en rango válido
    - **Propiedad 7: Ocupación de gimnasios en rango válido**
    - Verificar que para cualquier actualización de ocupación recibida por WebSocket, el valor es >= 0 y <= maxCapacity; el indicador visual refleja el valor actualizado
    - **Valida: Requisitos 9.2, 9.4**

- [ ] 12. Checkpoint — Verificar módulos de entrenamiento, nutrición y gimnasios
  - Asegurar que todas las pruebas pasan, preguntar al usuario si surgen dudas.

- [ ] 13. Implementar módulo social y gamificación
  - [ ] 13.1 Crear `CommunityPage` con desafíos activos, feed de actividad y rankings por categoría
    - Rankings ordenados de mayor a menor score con posiciones consecutivas empezando en 1
    - Componente `RankingTable` con avatar, nombre, posición, score
    - _Requisitos: 10.1, 10.2_

  - [ ]* 13.2 Escribir test de propiedad para rankings ordenados correctamente
    - **Propiedad 8: Rankings ordenados correctamente**
    - Verificar que para cualquier categoría de ranking, los resultados están ordenados de mayor a menor score, y cada entrada tiene rank consecutivo empezando en 1
    - **Valida: Requisitos 10.2**

  - [ ] 13.3 Crear `AchievementsPage` con galería de insignias y opción de compartir en redes sociales
    - POST a `/api/social/share` para generar enlace/tarjeta compartible
    - _Requisitos: 10.3, 10.4_

  - [ ] 13.4 Crear vista de grupos de entrenamiento con chat grupal
    - _Requisitos: 10.5_

- [ ] 14. Implementar módulo de mensajería en tiempo real
  - [ ] 14.1 Crear `MessagesPage` con layout split: lista de conversaciones + chat activo
    - Lista de conversaciones con último mensaje, conteo de no leídos, indicador online
    - _Requisitos: 12.1_

  - [ ] 14.2 Crear `ChatWindow` con historial de mensajes, scroll infinito y envío via WebSocket
    - Mensajes ordenados cronológicamente por `sentAt`
    - Envío mediante WebSocket (`message:send`), estado "enviando" al enviar
    - Insertar mensajes nuevos (`message:new`) en posición cronológica correcta
    - Actualizar estado a "leído" (`message:read`) con doble check
    - Indicador de escritura (`message:typing`)
    - _Requisitos: 12.2, 12.3, 12.4, 12.5, 12.6_

  - [ ]* 14.3 Escribir test de propiedad para mensajes en orden cronológico
    - **Propiedad 9: Mensajes en orden cronológico**
    - Verificar que para cualquier conversación, los mensajes están ordenados cronológicamente por `sentAt`; mensajes nuevos por WebSocket se insertan en la posición correcta
    - **Valida: Requisitos 12.2, 12.4**

  - [ ]* 14.4 Escribir test de propiedad para estado de mensajes consistente
    - **Propiedad 13: Estado de mensajes consistente**
    - Verificar que para cualquier mensaje, su estado progresa en orden: sending → sent → delivered → read, sin retroceder
    - **Valida: Requisitos 12.7**

- [ ] 15. Implementar módulo de calendario
  - [ ] 15.1 Crear `CalendarPage` con vistas mensual, semanal y diaria
    - Eventos consolidados coloreados por tipo (workout, class, trainer_session, nutrition_reminder, custom)
    - Detección visual de conflictos entre eventos solapados
    - _Requisitos: 13.1, 13.3_

  - [ ] 15.2 Implementar función `detectConflicts` y formulario de creación/edición de eventos
    - Dos eventos se solapan si `event1.endsAt > event2.startsAt` y `event1.startsAt < event2.endsAt`
    - Eventos sin solapamiento no deben reportarse como conflictos
    - Formulario para crear/editar eventos con selector de recordatorio
    - _Requisitos: 13.2, 13.3, 13.4_

  - [ ]* 15.3 Escribir test de propiedad para detección de conflictos en calendario
    - **Propiedad 10: Detección de conflictos en calendario**
    - Verificar que para cualquier par de eventos solapados, el algoritmo los identifica como conflicto; eventos sin solapamiento no se reportan como conflicto
    - **Valida: Requisitos 13.3, 13.4**

  - [ ] 15.4 Crear botón de sincronización con calendarios externos (Google Calendar, Apple Calendar, Outlook)
    - POST a `/api/calendar/sync` con proveedor y token de autorización
    - _Requisitos: 13.5_

- [ ] 16. Checkpoint — Verificar módulos social, mensajería y calendario
  - Asegurar que todas las pruebas pasan, preguntar al usuario si surgen dudas.

- [x] 17. Implementar módulo de pagos y suscripciones
  - [x] 17.1 Crear `SubscriptionPage` con plan actual, opciones de upgrade/downgrade
    - Integrar Stripe Elements o Adyen Drop-in para procesamiento de pagos (sin almacenar datos de tarjeta en frontend)
    - _Requisitos: 11.1, 11.2, 11.5, 19.5_

  - [x] 17.2 Crear `TransactionsPage` con historial paginado, filtros y opción de reembolso
    - Virtualización de lista con react-window
    - _Requisitos: 11.3, 18.3_

  - [x] 17.3 Crear `DonationPage` con montos sugeridos y personalizado, procesado mediante PayPal
    - _Requisitos: 11.4_

  - [ ]* 17.4 Escribir tests unitarios para módulo de pagos
    - Verificar integración con Stripe Elements/Adyen Drop-in
    - Verificar que no se almacenan datos de tarjeta en el frontend
    - _Requisitos: 11.2, 11.5, 19.5_

- [ ] 18. Implementar módulo de reservas de clases grupales
  - [ ] 18.1 Crear vista de clases disponibles con nombre, instructor, sala, capacidad, dificultad, fecha, duración
    - Reservar clase (POST `/api/bookings/classes/{id}/reserve`), actualizar capacidad
    - Cancelar reserva (POST `/api/bookings/classes/{id}/cancel`), actualizar estado
    - Consultar disponibilidad de entrenador
    - _Requisitos: 20.1, 20.2, 20.3, 20.4_

- [ ] 19. Implementar módulo de panel de entrenador
  - [ ] 19.1 Crear `TrainerDashboardPage` con lista de clientes, planes activos y métricas
    - _Requisitos: 14.1_

  - [ ] 19.2 Crear `ClientDetailPage` con perfil, plan actual, historial y gráficos comparativos con Victory Charts
    - _Requisitos: 14.2_

  - [ ] 19.3 Crear `PlanEditorPage` con editor drag & drop de ejercicios, configuración de series/reps y asignación a cliente
    - POST a `/api/training/plans/{id}/assign` para asignar plan
    - _Requisitos: 14.3, 14.4_

- [ ] 20. Implementar módulo de analíticas (administrador)
  - [ ] 20.1 Crear `AnalyticsDashboardPage` con métricas de negocio y filtros
    - Métricas: usuarios totales, suscripciones activas, ingresos mensuales, ocupación promedio, tasa de retención
    - Filtros por gimnasio, período y segmento de usuario
    - Integrar dashboards de Amazon QuickSight mediante componente embebido
    - _Requisitos: 15.1, 15.2, 15.4_

  - [ ] 20.2 Crear `ReportsPage` con lista de reportes generados y opción de descarga
    - _Requisitos: 15.3_

- [x] 21. Implementar módulo de configuración y privacidad
  - [x] 21.1 Crear `ProfileSettingsPage` con formulario editable de perfil
    - Foto, datos personales, objetivos de fitness, condiciones médicas
    - PUT a `/api/users/profile`, reflejar cambios inmediatamente
    - _Requisitos: 21.1, 21.2_

  - [x] 21.2 Crear `LanguageSettingsPage` con selector de idioma y preview de formatos
    - _Requisitos: 16.1, 16.2, 16.4_

  - [ ]* 21.3 Escribir test de propiedad para traducciones completas por idioma
    - **Propiedad 11: Traducciones completas por idioma**
    - Verificar que para cada clave de traducción en el namespace base y para cada idioma soportado (en, es, fr, de, ja), existe una traducción no vacía
    - **Valida: Requisitos 16.6**

  - [x] 21.4 Crear `NotificationSettingsPage` con preferencias por categoría y canal
    - PUT a `/api/notifications/preferences`
    - _Requisitos: 21.4_

  - [x] 21.5 Crear `PrivacySettingsPage` con setup MFA, exportación de datos y eliminación de cuenta
    - MFA: POST `/api/users/mfa/setup`, mostrar código QR
    - Exportación: POST `/api/users/data-export`
    - Eliminación: confirmación + DELETE `/api/users/profile/delete`, ejecutar logout
    - _Requisitos: 21.5, 21.6, 21.7_

- [ ] 22. Checkpoint — Verificar módulos de pagos, reservas, entrenador, analíticas y configuración
  - Asegurar que todas las pruebas pasan, preguntar al usuario si surgen dudas.

- [x] 23. Implementar módulo de generación de planes con IA y onboarding
  - [x] 23.1 Crear `OnboardingPage` con wizard multi-paso
    - Pasos: nivel de fitness, objetivos, condiciones médicas, equipamiento disponible
    - Progreso guardado parcialmente, pasos opcionales omitibles
    - Al completar: POST a `/api/ai/plans/generate` y mostrar plan generado
    - _Requisitos: 22.1, 22.3_

  - [x] 23.2 Integrar verificación de sobreentrenamiento en módulo de entrenamiento
    - Consultar `/api/ai/overtraining/check` y mostrar recomendación de descanso si se detecta fatiga
    - _Requisitos: 22.2_

- [ ] 24. Implementar seguridad del frontend y configuración CSP
  - [ ] 24.1 Verificar que JWT se almacena solo en memoria de Redux, refresh token en httpOnly cookie con SameSite=Strict
    - Configurar headers Content Security Policy (CSP)
    - Sanitizar entradas de usuario (validación con zod, escape automático de React)
    - _Requisitos: 19.1, 19.2, 19.3, 19.4_

- [ ] 25. Integración final y optimización de rendimiento
  - [ ] 25.1 Verificar virtualización de listas con react-window en historial de entrenamientos, mensajes y transacciones
    - _Requisitos: 18.3_

  - [ ] 25.2 Verificar lazy loading de imágenes con atributo `loading="lazy"`
    - _Requisitos: 18.5_

  - [ ] 25.3 Conectar todos los módulos: navegación completa, WebSocket activo tras login, invalidación de caché cruzada entre módulos
    - Verificar que notificaciones WebSocket (`notification:new`) invalidan caché de RTK Query
    - Verificar que cambio de tema e idioma se aplica globalmente
    - _Requisitos: 23.3, 16.2, 21.3_

- [ ] 26. Checkpoint final — Verificar integración completa
  - Asegurar que todas las pruebas pasan, preguntar al usuario si surgen dudas.

## Notas

- Las tareas marcadas con `*` son opcionales y pueden omitirse para un MVP más rápido
- Cada tarea referencia requisitos específicos para trazabilidad
- Los checkpoints aseguran validación incremental
- Los tests de propiedad validan propiedades universales de corrección
- Los tests unitarios validan ejemplos específicos y casos borde
- El lenguaje de implementación es TypeScript con React 18
