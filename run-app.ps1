# Run Finovate JavaFX app
$mvn = Get-Command mvn -ErrorAction SilentlyContinue
if ($mvn) {
    mvn javafx:run
} else {
    Write-Host "Maven is not in your PATH." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "To install Maven, run:" -ForegroundColor Cyan
    Write-Host "  winget install Apache.Maven"
    Write-Host ""
    Write-Host "Then restart the terminal and run: mvn javafx:run"
    Write-Host ""
    Write-Host "Alternatively, run the app from your IDE using the FinovateApp run configuration."
}
