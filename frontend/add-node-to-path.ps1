# Script pour ajouter Node.js au PATH (à exécuter en tant qu'administrateur)
# Remplacez le chemin ci-dessous par le chemin réel de votre installation Node.js

$nodePath = "C:\Program Files\nodejs"  # Changez ce chemin si nécessaire

# Vérifier si le chemin existe
if (Test-Path $nodePath) {
    # Récupérer le PATH actuel
    $currentPath = [Environment]::GetEnvironmentVariable("Path", "User")
    
    # Vérifier si Node.js est déjà dans le PATH
    if ($currentPath -notlike "*$nodePath*") {
        # Ajouter Node.js au PATH utilisateur
        [Environment]::SetEnvironmentVariable("Path", "$currentPath;$nodePath", "User")
        Write-Host "Node.js a été ajouté au PATH avec succès!" -ForegroundColor Green
        Write-Host "Veuillez redémarrer votre terminal pour que les changements prennent effet." -ForegroundColor Yellow
    } else {
        Write-Host "Node.js est déjà dans le PATH." -ForegroundColor Yellow
    }
} else {
    Write-Host "Le chemin $nodePath n'existe pas. Veuillez vérifier où Node.js est installé." -ForegroundColor Red
    Write-Host "Emplacements courants:" -ForegroundColor Yellow
    Write-Host "  - C:\Program Files\nodejs" -ForegroundColor Cyan
    Write-Host "  - C:\Users\$env:USERNAME\AppData\Local\Programs\nodejs" -ForegroundColor Cyan
}
