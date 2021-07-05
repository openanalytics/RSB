String packageProfiles= 'javax-dependencies,ldap,tomcat-distribution';

pipeline {
	
	agent {
		kubernetes {
			yamlFile 'kubernetesPod.yaml'
			defaultContainer 'rpooli-build'
		}
	}
	
	options {
		authorizationMatrix(['hudson.model.Item.Build:rsb', 'hudson.model.Item.Read:rsb'])
		buildDiscarder(logRotator(numToKeepStr: '10'))
	}
	
	stages {
		
		stage('build + deploy artifacts') {
			steps {
				configFileProvider([configFile(fileId: 'maven-settings-rsb', variable: 'MAVEN_SETTINGS_RSB')]) {
					sh "mvn package deploy\
							-P ${packageProfiles}\
							--batch-mode -s $MAVEN_SETTINGS_RSB\
							-Dmaven.test.failure.ignore=true"
				}
			}
		}
		
		stage('generate + deploy site') {
			steps {
				configFileProvider([configFile(fileId: 'maven-settings-rsb', variable: 'MAVEN_SETTINGS_RSB')]) {
					sh "mvn -f rsb/ site-deploy\
							-P ${packageProfiles}\
							--batch-mode -s $MAVEN_SETTINGS_RSB"
				}
			}
		}
		
		stage('run integration tests') {
			steps {
				sh "export JENKINS_NODE_COOKIE=dontKillMe R_HOME=/usr/lib/R &&\
					mvn -f it-rpooli/ jetty:run-war\
						--batch-mode > it-rpooli/jetty-rpooli.out 2>&1 &"
				sh "sleep 2 && wget --tries=60 --waitretry=1 --retry-connrefused --output-document /dev/null\
						http://127.0.0.1:8889/rpooli/"
				
				sh "mvn verify -P it,javax-dependencies\
						--batch-mode\
						-Dmaven.test.failure.ignore=true"
				
				sh "mvn -f it-rpooli/ jetty:stop\
						--batch-mode"
			}
		}
		
	}
	
	post {
		always {
			archiveArtifacts(
					artifacts: '**/target/dependency-*.txt',
					fingerprint: true )
			
			junit '**/target/surefire-reports/*.xml'
			junit '**/target/failsafe-reports/*.xml'
			archiveArtifacts artifacts: 'it-rpooli/*.out'
		}
	}
	
}
