import com.android.build.gradle.BaseExtension

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.1")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

fun Project.android(configuration: BaseExtension.() -> Unit) = extensions.getByName<BaseExtension>("android").configuration()

subprojects {
    if (name != "Aliucord") {
        apply(plugin = "com.android.library")

        android {
            compileSdkVersion(30)

            defaultConfig {
                minSdk = 24
                targetSdk = 30
                versionCode = 1
                versionName = "1.0"
            }

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_11
                targetCompatibility = JavaVersion.VERSION_11
            }
        }

        dependencies {
            val implementation by configurations

            implementation(project(":Aliucord"))

            implementation("androidx.appcompat:appcompat:1.3.1")
            implementation("com.google.android.material:material:1.4.0")
            implementation("androidx.constraintlayout:constraintlayout:2.1.0")
        }
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}
