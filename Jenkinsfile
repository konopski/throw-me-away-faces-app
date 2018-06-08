try {
    runPipeline()
} catch(e) { 
    //TODO notify error
}

def JDBC_URL = "jdbc:oracle:thin:@172.16.2.107:1521:AOU" 

def runPipeline() {
    node('master') {
        stage('build-init'){
            checkout scm 
            bat "mvn -q -B clean"
        }
        stage('code-compile'){
            bat "mvn -q -B install -DskipTests"
        }
        stage('code-test-unit'){
            echo "time for testing !!!"
            bat "mvn -q -B test"
        }
        stage('code-test-integration'){
            echo "time for even more testing !!!"
        }
        stage('code-analyze'){
            echo "execute sonar plugin here ?"
        }
        stage('database-migrate'){
            echo """mvn -q -B -Djdbc.url=$JDBC_URL properties:read-project-properties liquibase:update """
        }
        stage('deploy-package'){
            echo "TODO deploy"
        }
    }
}
