$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$ideaMaven = 'D:\environment\JAVA\IntelliJ IDEA 2024.1.7\plugins\maven\lib\maven3\bin\mvn.cmd'
$maven = if (Get-Command mvn.cmd -ErrorAction SilentlyContinue) { 'mvn.cmd' } elseif (Test-Path $ideaMaven) { $ideaMaven } else { throw '没有找到 Maven。' }

Push-Location (Join-Path $projectRoot 'backend')
try { & $maven test; if ($LASTEXITCODE -ne 0) { throw '后端测试失败' } } finally { Pop-Location }

Push-Location (Join-Path $projectRoot 'frontend')
try { & npm.cmd run build; if ($LASTEXITCODE -ne 0) { throw '前端构建失败' } } finally { Pop-Location }

Write-Host '验证完成：后端测试和前端生产构建全部通过。'
