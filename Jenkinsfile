pipeline {
    agent any

    parameters {
        choice(name: 'SUITE',
               choices: ['src/test/resources/testng.xml', 'src/test/resources/testng-smoke.xml'],
               description: 'TestNG suite to run')
        booleanParam(name: 'ZEPHYR_ENABLED', defaultValue: true, description: 'Send the results to Zephyr')
        booleanParam(name: 'EMAIL_ENABLED', defaultValue: true, description: 'Send the start and report emails')
    }

    options {
        timeout(time: 60, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '20'))
        disableConcurrentBuilds()
    }

    stages {
        stage('Run tests') {
            steps {
                script {
                    String command = "mvn -B clean test -DsuiteXmlFile=${params.SUITE} " +
                            "-Dzephyr.enabled=${params.ZEPHYR_ENABLED} -Demail.enabled=${params.EMAIL_ENABLED}"
                    if (isUnix()) {
                        sh command
                    } else {
                        bat command
                    }
                }
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: 'test-output/surefire-reports/*.xml'
            archiveArtifacts allowEmptyArchive: true, artifacts: 'test-output/ExtentReports/**, logs/*.log'
        }
    }
}
