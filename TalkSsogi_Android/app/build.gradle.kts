plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

android {
    namespace = "com.example.talkssogi"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.talkssogi"
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
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    // Indicator (page7)
    implementation(libs.circleIndicator)
    implementation(libs.androidx.ui.desktop)
    implementation(libs.mediation.test.suite)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    // api로그 보기
    implementation ("com.squareup.okhttp3:okhttp:4.9.3")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.3")

    // Lifecycle ViewModel KTX (viewModelScope)
    implementation(libs.lifecycle.viewmodel.ktx)
    // Fragment KTX (viewModels)
    implementation(libs.fragment.ktx)

    // Glide (이미지 로드 라이브러리 - page9)
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

    // API 관련
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0") // JSON 변환을 위한 Gson Converter
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0") // 문자열 변환을 위한 컨버터
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.2") // 로깅을 위한 Interceptor (선택 사항)
    implementation("com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:0.9.2")    //

    // 내장 서버로 이미지 받기
    implementation ("com.squareup.okhttp3:okhttp:4.9.1")
    implementation ("com.squareup.okio:okio:2.8.0")
    implementation ("com.squareup.picasso:picasso:2.71828")
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0") // Gson converter
    implementation ("com.squareup.retrofit2:adapter-rxjava2:2.9.0") // RxJava adapter


}