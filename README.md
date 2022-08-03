
                                                                                
                                                                                
                                    . .....                                     
                                 .=OO:....$ZZ..                                 
                                .O........,~=+Z.                                
                               =Z ..,:~=~~=+???Z.                               
                              .Z .,+++++++???I7IZ.                              
                              .O.~7??????????7$7Z.                              
                              .?.+ZO+~,???..+Z.$Z.                              
                              .,.OZOOOZI?IOZ$ZII7.                              
                            .O,7,O7,+~Z=??:I7?ZI7+Z.                            
                            .O7+.$=~=+O++??+==~=7ZO.                            
                            .ZZ..IIIOOZI?IIO7+=IIZO.                            
                             .O..?O$ZI7?????7$II7Z.                             
                              OZ.,77Z=$$$I?I??7?ZOO                             
                              .Z..O$ZIZZ$????=I7ZO.                             
                               O...O~Z+~:=+II$$7~                               
                                Z,Z:+.OZ7ZZ$7Z?O.                               
                                O.ZOZOOOOOZ7I+?O...                             
                               .Z.$OZZOO77$7+=?Z.O~.                            
                        ......OO..$OZOOZ7I$?~+?=$Z...                           
                 .,OZZO$,.... .,...OOOOI???++++O. =..ZZZZOO+...                 
              .=OI.            .   .OZ$???$OZ. .,~    ..,:~~~=ZO.               
             .O..                                         ..:===OO.             
            ,Z..                                             ,===:O.            
           .O.,.     ..                                      .~===,Z            
          .Z.,,.OZOZZOOZZ.                               ... .,====:Z           
         .Z..,O..?...++=?=Z.                           ...~.  .,::==Z:.         
        .O:...Z...?...=?+++IO..      ....+ZZOOO.....  .Z:+:.......~==O.         
        .Z..,:,ZZ+:.+Z7?????=O,IOZZZO..,+??????++++=ZZ,~OOI??++7OZ===~Z.        
        Z..OOI. .IZ$ZZI......   ..:+???????????????????+7Z7??~~+++ZZ==O.        
        O.O.,+  ..,~=+???~.,=?????????????????IZZ7????II?I$??==+++IZOO~Z        
        OO. .,~??????????????????????+++?I$Z77ZZZZOOOZZ7I?+??I??++?$$?ZO        
        O:.:7$I???????????????II?+++?IZZZ7ZO?$ZZZZ7+======+++++===?77?$O        
        .O+ZZZ$III7$ZZZZZZ7I?+++?I$OOO$O$??????++++++++??????????+?I?+O.        
        .OZOOOOOOZZ$$7I?++++?I$ZOOO$ZZ??+????????????????????????????$.         
        ,ZZOOOOZ$I?????????$ZOOZ7+~:,....,I7II??+++==~:,..,+?????????Z.         
        .OZZZZOZZZZ+,..... :::~...Z$$.$$.=$II$$=........$$.,==++++??+Z.         
         OOZZZZOOO$.$$..$..=  .$$$$$=,$=.,......$$$Z.$7.$$.ZIIII????+O.         
          OZZZ$ZZZ+.$$.~$.$. .$$$   .$$..I$ZZ.Z$.Z$.$$..$.$IOOOOOOZOOO          
           .,ZOOZZ:~.$$IZ$...$$$    .$$.$$.$7=$..$$.$$ $Z.ZZZZZ$$7ZO,           
                  .$.$.I.. .$$$.    $$.$$$Z..$$.$$=,$..$$.O?$OZZZZ..            
                  ...7OOO..$$$.    .$$.$$  ..$$$7$.7,...,Z.$~                   
                 ..       .$$$.    .$$.$$$$,......OZZ,.                         
                          .$$$.  ..$$......ZO.$..                               
                           Z$$$$$$Z..OZ....                                     
                          ..?$$$..OZ                                            
                            $OOOZ,.                                             
                                                                                
                                                                                
                                                    
Mr. Clean
===

Let Mr. Clean keep your logs clean of sensitive data.

Imagine you have the following model

```kotlin
data class SensitiveData(val creditCardNumber: String, val socialSecurityNumber: String)
```

This model might be inadvertently logged, leaking sensitive data to the world. 
One way to get around this is to override `toString` and manage your state.

```kotlin
data class SensitiveData(val creditCardNumber: String, val socialSecurityNumber: String) {
	override toString(): String {
		if (BuildConfig.DEBUG) {
			return "SensitiveData(
				creditCardNumber='$creditCardNumber'
				socialSecurityNumber='$socialSecurityNumber'
				)"
		}
		else {
			return "SensitiveData@${Integer.toHexString(hashCode())}"
		}
	}
}
```

But now you have to make sure this stays updated when you add/remove properties.

**Enter Mr. Clean**

Annotate your class with `@Sanitize` and delegate to the generated `sanitizedToString` function:

```kotlin
@Sanitize
data class SensitiveData(val creditCardNumber: String, val socialSecurityNumber: String) {
	override toString() = sanitizedToString()
}

```
**Note**: You don't have to build to get this function! The Gradle plugin will generate a default `Any.sanitizedToString` for use in the IDE.

Mr. Clean manages the implementation of the `toString` for you.

```kotlin
// in a debuggable build
inline fun SensitiveData.sanitizedToString(condition: Boolean): String =
        "SensitiveData(creditCardNumber = $creditCardNumber, socialSecurityNumber = $socialSecurityNumber)"

// in a non-debuggable build
inline fun SensitiveData.sanitizedToString(condition: Boolean): String = "SensitiveData@${Integer.toHexString(hashCode())}"
```

Don't leak sensitive info ever again, trust in Mr. Clean.

# Usage
```groovy
buildscript {
  repositories {
    mavenCentral()
    google()
   }
  dependencies {
    classpath 'com.trello.mrclean:mr-clean-plugin:1.2.0'
  }
}
```

and then apply to your modules
```groovy
apply plugin: 'com.trello.mrclean'

```


# Contributors

Pull requests, issues and comments welcome. For pull requests:

* Add tests for new features and bug fixes
* Follow the existing style
* Separate unrelated changes into multiple pull requests

See the existing issues for things to start contributing.

For bigger changes, make sure you start a discussion first by creating an issue and explaining the intended change.

Atlassian requires contributors to sign a Contributor License Agreement, known as a CLA. This serves as a record stating that the contributor is entitled to contribute the code/documentation/translation to the project and is willing to have it used in distributions and derivative works (or is willing to transfer ownership).

Prior to accepting your contributions we ask that you please follow the appropriate link below to digitally sign the CLA. The Corporate CLA is for those who are contributing as a member of an organization and the individual CLA is for those contributing as an individual.

* [CLA for corporate contributors](https://na2.docusign.net/Member/PowerFormSigning.aspx?PowerFormId=e1c17c66-ca4d-4aab-a953-2c231af4a20b)
* [CLA for individuals](https://na2.docusign.net/Member/PowerFormSigning.aspx?PowerFormId=3f94fbdc-2fbe-46ac-b14c-5d152700ae5d)
    
# License

Copyright (c) 2018 Atlassian and others. Apache 2.0 licensed, see LICENSE file.
