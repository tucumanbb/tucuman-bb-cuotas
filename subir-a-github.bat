@echo off
chcp 65001 > nul
echo ==========================================
echo  Sincronizando con GitHub - Club Tucuman BB
echo ==========================================

git config user.name "Club Tucuman BB"
git config user.email "tucumanbbcuotas@gmail.com"

git remote remove origin >nul 2>&1
git remote add origin https://github.com/tucumanbb/tucuman-bb-cuotas.git

if exist web\index.html (
    copy /Y web\index.html index.html >nul 2>&1
)

git add .
git commit -m "Sistema completo de cuotas y portal web Tucuman BB" >nul 2>&1

git branch -M main

echo.
echo Subiendo archivos a https://github.com/tucumanbb/tucuman-bb-cuotas.git ...
git push -u origin main

echo.
echo Listo! Presiona cualquier tecla para salir.
pause
