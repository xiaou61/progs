$required = @('admin-web', 'miniapp', 'server', 'infra', 'docs')
$missing = $required | Where-Object { -not (Test-Path $_) }

if ($missing.Count -gt 0) {
  Write-Error ("Missing: " + ($missing -join ', '))
  exit 1
}

Write-Host "Workspace structure ok"
