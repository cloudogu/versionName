versionName
===========

versionName is a tiny Java library that allows for conveniently reading the version name of an application from

 - Manifest or
 - property file.

It consists of one class (as said before it's tiny) [VersionNames](versionName/src/main/java/de/triology/versionname/VersionNames.java) that provides methods for reading the version name.
The public methods return a String that is never `null`. In case of error, messages are written to a [SLF4J](http://slf4j.org/)-logger.

- [VersionNames.getVersionNameFromProperties()](versionName/src/main/java/de/triology/versionname/VersionNames.java),
- [VersionNames.getVersionNameFromManifest()](versionName/src/main/java/de/triology/versionname/VersionNames.java)

# Examples
The examples show how to write a version name to your application using maven and how it can be read using the library from within applications (JAR or WAR).
See [examples/README.md](examples/README.md)

# Jenkins
Running [Jenkinsfile](Jenkinsfile) with the [pipeline plugin](https://wiki.jenkins-ci.org/display/JENKINS/Pipeline+Plugin) (tested with version 2.4) requires
- A JDK defined as  Jenkins tool (see [Jenkinsfile](Jenkinsfile) for name of JDK tool)
- Maven defined as Jenkins tool (see [Jenkinsfile](Jenkinsfile) for name of Maven tool)
- Optional: You can add a build parameter `RECIPIENTS` that contains a comma-separated list of all email recipients
