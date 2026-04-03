# Arquitectura de la App Móvil — Spartan Golden Gym

## Resumen

Aplicación móvil multiplataforma (Android/iOS) construida con React Native 0.75.x y TypeScript. Soporta operación offline de hasta 72 horas, integración con wearables (Apple Watch, Fitbit, Garmin), escaneo QR/códigos de barras, y notificaciones push vía Firebase Cloud Messaging.

## Stack Tecnológico

| Componente | Tecnología | Versión |
|---|---|---|
| Framework | React Native | 0.75.4 |
| Lenguaje | TypeScript | 5.7.x |
| Navegación | React Navigation | 6.x |
| Estado Global | Redux Toolkit | 2.6.x |
| UI Framework | React Native Paper | 5.x |
| Iconos | React Native Vector Icons | 10.x |
| Animaciones | React Native Reanimated | 3.x |
| Mapas | @rnmapbox/maps | 10.x |
| Cámara | React Native Vision Camera | 4.x |
| Cámara (legacy) | React Native Camera | 4.x |
| Wearables iOS | react-native-health (HealthKit) | 1.x |
| Wearables Android | react-native-google-fit | 0.21.x |
| Push | @react-native-firebase/messaging | 21.x |
| Almacenamiento | AsyncStorage + SQLite | 2.x / 6.x |
| WebSocket | Socket.io Client | 4.8.x |
| i18n | react-i18next | 15.x |
| Validación | Zod | 3.x |
| Testing | Jest + ts-jest + fast-check | 29.x / 4.x |

## Estructura del Proyecto

```
spartan-gym-mobile/
├── src/
│   ├── accessibility/        # Componentes accesibles y alto contraste
│   │   ├── AccessibleButton.tsx
│   │   ├── AccessibleText.tsx
│   │   ├── highContrast.ts
│   │   └── useAccessibility.ts
│   ├── auth/                 # Pantalla de autenticación
│   ├── barcode/              # Escaneo de códigos de barras (alimentos)
│   ├── calendar/             # Calendario de eventos
│   ├── camera/               # Cámara, escaneo QR
│   ├── dashboard/            # Dashboard con comparativas de objetivos
│   ├── gym/                  # Mapa de gimnasios (Mapbox)
│   ├── messages/             # Chat y mensajería
│   ├── notifications/        # Servicio de push (FCM)
│   ├── nutrition/            # Planes nutricionales
│   ├── offline/              # Motor offline (SQLite + sync)
│   │   ├── storage.ts        # Capa de persistencia SQLite
│   │   ├── offlineDataManager.ts  # Gestión de datos offline
│   │   ├── syncManager.ts    # Sincronización con backend
│   │   └── networkMonitor.ts # Detección de conectividad
│   ├── payments/             # Suscripciones y pagos
│   ├── settings/             # Configuración y preferencias
│   ├── social/               # Comunidad y desafíos
│   ├── theme/                # Tema con soporte de accesibilidad
│   ├── training/             # Entrenamientos y descarga de videos
│   ├── types/                # Tipos TypeScript
│   ├── wearables/            # Integración HealthKit/Google Fit
│   └── App.tsx               # Componente raíz con navegación
├── app.json
├── babel.config.js
├── metro.config.js
├── jest.config.js
├── tsconfig.json
└── package.json
```

## Funcionalidades Clave

### Modo Offline (72 horas)
- Almacenamiento local del plan de entrenamiento activo y últimos 7 días de historial
- Registro de entrenamientos, series y repeticiones sin conexión
- Sincronización automática al recuperar conexión (last-write-wins)
- SQLite para persistencia estructurada, AsyncStorage para preferencias

### Integración con Wearables
- HealthKit (iOS): frecuencia cardíaca, pasos, calorías, sueño
- Google Fit (Android): mismos datos biométricos
- Sincronización automática y almacenamiento local ante desconexión
- Datos cifrados en tránsito (TLS 1.3) y en reposo (AES-256)

### Accesibilidad
- VoiceOver (iOS) y TalkBack (Android)
- Dynamic Type (iOS) y escalado de fuente (Android)
- Navegación por control de voz
- Modo de alto contraste respetando configuración del sistema
- `contentDescription` en todos los elementos interactivos
- Componentes `AccessibleButton` y `AccessibleText` reutilizables

### Notificaciones Push
- Firebase Cloud Messaging para recordatorios de entrenamiento, logros y actualizaciones
- Gestión de permisos y tokens de dispositivo
- Entrega de mensajes offline almacenados

### Escaneo
- QR para check-in en gimnasios (verificación de membresía en < 3s)
- Códigos de barras para búsqueda de información nutricional de alimentos
