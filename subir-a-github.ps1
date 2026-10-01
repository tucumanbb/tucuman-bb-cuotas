# Script de automatización de subida a GitHub - Club Tucumán BB
# Correo: tucumanbbcuotas@gmail.com
# Repositorio: https://github.com/tucumanbb/tucuman-bb-cuotas.git

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host " Sincronizando con GitHub - Club Tucumán BB " -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

# 1. Configurar identidad del club
git config user.name "Club Tucuman BB"
git config user.email "tucumanbbcuotas@gmail.com"

# 2. Corregir enlace remoto oficial
git remote remove origin 2>$null
git remote add origin https://github.com/tucumanbb/tucuman-bb-cuotas.git

# Asegurar carpetas public\ y web\
if (!(Test-Path "public")) { New-Item -ItemType Directory -Path "public" }
if (!(Test-Path "web")) { New-Item -ItemType Directory -Path "web" }

# Asegurar que index.html esté sincronizado en web\, public\ y raíz
if (Test-Path "index.html") {
    Copy-Item "index.html" "web\index.html" -Force
    Copy-Item "index.html" "public\index.html" -Force
}

# Sincronizar el logo oficial sin sobrescribir el archivo nuevo del usuario
if (Test-Path "public\club_logo.png") {
    Copy-Item "public\club_logo.png" "club_logo.png" -Force
    Copy-Item "public\club_logo.png" "web\club_logo.png" -Force
}
if (Test-Path "club_logo.png") {
    Copy-Item "club_logo.png" "public\club_logo.png" -Force
    Copy-Item "club_logo.png" "web\club_logo.png" -Force
}
if (Test-Path "public\club_logo.jpeg") {
    Copy-Item "public\club_logo.jpeg" "club_logo.jpeg" -Force
    Copy-Item "public\club_logo.jpeg" "web\club_logo.jpeg" -Force
}
if (Test-Path "public\club_logo.jpg") {
    Copy-Item "public\club_logo.jpg" "club_logo.jpg" -Force
    Copy-Item "public\club_logo.jpg" "web\club_logo.jpg" -Force
} elseif (Test-Path "club_logo.jpg") {
    Copy-Item "club_logo.jpg" "public\club_logo.jpg" -Force
    Copy-Item "club_logo.jpg" "web\club_logo.jpg" -Force
}

# 3. Preparar archivos y comitear cambios
git add index.html web/index.html public/index.html club_logo.* public/club_logo.* web/club_logo.* supabase_schema.sql vercel.json subir-a-github.bat subir-a-github.ps1
git add .
git commit -m "Actualizacion: categorias de basquet, asignacion de profesores, nuevo logo oficial y restricciones de cuotas"


# 4. Asegurar rama principal
git branch -M main

# 5. Sincronizar y Subir cambios a GitHub
Write-Host "`nSincronizando con la rama remota..." -ForegroundColor Yellow
git pull origin main --allow-unrelated-histories -X ours --no-edit 2>$null

Write-Host "`nSubiendo archivos a https://github.com/tucumanbb/tucuman-bb-cuotas.git ..." -ForegroundColor Yellow
git push -u origin main

if ($LASTEXITCODE -ne 0) {
    Write-Host "`nSincronizando diferencias y asegurando la version mas reciente..." -ForegroundColor Yellow
    git push -u origin main --force
}

Write-Host "`n¡Proceso completado con éxito! Vercel actualizará el sitio en 1 minuto." -ForegroundColor Green
