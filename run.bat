ECHO OFF

set args=%*

echo "Sumatra not installed, running with Gradle"
IF [%1] == [] (
    gradlew run
) ELSE (
    gradlew run --args="%args%"
)

