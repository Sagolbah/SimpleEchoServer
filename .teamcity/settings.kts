import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.triggers.vcs
import jetbrains.buildServer.configs.kotlin.buildSteps.script

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2024.12"

project {
    description = "echo server project"

    // Define Build Configuration
    val echoServerBuild = buildType {
        id("EchoServerBuild")
        name = "Build Echo Server"

        // VCS Settings
        vcs {
            root(DslContext.settingsRoot)
        }

        // Build Steps
        steps {
            // Clean and compile
            maven {
                name = "Clean and Compile"
                goals = "clean compile"
                runnerArgs = "-Dmaven.test.failure.ignore=true"
            }

            // Run tests
            maven {
                name = "Run Tests"
                goals = "test"
                runnerArgs = "-Dmaven.test.failure.ignore=true"
            }

            // Package application
            maven {
                name = "Package Application"
                goals = "package"
                runnerArgs = "-Dmaven.test.skip=true"
            }
        }

        // Triggers
        triggers {
            vcs {
                branchFilter = "+:*"
            }
        }

        // Features
        features {
            perfmon {
            }
        }

        // Artifacts
        artifactRules = """
            target/*.jar => artifacts
        """.trimIndent()
    }

    // Define second build configuration with snapshot dependency
    buildType {
        id("EchoServerDeploy")
        name = "Deploy Echo Server"

        // VCS Settings
        vcs {
            root(DslContext.settingsRoot)
        }

        // Build Steps
        steps {
            // Clean and compile
            maven {
                name = "Clean and Compile"
                goals = "clean compile"
                runnerArgs = "-Dmaven.test.failure.ignore=true"
            }

            // Run tests
            maven {
                name = "Run Tests"
                goals = "test"
                runnerArgs = "-Dmaven.test.failure.ignore=true"
            }

            // Package application
            maven {
                name = "Package Application"
                goals = "package"
                runnerArgs = "-Dmaven.test.skip=true"
            }
        }

        // Triggers
        triggers {
            vcs {
                branchFilter = "+:*"
            }
        }

        // Features
        features {
            perfmon {
            }
        }

        // Artifacts
        artifactRules = """
            target/*.jar => artifacts
        """.trimIndent()

        // Add snapshot dependency on the first build
        dependencies {
            snapshot(echoServerBuild) {
                onDependencyFailure = FailureAction.FAIL_TO_START
            }

            // Add artifact dependency on the first build
            artifacts(echoServerBuild) {
                artifactRules = "target/*.jar => artifacts"
            }
        }
    }
}
