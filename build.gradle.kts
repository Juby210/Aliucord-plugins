import com.aliucord.gradle.AliucordExtension
import com.android.build.gradle.BaseExtension

buildscript {
    repositories {
        // mavenLocal()
        google()
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://maven.aliucord.com/snapshots")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.4")
        classpath("com.aliucord:gradle:bbcd8a8")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.32")
    }
}

allprojects {
    repositories {
        // mavenLocal()
        google()
        mavenCentral()
        maven("https://maven.aliucord.com/snapshots")
    }
}

fun Project.aliucord(configuration: AliucordExtension.() -> Unit) = extensions.getByName<AliucordExtension>("aliucord").configuration()

fun Project.android(configuration: BaseExtension.() -> Unit) = extensions.getByName<BaseExtension>("android").configuration()

subprojects {
    apply(plugin = "com.android.library")
    apply(plugin = "com.aliucord.gradle")
    apply(plugin = "kotlin-android")

    aliucord {
        author("RazerTexz", 633565155501801472L)
        updateUrl.set("https://raw.githubusercontent.com/RazerTexz/Aliucord-plugins/builds/updater.json")
        buildUrl.set("https://raw.githubusercontent.com/RazerTexz/Aliucord-plugins/builds/%s.zip")
    }

    android {
        compileSdkVersion(34)

        defaultConfig {
            minSdk = 24
            targetSdk = 34
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }

        tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
            kotlinOptions {
                jvmTarget = "11"
                freeCompilerArgs = freeCompilerArgs +
                    "-Xno-call-assertions" +
                    "-Xno-param-assertions" +
                    "-Xno-receiver-assertions"
            }
        }
    }

    dependencies {
        val discord by configurations
        val compileOnly by configurations

        discord("com.discord:discord:126021")
        compileOnly("com.aliucord:Aliucord:main-SNAPSHOT")
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}
