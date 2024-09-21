plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.zenbo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.zenbo"
        minSdk = 26
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    testImplementation("junit:junit:4.13.2") // JUnit 4 的最新版本
    implementation("androidx.appcompat:appcompat:1.6.1") // 目前的最新版本
    implementation(project(":RobotActivityLibrary"))
    implementation(fileTree(mapOf("include" to listOf("*.jar"), "dir" to "libs"))) // 如果有本地 JAR 文件

}