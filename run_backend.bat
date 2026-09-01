@echo off
echo Starting Personal University ALTER Backend on http://0.0.0.0:8000 ...
.\.venv\Scripts\python.exe -m uvicorn main:app --reload --host 0.0.0.0 --port 8000
pause
