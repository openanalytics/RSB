pipeline {

    agent {
        kubernetes {
            yamlFile 'kubernetesPod.yaml'
        }
    }

    options {
        authorizationMatrix(['hudson.model.Item.Build:rsb', 'hudson.model.Item.Read:rsb'])
        buildDiscarder(logRotator(numToKeepStr: '3'))
    }
    
    stages {
        
        stage('build and deploy to nexus'){
        
            steps {
            
                container('maven') {
                     
                     configFileProvider([configFile(fileId: 'maven-settings-rsb', variable: 'MAVEN_SETTINGS_RSB')]) {
                         
                         sh 'mvn -s $MAVEN_SETTINGS_RSB -Pjavax-dependencies,ldap,tomcat-distribution -Dmaven.test.skip=true clean package deploy'
                         
                     }
                }
            }
        }
    }
}
