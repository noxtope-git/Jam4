# Generar keystore para release
# IMPORTANTE: Cambia los valores antes de ejecutar

$KEYSTORE_FILE = "jam-release.keystore"
$ALIAS = "jam"
$VALIDITY = 10000  # días

keytool -genkey -v -keystore $KEYSTORE_FILE -alias $ALIAS -keyalg RSA -keysize 2048 -validity $VALIDITY

Write-Host ""
Write-Host "Keystore creado: $KEYSTORE_FILE"
Write-Host ""
Write-Host "Para compilar release, define estas variables de entorno:"
Write-Host '  $env:JAM_STORE_FILE = "jam-release.keystore"'
Write-Host '  $env:JAM_STORE_PASSWORD = "tu_password"'
Write-Host '  $env:JAM_KEY_ALIAS = "jam"'
Write-Host '  $env:JAM_KEY_PASSWORD = "tu_password"'
Write-Host ""
Write-Host "Luego compila con:"
Write-Host "  ./gradlew assembleRelease"
