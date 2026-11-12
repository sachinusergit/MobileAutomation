pipeline {
    agent any

    tools {
        jdk 'jdk-27'
        maven 'maven'
    }

    parameters {
        choice(name: 'SUITE', choices: ['all-tests', 'android', 'e2e', 'filters', 'search', 'categories'],
                description: 'Which suite to run')
    }

    options {
        timeout(time: 60, unit: 'MINUTES')
        disableConcurrentBuilds()
        timestamps()
    }

    environment {
        JAVA_HOME = 'C:\\Users\\sachi\\.jdks\\openjdk-27'
        ANDROID_HOME = 'C:\\Users\\sachi\\AppData\\Local\\Android\\Sdk'
        ANDROID_SDK_ROOT = 'C:\\Users\\sachi\\AppData\\Local\\Android\\Sdk'
        ADB = 'C:\\Users\\sachi\\AppData\\Local\\Android\\Sdk\\platform-tools\\adb.exe'
        APP_PACKAGE = 'com.meesho.supply'
        APP_ACTIVITY = 'com.meesho.supply/.default'
        VIDEO_ENABLED = 'false'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                bat 'mvnw.cmd -B clean test-compile'
            }
        }

        stage('Start Appium') {
            steps {
                bat '''
                    where appium >nul 2>&1
                    if errorlevel 1 (
                        echo Appium CLI not found on PATH; install with: npm install -g appium
                        exit /b 1
                    )
                    start /b appium --address 127.0.0.1 --port 4723 --log-level info > appium.log 2>&1
                    ping -n 10 127.0.0.1 >nul
                '''
            }
        }

        stage('Test') {
            steps {
                script {
                    def tag = ['filters': '@filters', 'search': '@search', 'categories': '@categories'].get(params.SUITE)
                    def cmd
                    if (params.SUITE == 'all-tests') {
                        cmd = 'mvnw.cmd -B test -Pall-tests -Dvideo.recording.enabled=%VIDEO_ENABLED% -Dmaven.test.failure.ignore=true'
                    } else if (params.SUITE == 'android') {
                        cmd = 'mvnw.cmd -B test -Pandroid -Dvideo.recording.enabled=%VIDEO_ENABLED% -Dmaven.test.failure.ignore=true'
                    } else if (params.SUITE == 'e2e') {
                        cmd = 'mvnw.cmd -B test -Pe2e -Dvideo.recording.enabled=%VIDEO_ENABLED% -Dmaven.test.failure.ignore=true'
                    } else {
                        cmd = "mvnw.cmd -B test -Pandroid -Dcucumber.filter.tags=\"${tag}\" -Dvideo.recording.enabled=%VIDEO_ENABLED% -Dmaven.test.failure.ignore=true"
                    }
                    bat cmd
                }
            }
        }

        stage('Publish Results') {
            steps {
                archiveArtifacts artifacts: 'target/allure-results/**', allowEmptyArchive: true
                archiveArtifacts artifacts: 'target/screenshots/**', allowEmptyArchive: true
                archiveArtifacts artifacts: 'target/logs/**', allowEmptyArchive: true
                script {
                    try {
                        allure([
                            includeProperties: false,
                            jdk: '',
                            results: [[path: 'target/allure-results']]
                        ])
                    } catch (Exception e) {
                        echo "Allure plugin not installed or generation failed: ${e.message}"
                    }
                }
            }
            post {
                always {
                    junit allowEmptyResults: true, skipMarkingBuildUnstable: true, testResults: 'target/surefire-reports/**/*.xml'
                }
            }
        }
    }

    post {
        always {
            bat 'taskkill /F /IM node.exe 2>nul || exit /b 0'
        }
        success {
            echo 'Mobile automation suite PASSED'
        }
        failure {
            echo 'Mobile automation suite FAILED'
        }
    }
}
