import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.triggers.vcs

/*
Project DSL for SimpleEchoServer
*/

version = "2023.05"

project {
    description = "Simple Echo Server Project"

    buildType(BuildConfiguration)
    buildType(BuildConfigurationWithDependency)
}

object BuildConfiguration : BuildType({
    id("Build")
    name = "Build"
    description = "Builds the project with Maven"

    vcs {
        root(DslContext.settingsRoot)
    }

    triggers {
        vcs {
            branchFilter = "+:*"
        }
    }

    steps {
        maven {
            goals = "clean package"
            runnerArgs = "-Dmaven.test.failure.ignore=true"
        }
    }

    artifactRules = "target/*.jar => myArtifacts"
})

object BuildConfigurationWithDependency : BuildType({
    id("BuildWithDependency")
    name = "Build With Dependency"
    description = "Builds with dependency on the first build configuration"

    vcs {
        root(DslContext.settingsRoot)
    }

    triggers {
        vcs {
            branchFilter = "+:*"
        }
    }

    steps {
        maven {
            goals = "clean package"
            runnerArgs = "-Dmaven.test.failure.ignore=true"
        }
    }

    dependencies {
        snapshot(BuildConfiguration) {
            onDependencyFailure = FailureAction.FAIL_TO_START
        }
        
        artifacts(BuildConfiguration) {
            artifactRules = "myArtifacts/** => myArtifacts"
        }
    }

    artifactRules = """
        target/*.jar
        myArtifacts/** => myArtifacts
    """.trimIndent()

    features {
        feature {
            type = "teamcity.emailNotifier"
            param("email", "sagolbah@gmail.com")
            param("notifyBuildFailed", "true")
            param("notifyBuildFailedToStart", "true")
        }
    }
    
    failureConditions {
        executionTimeoutMin = 10
    }
})