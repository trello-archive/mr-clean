buildscript {
    allprojects {
        group = providers.gradleProperty("GROUP").get()
        version = providers.gradleProperty("VERSION_NAME").get()
    }
}

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.vanniktech.publish) apply false
    alias(libs.plugins.versions)
    alias(libs.plugins.com.android.application) apply false
    alias(libs.plugins.org.jetbrains.kotlin.android) apply false
}
