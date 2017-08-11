#!groovy

node { // No specific label

    mvnHome = tool 'M3'
    javaHome = tool 'JDK8'
   
    catchError {
        stage('Checkout') {
            checkout scm
        }

        stage('Build') {
            mvn 'clean install'
        }

        stage('Print version number') {
            java "-jar examples/jar/target/jar-${mavenVersion()}-jar-with-dependencies.jar"
        }
    }
    // Archive JUnit results, if any
    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/TEST-*.xml'
    // Send mail on failure
    step([$class: 'Mailer', recipients: env.RECIPIENTS, notifyEveryUnstableBuild: true, sendToIndividuals: true])
}

def mvnHome
def javaHome

def mvn(def args) {
    // Apache Maven related side notes:
    // --batch-mode : recommended in CI to inform maven to not run in interactive mode (less logs)
    // -V : strongly recommended in CI, will display the JDK and Maven versions in use.
    //      Very useful to be quickly sure the selected versions were the ones you think.
    // -U : force maven to update snapshots each time (default : once an hour, makes no sense in CI).
    // -Dsurefire.useFile=false : useful in CI. Displays test errors in the logs directly (instead of
    //                            having to crawl the workspace files to see the cause).

    // Advice: don't define M2_HOME in general. Maven will autodetect its root fine.
    withEnv(["JAVA_HOME=${javaHome}", "PATH+MAVEN=${mvnHome}/bin:${env.JAVA_HOME}/bin"]) {
        sh "${mvnHome}/bin/mvn ${args} --batch-mode -V -U -e -Dsurefire.useFile=false"
    }
}

def java(def args) {
    withEnv(["PATH=${javaHome}/jre/bin"+':$PATH']) {
        sh "java ${args}"
    }
}

def mavenVersion() {
    def matcher = readFile('pom.xml') =~ '<version>(.+)</version>'
    matcher ? matcher[0][1] : null
}
