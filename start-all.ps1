# ==================================================
# 中州养老管理系统 一键启动脚本
# 编码必须保存为 UTF-8 with BOM，否则 PowerShell 5.1 会按 GBK 解码导致语法错误
# 用法：在 PowerShell 中执行  .\start-all.ps1
# 访问：http://localhost    账号：admin / admin123
# ==================================================

$Root       = "D:\sxunkaifa\xitongkaifa"
$RedisDir   = "D:\中州养老项目资料\中州养老项目资料\资料\Redis-x64-5.0.14.1"
$RedisExe   = "$RedisDir\redis-server.exe"
$RedisConf  = "$RedisDir\redis.windows.conf"
$JavaExe    = "D:\idea2022\jdk11\jdk11\bin\java.exe"
$BackendJar = "$Root\zhongzhou-backend\ruoyi-admin\target\ruoyi-admin.jar"
$UiDir      = "$Root\zhongzhou-ui"

function Test-PortUp($Port) {
    return (Test-NetConnection -ComputerName localhost -Port $Port -InformationLevel Quiet -WarningAction SilentlyContinue)
}

function Wait-Port($Port, $Name, $TimeoutSec) {
    $deadline = (Get-Date).AddSeconds($TimeoutSec)
    while ((Get-Date) -lt $deadline) {
        if (Test-PortUp $Port) { Write-Host "  [OK] $Name 已就绪 ($Port)" -ForegroundColor Green; return $true }
        Start-Sleep -Seconds 2
    }
    Write-Host "  [WARN] $Name 启动超时 ($Port)" -ForegroundColor Yellow
    return $false
}

# 取监听端口对应的进程号
function Get-PortProcessId($Port) {
    $conn = Get-NetTCPConnection -State Listen -LocalPort $Port -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($null -eq $conn) { return $null }
    return $conn.OwningProcess
}

# 取进程命令行（用于识别“到底跑的是哪个前端”）
function Get-ProcessCommandLine($ProcessId) {
    if ($null -eq $ProcessId) { return "" }
    $proc = Get-CimInstance Win32_Process -Filter "ProcessId=$ProcessId" -ErrorAction SilentlyContinue
    if ($null -eq $proc) { return "" }
    return [string]$proc.CommandLine
}

# 判断 80 端口是否为本次项目的 Vite 服务
function Test-FrontendOwnedByProject {
    $frontPid = Get-PortProcessId 80
    if ($null -eq $frontPid) { return $false }
    return (Get-ProcessCommandLine $frontPid) -match "zhongzhou-ui"
}

function Write-Check($Name, $Pass, $Detail) {
    $flag = if ($Pass) { "[PASS]" } else { "[FAIL]" }
    $color = if ($Pass) { "Green" } else { "Red" }
    Write-Host ("  {0} {1}{2}" -f $flag, $Name, $(if ($Detail) { " -> $Detail" } else { "" })) -ForegroundColor $color
}

Write-Host "===== 1/4 启动 Redis 6379 =====" -ForegroundColor Green
if (Test-PortUp 6379) {
    Write-Host "  [OK] Redis 已在运行" -ForegroundColor DarkGray
} else {
    if (-not (Test-Path $RedisExe)) { Write-Host "  [ERROR] 找不到 $RedisExe" -ForegroundColor Red; exit 1 }
    Start-Process -FilePath $RedisExe -ArgumentList "`"$RedisConf`"" -WorkingDirectory $RedisDir -WindowStyle Hidden
    Write-Host "  [START] Redis 启动中..." -ForegroundColor Cyan
    Wait-Port 6379 "Redis" 15 | Out-Null
}

Write-Host "===== 2/4 启动 MySQL 3306 =====" -ForegroundColor Green
$svc = Get-Service -Name "MySQL80" -ErrorAction SilentlyContinue
if ($null -eq $svc) {
    Write-Host "  [ERROR] 未找到 MySQL80 服务" -ForegroundColor Red
} elseif ($svc.Status -ne "Running") {
    Start-Service "MySQL80"
    Write-Host "  [START] MySQL80 服务已启动" -ForegroundColor Cyan
    Wait-Port 3306 "MySQL" 30 | Out-Null
} else {
    Write-Host "  [OK] MySQL80 服务运行中" -ForegroundColor DarkGray
}

Write-Host "===== 3/4 启动后端 Java 8080 =====" -ForegroundColor Green
if (Test-PortUp 8080) {
    Write-Host "  [OK] 后端已在运行" -ForegroundColor DarkGray
} else {
    if (-not (Test-Path $JavaExe))    { Write-Host "  [ERROR] 找不到 $JavaExe" -ForegroundColor Red; exit 1 }
    if (-not (Test-Path $BackendJar)) { Write-Host "  [ERROR] 找不到 $BackendJar，请先 Maven 打包" -ForegroundColor Red; exit 1 }
    Start-Process -FilePath $JavaExe -ArgumentList "-jar", "`"$BackendJar`"" -WorkingDirectory $Root -WindowStyle Minimized
    Write-Host "  [START] 后端启动中（约 30 秒）..." -ForegroundColor Cyan
    Wait-Port 8080 "后端" 90 | Out-Null
}

