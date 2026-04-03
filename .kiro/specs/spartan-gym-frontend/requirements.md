# Documento de Requisitos — Spartan Golden Gym Frontend Web

## Introducción

Este documento define los requisitos funcionales y no funcionales de la aplicación web frontend de Spartan Golden Gym, construida con React 18 + TypeScript + Vite. La aplicación se comunica con 13 microservicios backend a través de un API Gateway centralizado mediante REST y WebSocket. Cubre 12 módulos funcionales (autenticación, dashboard, entrenamiento, nutrición, social, gimnasios, pagos, calendario, mensajería, panel de entrenador, analíticas y configuración) con soporte para internacionalización en 5 idiomas, control de acceso basado en roles (RBAC) y comunicación en tiempo real.

## Glosario

- **App_Web**: Aplicación web frontend desarrollada en React 18 + TypeScript + Vite.
- **API_Gateway**: Punto de entrada único (Spring Cloud Gateway) para todas las solicitudes REST del frontend hacia los microservicios backend.
- **Redux_Store**: Almacén centralizado de estado global de la aplicación, gestionado con Redux Toolkit.
- **RTK_Query**: Capa de datos basada en Redux Toolkit Query que gestiona caché, invalidación y sincronización con las APIs del backend.
- **WebSocket_Service**: Servicio centralizado de Socket.io-client que gestiona conexiones en tiempo real (chat, ocupación, notificaciones).
- **ProtectedRoute**: Componente de guardia que restringe el acceso a rutas según autenticación y rol del usuario.
- **Usuario**: Persona registrada en la plataforma con uno de los tres roles: cliente, entrenador o administrador.
- **JWT**: JSON Web Token utilizado para autenticación. El access token se almacena en memoria de Redux y el refresh token en httpOnly cookie.
- **Módulo_Funcional**: Cada una de las 12 secciones de la aplicación cargadas bajo demanda (auth, dashboard, training, nutrition, social, gym, payments, calendar, messages, trainer, analytics, settings).
- **i18n**: Sistema de internacionalización basado en react-i18next con soporte para 5 idiomas (en, es, fr, de, ja).
- **RBAC**: Control de acceso basado en roles (client, trainer, admin) implementado mediante guards declarativos.
- **ErrorBoundary**: Componente React que captura errores de renderizado y muestra una interfaz de recuperación.
- **Sesión_Entrenamiento**: Registro en tiempo real de un entrenamiento activo del usuario, incluyendo series, repeticiones y peso.
- **Balance_Diario**: Resumen de calorías y macronutrientes consumidos en un día comparado con los objetivos del plan nutricional.

## Requisitos

### Requisito 1: Registro de Usuarios

**Historia de Usuario:** Como visitante, quiero registrarme en la plataforma proporcionando mis datos básicos, para poder acceder a las funcionalidades de entrenamiento y comunidad.

#### Criterios de Aceptación

1. WHEN un visitante accede a la ruta `/register`, THE App_Web SHALL mostrar un formulario con los campos nombre, correo electrónico, contraseña y fecha de nacimiento.
2. WHEN un visitante envía el formulario de registro con datos válidos, THE App_Web SHALL enviar una petición POST a `/api/users/register` y redirigir al usuario a la página de login tras una respuesta exitosa.
3. WHEN el backend retorna un error 409 (correo duplicado), THE App_Web SHALL mostrar un mensaje de error en el formulario indicando que el correo electrónico ya está registrado.
4. THE App_Web SHALL validar los campos del formulario de registro en el lado del cliente utilizando react-hook-form y zod antes de enviar la petición al backend.
5. IF el formulario contiene campos vacíos o con formato inválido (correo sin formato válido, contraseña menor a 8 caracteres), THEN THE App_Web SHALL mostrar mensajes de validación específicos por campo e impedir el envío.

---

### Requisito 2: Autenticación y Gestión de Sesión

**Historia de Usuario:** Como usuario registrado, quiero iniciar sesión de forma segura y mantener mi sesión activa, para acceder a la plataforma sin interrupciones.

#### Criterios de Aceptación

