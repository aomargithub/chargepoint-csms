plugins {
    kotlin("jvm") version "1.9.20" apply false
    kotlin("plugin.spring") version "1.9.20" apply false
    id("org.springframework.boot") version "3.1.5" apply false
    id("io.spring.dependency-management") version "1.1.3" apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    group = "com.chargepoint.csms"
    version = "1.0.0"
}
