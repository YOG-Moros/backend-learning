$ErrorActionPreference = 'Stop'
Push-Location (Split-Path $PSScriptRoot -Parent)
try {
    & mvn -B -pl 10-doc -am test '-Dtest=OpenApiDocumentTest' '-Dsurefire.failIfNoSpecifiedTests=false'
    if ($LASTEXITCODE -ne 0) { throw 'OpenAPI contract tests failed' }
    New-Item -ItemType Directory -Force docs/apifox | Out-Null
    Copy-Item 10-doc/target/openapi/*.json docs/apifox/
} finally { Pop-Location }
