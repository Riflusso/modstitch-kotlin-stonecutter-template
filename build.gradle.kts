import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("dev.kikugie.stonecutter")
    id("dev.isxander.modstitch.base")
    kotlin("jvm")
    `maven-publish`
}

// ========== Versions & Project Info ==========
val mcVersion: String by project
val versionWithoutMC = property("modVersion")!!.toString()

val isAlpha = "alpha" in versionWithoutMC
val isBeta = "beta" in versionWithoutMC

val isFabric = modstitch.isLoom
val isNeoforge = modstitch.isModDevGradleRegular
val isForge = modstitch.isModDevGradleLegacy
val isForgeLike = modstitch.isModDevGradle

val loader = when {
    isFabric -> "fabric"
    isNeoforge -> "neoforge"
    isForge -> "forge"
    else -> error("Unknown loader")
}

val javaTargetVersion = if (stonecutter.eval(mcVersion, ">1.20.4")) 21 else 17
val resolvedModId = resolveProp("modId") ?: error("modId is required")

// ========== ModStitch Setup ==========
modstitch {
    minecraftVersion = mcVersion
    javaTarget = javaTargetVersion

    kotlin {
        jvmToolchain(javaTarget.get())
        compilerOptions.jvmTarget.set(JvmTarget.fromTarget(javaTarget.get().toString()))
    }

    parchment {
        mappingsVersion = resolveProp("parchment.version")
        minecraftVersion = resolveProp("parchment.minecraft")
    }

    metadata {
        modId = resolvedModId
        modName = resolveProp("modName")
        modVersion = "$versionWithoutMC+${stonecutter.current.project}"
        modGroup = resolveProp("modGroup")
        modDescription = resolveProp("modDescription")
        modLicense = resolveProp("modLicense")
        modAuthor = resolveProp("modAuthor")

        replacementProperties.put(
            "pack_format", when (mcVersion) {
                "1.19.2" -> 9
                "1.20.1" -> 15
                "1.21.1" -> 34
                else -> throw IllegalArgumentException("Please store the resource pack version for $mcVersion in build.gradle.kts! https://minecraft.wiki/w/Pack_format")
            }.toString()
        )

        // replacementProperties DSL
        fun setReplace(key: String, property: String) {
            resolveProp(property)?.let { replacementProperties.put(key, it) }
        }

        setReplace("github", "githubProject")
        setReplace("mc", "meta.mcDep")
        setReplace("fabricLoader", "deps.fabricLoader")
        setReplace("fapi", "deps.fabricApi")
    }

    loom {
        resolveProp("deps.fabricLoader")?.let { fabricLoaderVersion = it }
    }

    moddevgradle {
        enable {
            resolveProp("deps.neoforge")?.let { neoForgeVersion = it }
            resolveProp("deps.forge")?.let { forgeVersion = it }
        }
        defaultRuns()

        configureNeoforge {
            runs.all {
                jvmArguments.add("-Dmixin.debug.export=true")
            }
        }

        tasks.named("createMinecraftArtifacts") {
            dependsOn("stonecutterGenerate")
        }
    }

    mixin {
        addMixinsToModManifest = true
        configs.register(resolvedModId)
    }
}

// ========== Stonecutter ==========
stonecutter {
    constants {
        put("fabric", isFabric)
        put("neoforge", isNeoforge)
        put("forge", isForge)
        put("forgelike", isForgeLike)
    }

    dependencies {
        put("fapi", resolveProp("deps.fabricApi") ?: "0.0.0")
    }
}

// ========== Dependencies ==========
dependencies {
    if (isFabric) {
        modstitchModImplementation("net.fabricmc.fabric-api:fabric-api:${resolveProp("deps.fabricApi")}")
        modstitchModImplementation("net.fabricmc:fabric-language-kotlin:${resolveProp("deps.fabricLangKotlin")}")

        modstitchModImplementation("com.terraformersmc:modmenu:${resolveProp("deps.modmenu")}")
    }

    if (isNeoforge) {
        modstitchModRuntimeOnly("thedarkcolour:kotlinforforge-neoforge:${resolveProp("deps.kotlinForForge")}")
    }

    if (isForge) {
        modstitchModRuntimeOnly("thedarkcolour:kotlinforforge:${resolveProp("deps.kotlinForForge")}")
        compileOnly("org.jetbrains:annotations:20.1.0")
    }
}

// ========== Tasks ==========
tasks {
    withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget = modstitch.javaTarget.map { JvmTarget.fromTarget(it.toString()) }
        }
        dependsOn("stonecutterGenerate")
    }

    named("generateModMetadata") {
        dependsOn("stonecutterGenerate")
    }
}

// ========== Helpers ==========
fun resolveProp(property: String): String? =
    System.getenv(property)?.takeIf { it.isNotBlank() }
        ?: findProperty(property)?.toString()?.takeIf { it.isNotBlank() }
