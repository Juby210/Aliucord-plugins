import com.aliucord.gradle.AliucordExtension
import com.android.build.gradle.BaseExtension

buildscript {
    repositories {
        // mavenLocal()
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.4")
        classpath("com.github.Aliucord:gradle:main-SNAPSHOT")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.30")
    }
}

allprojects {
    repositories {
        // mavenLocal()
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

fun Project.aliucord(configuration: AliucordExtension.() -> Unit) = extensions.getByName<AliucordExtension>("aliucord").configuration()

fun Project.android(configuration: BaseExtension.() -> Unit) = extensions.getByName<BaseExtension>("android").configuration()

subprojects {
    apply(plugin = "com.android.library")
    apply(plugin = "com.aliucord.gradle")
    apply(plugin = "kotlin-android")

    aliucord {
        author("Juby210", 324622488644616195L)
        updateUrl.set("https://raw.githubusercontent.com/Juby210/Aliucord-plugins/builds/updater.json")
        buildUrl.set("https://raw.githubusercontent.com/Juby210/Aliucord-plugins/builds/%s.zip")
    }

    android {
        compileSdkVersion(30)

        defaultConfig {
            minSdk = 24
            targetSdk = 30
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

        discord("com.discord:discord:aliucord-SNAPSHOT")
        compileOnly("com.github.Aliucord:Aliucord:main-SNAPSHOT")
        // compileOnly("com.github.Aliucord:Aliucord:unspecified")

        compileOnly("androidx.appcompat:appcompat:1.3.1")
        compileOnly("com.google.android.material:material:1.4.0")
        compileOnly("androidx.constraintlayout:constraintlayout:2.1.1")
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}
