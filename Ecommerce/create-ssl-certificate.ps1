# =====================================================
# Script to create SSL Certificate for Spring Boot Backend
# Run this file in PowerShell with Administrator privileges
# =====================================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Create SSL Certificate for Spring Boot  " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Path to backend resources directory
$resourcesPath = "D:\Proj_Nam4\PBL6_E-Commerce\Ecommerce\src\main\resources"
$keystorePath = "$resourcesPath\keystore.p12"

# Check if keystore already exists
if (Test-Path $keystorePath) {
    Write-Host "Warning: Keystore already exists at: $keystorePath" -ForegroundColor Yellow
    $confirm = Read-Host "Do you want to create a new one? (y/N)"
    if ($confirm -ne "y" -and $confirm -ne "Y") {
        Write-Host "Cancelled. Using existing keystore." -ForegroundColor Yellow
        exit 0
    }
    Remove-Item $keystorePath -Force
}

# Check if keytool is available
$keytoolPath = $null
$javaHome = $env:JAVA_HOME

if ($javaHome) {
    $keytoolPath = "$javaHome\bin\keytool.exe"
    if (-not (Test-Path $keytoolPath)) {
        $keytoolPath = $null
    }
}

if (-not $keytoolPath) {
    # Find in PATH
    $keytoolPath = (Get-Command keytool -ErrorAction SilentlyContinue).Source
}

if (-not $keytoolPath) {
    Write-Host "ERROR: keytool not found! Make sure Java JDK is installed and JAVA_HOME is set." -ForegroundColor Red
    Write-Host "   Or add JDK bin path to PATH." -ForegroundColor Red
    exit 1
}

Write-Host "Found keytool at: $keytoolPath" -ForegroundColor Green
Write-Host ""

# Certificate information
$alias = "ecommerce"
$keyPassword = "password"
$validity = 365  # Days valid

Write-Host "Certificate Info:" -ForegroundColor Cyan
Write-Host "   - Alias: $alias"
Write-Host "   - Keystore: $keystorePath"
Write-Host "   - Validity: $validity days"
Write-Host "   - Password: $keyPassword"
Write-Host ""

# Create keystore with self-signed certificate
Write-Host "Creating SSL Certificate..." -ForegroundColor Yellow

$arguments = @(
    "-genkeypair",
    "-alias", $alias,
    "-keyalg", "RSA",
    "-keysize", "2048",
    "-storetype", "PKCS12",
    "-keystore", $keystorePath,
    "-validity", $validity,
    "-storepass", $keyPassword,
    "-keypass", $keyPassword,
    "-dname", "CN=localhost, OU=Development, O=PBL6 E-Commerce, L=DaNang, S=DaNang, C=VN",
    "-ext", "SAN=dns:localhost,ip:127.0.0.1"
)

try {
    & $keytoolPath $arguments
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "SSL Certificate created successfully!" -ForegroundColor Green
        Write-Host "   Path: $keystorePath" -ForegroundColor Green
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Cyan
        Write-Host "  CONFIG IN application.properties  " -ForegroundColor Cyan
        Write-Host "========================================" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "server.ssl.enabled=true" -ForegroundColor White
        Write-Host "server.ssl.key-store=classpath:keystore.p12" -ForegroundColor White
        Write-Host "server.ssl.key-store-type=PKCS12" -ForegroundColor White
        Write-Host "server.ssl.key-store-password=$keyPassword" -ForegroundColor White
        Write-Host "server.ssl.key-alias=$alias" -ForegroundColor White
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Cyan
        Write-Host "  USAGE INSTRUCTIONS  " -ForegroundColor Cyan
        Write-Host "========================================" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "1. Open IntelliJ IDEA" -ForegroundColor White
        Write-Host "2. Run/Debug project as usual" -ForegroundColor White
        Write-Host "3. Backend will run at: https://localhost:8081" -ForegroundColor Green
        Write-Host ""
        Write-Host "NOTE:" -ForegroundColor Yellow
        Write-Host "   - This is a self-signed certificate, browser will show warning" -ForegroundColor Yellow
        Write-Host "   - Click Advanced -> Proceed to localhost to continue" -ForegroundColor Yellow
        Write-Host ""
    } else {
        Write-Host "ERROR creating keystore. Exit code: $LASTEXITCODE" -ForegroundColor Red
    }
} catch {
    Write-Host "ERROR: $_" -ForegroundColor Red
}
