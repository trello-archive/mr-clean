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
