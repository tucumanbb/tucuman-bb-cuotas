@echo off
chcp 65001 > nul
echo ==========================================
echo  Sincronizando con GitHub - Club Tucuman BB
echo ==========================================

git config user.name "Club Tucuman BB"
git config user.email "tucumanbbcuotas@gmail.com"

git remote remove origin >nul 2>&1
git remote add origin https://github.com/tucumanbb/tucuman-bb-cuotas.git

if exist index.html (
    copy /Y index.html web\index.html >nul 2>&1
)
if exist club_logo.jpg (
    copy /Y club_logo.jpg web\club_logo.jpg >nul 2>&1
)

git add index.html web/index.html club_logo.jpg web/club_logo.jpg supabase_schema.sql subir-a-github.bat subir-a-github.ps1
git add .
git commit -m "Actualizacion: categorias de basquet, asignacion de profesores, nuevo logo oficial y restricciones de cuotas"


git branch -M main

echo.
echo Sincronizando con la rama remota de GitHub...
git pull origin main --allow-unrelated-histories -X ours --no-edit >nul 2>&1

echo.
echo Subiendo archivos a https://github.com/tucumanbb/tucuman-bb-cuotas.git ...
git push -u origin main
if errorlevel 1 (
    echo.
    echo Sincronizando diferencias y asegurando la version mas reciente...
    git push -u origin main --force
)

echo.
echo ==========================================
echo  Listo! Si viste 'main -> main', la subida fue exitosa.
echo  Vercel actualizara el sitio en 1 minuto.
echo ==========================================
pause
