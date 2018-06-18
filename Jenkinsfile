try {
    runPipeline()
} catch(e) { 
    //TODO notify error
    throw e
}

def JDBC_URL = "jdbc:oracle:thin:@172.16.2.107:1521:AOU" 

def runPipeline() {
    node('master') {
        stage('build-init'){
            checkout scm 
            currentBuild.displayName = readMavenPom().version
            bat "mvn -q -B clean"
        }
        stage('code-compile'){
            bat "mvn -q -B compile -DskipTests -s my-settings.xml"
        }
        stage('code-test-unit'){
            echo "time for testing !!!"
            bat "mvn -q -B test -s my-settings.xml"
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
        stage('package-build'){
            bat "mvn -q -B install -DskipTests -s my-settings.xml"
        }
        stage('package-deploy'){
            echo "TODO deploy"
        }
    }
}
