#!groovy
@Library('github.com/cloudogu/ces-build-lib@b5ce9f1')
import com.cloudogu.ces.cesbuildlib.*

node { // No specific label

    def mvnHome = tool 'M3'
    javaHome = tool 'JDK8'

    Maven mvn = new MavenLocal(this, mvnHome, javaHome)

    catchError {

        stage('Checkout') {
            checkout scm
        }

        initMaven(mvn)

        stage('Build') {
            mvn 'clean install'
        }

        stage('Print version number') {
            java "-jar examples/jar/target/jar-${mvn.getVersion()}-jar-with-dependencies.jar"
        }

        stage('Deploy') {
            if (preconditionsForDeploymentFulfilled()) {

                mvn.additionalArgs += ' -pl versionName ' // Deploy only the lib itself

                mvn.setDeploymentRepository('ossrh', 'https://oss.sonatype.org/', 'de.triology-mavenCentral-acccessToken')

                mvn.setSignatureCredentials('de.triology-mavenCentral-secretKey-asc-file',
                    'de.triology-mavenCentral-secretKey-Passphrase')

                mvn.deployToNexusRepositoryWithStaging()
            }
        }
    }
    // Archive JUnit results, if any
    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/TEST-*.xml'

    mailIfStatusChanged(findEmailRecipients(env.EMAIL_RECIPIENTS))
}

def javaHome

def java(def args) {
    withEnv(["PATH+EXTRA=${javaHome}/jre/bin"]) {
        sh "java ${args}"
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
        mvn.additionalArgs =+ " -DperformRelease "
        currentBuild.description = mvn.getVersion()
    }
}
