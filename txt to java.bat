@echo off
for /r %%a in (*.txt) do ren "%%a" "%%~na.java"