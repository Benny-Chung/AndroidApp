plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "hkucs.example.comp3330project"
    compileSdk = 34

    defaultConfig {
        applicationId = "hkucs.example.comp3330project"
        minSdk = 28
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
        useLibrary("org.apache.http.legacy")
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

    buildFeatures {
        viewBinding = true
        mlModelBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("org.tensorflow:tensorflow-lite-support:0.1.0")
    implementation("org.tensorflow:tensorflow-lite-metadata:0.1.0")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.3.0")

    implementation("io.ktor:ktor-client-android:2.3.6")
    implementation("com.aallam.openai:openai-client:3.5.1")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation ("com.squareup.retrofit2:converter-jackson:2.7.2")
    implementation ("com.fasterxml.jackson.core:jackson-databind:2.10.3")
    implementation ("com.fasterxml.jackson.core:jackson-core:2.10.3")
    implementation ("com.fasterxml.jackson.core:jackson-annotations:2.10.3")
    implementation ("mysql:mysql-connector-java:5.1.49")
    implementation ("com.google.code.gson:gson:2.8.5")
    //implementation("android.support.v7.app.ActionBar")
    //implementation("com.readystatesoftware.sqliteasset:sqliteassethelper:+")

    implementation ("com.google.android.gms:play-services-vision:20.1.3")

}
