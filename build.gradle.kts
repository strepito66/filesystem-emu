
plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<JavaExec>("runFS") {
    group = "application"
    description = "Avvia Shell"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("org.example.Main") // Complete the main class
    standardInput = System.`in`
}

