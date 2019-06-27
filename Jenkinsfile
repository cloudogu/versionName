#!groovy
@Library('github.com/cloudogu/ces-build-lib@a4e0212')
import com.cloudogu.ces.cesbuildlib.*

node { // No specific label

    def mvnHome = tool 'M3'
    javaHome = tool 'JDK8'

    mvn = new MavenLocal(this, mvnHome, javaHome)

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

            readsFromManifestInJar()
            readsFromManifestInJarOpenJdk()
            readsFromPropertiesInJar()
            readsFromGeneratedFileInJar()
            readsFromPropertiesInWar()
        }

        stage('Statical Code Analysis') {
            dir('versionName') { // Scan only the library module
                def sonarQube = new SonarCloud(this, [sonarQubeEnv: 'sonarcloud.io-cloudogu'])

                sonarQube.analyzeWith(mvn)

                if (!sonarQube.waitForQualityGateWebhookToBeCalled()) {
                    currentBuild.result = 'UNSTABLE'
                }
            }
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

def javaHome
def mvn

def java(def args) {
    withEnv(["PATH+EXTRA=${javaHome}/jre/bin"]) {
        return sh(returnStdout: true, script: "java ${args}")
    }
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
    return env.BRANCH_NAME == 'master' || env.BRANCH_NAME == 'develop'
}

private boolean isBuildSuccessful() {
    currentBuild.currentResult == 'SUCCESS' &&
        // Build result == SUCCESS seems not to set be during pipeline execution.
        (currentBuild.result == null || currentBuild.result == 'SUCCESS')
}

void initMaven(Maven mvn) {

    if ("master".equals(env.BRANCH_NAME)) {

        echo "Building master branch"
        mvn.additionalArgs += " -DperformRelease "
        currentBuild.description = mvn.getVersion()
    }
}


void readsFromManifestInJar() {
    echo "Test: readsFromManifestInJar"
    testJarFromManifest()
}

void readsFromManifestInJarOpenJdk() {
    echo "Test: readsFromManifestInJarOpenJdk"
    docker.image('openjdk:8u102-jre').inside {
        testJarFromManifest()
    }
}

void readsFromPropertiesInJar() {
    echo "Test: readsFromPropertiesInJar"
    testJarFromProperties()
}

void readsFromGeneratedFileInJar() {
    echo "Test: readsFromGeneratedFileInJar"

    testJarFromGeneratedFile()
}

void readsFromPropertiesInWar() {
    echo "Test: readsFromPropertiesInWarOpenJdk"

    uid = findUid()
    gid = findGid()

    docker.image('openjdk:8u102-jre').withRun(
        "-v ${WORKSPACE}:/v -w /v/examples " +
            // Run with Jenkins user, so the files created in the workspace by server can be deleted later
            "-u ${uid}:${gid} -e HOST_UID=${uid} -e HOST_GID=${gid} ",
        "java -jar server/target/server-${mvn.version}-jar-with-dependencies.jar") {
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
    echo "Expected version number: ${mvn.version}"
    assert actualVersionNumber.contains(mvn.version)
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
