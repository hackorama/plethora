echo on
setlocal

REM ***** Local Environment Variables ***********

REM The JRE java.exe to be used
set JAVAEXE="\Programs\jdk1.5.0_05\jre\bin\java.exe"

REM the eclipse home
set ECLIPSEHOME="\Programs\eclipse_330"

:: get path to equinox jar inside ECLIPSEHOME folder
for /f "delims= tokens=1" %%c in ('dir /B /S /OD %ECLIPSEHOME%\plugins\org.eclipse.equinox.launcher_*.jar') do set EQUINOXJAR=%%c
 
REM The location of your workspace (does not need to exist)
set WORKSPACE="\Programs\eclipse_330_headless\headless_arbitrary_src_1\workspace"

REM ****************************************************

if not exist %JAVAEXE% echo ERROR: incorrect java.exe=%JAVAEXE%, edit this file and correct the JAVAEXE envar
if not exist %JAVAEXE% goto done

if not exist %EQUINOXJAR% echo ERROR: incorrect startup.jar=%EQUINOXJAR%, edit this file and correct the STARTUPJAR envar
if not exist %EQUINOXJAR% goto done

:run
@echo on
%JAVAEXE% -jar %EQUINOXJAR% -clean -noupdate -application org.eclipse.ant.core.antRunner -data %WORKSPACE% -verbose -file headless.xml %* >headless_out.txt 2>&1

:done
pause
