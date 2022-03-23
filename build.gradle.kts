import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    `maven-publish`
    eclipse
    idea
    checkstyle
    id("org.cadixdev.licenser") version "0.6.1"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

defaultTasks("clean", "updateLicenses", "build", "shadowJar")

evaluationDependsOnChildren()

group = "net.caseif.flint.steel"
version = "1.3.8-SNAPSHOT"

description = "The implementation of Flint minigame framework for the Bukkit Minecraft server mod."

val inceptionYear by extra { "2015" }
val packaging by extra {"jar" }
val author by extra { "Max Roncace" }

java {
    sourceCompatibility = JavaVersion.VERSION_1_7
    targetCompatibility = JavaVersion.VERSION_1_7
}

repositories {
    mavenCentral()
    maven("https://repo.caseif.net/")
    maven("https://hub.spigotmc.org/nexus/content/groups/public/")
    maven("https://repo.spongepowered.org/maven")
}

dependencies {
    shadow("com.google.guava:guava:17.0")
    shadow("org.spigotmc:spigot-api:1.14-pre5-SNAPSHOT")
    implementation(project("flintcommon"))
    implementation("org.bstats:bstats-bukkit:1.2")
    implementation("net.caseif.jtelemetry:jtelemetry:1.1.0")
    implementation("com.google.code.gson:gson:2.2.4")
    implementation("com.googlecode.json-simple:json-simple:1.1.1")

    checkstyle("org.spongepowered:checkstyle:6.1.1-sponge1")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-Xlint:all")
}

tasks.withType<Jar> {
    archiveClassifier.set("base")
    
    manifest {
        attributes["Created-By"] = System.getProperty("java.vm.version") + " (" + System.getProperty("java.vm.vendor") + ")"
        attributes["Specification-Title"] = project("flintcommon:flint").name
        attributes["Specification-Version"] = project("flintcommon:flint").version
        attributes["Specification-Vendor"] = project("flintcommon:flint").extra["author"]
        attributes["Implementation-Title"] = project.name
        attributes["Implementation-Version"] = project.version
        attributes["Implementation-Vendor"] = project.extra["author"]
    }
}

tasks.named<Copy>("processResources") {
    from("LICENSE")
    expand("version" to project.version,
           "author" to author)
}

license {
    include("**/*.java")
    ignoreFailures(false)
}

tasks.withType<Checkstyle> {
    configDirectory.set(file("etc"))
    configFile = file("etc/checkstyle.xml")

    exclude("**/*.properties")
    exclude("**/*.yml")
}

tasks.withType<Javadoc> {
    enabled = false
}

tasks.withType<ShadowJar> {
    dependencies {
        include(project("flintcommon:flint"))
        include(project("flintcommon"))
        include(dependency("org.bstats:bstats-bukkit"))
        include(dependency("net.caseif.jtelemetry:jtelemetry"))
        include(dependency("com.google.code.gson:gson"))
        include(dependency("com.googlecode.json-simple:json-simple"))
    }

    archiveClassifier.set("")

    relocate("org.bstats", "net.caseif.flint.steel.lib.org.bstats")
    relocate("net.caseif.jtelemetry", "net.caseif.flint.steel.lib.net.caseif.jtelemetry")
    relocate("com.google.gson", "net.caseif.flint.steel.lib.com.google.gson")
    relocate("org.json.simple", "net.caseif.flint.steel.lib.org.json.simple")
}

tasks.create<Jar>("sourceJar") {
    from(sourceSets["main"].java)
    from(sourceSets["main"].resources)
    archiveClassifier.set("sources")
}

artifacts {
    archives(tasks["shadowJar"])
    archives(tasks["sourceJar"])
}

tasks.withType<Wrapper> {
    gradleVersion = "7.4.1"
}
