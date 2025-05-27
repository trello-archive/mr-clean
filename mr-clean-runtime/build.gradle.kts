plugins{
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.vanniktech.publish)
}
repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.stdlib)
}

