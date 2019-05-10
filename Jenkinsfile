#!groovy
@Library('github.com/cloudogu/ces-build-lib@a4e0212')
import com.cloudogu.ces.cesbuildlib.*

node { // No specific label

    def mvnHome = tool 'M3'
    javaHome = tool 'JDK8'

    Maven mvn = new MavenLocal(this, mvnHome, javaHome)
    expectedVersion = mvn.version

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

            readsFromManifestInJarLocalJava()
            readsFromManifestInJarOpenJdk()

            //TODO test jar properties, website and REST api
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
String expectedVersion

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


void readsFromManifestInJarLocalJava() {
        testJarFromManifest()
}

void readsFromManifestInJarOpenJdk() {
    docker.image('openjdk:8u102-jre').inside {
        testJarFromManifest()
    }
}

void testJarFromManifest() {
    actualVersionNumber = java "-jar examples/jar-from-manifest/target/jar-*-jar-with-dependencies.jar"
    echo "Returned version number: ${actualVersionNumber}"
    assert actualVersionNumber.contains(expectedVersion)
}
