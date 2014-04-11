@echo off
rem -------------------------------------------------------------------------
rem GateIn Portal Setup
rem -------------------------------------------------------------------------
rem

rem $Id$

@if not "%ECHO%" == "" echo %ECHO%
@if "%OS%" == "Windows_NT" setlocal

if "%OS%" == "Windows_NT" (
  set "DIRNAME=%~dp0%"
) else (
  set DIRNAME=.\
)

pushd %DIRNAME%..
set "RESOLVED_JBOSS_HOME=%CD%"
popd

if "x%JBOSS_HOME%" == "x" (
  set "JBOSS_HOME=%RESOLVED_JBOSS_HOME%"
)

pushd "%JBOSS_HOME%"
set "SANITIZED_JBOSS_HOME=%CD%"
popd

if "%RESOLVED_JBOSS_HOME%" NEQ "%SANITIZED_JBOSS_HOME%" (
    echo WARNING JBOSS_HOME may be pointing to a different installation - unpredictable results may occur.
)

set DIRNAME=

if "%OS%" == "Windows_NT" (
  set "PROGNAME=%~nx0%"
) else (
  set "PROGNAME=jdr.bat"
)

rem Setup JBoss specific properties
if "x%JAVA_HOME%" == "x" (
  set JAVA=java
  echo JAVA_HOME is not set. Unexpected results may occur.
  echo Set JAVA_HOME to the directory of your local JDK to avoid this message.
) else (
  set "JAVA=%JAVA_HOME%\bin\java"
)

rem Find jboss-modules.jar, or we can't continue
if exist "%JBOSS_HOME%\jboss-modules.jar" (
    set "RUNJAR=%JBOSS_HOME%\jboss-modules.jar"
) else (
  echo Could not locate "%JBOSS_HOME%\jboss-modules.jar".
  echo Please check that you are in the bin directory when running this script.
  goto END
)

rem Setup JBoss specific properties

rem Setup the java endorsed dirs
set JBOSS_ENDORSED_DIRS=%JBOSS_HOME%\lib\endorsed

rem Set default module root paths
if "x%JBOSS_MODULEPATH%" == "x" (
  set "JBOSS_MODULEPATH=%JBOSS_HOME%\modules\system\layers\gatein"
)

"%JAVA%" ^
    -cp "%JBOSS_MODULEPATH%\org\gatein\lib\main\*;%JBOSS_MODULEPATH%\org\gatein\common\main\*" ^
    org.gatein.portal.installer.PortalSetupCommand ^
    -f "%JBOSS_HOME%\standalone\configuration\gatein\configuration.properties" ^
     %*

"%JAVA%" ^
     -cp "%JBOSS_HOME%/bin/migration^
     ;%JBOSS_HOME%/modules/system/layers/gatein/org/gatein/lib/main/*^
     ;%JBOSS_HOME%/modules/system/layers/gatein/org/picketlink/idm/main/*^
     ;%JBOSS_HOME%/modules/system/layers/base/com/h2database/h2/main/*^
     ;%JBOSS_HOME%/modules/system/layers/base/org/slf4j/main/*^
     ;%JBOSS_HOME%/modules/system/layers/base/org/hibernate/main/*^
     ;%JBOSS_HOME%/modules/system/layers/base/org/hibernate/commons-annotations/main/*^
     ;%JBOSS_HOME%/modules/system/layers/base/org/antlr/main/*^
     ;%JBOSS_HOME%/modules/system/layers/base/javax/persistence/api/main/*^
     ;%JBOSS_HOME%/modules/system/layers/base/org/dom4j/main/*^
     ;%JBOSS_HOME%/modules/system/layers/base/org/jboss/logging/main/*^
     ;%JBOSS_HOME%/modules/system/layers/base/org/javassist/main/*^
     ;%JBOSS_HOME%/modules/system/layers/base/javax/transaction/api/main/*^
     ;%JBOSS_HOME%/gatein/gatein.ear/portal.war/WEB-INF/conf/organization/picketlink-idm/*" ^
     org.exoplatform.services.organization.idm.DisabledUserMigrationScript migration.configuration ^
     %*

:END
if "x%NOPAUSE%" == "x" pause
