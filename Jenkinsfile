pipeline {

    agent {
        kubernetes {
            yamlFile 'kubernetesPod.yaml'
        }
    }

    stages {
        stage('build RSB'){
            steps {
                container('maven') {
                     sh 'mvn -Pjavax-dependencies,tomcat-distribution clean package deploy'                   
                }
            }
        }
    }
   
}
