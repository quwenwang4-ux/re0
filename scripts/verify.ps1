$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$systemMaven = Get-Command mvn.cmd -ErrorAction SilentlyContinue
$wrapperMaven = Get-ChildItem -Path (Join-Path $env:USERPROFILE '.m2\wrapper\dists') -Recurse -Filter mvn.cmd -ErrorAction SilentlyContinue |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1
$maven = if ($systemMaven) { $systemMaven.Source } elseif ($wrapperMaven) { $wrapperMaven.FullName } else { throw '没有找到 Maven。' }

Push-Location (Join-Path $projectRoot 'backend')
try { & $maven test; if ($LASTEXITCODE -ne 0) { throw '后端测试失败' } } finally { Pop-Location }

Push-Location (Join-Path $projectRoot 'frontend')
try { & npm.cmd run build; if ($LASTEXITCODE -ne 0) { throw '前端构建失败' } } finally { Pop-Location }

Write-Host '验证完成：后端测试和前端生产构建全部通过。'
