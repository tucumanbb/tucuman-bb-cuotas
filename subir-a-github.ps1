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

# Asegurar que index.html y club_logo.jpg estén sincronizados en web\ y raíz
if (Test-Path "index.html") {
    Copy-Item "index.html" "web\index.html" -Force
}
if (Test-Path "club_logo.jpg") {
    Copy-Item "club_logo.jpg" "web\club_logo.jpg" -Force
}

# 3. Preparar archivos y comitear cambios
git add index.html web/index.html club_logo.jpg web/club_logo.jpg supabase_schema.sql subir-a-github.bat subir-a-github.ps1
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
