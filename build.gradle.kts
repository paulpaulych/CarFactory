plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.0.9"
}

group = "com.paulpaulych.carfactory"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.openjfx:javafx-control:11.0.2")
    implementation("org.openjfx:javafx-fxml:11.0.2")

    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    testImplementation("junit:junit:4.12")
}

tasks {
    javafx {
        version = "15"
        modules("javafx.controls", "javafx.fxml")
    }
    java {
        sourceCompatibility = JavaVersion.VERSION_15
        targetCompatibility = JavaVersion.VERSION_15
    }
    withType<JavaCompile>().forEach {
        it.options.compilerArgs.add("--enable-preview")
    }
    application {
        applicationDefaultJvmArgs = listOf("--enable-preview")
    }
    jar {
        manifest {
            attributes["Main-Class"] = "carfactory.Main"
        }
    }
}

configurations {
    setProperty("mainClassName", "carfactory.Main")
}
