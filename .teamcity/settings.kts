import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.triggers.vcs
import jetbrains.buildServer.configs.kotlin.buildFeatures.perfmon

/*
* Available context parameters:
*
* DslContext.settingsRoot - VCS root to use
*/

project {
    description = "Simple Echo Server Project"

    val buildConfig = BuildConfiguration()
    buildType(buildConfig)

    val secondBuildConfig = SecondBuildConfiguration(buildConfig)
    buildType(secondBuildConfig)
}

class BuildConfiguration : BuildType({
    id("Build")
    name = "Build"
    description = "Builds the project with Maven"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        maven {
            goals = "clean package"
            runnerArgs = "-Dmaven.test.failure.ignore=true"
        }
    }

    triggers {
        vcs {
        }
    }

    artifactRules = "target/*.jar => myArtifacts/"

    features {
        perfmon {}
    }
})

class SecondBuildConfiguration(dependency: BuildType) : BuildType({
    id("SecondBuild")
    name = "Second Build"
    description = "Second build with dependencies on the first build"

    vcs {
        root(DslContext.settingsRoot)
    }

    dependencies {
        snapshot(dependency) {
            reuseBuilds = ReuseBuilds.ANY
            onDependencyFailure = FailureAction.FAIL_TO_START
            onDependencyCancel = FailureAction.CANCEL
        }

        artifacts(dependency) {
            artifactRules = "myArtifacts/*.jar => myArtifacts/"
        }
    }

    steps {
        maven {
            goals = "clean package"
            runnerArgs = "-Dmaven.test.failure.ignore=true"
        }
    }

    artifactRules = """
        target/*.jar
        myArtifacts/*.jar
    """.trimIndent()

    failureConditions {
        executionTimeoutMin = 10
    }

    features {
        perfmon {}
    }
})
