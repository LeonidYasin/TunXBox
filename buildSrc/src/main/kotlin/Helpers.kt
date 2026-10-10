import com.android.build.api.dsl.ApplicationExtension
import com.android.build.gradle.AbstractAppExtension
import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.getByName
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import java.util.Base64
import java.util.Properties
import kotlin.system.exitProcess

private val Project.android get() = extensions.getByName<ApplicationExtension>("android")

private lateinit var metadata: Properties
private lateinit var localProperties: Properties

fun Project.requireMetadata(): Properties {
    if (!::metadata.isInitialized) {
        metadata = Properties().apply {
            load(rootProject.file("nb4a.properties").inputStream())
        }
    }
    return metadata
}

fun Project.requireLocalProperties(): Properties {
    if (!::localProperties.isInitialized) {
        localProperties = Properties()

        val base64 = System.getenv("LOCAL_PROPERTIES")
        if (!base64.isNullOrBlank()) {

            localProperties.load(Base64.getDecoder().decode(base64).inputStream())
        } else if (project.rootProject.file("local.properties").exists()) {
            localProperties.load(rootProject.file("local.properties").inputStream())
        }
    }
    return localProperties
}

fun Project.setupCommon() {
    android.apply {
        buildToolsVersion = "35.0.1"
        compileSdk = 35
        defaultConfig {
            minSdk = 21
            targetSdk = 35
        }
        buildTypes {
            getByName("release") {
                isMinifyEnabled = true
            }
        }
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }
        (android as ExtensionAware).extensions.getByName<KotlinJvmOptions>("kotlinOptions").apply {
            jvmTarget = JavaVersion.VERSION_1_8.toString()
        }
        lint {
            showAll = true
            checkAllWarnings = true
            checkReleaseBuilds = true
            warningsAsErrors = true
            textOutput = project.file("build/lint.txt")
            htmlOutput = project.file("build/lint.html")
        }
        packaging {
            resources.excludes.addAll(
                listOf(
                    "**/*.kotlin_*",
                    "/META-INF/*.version",
                    "/META-INF/native/**",
                    "/META-INF/native-image/**",
                    "/META-INF/INDEX.LIST",
                    "DebugProbesKt.bin",
                    "com/**",
                    "org/**",
                    "**/*.java",
                    "**/*.proto",
                    "okhttp3/**"
                )
            )
        }
        (this as? AbstractAppExtension)?.apply {
            buildTypes {
                getByName("release") {
                    isShrinkResources = true
                    if (System.getenv("nkmr_minify") == "0") {
                        isShrinkResources = false
                        isMinifyEnabled = false
                    }
                }
                getByName("debug") {
                    // applicationIdSuffix removed for TV compatibility
                    debuggable(true)
                    jniDebuggable(true)
                }
            }
            applicationVariants.forEach { variant ->
                variant.outputs.forEach {
                    it as BaseVariantOutputImpl
                    it.outputFileName = it.outputFileName.replace(
                        "app", "${project.name}-" + variant.versionName
                    ).replace("-release", "").replace("-oss", "")
                }
            }
        }
    }
}

fun Project.setupAppCommon() {
    setupCommon()

    val lp = requireLocalProperties()
    val keystorePwd = lp.getProperty("KEYSTORE_PASS") ?: System.getenv("KEYSTORE_PASS")
    val alias = lp.getProperty("ALIAS_NAME") ?: System.getenv("ALIAS_NAME")
    val pwd = lp.getProperty("ALIAS_PASS") ?: System.getenv("ALIAS_PASS")

    if (System.getenv("TUNXBOX_REQUIRE_SIGNING") == "1") {
        require(!keystorePwd.isNullOrBlank() && !alias.isNullOrBlank() && !pwd.isNullOrBlank()) { "Missing required signing identity" }
    }
    android.apply {
        if (keystorePwd != null) {
            signingConfigs {
                create("release") {
                    storeFile = rootProject.file(System.getenv("TUNXBOX_SIGNING_KEYSTORE") ?: "release.keystore")
                    storePassword = keystorePwd
                    keyAlias = alias
                    keyPassword = pwd
                }
            }
        }
        buildTypes {
            val key = signingConfigs.findByName("release")
            if (key != null) {
                getByName("release").signingConfig = key
                getByName("debug").signingConfig = key
            } else {
                // Fallback to debug signing for CI builds without release keystore
                val debugKey = signingConfigs.findByName("debug")
                if (debugKey != null) {
                    getByName("release").signingConfig = debugKey
                    names.filter { it != "release" && it != "debug" }.forEach { name ->
                        findByName(name)?.signingConfig = debugKey
                    }
                }
            }
        }
    }
}

