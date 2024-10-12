plugins {
    kotlin("jvm") version "1.8.10"
    id("org.jetbrains.compose") version "1.3.1"
    kotlin("plugin.serialization") version "1.8.0"
}

group = "org.chronusartcenter"
version = "1.0-SNAPSHOT"
val ktorVersion = "2.3.3"

dependencies {
    implementation(compose.desktop.currentOs)

    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.alibaba.fastjson2:fastjson2:2.0.26")
    implementation("log4j:log4j:1.2.17")
    implementation("org.apache.logging.log4j:log4j-api-kotlin:1.5.0")
    implementation("org.apache.logging.log4j:log4j-core:2.10.0")
    implementation("stax:stax:1.2.0")
    implementation("commons-codec:commons-codec:1.15")
    implementation("com.illposed.osc:javaosc-core:0.7")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")

    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-json:$ktorVersion")
    implementation("io.ktor:ktor-client-serialization:$ktorVersion")
}

compose.desktop {
    application {
        mainClass = "org.chronusartcenter.MainKt"
    }
}
