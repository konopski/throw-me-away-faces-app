import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.Files

class Propagated implements Serializable {

    final String email = "konopski@throw.away.me"
    final String user = "Lukasz Konopski"

    def hudson
    String directoryName
    String remoteRepository
    String outputCredentialsId

    Command command

    Propagated(
            hudson,
            Command command,
            String directoryName,
            String remoteRepository,
            String outputCredentialsId) {
        this.hudson = hudson
        this.directoryName = directoryName
        this.remoteRepository = remoteRepository
        this.outputCredentialsId = outputCredentialsId
        this.command = command
    }

    def moveToOutputDir() {
        Path fromPath = Paths.get(
                hudson.pwd(), 'original', directoryName
        ).normalize()
        Path toPath = Paths.get(
                hudson.pwd(), 'output', directoryName
        ).normalize()
        Files.move(fromPath, toPath)
    }

    def initGitRepo() {

        hudson.sshagent(credentials: [outputCredentialsId]) {
            command.emit("""git init""")
            command.emit("""git config user.email ${email}""")
            command.emit("""git config user.name ${user}""")
            command.emit("""git config pull.rebase true""")
            command.emit("""git remote add origin ${remoteRepository}""")
            command.emit("""git add .""")
            command.emit("""git commit -m "next build" """)
            command.emit("""git pull origin master""")
        }
    }

    def tagAndPush(String version) {
        hudson.sshagent(credentials: [outputCredentialsId]) {
            command.emit("""git push origin master""")
            try {
                def i = new java.util.Random(new java.util.Date().getTime()).nextInt()
                command.emit("""git tag -a ${version}_${i} -m "version ${version}" && git push --tags""")
                //TODO use just version here
                //command.emit("""git tag -a ${version} -m "version ${version}" && git push --tags""")
            } catch (e) {
                hudson.echo e.getMessage()
            }
        }
    }

}

class Command implements Serializable {
    def hudson
    boolean echoOnly
    boolean isWindows

    Command(hudson, boolean echoOnly) {
        this.hudson = hudson
        this.echoOnly = echoOnly
        this.isWindows = System.getProperty("os.name").toLowerCase().contains("windows")
    }

    def emit(String command) {
        if(echoOnly) {
            hudson.echo "$command"
        } else {
            if(isWindows) {
                hudson.bat "$command"
            } else {
                hudson.sh "$command"
            }
        }
    }

}


class Maven implements Serializable {
    final String JDBC_URL = "jdbc:oracle:thin:@172.16.2.107:1521:AOU"

    final String mvnCmdPrefix = "mvn -q -B -s my-settings.xml "

    Command command

    Maven(Command command) {
        this.command = command
    }

    def cleanProject() {
        command.emit(mvnCmdPrefix + "clean")
    }

    def compileProject() {
        command.emit(mvnCmdPrefix + "-DskipTests compile")
    }

    def newVersion(String version) {
        command.emit(mvnCmdPrefix + "versions:set -DnewVersion=${version}")
    }

    def liquibaseUpdate() {
        command.emit("""-Djdbc.url=${JDBC_URL} properties:read-project-properties liquibase:update """)
    }
}

class TurboPipeline implements Serializable {
    final def credentialsId = 'throw-me-away-key'

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
        versionComponents[-1] = versionComponents.last().isInteger() ? versionComponents.last().toInteger() + 1 : 0
        versionComponents.join(".")
    }

}


def command = new Command(this, false)

def maven = new Maven(command)

def propagate = [
    new Propagated(this, command, ".", "git@github.com:konopski/jprog.git", "konopski-jprog")
]

def turbo = new TurboPipeline(this)

turbo.turboRun({

    node('master') {
        stage('build-init') {
            cleanWs()
            bat "rm -rf original"
            bat "rm -rf output"
            dir('original') {
                checkout scm
                bat """git checkout ${params.sourceBranch}"""
                def oldVersion = readMavenPom().version
                echo "old version: $oldVersion"
                def version = turbo.prepareNextVersion(oldVersion)
                echo "updating to version: $version"
                currentBuild.displayName = version
                maven.newVersion(version)
                maven.cleanProject()
            }
        }
        stage('extract-build-propagate') {
            dir('original') {
                bat "rm -rf .git"
            }
            for(p in propagate) {
                p.moveToOutputDir()
                dir('output') {
                    dir(p.directoryName) {
                        p.initGitRepo()
                        maven.compileProject()
                        p.tagAndPush(readMavenPom().version)
                    }
                }
            }
        }

    }
})