fun Project.setupApp() {
    val pkgName = requireMetadata().getProperty("PACKAGE_NAME")
    val verName = requireMetadata().getProperty("VERSION_NAME")
    val versionBase = requireMetadata().getProperty("VERSION_CODE").toInt()
    val buildSequence = System.getenv("TUNXBOX_BUILD_SEQUENCE")?.toIntOrNull() ?: 0
    require(versionBase in 1..2100 && buildSequence in 0..999998) { "Invalid Android version sequence" }
    val verCode = versionBase * 1_000_000 + 999999
    // Actual checkout identity, not the workflow's possibly different trigger SHA.
    fun gitValue(vararg args: String): String = runCatching {
        val process = ProcessBuilder(listOf("git") + args).directory(rootProject.projectDir).start()
        val value = process.inputStream.bufferedReader().readText().trim()
        if (process.waitFor() == 0) value else ""
    }.getOrDefault("")
    val sourceCommit = gitValue("rev-parse", "HEAD").takeIf { it.matches(Regex("[0-9a-f]{40}")) } ?: "unknown"
    require(System.getenv("CI") != "true" || sourceCommit != "unknown") { "CI requires actual checkout SHA" }
    val sourceRef = gitValue("symbolic-ref", "--short", "HEAD").ifBlank { "detached" }
    val pins = rootProject.file("buildScript/lib/core/get_source_env.sh").readText()
    fun sourcePin(name: String): String = Regex(name + "=\"([0-9a-f]{40})\"").find(pins)?.groupValues?.get(1) ?: "unknown"
    val patch = rootProject.file("buildScript/lib/core/reality-hybrid.patch")
    val patchHash = if (patch.exists()) java.security.MessageDigest.getInstance("SHA-256")
        .digest(patch.readBytes()).joinToString("") { "%02x".format(it.toInt() and 255) } else "none"
    val buildRun = System.getenv("GITHUB_RUN_NUMBER")?.takeIf { it.matches(Regex("[0-9]+")) } ?: "local"
    val buildRunId = System.getenv("GITHUB_RUN_ID")?.takeIf { it.matches(Regex("[0-9]+")) } ?: "local"
    val buildTime = java.time.Instant.now().toString()
    fun quoted(value: String) = "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
    android.apply {
        defaultConfig {
            applicationId = pkgName
            versionCode = verCode
            versionName = verName
            buildConfigField("String", "PRE_VERSION_NAME", "\"\"")
            buildConfigField("String", "BUILD_COMMIT", quoted(sourceCommit))
            buildConfigField("String", "BUILD_SOURCE_REF", quoted(sourceRef))
            buildConfigField("String", "BUILD_RUN_NUMBER", quoted(buildRun))
            buildConfigField("String", "BUILD_RUN_ID", quoted(buildRunId))
            buildConfigField("String", "BUILD_TIME_UTC", quoted(buildTime))
            buildConfigField("String", "CORE_SOURCE_COMMIT", quoted(sourcePin("COMMIT_SING_BOX")))
            buildConfigField("String", "LIBNEKO_SOURCE_COMMIT", quoted(sourcePin("COMMIT_LIBNEKO")))
            buildConfigField("String", "CORE_PATCH_SHA256", quoted(patchHash))
        }
    }
    setupAppCommon()

    android.apply {
        this as AbstractAppExtension

        buildTypes {
            getByName("release") {
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    file("proguard-rules.pro")
                )
            }
        }

        splits.abi {
            reset()
            isEnable = true
            isUniversalApk = true
            include("armeabi-v7a")
            include("arm64-v8a")
        }

        flavorDimensions += "vendor"
        productFlavors {
            create("oss")
            create("fdroid")
            create("play")
            create("preview") {
                versionCode = versionBase * 1_000_000 + buildSequence
                versionNameSuffix = "-rc.$buildSequence"
                buildConfigField(
                    "String",
                    "PRE_VERSION_NAME",
                    "\"${requireMetadata().getProperty("PRE_VERSION_NAME")}\""
                )
            }
        }

        applicationVariants.all {
            outputs.all {
                this as BaseVariantOutputImpl
                val isPreview = outputFileName.contains("-preview")
                outputFileName = if (isPreview) {
                    outputFileName.replace(
                        project.name,
                        "TunXBox-" + requireMetadata().getProperty("PRE_VERSION_NAME")
                    ).replace("-preview", "")
                } else {
                    outputFileName.replace(project.name, "TunXBox-$versionName")
                        .replace("-release", "")
                        .replace("-oss", "")
                }
            }
        }

        for (abi in listOf("Arm64", "Arm", "X64", "X86")) {
            tasks.create("assemble" + abi + "FdroidRelease") {
                dependsOn("assembleFdroidRelease")
            }
        }

        sourceSets.getByName("main").apply {
            jniLibs.srcDir("executableSo")
        }
    }
}
