versionName
===========
[![Build Status](https://opensource.triology.de/jenkins/buildStatus/icon?job=triologygmbh-github/versionName/master)](https://opensource.triology.de/jenkins/job/triologygmbh-github/job/versionName/job/master/)
[![Current Version jitpack](https://jitpack.io/v/triologygmbh/versionName.svg)](https://jitpack.io/#triologygmbh/versionName)



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

To use versionName, either copy [VersionNames](versionName/src/main/java/de/triology/versionName/VersionNames.java) to your classpath or use the dependency resolution tool of your choice.
With maven for example, add the jitpack repo

```XML
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
and the versionName dependency
```XML
<dependency>
    <groupId>com.github.triologygmbh.versionName</groupId>
    <artifactId>versionName</artifactId>
    <version>1.0.0</version>
</dependency>
```

# Examples
The examples show how to write a version name to your application using maven and how it can be read using the library from within applications (JAR or WAR).
See [examples/README.md](examples/README.md)

# Jenkins
Running [Jenkinsfile](Jenkinsfile) with the [pipeline plugin](https://wiki.jenkins-ci.org/display/JENKINS/Pipeline+Plugin) (tested with version 2.4) requires
- A JDK defined as  Jenkins tool (see [Jenkinsfile](Jenkinsfile) for name of JDK tool)
- Maven defined as Jenkins tool (see [Jenkinsfile](Jenkinsfile) for name of Maven tool)
- Optional: You can add a build parameter `RECIPIENTS` that contains a comma-separated list of all email recipients
