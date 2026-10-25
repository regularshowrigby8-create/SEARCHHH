@rem ---------------------------------------------------------------------------
@rem Gradle wrapper launcher (Windows). See gradlew for notes.
@rem ---------------------------------------------------------------------------
@echo off
setlocal

set APP_HOME=%~dp0

if defined JAVA_HOME (
    set "JAVACMD=%JAVA_HOME%\bin\java.exe"
) else (
    set "JAVACMD=java.exe"
)

if not exist "%APP_HOME%gradle\wrapper\gradle-wrapper.jar" (
    echo ERROR: gradle\wrapper\gradle-wrapper.jar is missing. 1>&2
    exit /b 1
)

"%JAVACMD%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% "-Dorg.gradle.appname=gradlew" -classpath "%APP_HOME%gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*

endlocal
