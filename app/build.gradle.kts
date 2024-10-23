plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "mobile.application.footcardz_ui"
    compileSdk = 35

    defaultConfig {
        applicationId = "mobile.application.footcardz_ui"
        minSdk = 35 // Ensure that the minSdk is supported by the devices you are targeting
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false // Consider enabling this in production builds
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        // If you are using Java 11 or newer, update these versions:
        // sourceCompatibility = JavaVersion.VERSION_11
        // targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.leanback)
    implementation(libs.glide)
    annotationProcessor(libs.compiler)
    implementation(libs.jwtdecode.v201)

    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(libs.logging.interceptor.v4100)
}
