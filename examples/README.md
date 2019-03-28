versionName-examples
====================

Examples on how to generate an extended version name with maven and read it from within the application using the [version name library](https://github.com/cloudogu/versionName).
The examples show how to extended the version names with the following attributes
- the version,
- the build date,
- branch,
- commit hash,
- build number
- (and a special version name for releases)
- e.g. ``1.0.1-SNAPSHOT build #17 (2016-09-27T07:55:43Z, branch master, commit 4dd3cf5)``

See [parent pom.xml](pom.xml)

The build number is only written to version name, when the environment variable ``BUILD_NUMBER`` is defined. This works automatically in Jenkins, for example (see [Jenkinsfile](../Jenkinsfile)).

For releases a simple version name containing only the version number is created. This is activated using the property ``performRelease``. This is for example set when using the ``release:perform`` goal of the [maven-release-plugin](http://maven.apache.org/maven-release/maven-release-plugin), or can be set like so: ``mvn clean install -DperformRelease``.


The example also shows how to read the version name from within applications (JAR or WAR), using different ways of reading the version name:
 - Manifest,
 - property file or
 - HTML.

The example have been testes with Maven 3.3.9 and should work with Java 1.6+.

## Build and run examples
- ``mvn clean package``
- ``java -jar jar/target/jar-1.0.1-SNAPSHOT-jar-with-dependencies.jar ``
- ``java -jar server/target/server-1.0.1-SNAPSHOT-jar-with-dependencies.jar``  
    [http://localhost:8080](http://localhost:8080)  
    [http://localhost:8080/api/version](http://localhost:8080/api/version)

## Version name in manifest
- Write to manifest using maven, see [jar/pom.xml](jar/pom.xml)
- Read from manifest. For logic, see [VersionNames.getVersionNameFromManifest()](../versionName/src/main/java/com/cloudogu/versionname/VersionNames.java), example: [JAR](jar/src/main/java/com/cloudogu/versionname/App.java)
- Run it: ``java -jar jar/target/jar-1.0.1-SNAPSHOT-jar-with-dependencies.jar ``

## Version name in properties file
- Write to properties using maven, see [war/pom.xml](war/pom.xml)
- Read from properties. For logic, see [VersionNames.getVersionNameFromProperties()](../versionName/src/main/java/com/cloudogu/versionname/VersionNames.java), example: [REST resource](war/src/main/java/com/cloudogu/versionname/VersionResource.java)
- Run it: ``java -jar server/target/server-1.0.1-SNAPSHOT-jar-with-dependencies.jar``  
  [http://localhost:8080/api/version](http://localhost:8080/api/version)

## Version name in HTML
- "Inject" to HTML using maven, see [war/pom.xml](war/pom.xml)
- HTML see [index.html](war/src/main/webapp/index.html)
- Run it: ``java -jar server/target/server-1.0.1-SNAPSHOT-jar-with-dependencies.jar``  
  [http://localhost:8080](http://localhost:8080)
