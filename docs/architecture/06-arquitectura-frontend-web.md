# Arquitectura del Frontend Web — Spartan Golden Gym

## Resumen

Aplicación web SPA construida con React 18, TypeScript, Vite y Tailwind CSS. Utiliza Material-UI como sistema de diseño, Redux Toolkit para estado global, RTK Query para comunicación con la API, y Socket.io para funcionalidades en tiempo real.

## Stack Tecnológico

| Componente | Tecnología | Versión |
|---|---|---|
| Framework | React | 18.3.x |
| Lenguaje | TypeScript | 5.7.x |
| Bundler | Vite | 6.3.x |
| UI Framework | Material-UI (MUI) | 6.5.x |
| CSS | Tailwind CSS | 3.4.x |
| Estado Global | Redux Toolkit | 2.6.x |
| Formularios | React Hook Form + Zod | 7.x / 3.x |
| Routing | React Router DOM | 6.30.x |
| Gráficos | Recharts + Victory | 2.x / 37.x |
| Mapas | Mapbox GL + react-map-gl | 3.x / 7.x |
| Video | Video.js | 8.x |
| i18n | react-i18next | 15.x |
| WebSocket | Socket.io Client | 4.8.x |
| Virtualización | react-window | 1.8.x |
| Notificaciones | notistack | 3.x |
| Testing | Vitest + Testing Library + axe-core | 3.x / 16.x |
| PBT | fast-check | 4.x |
| Mocking | MSW (Mock Service Worker) | 2.x |

## Estructura del Proyecto

```
spartan-gym-frontend/
├── public/
│   ├── locales/              # Archivos de traducción (i18n)
│   ├── favicon.svg
│   └── icons.svg
├── src/
│   ├── api/                  # RTK Query API slices
│   │   ├── baseApi.ts        # Configuración base de RTK Query
│   │   ├── usersApi.ts       # Endpoints de usuarios
│   │   ├── gymsApi.ts        # Endpoints de gimnasios
│   │   ├── trainingApi.ts    # Endpoints de entrenamiento
│   │   ├── workoutsApi.ts    # Endpoints de seguimiento
│   │   ├── nutritionApi.ts   # Endpoints de nutrición
│   │   ├── aiCoachApi.ts     # Endpoints de IA Coach
│   │   ├── socialApi.ts      # Endpoints sociales
│   │   ├── paymentsApi.ts    # Endpoints de pagos
│   │   ├── bookingsApi.ts    # Endpoints de reservas
│   │   ├── messagesApi.ts    # Endpoints de mensajería
│   │   ├── calendarApi.ts    # Endpoints de calendario
│   │   ├── notificationsApi.ts
│   │   ├── analyticsApi.ts
│   │   └── trainerApi.ts
│   ├── app/                  # Configuración de Redux store
│   │   ├── store.ts
│   │   ├── rootReducer.ts
│   │   └── hooks.ts          # Typed hooks (useAppDispatch, useAppSelector)
│   ├── components/           # Componentes reutilizables
│   │   ├── Charts/           # Wrappers de Recharts/Victory
│   │   ├── DonationButton/   # Botón de donación PayPal
│   │   ├── Guards/           # Route guards por rol
│   │   ├── Layout/           # Layout principal, sidebar, header
│   │   ├── Map/              # Componente de mapa Mapbox
│   │   ├── Navigation/       # Navegación principal
│   │   └── VideoPlayer/      # Wrapper de Video.js
│   ├── features/             # Módulos por dominio (feature slices)
│   │   ├── auth/             # Login, registro, MFA
│   │   ├── dashboard/        # Dashboard personalizado
│   │   ├── training/         # Planes, rutinas, seguimiento
│   │   ├── nutrition/        # Planes nutricionales, comidas
│   │   ├── social/           # Comunidad, desafíos, rankings
│   │   ├── gym/              # Mapa de gimnasios, ocupación
│   │   ├── payments/         # Suscripciones, donaciones
│   │   ├── bookings/         # Reservas de clases
│   │   ├── calendar/         # Calendario unificado
│   │   ├── messages/         # Chat y mensajería
│   │   ├── trainer/          # Panel de entrenador
│   │   ├── analytics/        # Panel de analíticas (admin)
│   │   ├── settings/         # Configuración, preferencias
│   │   └── websocket/        # Gestión de conexión WebSocket
│   ├── hooks/                # Custom hooks
│   │   ├── useAuth.ts
│   │   ├── useGymOccupancy.ts
│   │   ├── useLocale.ts
│   │   ├── useRoleGuard.ts
│   │   └── useWorkout.ts
│   ├── i18n/                 # Configuración de internacionalización
│   ├── routes/               # Definición de rutas
│   ├── theme/                # Tema MUI personalizado
│   ├── types/                # Tipos TypeScript compartidos
│   ├── utils/                # Utilidades (contraste de color, etc.)
│   ├── websocket/            # Servicio Socket.io
│   ├── test/                 # Setup de tests y helpers
│   ├── App.tsx               # Componente raíz
│   ├── main.tsx              # Entry point
│   └── index.css             # Estilos globales + Tailwind
├── vite.config.ts            # Configuración de Vite + proxy API
├── tailwind.config.ts
├── tsconfig.json
└── package.json
```

## Patrones de Arquitectura

- Feature-based structure: cada dominio de negocio es un módulo independiente bajo `features/`
- RTK Query para data fetching con cache automático e invalidación
- Redux Toolkit slices para estado local de cada feature
- Proxy de desarrollo: `/api` redirige a `http://localhost:8080` (API Gateway)
- Alias `@/` para imports absolutos desde `src/`
- Lazy loading de rutas para optimizar el bundle
- Idiomas soportados: español, inglés, francés, alemán, japonés

## Accesibilidad

- axe-core integrado en CI para auditorías WCAG 2.1 AA
- Contraste de color validado programáticamente
- Componentes MUI con soporte ARIA nativo
- Tests de accesibilidad con `@testing-library/jest-dom`
