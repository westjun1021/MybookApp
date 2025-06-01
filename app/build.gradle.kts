plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // AndroidX / Material
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // Retrofit + Scalars + Gson + Logging
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")

    // DrawerLayout
    implementation("androidx.drawerlayout:drawerlayout:1.1.1")
    implementation ("androidx.recyclerview:recyclerview:1.3.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")

    // ───────────────────────────────────────────────────────────────────────────
    // Firebase BoM 선언 (버전을 여기서만 관리)
    implementation(platform("com.google.firebase:firebase-bom:32.0.0"))

    // Firebase Auth (KTX) – 버전 생략
    implementation("com.google.firebase:firebase-auth-ktx")

    // Firestore (KTX) – 버전 생략
    implementation("com.google.firebase:firebase-firestore-ktx")
    // ───────────────────────────────────────────────────────────────────────────
}
