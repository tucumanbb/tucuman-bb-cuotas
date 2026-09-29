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

# Asegurar que index.html esté en la raíz para Vercel
if (Test-Path "web\index.html") {
    Copy-Item "web\index.html" "index.html" -Force
}

# 3. Preparar archivos y comitear cambios
git add .
git commit -m "Actualizacion del sistema de cuotas Club Tucuman BB" 2>$null

# 4. Asegurar rama principal
git branch -M main

# 5. Subir cambios a GitHub
Write-Host "`nSubiendo archivos a https://github.com/tucumanbb/tucuman-bb-cuotas.git ..." -ForegroundColor Yellow
git push -u origin main

Write-Host "`n¡Proceso completado con éxito!" -ForegroundColor Green
