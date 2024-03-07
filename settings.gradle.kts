pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

include(":mr-clean-runtime")
include(":mr-clean-processor")
include(":mr-clean-processor-core")
include(":mr-clean-debug-processor")
include(":mr-clean-plugin")
