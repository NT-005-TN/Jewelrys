plugins {
    id("java")
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
}

group = "con.jewelry"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    runtimeOnly("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-core")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")

    implementation("org.springframework.kafka:spring-kafka")

    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // Spring Security
    implementation("org.springframework.boot:spring-boot-starter-security")

    // Для создания и проверки JWT токенов
    implementation("io.jsonwebtoken:jjwt-api:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3")

    //email
    implementation("org.springframework.boot:spring-boot-starter-mail")

    // Jakarta Validation (для @Valid, @NotBlank и т.д.)
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")
    // Jakarta Persistence (JPA)
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")
    // Jakarta Transaction (JTA)
    implementation("jakarta.transaction:jakarta.transaction-api:2.0.1")
    // Jakarta Servlet API (если нужен для web-слоя)
    compileOnly("jakarta.servlet:jakarta.servlet-api:6.0.0")
    // Jakarta Annotation API
    compileOnly("jakarta.annotation:jakarta.annotation-api:2.1.1")
    // Jakarta Mail (если используете более новую реализацию)
    implementation("com.sun.mail:jakarta.mail:2.0.1")

    // Redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis");

    //OpenCSV
    implementation("com.opencsv:opencsv:5.7.1")

    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
}

tasks.test {
    useJUnitPlatform()
}