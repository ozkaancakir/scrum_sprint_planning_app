buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.2")
    }
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.3" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false // Add this line
}

// buildscript ve allprojects bölümlerini kaldırıyoruz
tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
