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
        rsbVersion = sh(returnStdout: true, script: "mvn -q -Dexec.executable=echo -Dexec.args='${project.version}' --non-recursive exec:exec").trim()
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
                                       version: '6.4.0-SNAPSHOT']
                       ],
                       [$class: 'MavenPackage', 
                       mavenAssetList: [[classifier: 'tomcat-distribution', 
                                         extension: 'zip', 
                                         filePath: '/target/rsb-*-tomcat-distribution.zip']],
                       mavenCoordinate: [artifactId: 'rsb', 
                                         groupId: 'eu.openanalytics',
                                         packaging: 'zip',
                                         version: '6.4.0-SNAPSHOT']
                       ]
                    ]
                } 
            }
        }
    }
}
