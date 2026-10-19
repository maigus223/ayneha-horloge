plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.maigus.aynehahorloge"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.maigus.aynehahorloge"
        minSdk = 24
        targetSdk = 34
        versionCode = 2
        versionName = "1.1"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

// Force Gradle à compiler le code Kotlin/Java avec JDK 17 (téléchargé
// automatiquement si besoin), indépendamment du JDK utilisé par Android
// Studio lui-même pour exécuter l'IDE (JDK 25 dans votre cas).
kotlin {
    jvmToolchain(17)
}

// Empêche une dépendance transitive de faire remonter androidx.core vers une
// version trop récente, incompatible avec compileSdk 34 / AGP 8.5.2.
configurations.all {
    resolutionStrategy {
        force("androidx.core:core-ktx:1.13.1")
        force("androidx.core:core:1.13.1")
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.webkit:webkit:1.11.0")
}
