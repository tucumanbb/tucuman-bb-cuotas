# Sistema de Gestión de Cuotas y Actividades - Club Tucumán BB

Sistema integral para la administración, control de cuotas, alertas de deudas y emisión de comprobantes para el **Club Tucumán BB** (Básquetbol, Gimnasio de Musculación y Salón de Actividades).

- **Correo Oficial Institucional**: `tucumanbbcuotas@gmail.com`
- **Sede**: Suipacha 1160, San Miguel de Tucumán
- **Alias para Transferencias**: `TUCUMAN.BB.OFICIAL`

---

## 📁 Estructura del Proyecto

```text
├── app/                      # Aplicación Android Nativa (Kotlin + Jetpack Compose + Room)
├── web/                      # Portal Web de Administración listo para Vercel
│   ├── index.html            # Dashboard Web completo con Tailwind CSS y Supabase
│   └── vercel.json           # Configuración de despliegue en Vercel
├── supabase_schema.sql       # Script SQL para crear las tablas en Supabase
└── README.md                 # Guía paso a paso
```

---

## ⚡ 1. Despliegue en Supabase (Base de Datos en la Nube Gratuita)

1. Ingresa a [supabase.com](https://supabase.com) e inicia sesión con **`tucumanbbcuotas@gmail.com`**.
2. Crea un nuevo proyecto llamado `tucuman-bb-db` (selecciona el plan **Free** y la región más cercana, ej: `sa-east-1` São Paulo).
3. En el menú lateral izquierdo de Supabase, entra a **SQL Editor**.
4. Copia y pega todo el contenido del archivo `supabase_schema.sql` y presiona **Run**.
5. ¡Listo! Se crearán automáticamente las tablas: `club_settings`, `activities`, `members`, `member_activities` y `payments`, junto con los datos iniciales y las políticas de seguridad.
6. Ve a **Project Settings ➔ API** y copia tu:
   - **Project URL** (ejemplo: `https://xyzabcdefg.supabase.co`)
   - **Project API Keys (anon public)**

---

## 🌐 2. Despliegue del Portal Web en Vercel (Gratuito)

1. Sube este proyecto a tu repositorio de GitHub (siguiendo los pasos de Git abajo).
2. Entra a [vercel.com](https://vercel.com) e inicia sesión con la cuenta de GitHub del club.
3. Haz clic en **Add New... ➔ Project**.
4. Selecciona el repositorio `tucuman-bb-cuotas`.
5. En la sección **Root Directory**, selecciona la carpeta `web` (o déjalo en raíz si deseas que sirva `web/index.html`).
6. Presiona **Deploy**.
7. En menos de 30 segundos tendrás tu enlace web oficial (ejemplo: `https://tucuman-bb-cuotas.vercel.app`) para ingresar desde cualquier computadora o celular.

---

## 💻 3. Configuración en Visual Studio Code y Git

Para mantener tus commits limpios y asociados exclusivamente a la identidad del club:

```bash
# 1. En la terminal de VS Code en la carpeta del proyecto:
git init

# 2. Configurar la identidad del club para este proyecto:
git config user.name "Club Tucuman BB"
git config user.email "tucumanbbcuotas@gmail.com"

# 3. Guardar y subir a GitHub:
git add .
git commit -m "Sistema completo de cuotas y portal web Tucuman BB"
git branch -M main
git remote add origin https://github.com/TU_USUARIO_O_CLUB/tucuman-bb-cuotas.git
git push -u origin main
```
