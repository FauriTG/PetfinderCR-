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

Antes de compilar, crea un archivo `local.properties` en la raíz con:

```properties
sdk.dir=RUTA_A_TU_ANDROID_SDK
MAPS_API_KEY=TU_GOOGLE_MAPS_API_KEY
```

Y configura tus credenciales de Supabase en `app/build.gradle.kts`
(`SUPABASE_URL` y `SUPABASE_ANON_KEY` — usa la clave **publishable/anon**, nunca la secret).

El esquema de la base de datos está en [`supabase_schema.sql`](supabase_schema.sql).

## 📱 Requisitos

- Android 8.0 (API 26) o superior
- Android Studio Ladybug o superior
