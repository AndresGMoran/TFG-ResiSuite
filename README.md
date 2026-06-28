<div align="center">

  # ResiSuite: App Trabajadores · Juegos para residentes
  
  <img src="Presentacion y memoria/logo_resisuite.png" alt="ResiSuite Logo" width="300"/>

  Aplicación móvil Android para el personal de residencias geriátricas.  
  Gestión de residentes, actividades y seguimiento de partidas de **ResiGames**, todo en un único ecosistema digital.

  ![Android](https://img.shields.io/badge/Android-8.1%2B%20(API%2027)-3DDC84?logo=android&logoColor=white)
  ![Java](https://img.shields.io/badge/Java-Android%20Studio-007396?logo=java&logoColor=white)
  ![Architecture](https://img.shields.io/badge/Architecture-MVC%20%2B%20Fragment-blueviolet)
  ![License](https://img.shields.io/badge/License-Privado-red)

</div>

---

## 📖 Sobre el proyecto

**ResiSuite** es un ecosistema digital orientado a digitalizar la gestión y el entretenimiento en centros geriátricos. Está compuesto por dos aplicaciones interconectadas a través de una API central:

| App | Plataforma | Descripción |
|-----|-----------|-------------|
| 🎮 **ResiGames** | Tablet (Android/Windows) | Juegos de estimulación psicomotriz para residentes, desarrollados en Godot Engine |
| 📱 **ResiCare** | Móvil Android | App para trabajadores — gestión de residentes, actividades y estadísticas |

> Este repositorio contiene el frontend de **ResiCare** (app de trabajadores) y **ResiGames** (app de juegos). El backend y la API central fueron desarrollados por un compañero de equipo.

---

## ✨ Funcionalidades principales

- **Gestión de residentes** — Alta, baja, perfil y seguimiento individualizado
- **Gestión de actividades** — Organización de salidas, cambio de estado y control de participantes con necesidades específicas
- **Registro de partidas** — Historial de sesiones de juego con observaciones del trabajador
- **Estadísticas visuales** — PieCharts y BarCharts por residente y por juego
- **Panel de administración** — Gestión de residencias, usuarios y juegos
- **Multiidioma** — Español, inglés y valenciano
- **Sesión persistente** — Login con token JWT almacenado de forma segura

---

## 🛠️ Stack tecnológico

| Herramienta | Uso |
|------------|-----|
| **Android Studio** | IDE de desarrollo |
| **Java** | Lenguaje principal |
| **Figma** | Diseño de interfaces y prototipado |
| **REST API** | Backend central (JWT auth) |
| **MPAndroidChart** | Gráficas estadísticas |
| **RecyclerView + Adapters** | Listados dinámicos |

---

## 🏗️ Arquitectura

La app sigue el patrón **MVC** con una única `MainActivity` que actúa como enrutador central de fragments mediante `FragmentManager` / `FragmentTransaction`.

```
MainActivity
├── LoginFragment
├── HomeFragment
│   ├── AddResidentFragment
│   └── ResidentFragment
├── ActivitiesFragment
│   ├── AddActivityFragment
│   ├── ActivityDetailFragment
│   │   ├── AddOpinionFragment
│   │   └── ParticipantDetailFragment
├── GameFragment
│   └── GameDetailFragment
├── AccountFragment
│   └── ResidenceFragment
└── AdminFragment
```

Los datos se cargan desde `AppDataRepository` (singleton) que abstrae las llamadas a `ApiClient`, y se pasan entre fragments vía `Bundle`.

---

## 📱 Requisitos

| Requisito | Detalle |
|-----------|---------|
| Android mínimo | **8.1 Oreo (API 27)** |
| Conexión | Red local o internet (API REST) |
| Permisos | Internet |

---

## 🎨 Diseño

Los prototipos visuales están disponibles en Figma:

🔗 [**Figma — App Residencia**](https://www.figma.com/design/kBoVi5Lm7SE7f9k44wDuMQ/App-Residencia?node-id=0-1&t=2lpF1PnRuMja389g-1)

---

## 🗂️ Estructura del proyecto

```
app/src/main/java/com.andresgmoran.apptrabajadores/
├── exceptions/
├── interfaces/
├── models/
│   ├── adapters/        # RecyclerView adapters
│   ├── gameStats/
│   ├── parsers/
│   └── [entidades]      # Activity, Resident, Game, User...
├── network/             # ApiClient (HTTP + JWT)
├── repository/          # AppDataRepository
└── ui/
    └── fragments/
        ├── account/
        ├── activities/
        ├── admin/
        ├── authentication/
        ├── game / gameDetail/
        ├── home/
        ├── residence/
        └── resident/
```

---

## 🎮 Juegos disponibles en ResiGames

| # | Juego | Estimula |
|---|-------|---------|
| 1 | **Seguir la línea** | Coordinación viso-motora, atención sostenida |
| 2 | **Flecha y reacciona** | Reflejos y rapidez de reacción |
| 3 | **Gimnasio** | Condición física, motricidad gruesa |
| 4 | **Emparejar las sombras** | Coordinación ojo-mano, asociación visual |
| 5 | **Bingo auditivo** | Memoria auditiva, atención y concentración |

---

## 🔮 Ampliaciones previstas

- 🎨 **FormArt** — Recrear figuras con un número limitado de líneas
- 🧠 **Torres del Recuerdo** — Versión accesible del clásico Torres de Hanoi
- 💰 **ResiWallet** — Billetera virtual por residente para gestión económica interna

---

## 👥 Equipo

| Rol | Descripción |
|-----|-------------|
| **Andrés Glyn Moran Lamela** | Frontend — ResiCare (Android/Java) + ResiGames (Godot) |
| **Compañero de equipo** | Backend — API REST central |

Trabajo Final de Grado · CFGS Desarrollo de Aplicaciones Multiplataforma · IES La Mar, Xàbia · 2024–2025
