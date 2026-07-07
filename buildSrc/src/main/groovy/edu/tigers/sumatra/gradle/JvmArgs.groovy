package edu.tigers.sumatra.gradle

/**
 * Shared JVM-argument constants used by the Sumatra build.
 */
final class JvmArgs {
    private JvmArgs() {
    }

    static final List<String> APP_JVM_ARGS = [
            '-Dsun.java2d.d3d=false',
            '-Djava.net.preferIPv4Stack=true',
    ].asImmutable()

    /**
     * --add-opens flags required by the persistence layer to obtain VarHandles
     * for private fields of java.base / java.desktop classes via
     * MethodHandles.privateLookupIn(...), and to access ReflectionFactory
     * for synthesising serialization constructors.
     *
     * Used by the Gradle test task (sumatra.test), the launched application
     * (sumatra.application), and IntelliJ run-config templates (idea-ext).
     */
    static final List<String> PERSISTENCE_ADD_OPENS = [
            '--add-opens=java.base/java.lang=ALL-UNNAMED',
            '--add-opens=java.base/java.lang.invoke=ALL-UNNAMED',
            '--add-opens=java.base/java.lang.reflect=ALL-UNNAMED',
            '--add-opens=java.base/java.io=ALL-UNNAMED',
            '--add-opens=java.base/java.nio=ALL-UNNAMED',
            '--add-opens=java.base/java.util=ALL-UNNAMED',
            '--add-opens=java.base/java.util.concurrent=ALL-UNNAMED',
            '--add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED',
            '--add-opens=java.base/java.time=ALL-UNNAMED',
            '--add-opens=java.base/java.math=ALL-UNNAMED',
            '--add-opens=java.base/jdk.internal.reflect=ALL-UNNAMED',
            '--add-opens=java.desktop/java.awt=ALL-UNNAMED',
    ].asImmutable()

    static final List<String> NATIVE_ACCESS_ARGS = [
            '--enable-native-access=ALL-UNNAMED',
    ].asImmutable()

    static final List<String> ALL_JVM_ARGS = (APP_JVM_ARGS + PERSISTENCE_ADD_OPENS + NATIVE_ACCESS_ARGS).asImmutable()
}