Write-Host "===== 4/4 启动前端 80 =====" -ForegroundColor Green
# 关键：80 端口可能被素材目录的旧 Vue CLI 前端占用，页面看似正常实为旧系统
$frontReady = $false
$frontPid = Get-PortProcessId 80
if ($null -ne $frontPid) {
    $frontCmd = Get-ProcessCommandLine $frontPid
    if ($frontCmd -match "zhongzhou-ui") {
        Write-Host "  [OK] 前端已在运行（本项目，PID $frontPid）" -ForegroundColor DarkGray
        $frontReady = $true
    } elseif ($frontCmd -match "vue-cli-service" -or $frontCmd -match "RuoYi-Vue-master") {
        Write-Host "  [FIX] 80 端口被素材目录旧前端占用（PID $frontPid），正在停止..." -ForegroundColor Cyan
        Stop-Process -Id $frontPid -Force -ErrorAction SilentlyContinue
        Start-Sleep -Seconds 2
    } else {
        Write-Host "  [ERROR] 80 端口被其他进程占用（PID $frontPid）" -ForegroundColor Red
        Write-Host "         $frontCmd" -ForegroundColor DarkGray
    }
}

if (-not $frontReady -and -not (Test-PortUp 80)) {
    if (-not (Test-Path "$UiDir\package.json")) { Write-Host "  [ERROR] 找不到 $UiDir" -ForegroundColor Red; exit 1 }
    Start-Process -FilePath "powershell.exe" -ArgumentList "-NoExit", "-Command", "cd '$UiDir'; npm run dev" -WorkingDirectory $UiDir -WindowStyle Minimized
    Write-Host "  [START] 前端启动中..." -ForegroundColor Cyan
    Wait-Port 80 "前端" 60 | Out-Null
} elseif (-not $frontReady) {
    Write-Host "  [WARN] 80 端口仍被占用，已跳过前端启动；请手动结束后重试" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "================ 服务状态 ================" -ForegroundColor Cyan
$redisUp    = Test-PortUp 6379
$mysqlUp    = Test-PortUp 3306
$backendUp  = Test-PortUp 8080
$frontendUp = Test-PortUp 80
$frontOwned = Test-FrontendOwnedByProject

Write-Check "Redis   (6379)" $redisUp ""
Write-Check "MySQL   (3306)" $mysqlUp ""
Write-Check "后端    (8080)" $backendUp ""
Write-Check "前端    (80)  " ($frontendUp -and $frontOwned) $(if ($frontendUp -and -not $frontOwned) { "端口被非本项目进程占用" } else { "" })

Write-Host ""
Write-Host "================ 链路自检 ================" -ForegroundColor Cyan
# 1) 后端直连（排除代理因素）
$backendApiOk = $false
if ($backendUp) {
    try {
        $direct = Invoke-WebRequest -Uri "http://localhost:8080/captchaImage" -UseBasicParsing -TimeoutSec 10
        $backendApiOk = ($direct.StatusCode -eq 200)
    } catch { $backendApiOk = $false }
}
Write-Check "后端 API 直连" $backendApiOk ""

# 2) 前端 Vite 代理透传（页面实际使用的链路）
$proxyOk = $false
$captchaEnabled = $null
if ($frontendUp -and $frontOwned) {
    try {
        $viaProxy = Invoke-WebRequest -Uri "http://localhost/dev-api/captchaImage" -UseBasicParsing -TimeoutSec 10
        if ($viaProxy.StatusCode -eq 200) {
            $proxyOk = $true
            $captchaEnabled = ($viaProxy.Content | ConvertFrom-Json).captchaEnabled
        }
    } catch { $proxyOk = $false }
}
Write-Check "前端 /dev-api 代理" $proxyOk $(if ($proxyOk) { "http://localhost -> 8080" } else { "" })

# 3) 登录页可访问
$loginPageOk = $false
if ($frontendUp) {
    try {
        $page = Invoke-WebRequest -Uri "http://localhost" -UseBasicParsing -TimeoutSec 10
        $loginPageOk = ($page.StatusCode -eq 200)
    } catch { $loginPageOk = $false }
}
Write-Check "前端页面可访问" $loginPageOk "http://localhost"

Write-Host ""
Write-Host "================ 启动结果 ================" -ForegroundColor Cyan
$allOk = $redisUp -and $mysqlUp -and $backendUp -and $frontendUp -and $frontOwned -and $backendApiOk -and $proxyOk -and $loginPageOk
if ($allOk) {
    Write-Host "  全部自检通过，可以开始演示" -ForegroundColor Green
} else {
    Write-Host "  存在未通过项，请按上面的 [FAIL] 逐项处理" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "  浏览器访问: http://localhost" -ForegroundColor Cyan
Write-Host "  登录账号  : admin / admin123    机构账号: chunhui / admin123" -ForegroundColor Cyan
if ($null -ne $captchaEnabled) {
    if ($captchaEnabled) {
        Write-Host "  验证码状态: 已启用（登录页需填写验证码）" -ForegroundColor Yellow
    } else {
        Write-Host "  验证码状态: 已关闭（录屏演示模式）" -ForegroundColor Yellow
    }
}
Write-Host "=========================================" -ForegroundColor Cyan
