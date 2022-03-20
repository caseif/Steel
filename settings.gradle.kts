pluginManagement {
    repositories {
        maven("https://repo.caseif.net")
        gradlePluginPortal()
    }
}

rootProject.name = "Steel"

include("FlintCommon")
include("FlintCommon:Flint")
