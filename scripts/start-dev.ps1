param(
    [switch]$UseRealAi
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$systemMaven = Get-Command mvn.cmd -ErrorAction SilentlyContinue
$wrapperMaven = Get-ChildItem -Path (Join-Path $env:USERPROFILE '.m2\wrapper\dists') -Recurse -Filter mvn.cmd -ErrorAction SilentlyContinue |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1
$maven = if ($systemMaven) { $systemMaven.Source } elseif ($wrapperMaven) { $wrapperMaven.FullName } else { throw '没有找到 Maven，请先在 IDEA 中配置 Maven。' }

if ($UseRealAi) {
    $python = Join-Path $projectRoot 'ai-service\.venv\Scripts\python.exe'
    if (-not (Test-Path $python)) { throw 'AI 虚拟环境不存在，请先按 ai-service/README.md 安装依赖。' }
    Start-Process -FilePath $python -ArgumentList '-m','uvicorn','app.main:app','--host','127.0.0.1','--port','8000' -WorkingDirectory (Join-Path $projectRoot 'ai-service') -WindowStyle Hidden
    $env:SEAFISH_DETECTION_PROVIDER = 'yolo-service'
    $env:SEAFISH_DETECTION_DEFAULT_MODEL = 'best'
}

Start-Process -FilePath $maven -ArgumentList 'spring-boot:run' -WorkingDirectory (Join-Path $projectRoot 'backend') -WindowStyle Hidden
Start-Process -FilePath 'npm.cmd' -ArgumentList 'run','dev' -WorkingDirectory (Join-Path $projectRoot 'frontend') -WindowStyle Hidden

Write-Host '蓝境项目已启动：'
Write-Host '前端：http://localhost:5173'
Write-Host '后端：http://localhost:8081/api/health'
if ($UseRealAi) { Write-Host 'AI 文档：http://localhost:8000/docs' }
