import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.triggers.vcs
import jetbrains.buildServer.configs.kotlin.failureConditions.BuildFailureOnMetric
import jetbrains.buildServer.configs.kotlin.failureConditions.failOnMetricChange
import jetbrains.buildServer.configs.kotlin.buildFeatures.notifications

/*
* TeamCity DSL Script for SimpleEchoServer project
*/

version = "2023.05"

project {
    description = "Simple Echo Server Project"

    buildType(Build)
    buildType(BuildWithDependency)
}

object Build : BuildType({
    name = "Build"
    description = "Builds the project with Maven"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        maven {
            name = "Maven Build"
            goals = "clean package"
            runnerArgs = "-Dmaven.test.failure.ignore=true"
        }
    }

    triggers {
        vcs {
        }
    }

    artifactRules = "target/*.jar => myArtifacts"
})

object BuildWithDependency : BuildType({
    name = "Build With Dependency"
    description = "Builds the project with dependency on the first build"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        maven {
            name = "Maven Build"
            goals = "clean package"
            runnerArgs = "-Dmaven.test.failure.ignore=true"
        }
    }

    dependencies {
        snapshot(Build) {
        }

        artifacts(Build) {
            artifactRules = "myArtifacts/*.jar => myArtifacts"
        }
    }

    failureConditions {
        executionTimeoutMin = 10
    }

    features {
        notifications {
            notifierSettings = emailNotifier {
                email = "sagolbah@gmail.com"
            }
            buildFailed = true
            buildFailedToStart = true
        }
    }

    artifactRules = """
        target/*.jar
        myArtifacts/*.jar
    """.trimIndent()
})
