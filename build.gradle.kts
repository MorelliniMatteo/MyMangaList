// File di configurazione a livello di progetto per tutte le sottoproject/modules.
plugins {
    id("com.android.application") version "8.2.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false // Aggiorniamo Kotlin a 1.9.10
    id("com.google.devtools.ksp") version "1.9.10-1.0.13" apply false // Aggiorniamo KSP per allinearlo a Kotlin 1.9.10
}
