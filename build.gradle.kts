plugins {
	java
	id("org.springframework.boot") version "3.3.2"
	id("io.spring.dependency-management") version "1.1.6"
}

group = "ru.library"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	// https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-thymeleaf
	val thymeleafVersion = "3.3.2"
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf:$thymeleafVersion")
	// https://mvnrepository.com/artifact/org.modelmapper/modelmapper
	val modelmapperVersion = "3.2.1"
	implementation("org.modelmapper:modelmapper:$modelmapperVersion")
	// https://mvnrepository.com/artifact/com.auth0/java-jwt
	val jwtVersion = "4.4.0"
	implementation("com.auth0:java-jwt:$jwtVersion")
	// https://mvnrepository.com/artifact/jmimemagic/jmimemagic
	implementation("jmimemagic:jmimemagic:0.1.2")
	compileOnly("org.projectlombok:lombok")
	runtimeOnly("org.postgresql:postgresql")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
