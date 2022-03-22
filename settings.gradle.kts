pluginManagement {
    repositories {
        maven("https://repo.caseif.net")
        gradlePluginPortal()
    }
}

rootProject.name = "steel"

include("flintcommon")
include("flintcommon:flint")
