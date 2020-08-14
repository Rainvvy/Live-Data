@echo off
for /r %%a in (*.java) do ren "%%a" "%%~na.txt"