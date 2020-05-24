pipeline {

    agent {
        kubernetes {
            yamlFile 'kubernetesPod.yaml'
        }
    }
    
    environment {
        RSB_VERSION = sh(returnStdout: true,
          script: "mvn -q -Dexec.executable=echo -Dexec.args='${project.version}' --non-recursive exec:exec").trim()
    }
    
    options {
        buildDiscarder(logRotator(numToKeepStr: '3'))
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
                                       filePath: '/target/rsb.war']],
                       mavenCoordinate: [artifactId: 'rsb', 
                                       groupId: 'eu.openanalytics',
                                       packaging: 'war',
                                       version: "${env.RSB_VERSION}"]
                       ],
                       [$class: 'MavenPackage', 
                       mavenAssetList: [[classifier: 'tomcat-distribution', 
                                         extension: 'zip', 
                                         filePath: '/target/rsb-*-tomcat-distribution.zip']],
                       mavenCoordinate: [artifactId: 'rsb', 
                                         groupId: 'eu.openanalytics',
                                         packaging: 'zip',
                                         version: "${env.RSB_VERSION}"]
                       ]
                    ]
                } 
            }
        }
    }
}