1. WHEN un usuario accede a la ruta `/login`, THE App_Web SHALL mostrar un formulario con campos de correo electrónico y contraseña.
2. WHEN un usuario envía credenciales válidas, THE App_Web SHALL almacenar el access token JWT en memoria de Redux (no en localStorage ni sessionStorage) y el refresh token en httpOnly cookie.
3. WHEN el backend responde con `mfaRequired: true`, THE App_Web SHALL redirigir al usuario a la ruta `/mfa` para ingresar el código TOTP de 6 dígitos.
4. WHEN un usuario completa la verificación MFA exitosamente, THE App_Web SHALL almacenar las credenciales en Redux_Store y redirigir al dashboard.
5. WHEN una petición API recibe respuesta 401, THE App_Web SHALL intentar refrescar el token automáticamente mediante POST a `/api/users/token/refresh` exactamente una vez.
6. IF el refresco de token es exitoso, THEN THE App_Web SHALL reintentar la petición original con el nuevo token de forma transparente para el usuario.
7. IF el refresco de token falla, THEN THE App_Web SHALL ejecutar logout, limpiar el Redux_Store, desconectar el WebSocket_Service y redirigir a `/login`.
8. WHEN un usuario ejecuta logout, THE App_Web SHALL establecer el token como null, el usuario como null, vaciar el caché de RTK_Query, cerrar la conexión WebSocket y redirigir a `/login`.

---

### Requisito 3: Control de Acceso Basado en Roles (RBAC)

**Historia de Usuario:** Como administrador de la plataforma, quiero que cada rol tenga acceso solo a las funcionalidades correspondientes, para mantener la seguridad y la separación de responsabilidades.

#### Criterios de Aceptación

1. WHEN un usuario no autenticado intenta acceder a una ruta protegida, THE ProtectedRoute SHALL redirigir al usuario a `/login` preservando la ruta original como estado de navegación.
2. WHEN un usuario autenticado con rol no autorizado intenta acceder a una ruta restringida, THE ProtectedRoute SHALL redirigir al usuario a `/unauthorized`.
3. THE App_Web SHALL restringir el acceso a las rutas `/trainer/*` exclusivamente a usuarios con rol `trainer`.
4. THE App_Web SHALL restringir el acceso a las rutas `/analytics/*` exclusivamente a usuarios con rol `admin`.
5. THE App_Web SHALL permitir el acceso a las rutas comunes (dashboard, training, nutrition, social, gym, payments, calendar, messages, settings) a todos los usuarios autenticados independientemente de su rol.

---

### Requisito 4: Dashboard Personalizado

**Historia de Usuario:** Como usuario, quiero ver un panel principal personalizado según mi rol, para tener acceso rápido a la información más relevante.

#### Criterios de Aceptación

1. WHEN un usuario con rol `client` accede al dashboard, THE App_Web SHALL mostrar widgets de resumen de entrenamiento, próximas clases, balance nutricional y logros recientes.
2. WHEN un usuario con rol `trainer` accede al dashboard, THE App_Web SHALL mostrar la lista de clientes asignados, sesiones del día y alertas de progreso.
3. WHEN un usuario con rol `admin` accede al dashboard, THE App_Web SHALL mostrar métricas clave de negocio, ocupación de gimnasios e ingresos.
4. THE App_Web SHALL obtener los datos del dashboard mediante RTK_Query con caché automático y re-fetch al montar el componente.

---

### Requisito 5: Gestión de Planes de Entrenamiento

**Historia de Usuario:** Como usuario, quiero visualizar y gestionar mis planes de entrenamiento, para seguir rutinas organizadas y alcanzar mis objetivos de fitness.

#### Criterios de Aceptación

1. WHEN un usuario accede a la sección de entrenamiento, THE App_Web SHALL mostrar la lista de planes de entrenamiento con filtros por estado (activo, completado, pausado).
2. WHEN un usuario selecciona un plan, THE App_Web SHALL mostrar el detalle con rutinas, ejercicios, series, repeticiones y tiempo de descanso.
3. WHEN un usuario visualiza un ejercicio, THE App_Web SHALL mostrar el video tutorial correspondiente mediante un reproductor Video.js con soporte HLS, controles de velocidad y repetición.
4. WHEN un usuario solicita ejercicios alternativos para un ejercicio cuyo equipamiento no está disponible, THE App_Web SHALL consultar `/api/ai/alternatives` y mostrar las alternativas que trabajan los mismos grupos musculares.
5. WHEN un usuario accede al detalle de una rutina, THE App_Web SHALL consultar `/api/ai/warmup` y mostrar los ejercicios de calentamiento recomendados previos a la rutina.

