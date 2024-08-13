plugins {
    id("com.android.application")
    id("com.google.gms.google-services") // Add this line
}

android {
    namespace = "com.ozkancakir.sprintplanningapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ozkancakir.sprintplanningapp"
        minSdk = 24
        targetSdk = 33
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
    // Add the Firebase BoM dependency
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))

    // Add the Firebase dependencies
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-auth")

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}

// Apply the Google services plugin at the bottom
apply(plugin = "com.google.gms.google-services")
