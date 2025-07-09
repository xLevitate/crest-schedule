plugins {
    id("java-library")
    id("maven-publish")
}

group = "com.moocrest"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(17)
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            
            pom {
                name.set("Crest Schedule")
                description.set("A powerful and intuitive task scheduling library for Bukkit/Paper plugins")
                url.set("https://github.com/xLevitate/crest-schedule")
                
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                
                developers {
                    developer {
                        id.set("xLevitate")
                        name.set("xLevitate")
                    }
                }
                
                scm {
                    connection.set("scm:git:git://github.com/xLevitate/crest-schedule.git")
                    developerConnection.set("scm:git:ssh://github.com:xLevitate/crest-schedule.git")
                    url.set("https://github.com/xLevitate/crest-schedule/tree/main")
                }
            }
        }
    }
}