-- ============================================================
-- PetFinder CR — Esquema Supabase
-- COMPATIBLE con el código de la app + cubre los requerimientos del equipo.
--
-- Cómo usarlo:
--   1. Supabase Dashboard -> tu proyecto -> SQL Editor (icono </>)
--   2. New query
--   3. Pega TODO este archivo
--   4. Run  ▶
--
-- Nota: los USUARIOS y sus CONTRASEÑAS los maneja Supabase Auth (tabla
-- auth.users, contraseñas cifradas). Aquí solo guardamos el PERFIL, ligado
-- por el id (uuid) de auth.users. Este script se puede correr varias veces.
-- ============================================================

-- =========================
-- PERFILES  (datos del usuario)
-- =========================
create table if not exists perfiles (
  id uuid primary key references auth.users(id) on delete cascade,
  nombre text not null,
  telefono text,
  foto_perfil text,
  descripcion text,                            -- bio pública (opcional)
  sexo text,                                   -- Hombre / Mujer / Otro (opcional)
  procedencia text,                            -- de dónde es (opcional)
  estado boolean default true,                 -- activo / inactivo
  fecha_registro timestamptz default now()
);

-- Migración para bases existentes: agrega las columnas nuevas si faltan
alter table perfiles add column if not exists descripcion text;
alter table perfiles add column if not exists sexo text;
alter table perfiles add column if not exists procedencia text;

alter table perfiles enable row level security;

drop policy if exists "Perfiles visibles para todos" on perfiles;
create policy "Perfiles visibles para todos"
  on perfiles for select using (true);

drop policy if exists "Usuario puede editar su perfil" on perfiles;
create policy "Usuario puede editar su perfil"
  on perfiles for all using (auth.uid() = id);

-- =========================
-- TIPOS DE MASCOTA
-- =========================
create table if not exists tipos_mascota (
  id bigint generated always as identity primary key,
  nombre text not null
);

insert into tipos_mascota (nombre) values
  ('Perro'), ('Gato'), ('Ave'), ('Conejo'), ('Reptil'), ('Otro')
on conflict do nothing;

alter table tipos_mascota enable row level security;

drop policy if exists "Tipos visibles para todos" on tipos_mascota;
create policy "Tipos visibles para todos"
  on tipos_mascota for select using (true);

-- =========================
-- REPORTES
-- =========================
create table if not exists reportes (
  id bigint generated always as identity primary key,
  usuario_id uuid references perfiles(id) on delete set null,
  tipo_mascota_id bigint references tipos_mascota(id),
  titulo text not null,
  descripcion text,
  color text,
  raza text,
  fecha_reporte timestamptz default now(),
  fecha_evento timestamptz,
  latitud numeric(10, 7),
  longitud numeric(10, 7),
  direccion text,
  estado text not null default 'PERDIDA'
    check (estado in ('PERDIDA', 'ENCONTRADA', 'RECUPERADA', 'CERRADA')),
  recompensa boolean default false,
  monto_recompensa numeric(12, 2)
);

alter table reportes enable row level security;

drop policy if exists "Reportes visibles para todos" on reportes;
create policy "Reportes visibles para todos"
  on reportes for select using (true);

drop policy if exists "Usuarios autenticados pueden crear reportes" on reportes;
create policy "Usuarios autenticados pueden crear reportes"
  on reportes for insert with check (auth.uid() = usuario_id);

drop policy if exists "Usuario puede editar sus reportes" on reportes;
create policy "Usuario puede editar sus reportes"
  on reportes for update using (auth.uid() = usuario_id);

drop policy if exists "Usuario puede eliminar sus reportes" on reportes;
create policy "Usuario puede eliminar sus reportes"
  on reportes for delete using (auth.uid() = usuario_id);

-- =========================
-- IMÁGENES DE REPORTE
-- =========================
create table if not exists imagenes_reporte (
  id bigint generated always as identity primary key,
  reporte_id bigint references reportes(id) on delete cascade,
  url_imagen text not null,
  fecha_subida timestamptz default now()
);

alter table imagenes_reporte enable row level security;

drop policy if exists "Imagenes visibles para todos" on imagenes_reporte;
create policy "Imagenes visibles para todos"
  on imagenes_reporte for select using (true);

drop policy if exists "Usuarios autenticados pueden subir imagenes" on imagenes_reporte;
create policy "Usuarios autenticados pueden subir imagenes"
  on imagenes_reporte for insert with check (auth.role() = 'authenticated');

drop policy if exists "Usuarios autenticados pueden borrar imagenes" on imagenes_reporte;
create policy "Usuarios autenticados pueden borrar imagenes"
  on imagenes_reporte for delete using (auth.role() = 'authenticated');

-- =========================
-- COINCIDENCIAS_IA  (búsqueda por imagen con IA)
-- =========================
create table if not exists coincidencias_ia (
  id bigint generated always as identity primary key,
  imagen_origen_id bigint references imagenes_reporte(id) on delete cascade,
  imagen_coincidencia_id bigint references imagenes_reporte(id) on delete cascade,
  porcentaje_coincidencia numeric(5, 2),
  fecha_coincidencia timestamptz default now()
);

