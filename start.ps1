# Démarre le backend Spring Boot en arrière-plan
Write-Host "🚀 Démarrage du backend Spring Boot..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd backend; Write-Host '📦 Backend Spring Boot' -ForegroundColor Green; mvn spring-boot:run"

# Attente pour que le backend démarre
Write-Host "⏳ Attente du démarrage du backend (15 secondes)..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

# Démarre le frontend React + Vite
Write-Host "🎨 Démarrage du frontend React..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd Site; Write-Host '⚛️ Frontend React + Vite' -ForegroundColor Blue; npm run dev"

# Message de confirmation
Start-Sleep -Seconds 2
Write-Host ""
Write-Host "✅ Application Pickup & Delivery démarrée!" -ForegroundColor Green
Write-Host ""
Write-Host "📡 Backend API: http://localhost:8080" -ForegroundColor Cyan
Write-Host "🌐 Frontend: http://localhost:5173" -ForegroundColor Blue
Write-Host ""
Write-Host "Pour arrêter l'application, fermez les fenêtres PowerShell ouvertes." -ForegroundColor Yellow