---

### Requisito 6: Seguimiento de Entrenamientos en Vivo

**Historia de Usuario:** Como usuario, quiero registrar mis entrenamientos en tiempo real, para llevar un control preciso de mi progreso.

#### Criterios de Aceptación

1. WHEN un usuario inicia un entrenamiento, THE App_Web SHALL enviar POST a `/api/workouts/start` y crear una sesión activa en el estado local de Redux.
2. WHILE un usuario tiene una Sesión_Entrenamiento activa, THE App_Web SHALL mostrar el ejercicio actual, las series completadas, un temporizador de descanso y el próximo ejercicio.
3. WHEN un usuario registra una serie (peso y repeticiones), THE App_Web SHALL enviar POST a `/api/workouts/{id}/sets` y actualizar el estado local incrementando el conteo de series en uno.
4. WHEN un usuario finaliza el entrenamiento, THE App_Web SHALL enviar POST a `/api/workouts/{id}/complete` y mostrar un resumen con duración total, volumen total y calorías estimadas.
5. THE App_Web SHALL mantener la consistencia entre el número de series en el estado local y el número de series registradas exitosamente en el backend.
6. IF una petición de registro de serie falla, THEN THE App_Web SHALL mostrar un mensaje de error y permitir reintentar sin perder los datos ingresados.

---

### Requisito 7: Visualización de Progreso

**Historia de Usuario:** Como usuario, quiero visualizar gráficos de mi progreso de entrenamiento, para evaluar mi rendimiento a lo largo del tiempo.

#### Criterios de Aceptación

1. WHEN un usuario accede a la página de progreso, THE App_Web SHALL mostrar gráficos de volumen, duración y calorías quemadas utilizando Recharts.
2. THE App_Web SHALL permitir filtrar el progreso por período: día, semana, mes, año o rango personalizado.
3. THE App_Web SHALL mostrar comparativas de objetivos semanales y mensuales con indicadores visuales de cumplimiento.

---

### Requisito 8: Gestión Nutricional

**Historia de Usuario:** Como usuario, quiero gestionar mi alimentación con planes nutricionales y seguimiento de macronutrientes, para complementar mi entrenamiento.

#### Criterios de Aceptación

1. WHEN un usuario accede al módulo de nutrición, THE App_Web SHALL mostrar el Balance_Diario con un gráfico circular de macronutrientes (proteínas, carbohidratos, grasas) y el historial de comidas del día.
2. WHEN un usuario registra una comida, THE App_Web SHALL enviar POST a `/api/nutrition/meals` con el alimento, cantidad y tipo de comida, y actualizar el Balance_Diario automáticamente.
3. WHEN un usuario busca un alimento, THE App_Web SHALL consultar `/api/nutrition/foods` y mostrar resultados con información nutricional por cada 100 gramos.
4. THE App_Web SHALL mostrar una sección de recetas recomendadas basadas en los objetivos nutricionales del usuario.
5. THE App_Web SHALL mostrar una sección de suplementos con información de dosificación y beneficios.
6. THE App_Web SHALL calcular el Balance_Diario como la suma de los valores nutricionales de todas las comidas registradas en el día, comparados con los objetivos del plan nutricional.

---

### Requisito 9: Mapa de Gimnasios y Ocupación en Tiempo Real

**Historia de Usuario:** Como usuario, quiero ver los gimnasios cercanos en un mapa con su nivel de ocupación actual, para elegir el mejor momento para entrenar.

#### Criterios de Aceptación

