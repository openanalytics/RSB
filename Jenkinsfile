pipeline {

    agent {
        kubernetes {
            yamlFile 'kubernetesPod.yaml'
        }
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '3'))
    }

    environment {
        rsbVersion = sh(returnStdout: true, script: '''grep --max-count=2 \'<version>\' pom.xml | awk -F \'>\' \'{ print $2 }\' | awk -F \'<\' \'{ print $1 }\' | tail -n 1''').trim()
    }
    
    stages {
        
        stage('mvn build'){
            steps {
                container('maven') {
                     sh 'mvn -Pjavax-dependencies,tomcat-distribution -Dmaven.test.skip=true clean package'                   
                }
            }
        }
        
        stage('publish to nexus') {
             steps {
                 container('maven') {
             
                     nexusPublisher nexusInstanceId: 'nexus', nexusRepositoryId: 'snapshots', 
                       packages: [
                   
                       [$class: 'MavenPackage', 
                       mavenAssetList: [[classifier: '', 
                                       extension: 'war', 
                                       filePath: 'target/rsb.war']],
                       mavenCoordinate: [artifactId: 'rsb', 
                                       groupId: 'eu.openanalytics',
                                       packaging: 'war',
                                       version: '']
                       ],
                       [$class: 'MavenPackage', 
                       mavenAssetList: [[classifier: 'tomcat-distribution', 
                                         extension: 'zip', 
                                         filePath: "target/rsb-${env.rsbVersion}-tomcat-distribution.zip"]],
                       mavenCoordinate: [artifactId: 'rsb', 
                                         groupId: 'eu.openanalytics',
                                         packaging: 'zip',
                                         version: '']
                       ]
                    ]
                } 
            }
        }
    }
}
