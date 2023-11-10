plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
    alias(libs.plugins.vanniktech.publish)
}

dependencies {
    implementation(project(":mr-clean-processor-core"))
    implementation(project(":mr-clean-runtime"))
    ksp(libs.autoservice.ksp)
    implementation(libs.autoservice.annotations)

    implementation(libs.kotlin.stdlib)

    implementation(libs.kotlinPoet.core)
    implementation(libs.kotlinPoet.ksp)

    implementation(libs.ksp)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.zsweers.compileTesting.core)
    testImplementation(libs.zsweers.compileTesting.ksp)
}

ksp {
    arg("autoserviceKsp.verify", "true")
    arg("autoserviceKsp.verbose", "true")
}
