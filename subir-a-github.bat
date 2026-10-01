@echo off
chcp 65001 > nul
echo ==========================================
echo  Sincronizando con GitHub - Club Tucuman BB
echo ==========================================

git config user.name "Club Tucuman BB"
git config user.email "tucumanbbcuotas@gmail.com"

git remote remove origin >nul 2>&1
git remote add origin https://github.com/tucumanbb/tucuman-bb-cuotas.git

if not exist public mkdir public >nul 2>&1
if not exist web mkdir web >nul 2>&1

if exist index.html (
    copy /Y index.html web\index.html >nul 2>&1
    copy /Y index.html public\index.html >nul 2>&1
)

REM Sincronizar el logo oficial sin sobrescribir el archivo nuevo del usuario
if exist public\club_logo.png (
    copy /Y public\club_logo.png club_logo.png >nul 2>&1
    copy /Y public\club_logo.png web\club_logo.png >nul 2>&1
)
if exist club_logo.png (
    copy /Y club_logo.png public\club_logo.png >nul 2>&1
    copy /Y club_logo.png web\club_logo.png >nul 2>&1
)
if exist public\club_logo.jpeg (
    copy /Y public\club_logo.jpeg club_logo.jpeg >nul 2>&1
    copy /Y public\club_logo.jpeg web\club_logo.jpeg >nul 2>&1
)
if exist public\club_logo.jpg (
    copy /Y public\club_logo.jpg club_logo.jpg >nul 2>&1
    copy /Y public\club_logo.jpg web\club_logo.jpg >nul 2>&1
) else if exist club_logo.jpg (
    copy /Y club_logo.jpg public\club_logo.jpg >nul 2>&1
    copy /Y club_logo.jpg web\club_logo.jpg >nul 2>&1
)

git add index.html web/index.html public/index.html club_logo.* public/club_logo.* web/club_logo.* supabase_schema.sql vercel.json subir-a-github.bat subir-a-github.ps1
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
