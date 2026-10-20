plugins {
    id("com.android.application")
}

// The key that signs the release build.  When it is not given the release
// build is signed with the key of the debug build, so that the apk is always
// something that can be put on a phone.
//
//   ./gradlew assembleRelease \
//       -Pchess.keystore=/path/to/key.jks \
//       -Pchess.keystore.password=... \
//       -Pchess.key.alias=... \
//       -Pchess.key.password=...
val keystore: String? = providers.gradleProperty("chess.keystore").orNull

android {
    namespace = "com.mmunki.chess.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.mmunki.chess"
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    // The rules, the computer and the rating are the same code as the game
    // on the computer.  Only the part that draws the game is its own.
    sourceSets["main"].java.setSrcDirs(listOf("src/main/java", "../../src/main/java"))
    sourceSets["main"].java.exclude(
        "com/mmunki/chess/Main.java",
        "com/mmunki/chess/ui/**",
        "com/mmunki/chess/audio/**",
        "com/mmunki/chess/util/**",
    )

    signingConfigs {
        if (keystore != null) {
            create("release") {
                storeFile = file(keystore)
                storePassword = providers.gradleProperty("chess.keystore.password").orNull ?: ""
                keyAlias = providers.gradleProperty("chess.key.alias").orNull ?: ""
                keyPassword = providers.gradleProperty("chess.key.password").orNull ?: ""
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = if (keystore != null) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.14.1")
}