1. WHEN un usuario accede a la sección de gimnasios, THE App_Web SHALL mostrar un mapa interactivo con Mapbox GL JS con marcadores de gimnasios cercanos.
2. WHEN el WebSocket_Service recibe un evento `gym:occupancy`, THE App_Web SHALL actualizar el indicador de ocupación del gimnasio correspondiente en el mapa en tiempo real.
3. WHEN un usuario selecciona un gimnasio, THE App_Web SHALL mostrar el detalle con horarios, equipamiento disponible y ocupación actual.
4. THE App_Web SHALL mostrar un indicador visual de ocupación (barra de progreso con colores) donde el valor de ocupación es siempre >= 0 y <= la capacidad máxima del gimnasio.
5. WHEN un usuario sale de la vista del mapa, THE App_Web SHALL cancelar la suscripción WebSocket a las actualizaciones de ocupación de los gimnasios.

---

### Requisito 10: Comunidad y Gamificación

**Historia de Usuario:** Como usuario, quiero participar en desafíos, ver rankings y compartir logros, para mantener la motivación y conectar con la comunidad.

#### Criterios de Aceptación

1. WHEN un usuario accede a la sección social, THE App_Web SHALL mostrar los desafíos activos, el feed de actividad y los rankings por categoría.
2. THE App_Web SHALL mostrar rankings por categoría (fuerza, resistencia, consistencia, nutrición) ordenados de mayor a menor score con posiciones consecutivas empezando en 1.
3. WHEN un usuario accede a su galería de logros, THE App_Web SHALL mostrar las insignias obtenidas con opción de compartir en redes sociales externas.
4. WHEN un usuario comparte un logro, THE App_Web SHALL enviar POST a `/api/social/share` y generar un enlace o tarjeta compartible.
5. THE App_Web SHALL mostrar grupos de entrenamiento con funcionalidad de chat grupal.

---

### Requisito 11: Pagos y Suscripciones

**Historia de Usuario:** Como usuario, quiero gestionar mi membresía, métodos de pago y realizar donaciones, para acceder a los servicios premium y apoyar a creadores de contenido.

#### Criterios de Aceptación

1. WHEN un usuario accede a la sección de pagos, THE App_Web SHALL mostrar el plan de suscripción actual con opciones de upgrade y downgrade.
2. WHEN un usuario selecciona un plan de suscripción, THE App_Web SHALL procesar el pago mediante Stripe Elements o Adyen Drop-in integrados en el formulario.
3. WHEN un usuario accede al historial de transacciones, THE App_Web SHALL mostrar la lista paginada de transacciones con filtros y opción de solicitar reembolso.
4. WHEN un usuario accede al perfil de un creador de contenido, THE App_Web SHALL mostrar un botón de donación con montos sugeridos y opción de monto personalizado, procesado mediante PayPal.
5. THE App_Web SHALL permitir agregar y gestionar métodos de pago sin almacenar datos de tarjeta en el frontend (delegando a Stripe/Adyen para cumplimiento PCI DSS).

---

### Requisito 12: Mensajería en Tiempo Real

**Historia de Usuario:** Como usuario, quiero comunicarme con otros usuarios y entrenadores mediante chat en tiempo real, para coordinar entrenamientos y recibir asesoría.

#### Criterios de Aceptación

1. WHEN un usuario accede a la sección de mensajes, THE App_Web SHALL mostrar la lista de conversaciones con último mensaje, conteo de no leídos e indicador de estado online.
2. WHEN un usuario selecciona una conversación, THE App_Web SHALL cargar el historial de mensajes ordenados cronológicamente por fecha de envío con scroll infinito.
3. WHEN un usuario envía un mensaje, THE App_Web SHALL transmitirlo mediante WebSocket_Service (evento `message:send`) y mostrar el estado como "enviando".
4. WHEN el WebSocket_Service recibe un evento `message:new`, THE App_Web SHALL insertar el mensaje en la posición cronológica correcta y actualizar el conteo de no leídos.
5. WHEN el WebSocket_Service recibe un evento `message:read`, THE App_Web SHALL actualizar el estado del mensaje a "leído" (doble check).
6. THE App_Web SHALL mostrar un indicador de escritura cuando el WebSocket_Service recibe un evento `message:typing` de otro participante.
7. THE App_Web SHALL garantizar que el estado de un mensaje progrese en orden: sending → sent → delivered → read, sin retroceder a un estado anterior.

