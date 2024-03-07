plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.vanniktech.publish)
}

dependencies {

    implementation(project(":mr-clean-runtime"))
    implementation(libs.kotlin.stdlib)

    implementation(libs.kotlinPoet.core)
    implementation(libs.kotlinPoet.ksp)

    implementation(libs.ksp)

    testImplementation(libs.junit)
}

