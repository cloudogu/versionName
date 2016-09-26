versionName
===========

Examples on how to generate an extended version name with maven containing
- the version,
- the build date,
- branch and
- commit hash
- e.g. ``1.0-SNAPSHOT (2016-09-16 14:12, branch master, commit 0f300dd)``

Also shows how to read it from within applications (JAR or WAR), using different ways of reading the version name:
 - Manifest,
 - property or
 - HTML

## Build and run examples
- ``mvn clean package``
- ``java -jar jar/target/jar-1.0-SNAPSHOT-jar-with-dependencies.jar ``
- ``java -jar server/target/server-1.0-SNAPSHOT-jar-with-dependencies.jar``  
    [http://localhost:8080](http://localhost:8080)   
    [http://localhost:8080/api/version](http://localhost:8080/api/version)  

## Version name in manifest
- Write to manifest using maven, see [jar/pom.xml](jar/pom.xml)
- Read from manifest. For logic, see [VersionNames.getVersionNameFromProperties()](versionName/src/main/java/de/triology/versionname/VersionNames.java), example: [JAR](jar/src/main/java/de/triology/versionname/App.java)
- Run it: ``java -jar jar/target/jar-1.0-SNAPSHOT-jar-with-dependencies.jar ``

## Version name in properties file
- Write to properties using maven, see [war/pom.xml](war/pom.xml)
- Read from properties. For logic, see [VersionNames.getVersionNameFromProperties()](versionName/src/main/java/de/triology/versionname/VersionNames.java), example: [REST resource](war/src/main/java/de/triology/versionname/VersionResource.java)
- Run it: ``java -jar server/target/server-1.0-SNAPSHOT-jar-with-dependencies.jar``  
  [http://localhost:8080/api/version](http://localhost:8080/api/version)

## Version name in HTML
- "Inject" to HTML using maven, see [war/pom.xml](war/pom.xml)
- HTML see [index.html](war/src/main/webapp/index.html)
- Run it: ``java -jar server/target/server-1.0-SNAPSHOT-jar-with-dependencies.jar``  
  [http://localhost:8080](http://localhost:8080)  