---

### Requisito 13: Calendario Unificado

**Historia de Usuario:** Como usuario, quiero ver todos mis eventos (entrenamientos, clases, sesiones con entrenador) en un calendario unificado, para organizar mi agenda de fitness.

#### Criterios de Aceptación

1. WHEN un usuario accede al calendario, THE App_Web SHALL mostrar todos los eventos consolidados con vistas mensual, semanal y diaria, coloreados por tipo de evento.
2. WHEN un usuario crea o edita un evento, THE App_Web SHALL enviar la petición correspondiente a `/api/calendar/events` y actualizar la vista del calendario.
3. THE App_Web SHALL detectar conflictos entre eventos solapados (donde event1.endsAt > event2.startsAt y event1.startsAt < event2.endsAt) y mostrarlos visualmente.
4. THE App_Web SHALL garantizar que eventos sin solapamiento temporal no sean reportados como conflictos.
5. WHEN un usuario solicita sincronización con un calendario externo (Google Calendar, Apple Calendar, Outlook), THE App_Web SHALL enviar POST a `/api/calendar/sync` con el proveedor y token de autorización.

---

### Requisito 14: Panel de Entrenador

**Historia de Usuario:** Como entrenador, quiero gestionar a mis clientes, crear planes y monitorear su progreso, para ofrecer un servicio profesional y personalizado.

#### Criterios de Aceptación

1. WHEN un entrenador accede a su panel, THE App_Web SHALL mostrar la lista de clientes asignados con sus planes activos y métricas de progreso.
2. WHEN un entrenador selecciona un cliente, THE App_Web SHALL mostrar el perfil del cliente, plan actual, historial de entrenamientos y gráficos comparativos de progreso utilizando Victory Charts.
3. WHEN un entrenador crea un plan de entrenamiento, THE App_Web SHALL proporcionar un editor con funcionalidad de arrastrar y soltar ejercicios, configuración de series y repeticiones, y asignación a un cliente.
4. WHEN un entrenador asigna un plan a un cliente, THE App_Web SHALL enviar POST a `/api/training/plans/{id}/assign` y confirmar la asignación exitosa.

---

### Requisito 15: Panel de Analíticas (Administrador)

**Historia de Usuario:** Como administrador, quiero acceder a métricas de negocio y reportes, para tomar decisiones informadas sobre la operación de los gimnasios.

#### Criterios de Aceptación

1. WHEN un administrador accede al panel de analíticas, THE App_Web SHALL mostrar dashboards interactivos con métricas de usuarios totales, suscripciones activas, ingresos mensuales, ocupación promedio y tasa de retención.
2. THE App_Web SHALL permitir filtrar las métricas por gimnasio, período y segmento de usuario.
3. WHEN un administrador accede a la sección de reportes, THE App_Web SHALL mostrar la lista de reportes generados (semanales y mensuales) con opción de descarga.
4. THE App_Web SHALL integrar dashboards de Amazon QuickSight mediante componente embebido con filtros dinámicos.

---

### Requisito 16: Internacionalización y Localización

**Historia de Usuario:** Como usuario internacional, quiero usar la aplicación en mi idioma preferido con formatos regionales adecuados, para tener una experiencia cómoda y comprensible.

#### Criterios de Aceptación

1. THE App_Web SHALL soportar los idiomas: inglés (en), español (es), francés (fr), alemán (de) y japonés (ja).
2. WHEN un usuario selecciona un idioma, THE App_Web SHALL actualizar toda la interfaz visible, mensajes y contenido estático al idioma seleccionado.
3. THE App_Web SHALL cargar las traducciones bajo demanda por namespace de módulo (auth, dashboard, training, nutrition, social, gym, payments, calendar, messages, trainer, analytics, settings, common).
4. THE App_Web SHALL utilizar formatos de fecha, hora y moneda según la configuración regional del usuario mediante Intl.DateTimeFormat e Intl.NumberFormat.
5. WHEN un usuario configura unidades de medida como imperial, THE App_Web SHALL mostrar pesos en libras (kg × 2.20462) y distancias en millas (km × 0.621371). WHEN la configuración es métrica, THE App_Web SHALL mostrar pesos en kilogramos y distancias en kilómetros.
6. THE App_Web SHALL garantizar que para cada clave de traducción en el namespace base y para cada idioma soportado, exista una traducción no vacía.

