ECHO OFF

for %%X in (mvn) do (set FOUND=%%~$PATH:X)
if not defined FOUND (
	echo Maven not found
	exit
)

cd %~dp0

mvn -pl modules/sumatra-main exec:java

