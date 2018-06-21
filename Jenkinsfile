
class TurboPipeline implements Serializable {
    def JDBC_URL = "jdbc:oracle:thin:@172.16.2.107:1521:AOU"
    def credentialsId = "konopski"

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

    def prepareNextVersion(String currVersion) {
        def versionComponents = currVersion.replace("-SNAPSHOT", "").split("\\.")
        versionComponents[-1] = Integer.getInteger(versionComponents.last(), 0) + 1
        versionComponents.join(".")
    }

    def commitAndTag(String version) {
        hudson.sshagent(credentials: [credentialsId]) {
            hudson.bat """git add pom.xml"""
            hudson.bat """git commit -m "bump version to ${version}" && git push"""
            hudson.bat """git tag -a ${version} -m "version ${version}" && git push --tags"""
        }
    }
}

def turbo = new TurboPipeline(this)

turbo.turboRun({

    node('master') {
        stage('build-init'){
            checkout scm
            def oldVersion = readMavenPom().version
            echo "old version: $oldVersion"
            def version = turbo.prepareNextVersion(oldVersion)
            echo "updating to version: $version"
            currentBuild.displayName = version
            bat "mvn -q -B versions:set -DnewVersion=${version} -s my-settings.xml"
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
        stage('build-finalize'){
            def version = readMavenPom().version
            def credentialsId = "konopski"

            sshagent(credentials: [credentialsId]) {
                bat """git add pom.xml"""
                bat """git commit -m "bump version to ${version}" && git push"""
                bat """git tag -a ${version} -m "version ${version}" && git push --tags"""
            }
        }
    }
})


