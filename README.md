versionName
===========
[![Build Status](https://oss.cloudogu.com/jenkins/buildStatus/icon?job=cloudogu-github/versionName/develop)](https://oss.cloudogu.com/jenkins/blue/organizations/jenkins/cloudogu-github%2FversionName/branches/)
[![Quality Gates](https://sonarcloud.io/api/project_badges/measure?project=com.cloudogu.versionName%3AversionName&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.cloudogu.versionName%3AversionName)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=com.cloudogu.versionName%3AversionName&metric=coverage)](https://sonarcloud.io/dashboard?id=com.cloudogu.versionName%3AversionName)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=com.cloudogu.versionName%3AversionName&metric=sqale_index)](https://sonarcloud.io/dashboard?id=com.cloudogu.versionName%3AversionName)

versionName is a tiny Java library that allows for conveniently reading the version name of an application from

 - Manifest or
 - property file.
 
versionName consists of one class (as said before it's tiny) [VersionNames](versionName/src/main/java/com/cloudogu/versionname/VersionNames.java) that provides methods for reading the version name.
The public methods return a String that is never `null`. In case of error, messages are written to a [SLF4J](http://slf4j.org/)-logger.

- [VersionNames.getVersionNameFromProperties()](versionName/src/main/java/com/cloudogu/versionname/VersionNames.java),
- [VersionNames.getVersionNameFromManifest()](versionName/src/main/java/com/cloudogu/versionname/VersionNames.java)

To use versionName, either copy [VersionNames](versionName/src/main/java/com/cloudogu/versionName/VersionNames.java) to your classpath or add the [latest stable version](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%20%22com.cloudogu.versionName%22%20AND%20a%3A%22versionName%22) to the dependency management tool of your choice.

Read more about it in those two blog posts (Note that this refers to version 1.x and the maven coords and package names have changed since!)
- [Version names with Maven: Creating the version name](https://www.triology.de/en/blog-entries/versionsnamen-mit-maven-erzeugen-des-versionsnamens) (which refers to the [examples](examples)) and
- [Version names with Maven: Reading the version name](https://www.triology.de/en/blog-entries/version-names-with-maven-reading-the-version-name) (which refers to the library itself).

With maven for example
```XML
<dependency>
    <groupId>com.cloudogu.versionName</groupId>
    <artifactId>versionName</artifactId>
    <version>1.0.2</version>
</dependency>
```

[![Maven Central](https://img.shields.io/maven-central/v/com.cloudogu.versionName/versionName.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%20%22com.cloudogu.versionName%22%20AND%20a%3A%22versionName%22)

You can also get snapshot versions from our [snapshot repository](https://oss.sonatype.org/content/repositories/snapshots/com/cloudogu/versionName/versionName/) (for the most recent commit on develop branch).
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
