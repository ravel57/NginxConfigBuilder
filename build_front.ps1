$path = Get-Location
Set-Location "../../WebstormProjects/nginx-config-builder"

yarn install
yarn build

if (Test-Path "./dist/spa/") {
    Copy-Item -Force -Recurse "./dist/spa/*" "$path/src/main/resources/static"
    Set-Location $path
} else {
    Write-Error "Building error"
    Set-Location $path
    exit 1
}