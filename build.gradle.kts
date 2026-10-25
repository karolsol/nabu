val mockitoAgent = configurations.create("mockitoAgent")

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.liquibase:liquibase-core:5.0.4")
    }
}

plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.liquibase)
    alias(libs.plugins.freefair.lombok)
}

group = "com.karolsol"
version = "0.0.1-SNAPSHOT"

val mapstructVersion = "1.6.3"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.modulith:spring-modulith-bom:2.1.1")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-hateoas")
    implementation("org.springframework.boot:spring-boot-starter-liquibase")
    implementation("org.springframework.boot:spring-boot-starter-restclient")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.modulith:spring-modulith-starter-core")
    implementation(libs.springdoc.openapi.webmvc.ui)
    implementation(libs.mapstruct)

    runtimeOnly("org.postgresql:postgresql")

    annotationProcessor(libs.lombok.mapstruct.binding)
    annotationProcessor(libs.mapstruct.processor)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.modulith:spring-modulith-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    mockitoAgent("org.mockito:mockito-core") { isTransitive = false }

    val liquibaseRuntime = configurations.getByName("liquibaseRuntime")
    liquibaseRuntime(libs.picocli)
    liquibaseRuntime(libs.liquibase.hibernate)
    liquibaseRuntime(sourceSets.main.get().output)
}

configurations.named("liquibaseRuntime") {
    extendsFrom(configurations.runtimeClasspath.get())
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgumentProviders.add(CommandLineArgumentProvider {
        listOf("-javaagent:${mockitoAgent.asPath}")
    })
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(
        listOf(
			// Add @Component to generated mappers without adding componentModel = "spring" in every mapper
			"-Amapstruct.defaultComponentModel=spring",
            // Fail compilation if a mapping is missing
			"-Amapstruct.unmappedTargetPolicy=ERROR"
        )
    )
}

liquibase {
    activities.register("main") {
        arguments = mapOf(
            "changelogFile" to "src/main/resources/db/changelog/db.changelog-master.yaml",
            "url" to "jdbc:postgresql://localhost:5432/nabu",
            "username" to "admin",
            "password" to "password",
            "driver" to "org.postgresql.Driver",
            "referenceUrl" to "hibernate:spring:com.karolsol.nabu?dialect=org.hibernate.dialect.PostgreSQLDialect",
            "referenceDriver" to "liquibase.ext.hibernate.database.connection.HibernateDriver"
        )
    }
    runList = "main"
}
