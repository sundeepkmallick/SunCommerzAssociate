import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
    alias(libs.plugins.kotlin.serialization)
}

val sunAiSdkSource = providers.gradleProperty("sunAiSdkSource")
    .orElse("mavenCentral")
    .get()
val localSunAiSdkVersion = providers.gradleProperty("sunAiSdkVersion")
    .orElse("0.1.0-rc.2")
    .get()

if (sunAiSdkSource == "mavenLocal") {
    configurations.configureEach {
        resolutionStrategy.eachDependency {
            if (requested.group == "io.github.sundeepkmallick") {
                useVersion(localSunAiSdkVersion)
                because("Use the locally published SunMobileAiAgentSdk version")
            }
        }
    }
}

android {
    namespace = "com.suncommerz.associate"
    compileSdk {
        version = release(37)
    }

    val localProperties = Properties().apply {
        val file = rootProject.file("local.properties")
        if (file.exists()) file.inputStream().use { load(it) }
    }

    fun firebaseProperty(name: String): String =
        providers.gradleProperty(name).orElse(localProperties.getProperty(name, "")).get()

    defaultConfig {
        applicationId = "com.suncommerz.associate"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "FIREBASE_API_KEY", "\"${firebaseProperty("FIREBASE_API_KEY")}\"")
        buildConfigField("String", "FIREBASE_APP_ID", "\"${firebaseProperty("FIREBASE_APP_ID")}\"")
        buildConfigField("String", "FIREBASE_PROJECT_ID", "\"${firebaseProperty("FIREBASE_PROJECT_ID")}\"")
        buildConfigField("String", "LITERT_LM_VERSION", "\"${libs.versions.litertLm.get()}\"")
    }

    packaging {
        resources {
            excludes += "/META-INF/INDEX.LIST"
            excludes += "/META-INF/DEPENDENCIES"
        }
    }

    buildTypes {
        release { optimization { enable = false } }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

ksp {
    arg("appfunctions:aggregateAppFunctions", "true")
}

dependencies {
    implementation(libs.sunmobile.agent.core)
    implementation(libs.sunmobile.agent.adk)
    implementation(libs.sunmobile.agent.firebase)
    implementation(libs.sunmobile.agent.litertlm)
    implementation(libs.sunmobile.agent.mlkit)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.window.size.class1)
    implementation(libs.androidx.compose.material.icons.extended.android)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.constraintlayout.compose.android)

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    ksp(libs.hilt.compiler)

    // AppFunctions remain host-owned; ML Kit Agent/Prompt are encapsulated by agent-mlkit.
    implementation(libs.androidx.appfunctions)
    ksp(libs.androidx.appfunctions.compiler)

    implementation(libs.kotlinx.serialization.json)

    // Host-owned Firebase initialization and debug App Check provider.
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.appcheck)
    debugImplementation(libs.firebase.appcheck.debug)
}