alter table coincidencias_ia enable row level security;

drop policy if exists "Coincidencias visibles autenticados" on coincidencias_ia;
create policy "Coincidencias visibles autenticados"
  on coincidencias_ia for select using (auth.role() = 'authenticated');

drop policy if exists "Crear coincidencias autenticados" on coincidencias_ia;
create policy "Crear coincidencias autenticados"
  on coincidencias_ia for insert with check (auth.role() = 'authenticated');

-- =========================
-- MENSAJES
-- =========================
create table if not exists mensajes (
  id bigint generated always as identity primary key,
  emisor_id uuid references perfiles(id) on delete set null,
  receptor_id uuid references perfiles(id) on delete set null,
  mensaje text not null,
  fecha_envio timestamptz default now()
);

alter table mensajes enable row level security;

drop policy if exists "Ver mensajes propios" on mensajes;
create policy "Ver mensajes propios"
  on mensajes for select using (auth.uid() = emisor_id or auth.uid() = receptor_id);

drop policy if exists "Enviar mensajes" on mensajes;
create policy "Enviar mensajes"
  on mensajes for insert with check (auth.uid() = emisor_id);

-- =========================
-- NOTIFICACIONES
-- =========================
create table if not exists notificaciones (
  id bigint generated always as identity primary key,
  usuario_id uuid references perfiles(id) on delete cascade,
  titulo text not null,
  mensaje text not null,
  leida boolean default false,
  fecha_envio timestamptz default now()
);

alter table notificaciones enable row level security;

drop policy if exists "Ver notificaciones propias" on notificaciones;
create policy "Ver notificaciones propias"
  on notificaciones for select using (auth.uid() = usuario_id);

drop policy if exists "Marcar notificacion como leida" on notificaciones;
create policy "Marcar notificacion como leida"
  on notificaciones for update using (auth.uid() = usuario_id);

-- Permitir que un usuario autenticado cree notificaciones para otros
-- (necesario para avisos automáticos, p.ej. solicitudes de cambio de estado)
drop policy if exists "Crear notificaciones" on notificaciones;
create policy "Crear notificaciones"
  on notificaciones for insert with check (auth.role() = 'authenticated');

-- =========================
-- SOLICITUDES DE CAMBIO DE ESTADO
-- Un usuario que no es dueño puede pedir cambiar el estado de un reporte;
-- el dueño aprueba o rechaza.
-- =========================
create table if not exists solicitudes_estado (
  id bigint generated always as identity primary key,
  reporte_id bigint references reportes(id) on delete cascade,
  solicitante_id uuid references perfiles(id) on delete set null,
  dueno_id uuid references perfiles(id) on delete cascade,
  estado_solicitado text not null,
  estado text not null default 'PENDIENTE',   -- PENDIENTE / APROBADA / RECHAZADA
  fecha timestamptz default now()
);

alter table solicitudes_estado enable row level security;

drop policy if exists "Ver solicitudes propias" on solicitudes_estado;
create policy "Ver solicitudes propias"
  on solicitudes_estado for select
  using (auth.uid() = solicitante_id or auth.uid() = dueno_id);

drop policy if exists "Crear solicitud" on solicitudes_estado;
create policy "Crear solicitud"
  on solicitudes_estado for insert
  with check (auth.uid() = solicitante_id);

drop policy if exists "Responder solicitud" on solicitudes_estado;
create policy "Responder solicitud"
  on solicitudes_estado for update
  using (auth.uid() = dueno_id);

-- =========================
-- TRIGGER: crear el perfil automáticamente al registrarse
-- (la app luego lo completa con nombre y teléfono reales vía upsert)
-- =========================
create or replace function public.handle_new_user()
returns trigger language plpgsql security definer set search_path = public as $$
begin
  insert into public.perfiles (id, nombre)
  values (new.id, coalesce(new.raw_user_meta_data->>'nombre', split_part(new.email, '@', 1)))
  on conflict (id) do nothing;
  return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
  after insert on auth.users
  for each row execute procedure public.handle_new_user();

-- =========================
-- STORAGE: buckets públicos para imágenes
-- =========================
insert into storage.buckets (id, name, public) values
  ('reportes', 'reportes', true),
  ('perfiles', 'perfiles', true)
on conflict (id) do nothing;

drop policy if exists "Imagenes lectura publica" on storage.objects;
create policy "Imagenes lectura publica"
  on storage.objects for select
  using (bucket_id in ('reportes', 'perfiles'));

drop policy if exists "Imagenes subir autenticado" on storage.objects;
create policy "Imagenes subir autenticado"
  on storage.objects for insert to authenticated
  with check (bucket_id in ('reportes', 'perfiles'));

drop policy if exists "Imagenes actualizar autenticado" on storage.objects;
create policy "Imagenes actualizar autenticado"
  on storage.objects for update to authenticated
  using (bucket_id in ('reportes', 'perfiles'));

drop policy if exists "Imagenes borrar autenticado" on storage.objects;
create policy "Imagenes borrar autenticado"
  on storage.objects for delete to authenticated
  using (bucket_id in ('reportes', 'perfiles'));
