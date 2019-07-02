ECHO OFF

cd %~dp0

set args=%*

set CLASSPATH="modules/sumatra-main/target/lib/*"
set JAVA_OPTS="-Xms64m -Xmx4G -server -Xnoclassgc -Xverify:none -Dsun.java2d.d3d=false -XX:+UseConcMarkSweepGC -Djava.net.preferIPv4Stack=true -XX:-OmitStackTraceInFastThrow -XX:+AggressiveOpts"

java %JAVA_OPTS% edu.tigers.sumatra.Sumatra %args%
