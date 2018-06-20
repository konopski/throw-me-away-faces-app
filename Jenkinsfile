
class TurboPipeline implements Serializable {
    def JDBC_URL = "jdbc:oracle:thin:@172.16.2.107:1521:AOU"

    def hudson

    TurboPipeline(hudson) {
        this.hudson = hudson
    }

    def turboRun(Closure runPipeline) {
        try {
            hudson.timestamps { runPipeline() }
        } catch(e) {
            //TODO notify error
            throw e
        }

    }
}

def turbo = new TurboPipeline(this)

turbo.turboRun({

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
            echo """mvn -q -B -Djdbc.url=${turbo.JDBC_URL} properties:read-project-properties liquibase:update """
        }
        stage('package-build'){
            bat "mvn -q -B install -DskipTests -s my-settings.xml"
        }
        stage('package-deploy'){
            echo "TODO deploy"
        }
    }
})


