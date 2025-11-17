import java.util.Properties

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("maven-publish")
    id("signing")
}

android {
    namespace = "com.telnyx.voiceai.widget"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        
        // Load test assistant ID from local.properties
        val properties = Properties()
        val localPropertiesFile = project.rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(localPropertiesFile.inputStream())
        }
        val testAssistantId = properties.getProperty("TEST_ASSISTANT_ID") ?: "demo-assistant-id"
        buildConfigField("String", "TEST_ASSISTANT_ID", "\"$testAssistantId\"")
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    
    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // Telnyx WebRTC SDK
    implementation("com.github.team-telnyx:telnyx-webrtc-android:3.2.0")
    
    // Image loading
    implementation("io.coil-kt:coil-compose:2.5.0")
    
    // JSON parsing
    implementation("com.google.code.gson:gson:2.10.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

// Maven publishing configuration
val libraryVersion = "1.0.0"
val libraryGroupId = "com.telnyx"
val libraryArtifactId = "android-voice-ai-widget"

android {
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = libraryGroupId
            artifactId = libraryArtifactId
            version = libraryVersion

            afterEvaluate {
                from(components["release"])
            }

            pom {
                name.set("Telnyx Android Voice AI Widget")
                description.set("A standalone Android widget for Telnyx Voice AI Assistant integration")
                url.set("https://github.com/team-telnyx/android-telnyx-voice-ai-widget")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        id.set("telnyx")
                        name.set("Telnyx Team")
                        email.set("support@telnyx.com")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/team-telnyx/android-telnyx-voice-ai-widget.git")
                    developerConnection.set("scm:git:ssh://github.com/team-telnyx/android-telnyx-voice-ai-widget.git")
                    url.set("https://github.com/team-telnyx/android-telnyx-voice-ai-widget")
                }
            }
        }
    }

    repositories {
        maven {
            name = "Central"
            url = uri(layout.buildDirectory.dir("maven-central-publish"))
        }
    }
}

signing {
    val signingPassword = System.getenv("SIGNING_PASSWORD")

    // Use GPG command (works with keys imported by actions/setup-java)
    if (System.getenv("CI") == "true") {
        useGpgCmd()
        // Configure GPG to use the passphrase from environment
        if (signingPassword != null) {
            extra["signing.gnupg.keyName"] = System.getenv("SIGNING_KEY_ID") ?: ""
            extra["signing.gnupg.passphrase"] = signingPassword
        }
    } else {
        // Local development: use in-memory keys if available
        val signingKeyId = System.getenv("SIGNING_KEY_ID")
        val signingKey = System.getenv("GPG_KEY_CONTENTS")

        if (signingKeyId != null && signingKey != null && signingPassword != null) {
            useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
        }
    }
    sign(publishing.publications)
}
