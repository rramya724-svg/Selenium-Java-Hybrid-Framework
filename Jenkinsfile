pipeline {
    agent any

    tools {
        maven 'Maven3.9.16'
    }

    parameters {
        choice(name: 'SUITE',
               choices: ['src/test/resources/testng.xml', 'src/test/resources/testng-smoke.xml'],
               description: 'TestNG suite to run')
        booleanParam(name: 'ZEPHYR_ENABLED', defaultValue: true, description: 'Send the results to Zephyr')
        booleanParam(name: 'ZEPHYR_NEW_ROW_PER_RUN', defaultValue: true,
                     description: 'Add a new Zephyr row for every run instead of overwriting the last one')
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
                sh "mvn -B clean test -Dheadless=true -DsuiteXmlFile=${params.SUITE} " +
                   "-Dzephyr.enabled=${params.ZEPHYR_ENABLED} " +
                   "-Dzephyr.update.existing.execution=${!params.ZEPHYR_NEW_ROW_PER_RUN} " +
                   "-Demail.enabled=${params.EMAIL_ENABLED}"
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: 'test-output/surefire-reports/*.xml'
            allure includeProperties: false, jdk: '', results: [[path: 'target/allure-results']]
            archiveArtifacts allowEmptyArchive: true, artifacts: 'test-output/ExtentReports/**, logs/*.log'
        }
    }
}