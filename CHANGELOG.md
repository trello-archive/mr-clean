# 2.0.0
* Breaking revision - KSP support and support for KAPT has been dropped
* 1.2.2 should still be used for users using KAPT as only KSP will be supported moving forward
* mr-clean-annotations has been renamed mr-clean-runtime
* mr-clean-debug-processor is used to generate the debug implementation of the root function
* mr-clean-processor will generate the sanitized version of strings

# 1.2.2
* Updates Kotlin to 1.9.0
* Updates Gradle to 8.1.0
* Updates ben-manes versions plugin to 0.45.0
* Updates google auto service to 1.1.1
* Updates incap to 1.0.0

# 1.2.1
* Updates Kotlin to 1.8.10
* Updates Android Gradle Plugin to 7.4.1
* Updates ben-manes versions plugin to 0.45.0
* Updated to use new Gradle variant API internally

# 1.2.0
* Updates Kotlin to 1.7.10
* Updates ben-manes versions plugin to 0.42.0
* Updates google auto service to 1.0.1
* Updates kotlinpoet to 1.12.0
* Updates junit to 4.13.2
* Updates metadata-jvm to 0.5.0
* Removes JCenter
* Updates deprecated methods

# 1.1.0
* Updates Kotlin to 1.5.10
* Updates android gradle plugin to 4.2.2
* Updates metadata-jvm to 0.3.0
* Updates kotlinpoet to 1.9.0

# 1.0.3

* Actually _actually_ support incremental annotation processing
* Drops usage of package-identifier plugin 

# 1.0.1

* Actually support incremental annotation processing
    
# 1.0.0

* support incremental annotation processing

# 0.9.5

* generate root function with `internal` modifier to rid ourselves of cross module woes

# 0.9.4

* Uses %P for template generation in kotlinpoet
* Updates tests to check for the generated file, not the funspec

# 0.9.3

* Fixes code-gen in debug builds
* Adds tests to verify code gen

# 0.9.2

* Registers the generation task for creating the root function on configure
* Updates package identifier to 0.0.2

# 0.9.1

* Update kotlinpoet to 1.0.0
* Refactor packages to all be `mrclean`

# 0.9.0

* initial release
