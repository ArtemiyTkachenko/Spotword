# Libraries (CameraX, ML Kit, Compose) ship their own consumer rules; add app rules here only
# for a demonstrated R8 problem.

# Keep line numbers so Play Console can deobfuscate crash stack traces with the mapping file.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ML Kit creates its component registrars reflectively through their no-argument constructors
# (MlKitInitProvider -> ComponentDiscovery). Without this, R8 removes the constructors and live
# text recognition fails on the first camera frame in release builds.
-keep class * implements com.google.firebase.components.ComponentRegistrar {
    <init>();
}
