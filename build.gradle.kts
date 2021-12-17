import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Get a property from the `gradle.properties` file
fun properties(key: String) = project.findProperty(key).toString()

plugins {
    // Java support
    java
    // Kotlin support
    kotlin("jvm") version "1.6.10"
    // Gradle IntelliJ Plugin
    id("org.jetbrains.intellij") version "1.3.0"
    // Gradle Changelog Plugin
    id("org.jetbrains.changelog") version "1.3.1"
}

val kotlinBukkitAPIVersion = "0.2.0-SNAPSHOT"
val bukkriptVersion = "0.2.0-SNAPSHOT"

group = "br.com.devsrsouza.kotlinbukkitapi"
version = "0.0.7"

// Dependencies
repositories {
    jcenter()
    mavenLocal()
    maven("https://nexus.devsrsouza.com.br/repository/maven-public/")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    val changing = Action<ExternalModuleDependency> { isChanging = true }
    api("br.com.devsrsouza.bukkript:script-definition-embedded:$bukkriptVersion", changing)

    testCompileOnly("junit", "junit", "4.12")
}

// Configure Gradle IntelliJ Plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    pluginName.set(properties("pluginName"))
    version.set(properties("platformVersion"))
    type.set(properties("platformType"))

    // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
    plugins.set(
        properties("platformPlugins")
            .split(',')
            .map(String::trim)
            .filter(String::isNotEmpty)
    )
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    version.set(properties("pluginVersion"))
    groups.set(emptyList())
}

tasks {
    // Set the JVM compatibility versions
    properties("javaVersion").let {
        withType<JavaCompile> {
            sourceCompatibility = it
            targetCompatibility = it
        }
        withType<KotlinCompile> {
            kotlinOptions.jvmTarget = it
        }
    }

    wrapper {
        gradleVersion = properties("gradleVersion")
    }

    patchPluginXml {
        version.set(properties("pluginVersion"))
        sinceBuild.set(properties("pluginSinceBuild"))
        untilBuild.set(properties("pluginUntilBuild"))

        pluginDescription.set(
            """
        <img src="https://github.com/DevSrSouza/KotlinBukkitAPI/raw/master/logo.png" width="417" height="161"/>

        <br />

        The KotlinBukkitAPI Tooling is plugin for IntelliJ that helps
        <br />
        developers using <a href="https://github.com/DevSrSouza/KotlinBukkitAPI">KotlinBukkitAPI</a> and scripts for <a href="https://github.com/DevSrSouza/Bukkript">Bukkript</a>.
        <br />
        This libraries help build extensions for Minecraft Server using Spigot server.

        <br />

        <br />

        <ul>
        <li><a href='https://github.com/DevSrSouza/Bukkript'>Bukkript</a></li>
        <li<a href='https://github.com/DevSrSouza/KotlinBukkitAPI-Tooling'>KotlinBukkitAPI-Tooling (this plugin)</a></li>
        <li<a href='https://github.com/DevSrSouza/KotlinBukkitAPI'>KotlinBukkitAPI</a></li>
        <li><a href='https://github.com/KotlinMinecraft/KotlinBukkitAPI-Examples'>KotlinBukkitAPI Examples</a></li>
        </ul>

        <br />
        <br />

        <h3>Demonstration</h3>

        <br />

        <img src="https://i.imgur.com/exlwVUs.gif" width='680' height='390'/>
    """.trimIndent()
        )

        // Get the latest available change notes from the changelog file
        changeNotes.set(
            changelog.run {
                kotlin.runCatching { get(properties("pluginVersion")) }.getOrElse { getLatest() }
            }.toHTML()
        )
    }

    publishPlugin {
        token.set(System.getenv("ORG_GRADLE_PROJECT_intellijPublishToken"))
    }
}

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(120, "seconds")
}