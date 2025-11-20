@echo off
echo 🚀 Demarrage de l'application Pickup ^& Delivery...
echo.

echo 📦 Demarrage du backend Spring Boot...
start "Backend Spring Boot" cmd /k "cd backend && mvn spring-boot:run"

echo ⏳ Attente du demarrage du backend (10 secondes)...
timeout /t 10 /nobreak > nul

echo 🎨 Demarrage du frontend React...
start "Frontend React" cmd /k "cd frontend && npm run dev"

echo.
echo ✅ Application demarree!
echo 📡 Backend API: http://localhost:8080
echo 🌐 Frontend: http://localhost:5173
echo.
pause
