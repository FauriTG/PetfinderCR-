# 🐾 PetFinder CR

Aplicación Android para reportar y encontrar mascotas perdidas y encontradas en Costa Rica.

## ✨ Características

- 🔐 Registro e inicio de sesión (Supabase Auth)
- 📋 Crear, editar y eliminar reportes de mascotas
- 📍 Captura automática de ubicación GPS al crear un reporte
- 🗺️ Mapa interactivo con marcadores (perdidas en rojo, encontradas en azul)
- 📸 Subida de fotos de mascotas
- 🔔 Notificaciones de reportes cercanos (radio de 10 km)
- 🎨 Dashboard moderno con Material 3

## 🛠️ Stack Tecnológico

- **Lenguaje:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Arquitectura:** MVVM + StateFlow
- **Inyección de dependencias:** Hilt
- **Navegación:** Navigation Compose
- **Backend:** Supabase (Auth, PostgREST, Storage)
- **Mapas:** Google Maps Compose
- **Imágenes:** Coil

## ⚙️ Configuración

Copia [`local.properties.example`](local.properties.example) a `local.properties` y complétalo con la ruta de tu Android SDK y la `MAPS_API_KEY` (pídesela al equipo por un medio privado — no se sube a git).

## 📱 Requisitos

- Android 8.0 (API 26) o superior
- Android Studio Ladybug o superior
