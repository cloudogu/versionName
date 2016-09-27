versionName
===========

Examples on how to generate an extended version name with maven containing
- the version,
- the build date,
- branch,
- commit hash and
- build number.
- e.g. ``1.0-SNAPSHOT build #17 (2016-09-27T07:55:43Z, branch master, commit 4dd3cf5)``

See [parent pom.xml](pom.xml)
The build number is only written to version name, when the environment variable ``BUILD_NUMBER`` is defined. This works automatically in Jenkins, for example (see [Jenkinsfile](Jenkinsfile)).


These example also show how to read it from within applications (JAR or WAR), using different ways of reading the version name:
 - Manifest,
 - property or
 - HTML

These examples have been testes with Maven 3.3.9.

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
