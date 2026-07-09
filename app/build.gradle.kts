plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// 固定签名：CI 通过环境变量提供 keystore（来自 GitHub Secrets 解码），
// 使每次构建的 APK 签名一致，可直接覆盖安装、不丢数据。
// 本地无该环境变量时回退到默认 debug 签名。
val signingStoreFile: String? = System.getenv("SIGNING_STORE_FILE")
val signingKeyPassword: String? = System.getenv("SIGNING_KEY_PASSWORD")
val hasStableSigning =
    !signingStoreFile.isNullOrBlank() && !signingKeyPassword.isNullOrBlank() && file(signingStoreFile).exists()

android {
    namespace = "com.bodycontrol"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.bodycontrol"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    signingConfigs {
        create("shared") {
            if (hasStableSigning) {
                storeFile = file(signingStoreFile!!)
                storePassword = signingKeyPassword
                keyAlias = "bodycontrol"
                keyPassword = signingKeyPassword
            }
        }
    }

    buildTypes {
        getByName("debug") {
            if (hasStableSigning) signingConfig = signingConfigs.getByName("shared")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (hasStableSigning) signingConfig = signingConfigs.getByName("shared")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.2")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("io.coil-kt:coil-gif:2.6.0")
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1")
}