---

### Requisito 17: Manejo de Errores

**Historia de Usuario:** Como usuario, quiero recibir mensajes claros cuando ocurren errores, para entender qué sucedió y cómo proceder.

#### Criterios de Aceptación

1. WHEN una petición API retorna error 403, THE App_Web SHALL redirigir al usuario a la página `/unauthorized`.
2. WHEN una petición API retorna error 404, THE App_Web SHALL mostrar un componente de recurso no encontrado.
3. WHEN una petición API retorna error 429, THE App_Web SHALL mostrar un toast con el mensaje "Demasiadas solicitudes, intenta más tarde".
4. WHEN una petición API retorna error 500, THE App_Web SHALL mostrar un mensaje de error genérico con opción de reintentar la operación.
5. WHEN una petición API retorna error 503, THE App_Web SHALL mostrar un banner indicando "Servicio temporalmente no disponible".
6. WHEN el navegador pierde la conexión a internet, THE App_Web SHALL mostrar un banner de modo offline y deshabilitar las acciones que requieren conexión de red.
7. IF un componente React produce un error de renderizado, THEN THE ErrorBoundary SHALL capturar el error, mostrar una interfaz de recuperación con botón de reintentar y registrar el error en el servicio de monitoreo.
8. WHEN la conexión WebSocket se pierde, THE WebSocket_Service SHALL intentar reconexión automática hasta 10 veces con intervalos de 1 segundo y mostrar un indicador visual del estado de conexión.

---

### Requisito 18: Carga Bajo Demanda y Rendimiento

**Historia de Usuario:** Como usuario, quiero que la aplicación cargue rápidamente y sea fluida, para tener una experiencia de uso sin interrupciones.

#### Criterios de Aceptación

1. THE App_Web SHALL cargar cada Módulo_Funcional bajo demanda mediante React.lazy() y Suspense, generando chunks separados por módulo.
2. WHILE un módulo se está cargando, THE App_Web SHALL mostrar un componente de carga (spinner) como fallback de Suspense.
3. THE App_Web SHALL utilizar virtualización de listas (react-window) para el historial de entrenamientos, mensajes y transacciones.
4. THE App_Web SHALL utilizar caché de RTK_Query para evitar peticiones duplicadas, con invalidación automática tras mutaciones que modifican los datos.
5. THE App_Web SHALL aplicar lazy loading de imágenes con el atributo `loading="lazy"`.

---

### Requisito 19: Seguridad del Frontend

**Historia de Usuario:** Como usuario, quiero que mis datos estén protegidos contra ataques comunes del lado del cliente, para confiar en la seguridad de la plataforma.

#### Criterios de Aceptación

1. THE App_Web SHALL almacenar el access token JWT exclusivamente en memoria de Redux, sin persistirlo en localStorage ni sessionStorage.
2. THE App_Web SHALL enviar el refresh token únicamente mediante httpOnly cookie con atributo SameSite=Strict.
3. THE App_Web SHALL configurar headers Content Security Policy (CSP) para prevenir inyección de scripts.
4. THE App_Web SHALL sanitizar todas las entradas de usuario antes de renderizarlas, aprovechando el escape automático de React y validación con zod.
5. THE App_Web SHALL delegar el manejo de datos de tarjetas de pago a Stripe Elements y Adyen Drop-in, sin almacenar ni transmitir datos de tarjeta directamente.

---

### Requisito 20: Reservas de Clases Grupales

**Historia de Usuario:** Como usuario, quiero reservar clases grupales y gestionar mis reservas, para participar en sesiones dirigidas por instructores.

#### Criterios de Aceptación

