#!groovy
@Library('github.com/cloudogu/ces-build-lib@1.63.0')
import com.cloudogu.ces.cesbuildlib.*

node { // No specific label

    javaImage = 'eclipse-temurin:8-jdk-alpine'
    Maven mvn = new MavenWrapperInDocker(this, addGitToImage(javaImage))
    // Sonar stopped support for JRE8 for its client, so for now we run the analysis in a separate container.
    // Once the lib is upgraded to JDK11 this can be removed
    String SonarJreImage = 'eclipse-temurin:11-jdk-alpine'

    catchError {

        stage('Checkout') {
            checkout scm
        }

        initMaven(mvn)

        stage('Build') {
            mvn 'clean package -DskipTests'

            archiveArtifacts '**/target/*.*ar'
        }

        stage('Test') {
            mvn'test'
        }

        stage('Integration Test') {

            expectedVersion = mvn.version
            readsFromManifestInJar()
            readsFromPropertiesInJar()
            readsFromGeneratedFileInJar()
            readsFromPropertiesInWar()
        }

        stage('Statical Code Analysis') {
            def sonarQube = new SonarCloud(this, [sonarQubeEnv: 'sonarcloud.io-cloudogu'])

            // Running SQ with JDK11 when the app requires JDK8 is tricky.
            // Enable, once the app is migrated to run with JDK11
/*            sonarQube.analyzeWith(mvn)

            if (!sonarQube.waitForQualityGateWebhookToBeCalled()) {
                currentBuild.result = 'UNSTABLE'
            }*/
        }

        stage('Deploy') {
            if (preconditionsForDeploymentFulfilled()) {

                mvn.setDeploymentRepository('ossrh', 'https://oss.sonatype.org/', 'mavenCentral-acccessToken')

                mvn.setSignatureCredentials('mavenCentral-secretKey-asc-file',
                    'mavenCentral-secretKey-Passphrase')

                mvn.deployToNexusRepositoryWithStaging()
            }
        }
    }
    // Archive JUnit results, if any
    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/TEST-*.xml'

    mailIfStatusChanged(findEmailRecipients(env.EMAIL_RECIPIENTS))
}

def mvn
String javaImage

/** Our maven build needs git binary for some examples */
def addGitToImage(String image) {
    withTempDir {
        writeFile file: 'Dockerfile', text: "FROM ${image}\nRUN apk add git --no-cache"
        return docker.build("${env.BUILD_TAG}-${image}-git".toLowerCase()).imageName()
    }
}

void withTempDir(Closure body) {
    dir( "${env.BUILD_TAG}-${System.currentTimeMillis()}" ) {
        try {
            body()
        } finally {
            deleteDir()
        }
    }
}

def java(def args) {
    return sh(returnStdout: true, script: "java ${args}")
}

boolean preconditionsForDeploymentFulfilled() {
    if (isBuildSuccessful() &&
        !isPullRequest() &&
        shouldBranchBeDeployed()) {
        return true
    } else {
        echo "Skipping deployment because of branch or build result: currentResult=${currentBuild.currentResult}, " +
            "result=${currentBuild.result}, branch=${env.BRANCH_NAME}."
        return false
    }
}

private boolean shouldBranchBeDeployed() {
    return env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'develop'
}

private boolean isBuildSuccessful() {
    currentBuild.currentResult == 'SUCCESS' &&
        // Build result == SUCCESS seems not to set be during pipeline execution.
        (currentBuild.result == null || currentBuild.result == 'SUCCESS')
}

void initMaven(Maven mvn) {

    if ("main".equals(env.BRANCH_NAME)) {

        echo "Building main branch"
        mvn.additionalArgs += " -DperformRelease "
        currentBuild.description = mvn.getVersion()
    }
}

void readsFromManifestInJar() {
    echo "Test: readsFromManifestInJar"
    docker.image(javaImage).inside {
        testJarFromManifest()
    }
}

void readsFromPropertiesInJar() {
    echo "Test: readsFromPropertiesInJar"
    docker.image(javaImage).inside {
        testJarFromProperties()
    }
}

void readsFromGeneratedFileInJar() {
    echo "Test: readsFromGeneratedFileInJar"

    docker.image(javaImage).inside {
        testJarFromGeneratedFile()
    }
}

void readsFromPropertiesInWar() {
    echo "Test: readsFromPropertiesInWar"

    uid = findUid()
    gid = findGid()

    docker.image(javaImage).withRun(
        "-v ${WORKSPACE}:/v -w /v/examples " +
            // Run with Jenkins user, so the files created in the workspace by server can be deleted later
            "-u ${uid}:${gid} -e HOST_UID=${uid} -e HOST_GID=${gid} ",
        "java -jar server/target/server-${expectedVersion}-jar-with-dependencies.jar") {
        serverContainer ->
            echo "serverContainer: ${serverContainer.id}"

            def inspect = sh (returnStdout: true, script: "docker inspect ${serverContainer.id}")
            echo "inspect: ${inspect}"
            def logs = sh (returnStdout: true, script: "docker logs ${serverContainer.id}")
            echo "logs: ${logs}"

            def serverIp = findContainerIp(serverContainer)
            // Make sure server Is Up
            sleep(time: 5, unit: 'SECONDS')
            actualVersionNumber = sh(script: "curl http://${serverIp}:8080/api/version", returnStdout: true).trim()
            assertVersionNumber(actualVersionNumber)
    }
}

void testJarFromManifest() {
    actualVersionNumber = java "-jar examples/jar-from-manifest/target/jar-*-jar-with-dependencies.jar"
    assertVersionNumber(actualVersionNumber)
}

void testJarFromProperties() {
    actualVersionNumber = java "-jar examples/jar-from-properties/target/jar-*-jar-with-dependencies.jar"
    assertVersionNumber(actualVersionNumber)
}

void testJarFromGeneratedFile() {
    actualVersionNumber = java "-jar examples/jar-without-deps/target/jar-*-jar-with-dependencies.jar"
    assertVersionNumber(actualVersionNumber)
}

private void assertVersionNumber(actualVersionNumber) {
    echo "Returned version number: ${actualVersionNumber}"
    echo "Expected version number: ${expectedVersion}"
    assert actualVersionNumber.contains(expectedVersion)
}

String findContainerIp(container) {
    sh (returnStdout: true,
        script: "docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' ${container.id}")
        .trim()
}

String findUid() {
    sh (returnStdout: true,
        script: 'id -u')
        .trim()
}
String findGid() {
    sh (returnStdout: true,
        script: 'id -g')
        .trim()
}

String expectedVersion
