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
    implementation("org.apache.logging.log4j:log4j-core:2.11.2")
    testImplementation("junit:junit:4.12")
}

tasks {
    javafx {
        version = "15"
        modules("javafx.controls", "javafx.fxml")
    }
    java {
        sourceCompatibility = JavaVersion.VERSION_14
        targetCompatibility = JavaVersion.VERSION_14
    }
    application {
        applicationDefaultJvmArgs = listOf(" -Dlog4j.configurationFile=file:src/main/resources/log4j2.properties")
    }
    jar {
        manifest {
            attributes["Main-Class"] = "carfactory.Main"
        }
    }
}
project.setProperty("mainClassName", "carfactory.Main")