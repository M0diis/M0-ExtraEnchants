import xyz.jpenilla.runpaper.task.RunServer

plugins {
    java
    id("com.gradleup.shadow") version "8.3.5"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("org.cyclonedx.bom") version "1.8.1"
}

group = "me.m0dii"
version = "j21-mc1.21.3-4.1.0"

base {
    archivesName.set("M0-ExtraEnchants")
}

tasks.processResources {
    inputs.property("version", version)
    filesMatching("**/*plugin.yml") {
        expand("version" to version)
    }
}

tasks.shadowJar {
    listOf("org.bstats", "com.jeff_media").forEach {
        relocate(it, "me.m0dii.libs.$it")
    }

    minimize()

    archiveFileName.set("M0-ExtraEnchants-${project.version}.jar")
}

repositories {
    mavenLocal()
    mavenCentral()

    flatDir {
        dirs("libs")
    }

    listOf(
        "https://jitpack.io",
        "https://repo.papermc.io/repository/maven-public/",
        "https://hub.spigotmc.org/nexus/content/repositories/snapshots/",
        "https://repo.jeff-media.com/public/",
        "https://repo.extendedclip.com/releases/"
    ).forEach { repoUrl ->
        maven { url = uri(repoUrl) }
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.3-R0.1-SNAPSHOT")

    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly(files("libs/Residence5.0.1.5.jar"))

    implementation("org.bstats:bstats-bukkit:2.2.1")
    implementation("org.reflections:reflections:0.10.2")
    implementation("com.jeff_media:MorePersistentDataTypes:2.4.0")

    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

val allPlugins = runPaper.downloadPluginsSpec {
    modrinth("viaversion", "5.5.0-SNAPSHOT+793")
    modrinth("viabackwards", "5.4.2")
    modrinth("luckperms", "v5.5.0-bukkit")

    url("https://www.spigotmc.org/resources/placeholderapi.6245/download?version=541946") // 2.11.6
}

val doFirstEula: Task.() -> Unit = {
    val eulaFile = file("run/latest/eula.txt")
    eulaFile.parentFile.mkdirs()
    if (!eulaFile.exists()) {
        eulaFile.writeText("eula=true")
    }
}

tasks {
    val runVersions = mapOf(
        8 to setOf("1.8.8"),
        11 to setOf("1.9.4", "1.10.2", "1.11.2"),
        17 to setOf("1.12.2", "1.13.2", "1.14.4", "1.15.2", "1.16.5", "1.17.1", "1.18.2", "1.19.4", "1.20.4"),
        21 to setOf("1.20.6", "1.21.8"),
    )

    runVersions.forEach { (javaVersion, minecraftVersions) ->
        for (version in minecraftVersions) {
            createVersionedRun(version, javaVersion)
        }
    }

    runServer {
        runDirectory(file("run/latest"))
        minecraftVersion("1.21.8")

        downloadPlugins.from(allPlugins)

        doFirst(doFirstEula)
    }
}

fun TaskContainerScope.createVersionedRun(
    version: String,
    javaVersion: Int
) = register<RunServer>("runServer${version.replace(".", "_")}") {
    group = "run versioned"
    pluginJars.from(shadowJar.flatMap { it.archiveFile })
    minecraftVersion(version)

    downloadPlugins.from(allPlugins)

    runDirectory(file("run/$version"))
    systemProperty("Paper.IgnoreJavaVersion", true)
    javaLauncher.set(
        project.javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(javaVersion))
        }
    )

    doFirst(doFirstEula)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}