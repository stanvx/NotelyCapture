import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	alias(libs.plugins.kotlinMultiplatform)
	alias(libs.plugins.androidLibrary)
}

kotlin {
	androidTarget {
		compilerOptions {
			jvmTarget.set(JvmTarget.JVM_17)
		}
	}

	listOf(
		iosX64(),
		iosArm64(),
		iosSimulatorArm64(),
	).forEach {
		it.binaries.framework {
			baseName = "audio"
		}
	}

	sourceSets {
		commonMain.dependencies {

			// coroutines
			implementation(libs.kotlinx.coroutines.core)

			// logging
			implementation(libs.napier)

			// koin
			implementation(libs.koin.core)
		}

		commonTest.dependencies {
			implementation(kotlin("test"))
		}

		androidMain.dependencies {
			implementation(libs.androidx.appcompat)
			implementation(libs.androidx.core)

			// Wav Recorder
			implementation(libs.android.wave.recorder)
		}
	}

	@Suppress("OPT_IN_USAGE")
	compilerOptions {
		freeCompilerArgs = listOf("-Xexpect-actual-classes")
	}
}

android {
	namespace = "core.audio"
	compileSdk = libs.versions.android.compileSdk.get().toInt()
	sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
	defaultConfig {
		minSdk = libs.versions.android.minSdk.get().toInt()
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}
}
