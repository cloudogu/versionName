versionName
===========
[![Build Status](https://opensource.triology.de/jenkins/buildStatus/icon?job=triologygmbh-github/versionName/develop)](https://opensource.triology.de/jenkins/blue/organizations/jenkins/triologygmbh-github%2FversionName/branches/)
[![Quality Gates](https://sonarcloud.io/api/badges/gate?key=de.triology.versionName%3AversionName)](https://sonarcloud.io/dashboard?id=de.triology.versionName%3AversionName)
[![Coverage](https://sonarcloud.io/api/badges/measure?key=de.triology.versionName%3AversionName&metric=coverage)](https://sonarcloud.io/dashboard?id=de.triology.versionName%3AversionName)
[![Technical Debt](https://sonarcloud.io/api/badges/measure?key=de.triology.versionName%3AversionName&metric=sqale_debt_ratio)](https://sonarcloud.io/dashboard?id=de.triology.versionName%3AversionName)

versionName is a tiny Java library that allows for conveniently reading the version name of an application from

 - Manifest or
 - property file.
 
Read more about it in those two blog posts
- [Version names with Maven: Creating the version name](https://www.triology.de/en/blog-entries/versionsnamen-mit-maven-erzeugen-des-versionsnamens) (which refers to the [examples](examples)) and
- [Version names with Maven: Reading the version name](https://www.triology.de/en/blog-entries/version-names-with-maven-reading-the-version-name) (which refers to the library itself).

versionName consists of one class (as said before it's tiny) [VersionNames](versionName/src/main/java/de/triology/versionname/VersionNames.java) that provides methods for reading the version name.
The public methods return a String that is never `null`. In case of error, messages are written to a [SLF4J](http://slf4j.org/)-logger.

- [VersionNames.getVersionNameFromProperties()](versionName/src/main/java/de/triology/versionname/VersionNames.java),
- [VersionNames.getVersionNameFromManifest()](versionName/src/main/java/de/triology/versionname/VersionNames.java)

To use versionName, either copy [VersionNames](versionName/src/main/java/de/triology/versionName/VersionNames.java) to your classpath or add the [latest stable version](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%20%22de.triology.versionName%22%20AND%20a%3A%22versionName%22) to the dependency management tool of your choice.

With maven for example
```XML
<dependency>
    <groupId>de.triology.versionName</groupId>
    <artifactId>versionName</artifactId>
    <version>1.0.2</version>
</dependency>
```

[![Maven Central](https://img.shields.io/maven-central/v/de.triology.versionName/versionName.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%20%22de.triology.versionName%22%20AND%20a%3A%22versionName%22)

You can also get snapshot versions from our [snapshot repository](https://oss.sonatype.org/content/repositories/snapshots/de/triology/versionName/versionName/) (for the most recent commit on develop branch).
To do so, add the following repo to your `pom.xml` or `settings.xml`:
```xml
<repository>
    <id>snapshots-repo</id>
    <url>https://oss.sonatype.org/content/repositories/snapshots</url>
    <releases><enabled>false</enabled></releases>
    <snapshots><enabled>true</enabled></snapshots>
</repository>
```
# Examples
The examples show how to write a version name to your application using maven and how it can be read using the library from within applications (JAR or WAR).
See [examples/README.md](examples/README.md)

# Jenkins
Running [Jenkinsfile](Jenkinsfile) with the [pipeline plugin](https://wiki.jenkins-ci.org/display/JENKINS/Pipeline+Plugin) (tested with version 2.4) requires
- A JDK defined as  Jenkins tool (see [Jenkinsfile](Jenkinsfile) for name of JDK tool)
- Maven defined as Jenkins tool (see [Jenkinsfile](Jenkinsfile) for name of Maven tool)
- Optional: You can add a build parameter `RECIPIENTS` that contains a comma-separated list of all email recipients
