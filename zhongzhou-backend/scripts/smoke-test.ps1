# ============================================================
# 家属端接口巡检脚本
# 流程：发送验证码 -> 登录 -> 核心 GET 接口冒烟
# 用法：powershell -ExecutionPolicy Bypass -File scripts/smoke-test.ps1
#       powershell -ExecutionPolicy Bypass -File scripts/smoke-test.ps1 -BaseUrl http://localhost:8080 -Phone 13822220001
# ============================================================
param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$Phone = "13822220001"
)

$ErrorActionPreference = "Stop"
$script:pass = 0
$script:fail = 0
$results = New-Object System.Collections.ArrayList

function Invoke-Check {
    param(
        [string]$Name,
        [scriptblock]$Action
    )
    try {
        $value = & $Action
        $script:pass++
        [void]$results.Add([pscustomobject]@{ 结果 = "PASS"; 接口 = $Name; 说明 = "" })
        return $value
    }
    catch {
        $script:fail++
        [void]$results.Add([pscustomobject]@{ 结果 = "FAIL"; 接口 = $Name; 说明 = $_.Exception.Message })
        return $null
    }
}

function Invoke-Api {
    param(
        [string]$Method,
        [string]$Path,
        [hashtable]$Headers = @{},
        $Body = $null
    )
    $uri = "$BaseUrl$Path"
    $params = @{
        Uri         = $uri
        Method      = $Method
        Headers     = $Headers
        TimeoutSec  = 15
        ContentType = "application/json; charset=utf-8"
    }
    if ($null -ne $Body) {
        $params["Body"] = ($Body | ConvertTo-Json -Compress)
    }
    $resp = Invoke-RestMethod @params
    if ($resp.code -ne 200) {
        throw "业务码 $($resp.code): $($resp.msg)"
    }
    return $resp.data
}

Write-Host "巡检地址: $BaseUrl" -ForegroundColor Cyan
Write-Host "登录手机号: $Phone" -ForegroundColor Cyan
Write-Host ""

# 1. 发送验证码
$sendCode = Invoke-Check "POST /family/auth/sendCode" {
    Invoke-Api -Method "POST" -Path "/family/auth/sendCode" -Body @{ phone = $Phone }
}
$demoCode = $null
if ($sendCode) { $demoCode = $sendCode.demoCode }

# 2. 登录
$loginData = $null
if ($demoCode) {
    $loginData = Invoke-Check "POST /family/auth/login" {
        Invoke-Api -Method "POST" -Path "/family/auth/login" -Body @{ phone = $Phone; code = $demoCode }
    }
}
else {
    $script:fail++
    [void]$results.Add([pscustomobject]@{ 结果 = "FAIL"; 接口 = "POST /family/auth/login"; 说明 = "未获取到演示验证码，跳过登录" })
}

$token = $null
if ($loginData) { $token = $loginData.token }
$authHeaders = @{ "X-Family-Token" = [string]$token }

# 3. 需要登录的核心接口
$elders = @(Invoke-Check "GET /family/elders" {
    Invoke-Api -Method "GET" -Path "/family/elders" -Headers $authHeaders
})
[void](Invoke-Check "GET /family/bindings" {
    Invoke-Api -Method "GET" -Path "/family/bindings" -Headers $authHeaders
})
[void](Invoke-Check "GET /family/messages" {
    Invoke-Api -Method "GET" -Path "/family/messages" -Headers $authHeaders
})
[void](Invoke-Check "GET /family/visits" {
    Invoke-Api -Method "GET" -Path "/family/visits" -Headers $authHeaders
})
[void](Invoke-Check "GET /family/checkins" {
    Invoke-Api -Method "GET" -Path "/family/checkins" -Headers $authHeaders
})

$elderId = $null
if ($elders.Count -gt 0 -and $null -ne $elders[0]) { $elderId = $elders[0].elderId }

if ($elderId) {
    [void](Invoke-Check "GET /family/services" {
        Invoke-Api -Method "GET" -Path "/family/services?elderId=$elderId" -Headers $authHeaders
    })
    [void](Invoke-Check "GET /family/bills" {
        Invoke-Api -Method "GET" -Path "/family/bills?elderId=$elderId" -Headers $authHeaders
    })
    [void](Invoke-Check "GET /family/reports" {
        Invoke-Api -Method "GET" -Path "/family/reports?elderId=$elderId" -Headers $authHeaders
    })
    [void](Invoke-Check "GET /family/deposits" {
        Invoke-Api -Method "GET" -Path "/family/deposits?elderId=$elderId" -Headers $authHeaders
    })
    [void](Invoke-Check "GET /family/health-records/recent" {
        Invoke-Api -Method "GET" -Path "/family/health-records/recent?elderId=$elderId&days=7" -Headers $authHeaders
    })
}
else {
    Write-Host "当前账号没有绑定老人，跳过 elderId 相关接口检查" -ForegroundColor Yellow
}

# 4. 公开接口
[void](Invoke-Check "GET /family/public/service-items" {
    Invoke-Api -Method "GET" -Path "/family/public/service-items"
})
$rooms = @(Invoke-Check "GET /family/public/rooms" {
    Invoke-Api -Method "GET" -Path "/family/public/rooms"
})
if ($rooms.Count -gt 0 -and $null -ne $rooms[0]) {
    $roomId = $rooms[0].roomId
    [void](Invoke-Check "GET /family/public/rooms/{id}" {
        Invoke-Api -Method "GET" -Path "/family/public/rooms/$roomId"
    })
}

# 5. 输出结果
Write-Host ""
$results | Format-Table -AutoSize

$total = $script:pass + $script:fail
Write-Host "巡检完成：$($script:pass)/$total 通过，$($script:fail) 失败" -ForegroundColor $(if ($script:fail -eq 0) { "Green" } else { "Red" })
if ($script:fail -gt 0) { exit 1 }
exit 0