1. WHEN un usuario accede a la sección de clases, THE App_Web SHALL mostrar las clases disponibles con nombre, instructor, sala, capacidad disponible, nivel de dificultad, fecha y duración.
2. WHEN un usuario reserva una clase, THE App_Web SHALL enviar POST a `/api/bookings/classes/{id}/reserve` y actualizar la capacidad disponible en la interfaz.
3. WHEN un usuario cancela una reserva, THE App_Web SHALL enviar POST a `/api/bookings/classes/{id}/cancel` y actualizar el estado de la reserva.
4. WHEN un usuario consulta la disponibilidad de un entrenador, THE App_Web SHALL mostrar los horarios disponibles obtenidos de `/api/bookings/trainers/{id}/availability`.

---

### Requisito 21: Configuración y Privacidad del Usuario

**Historia de Usuario:** Como usuario, quiero gestionar mi perfil, preferencias de idioma, tema visual, notificaciones y privacidad, para personalizar mi experiencia en la plataforma.

#### Criterios de Aceptación

1. WHEN un usuario accede a la configuración de perfil, THE App_Web SHALL mostrar un formulario editable con foto, datos personales, objetivos de fitness y condiciones médicas.
2. WHEN un usuario actualiza su perfil, THE App_Web SHALL enviar PUT a `/api/users/profile` y reflejar los cambios inmediatamente en la interfaz.
3. WHEN un usuario cambia el tema visual (claro/oscuro), THE App_Web SHALL aplicar el tema seleccionado inmediatamente en toda la interfaz utilizando los temas MUI definidos (dorado Spartan #D4AF37 para claro, #FFD700 para oscuro).
4. WHEN un usuario configura sus preferencias de notificación, THE App_Web SHALL enviar PUT a `/api/notifications/preferences` con las categorías y canales seleccionados.
5. WHEN un usuario activa MFA, THE App_Web SHALL enviar POST a `/api/users/mfa/setup` y mostrar el código QR para configurar la aplicación de autenticación.
6. WHEN un usuario solicita la exportación de sus datos, THE App_Web SHALL enviar POST a `/api/users/data-export` y confirmar que la solicitud fue recibida.
7. WHEN un usuario solicita la eliminación de su cuenta, THE App_Web SHALL mostrar una confirmación y enviar DELETE a `/api/users/profile/delete`, ejecutando logout tras la confirmación del backend.

---

### Requisito 22: Generación de Planes con IA

**Historia de Usuario:** Como usuario, quiero que la inteligencia artificial genere planes de entrenamiento personalizados basados en mi evaluación, para alcanzar mis objetivos de forma segura.

#### Criterios de Aceptación

1. WHEN un usuario completa el onboarding (nivel de fitness, objetivos, condiciones médicas, equipamiento disponible), THE App_Web SHALL enviar POST a `/api/ai/plans/generate` y mostrar el plan generado.
2. WHEN un usuario solicita verificación de sobreentrenamiento, THE App_Web SHALL consultar `/api/ai/overtraining/check` y mostrar la recomendación de descanso si se detectan indicadores de fatiga.
3. THE App_Web SHALL proporcionar un wizard multi-paso para el onboarding con progreso guardado parcialmente y pasos opcionales omitibles.

---

### Requisito 23: Comunicación en Tiempo Real (WebSocket)

**Historia de Usuario:** Como usuario, quiero recibir actualizaciones en tiempo real de chat, ocupación de gimnasios y notificaciones, para tener información actualizada sin recargar la página.

#### Criterios de Aceptación

1. WHEN un usuario se autentica exitosamente, THE WebSocket_Service SHALL establecer una conexión Socket.io con el servidor utilizando el token JWT como autenticación.
2. THE WebSocket_Service SHALL multiplexar todas las comunicaciones en tiempo real (chat, ocupación, notificaciones) a través de una única conexión Socket.io.
3. WHEN el WebSocket_Service recibe un evento `notification:new`, THE App_Web SHALL invalidar el caché de notificaciones de RTK_Query para reflejar la nueva notificación.
4. THE WebSocket_Service SHALL configurar reconexión automática con un máximo de 10 intentos y un intervalo de 1 segundo entre intentos.
5. WHEN la conexión WebSocket se establece o se pierde, THE App_Web SHALL actualizar el estado `connected` en el websocketSlice de Redux y mostrar un indicador visual correspondiente.
