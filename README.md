# Package Identifier

A gradle plugin for generating package identifiers in your modules. 
This plugin isn't meant for direct usage in runtime code, but rather to help your libraries be module aware.

Every module in a project will generate a file that looks like this: 

```kotlin
     // Generated, do not modify!
     package com.trello.sample
     
     import com.trello.identifier.annotation.PackageId
     
     @PackageId(isDebug = false)
     class PackageIdentifier
     
```

and another like this:


```kotlin
     // Generated, do not modify!
     package com.trello.sample
     
     import com.trello.identifier.annotation.PackageId
     
     @PackageId(isDebug = true)
     class PackageIdentifier
     
```

Annotation processors can then use the enclosing package of the class annotated by `@PackageId` in order to generate files into module specific packages.
Additionally, parsing the `isDebug` field allows you to handle debug variants differently from release variants.

