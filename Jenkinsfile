def component = [
    'front': true,
    'back': true,
    'nginx': true,
    'ai': true,
    

]

// 파일 복사 작업을 수행하는 함수 정의
def fileCopy() {
    // 파일 복사 작업 수행
    sh 'cp /var/jenkins_home/workspace/fullerting/submodule/*.yml /var/jenkins_home/workspace/fullerting/backend/src/main/resources'
    sh 'cp /var/jenkins_home/workspace/fullerting/submodule/.env /var/jenkins_home/workspace/fullerting/frontend'
}

pipeline {
    agent any
    environment {
        // 환경변수 설정
        NGINX_TAG = 'latest'
        FRONT_TAG = 'latest'
        BACK_TAG = 'latest'
        REDIS_TAG = 'alpine'
        DOCKER_USER_ID = 'junwon1131'
        // Docker Hub 및 GitHub 크리덴셜 ID
        DOCKER_HUB_CREDENTIALS_ID = 'Docker-hub'
        GITHUB_CREDENTIALS_ID = 'Github-access-token'
        GITLAB_CREDENTIALS_ID = 'GitLab-access-token' // GitLab 크리덴셜 ID 추가
        REPO = 's10-ai-image-sub2/S10P12C102'
        GIT_REPO = 'https://github.com/junwon9824/fullertingsecretfolder.git'

        // Gradle 환경 변수 설정
        ORG_GRADLE_JAVA_HOME = '/usr/lib/jvm/java-17-openjdk-amd64'
    }

    stages {
//         stage('Setup Environment') {
//             steps {
//                 dir("${env.WORKSPACE}/back") {
//                     script {
//                         sh 'ls . -al'
//                         // 테스트용 쉘 코드 추가
//                         sh 'echo "This is a test shell script"'
//
//   // 시크릿 파일 사용
//
//
//
//                     }
//                 }
//             }
//         }


        stage('Checkout') {
            steps {
                script {
                    sh 'pwd'
                    sh 'ls -al'

                    dir('submodule')
                    {

                        // GitHub access token을 사용하여 submodule을 가져옴
                        checkout([$class: 'GitSCM', branches: [[name: '*/main']], extensions: [[$class: 'SubmoduleOption', disableSubmodules: false, parentCredentials: true, recursiveSubmodules: true, reference: '', trackingSubmodules: false]], userRemoteConfigs: [[credentialsId: 'Github-access-token', url: GIT_REPO]]])
//                         sh 'git pull origin main'
                        sh 'echo "This is a test submodule script"'
//                         sh 'rm .env'
                        sh 'cat  application.yml'
                        sh 'pwd'
                        sh 'ls -al'

                    }

                      sh 'pwd'
                      sh 'ls -al'
                }
            }
        }

        stage('Copy Files') {
                    steps {
                        script {
                            // 파일 복사 디버깅 메시지
                            echo "Copying YAML files from submodule to src/main/resources..."

                            // 파일 복사 작업 수행
                            fileCopy()

                            // 파일 복사 완료 디버깅 메시지
                            echo "Copying completed."
                        }
                    }
                }

        stage('Build') {
            steps {
                script {
                    // 현재 디렉토리 위치와 파일 목록 출력을 backend 디렉토리 내에서 실행
                    dir('backend') {
                        sh 'pwd'  // 현재 디렉토리 위치 출력
                        sh 'ls -al'  // 디렉토리 내의 파일 목록 출력

                        // docker-compose가 설치되어 있는지 확인하고, 없으면 설치
                        sh '''
                        if ! command -v docker-compose &> /dev/null
                        then
                            echo "docker-compose not found, installing..."
                            sudo curl -L "https://github.com/docker/compose/releases/download/1.29.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
                            sudo chmod +x /usr/local/bin/docker-compose
                        else
                            echo "docker-compose is already installed."
                        fi
                        '''

                        // Docker Compose를 사용하여 서비스 빌드
                        sh 'docker-compose -f docker-compose.yml build'
                    }
                }
            }
        }

        stage('Docker Login') {
            steps {
                // Docker Hub 크리덴셜을 사용하여 Docker에 로그인
                withCredentials([usernamePassword(credentialsId: 'Docker-hub', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASSWORD')]) {
                    sh 'echo $DOCKER_PASSWORD | docker login --username $DOCKER_USER --password-stdin'
                }
            }
        }

        stage('Tag and Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'Docker-hub', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASSWORD')]) {
                     sh 'pwd'
                    sh 'docker-compose -f backend/docker-compose.yml push'
                }
            }
        }

        stage('Prune old images') {
            steps {
                script {
                    sh 'docker image prune --filter until=1h'
                }
            }
        }

        stage('Pull') {
            steps {
                script {
                    component.each { entry ->
                        if (entry.value && entry.key != 'redis') {
                            def var = entry.key
                            sh "docker-compose -f backend/docker-compose.yml -p develop-server pull ${var.toLowerCase()}"
                        }
                    }
                }
            }
        }
        
        stage('Down') {
            steps {
                script {
                    component.each { entry ->
                        if (entry.value) {
                            def var = entry.key
                            try {
                                sh "docker-compose -f backend/docker-compose.yml -p develop-server down ${var.toLowerCase()}"
                            } catch (Exception e) {
                                echo "Failed to down ${var.toLowerCase()}."
                            }
                        }
                    }
                }
            }
        }


        stage('Up') {
            steps {
                script {
                    component.each { entry ->
                        if (entry.value) {
                            def var = entry.key
                            try {
                                sh "docker-compose -f backend/docker-compose.yml -p develop-server up -d ${var.toLowerCase()}"
                            } catch (Exception e) {
                                // 'docker compose up -d' 명령이 실패한 경우
                                echo "Failed to up. Starting 'docker compose start'..."
                                sh "docker-compose -f backend/docker-compose.yml -p develop-server restart ${var.toLowerCase()}"
                            }
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            script {
                def Author_ID = sh(script: 'git show -s --pretty=%an', returnStdout: true).trim()
                def Author_Name = sh(script: 'git show -s --pretty=%ae', returnStdout: true).trim()
                mattermostSend(color: 'good',
                    message: "빌드 ${currentBuild.currentResult}: ${env.JOB_NAME} #${env.BUILD_NUMBER} by ${Author_ID}(${Author_Name})\n(<${env.BUILD_URL}|Details>)",
                    endpoint: 'https://meeting.ssafy.com/hooks/pbwfpcrqgff1zr8fmjzq7iukfr',
                    channel: 'C102-jenkins'
            )
            }
        }
    }
}
