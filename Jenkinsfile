pipeline {
    agent any
    tools {
        maven 'default'
        jdk '21'
    }
    parameters {
        booleanParam(name: 'core', defaultValue: true, description: 'Build tortuga-discord-core')
        booleanParam(name: 'jellyfin', defaultValue: false, description: 'Deploy tortuga-discord-jellyfin')
        booleanParam(name: 'music', defaultValue: true, description: 'Deploy tortuga-discord-music')
    }
    environment {
        TORTUGA_DISCORD_JELLYFIN_HOST = credentials('tortuga-discord-jellyfin-host')
        TORTUGA_DISCORD_MUSIC_HOST = credentials('tortuga-discord-music-host')
    }
    stages {
        stage('build core') {
            when { expression { return params.core } }
            steps {
                script {
                    sh 'mvn --version'
                    sh 'mvn -f core clean install'
                }
            }
        }
        stage('build jellyfin') {
            when { expression { return params.jellyfin } }
            steps {
                script {
                    sh 'mvn -f jellyfin clean package'
                }
            }
        }
        stage('deploy jellyfin') {
            when { expression { return params.jellyfin } }
            steps {
                script {
                    deploy('jellyfin', env.TORTUGA_DISCORD_JELLYFIN_HOST)
                }
            }
        }
        stage('build music') {
            when { expression { return params.music } }
            steps {
                script {
                    sh 'mvn -f music clean package'
                }
            }
        }
        stage('deploy music') {
            when { expression { return params.music } }
            steps {
                script {
                    deploy('music', env.TORTUGA_DISCORD_MUSIC_HOST)
                }
            }
        }
    }
}

def deploy(String project, String host) {
    sh "scp ${project}/target/tortuga-discord-${project}.jar ${host}:/opt/tortuga/discord/${project}"
    sh "ssh ${host} 'sudo systemctl restart tortuga-discord-${project}'"
}