Write-Host "Starting Personal University ALTER Backend on http://0.0.0.0:8000 ..." -ForegroundColor Cyan
& ".\.venv\Scripts\python.exe" -m uvicorn main:app --reload --host 0.0.0.0 --port 8000
