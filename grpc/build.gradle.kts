import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.google.protobuf.gradle.*;

buildscript {
	dependencies {
		classpath("com.google.protobuf:protobuf-gradle-plugin:0.8.13")
	}
}
plugins {
//	id("org.springframework.boot") version "2.5.5"
//	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	id("com.google.protobuf") version "0.8.13"
	kotlin("jvm") version "1.5.31"
//	kotlin("plugin.spring") version "1.5.31"
//	id("org.jlleitschuh.gradle.ktlint") version "9.2.1"
}

group = "com.kotlingrpc"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

allprojects {
	repositories {
		mavenLocal()
		mavenCentral()
		jcenter()
		google()
	}

//	apply(plugin = "org.jlleitschuh.gradle.ktlint")
}

dependencies {
//	implementation("org.springframework.boot:spring-boot-starter-web")
//	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("io.grpc:grpc-protobuf:1.33.1")
	implementation("io.grpc:grpc-stub:1.33.1")
	implementation("io.grpc:grpc-netty:1.33.1")
	compileOnly("javax.annotation:javax.annotation-api:1.3.2")
	api("com.google.protobuf:protobuf-java-util:3.13.0")
	implementation("io.grpc:grpc-all:1.33.1")
	api("io.grpc:grpc-kotlin-stub:0.2.1")
	implementation("io.grpc:protoc-gen-grpc-kotlin:1.1.0")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
	implementation("com.google.protobuf:protobuf-gradle-plugin:0.8.13")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
//	developmentOnly("org.springframework.boot:spring-boot-devtools")
//	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "1.8"
	}
}

//tasks.withType<Test> {
//	useJUnitPlatform()
//}

protobuf {
	protoc{
		artifact = "com.google.protobuf:protoc:3.14.0"
	}
	generatedFilesBaseDir = "$projectDir/src/main/kotlin/com.kotlingrpc.demoGrpc/generated"
	plugins {
		id("grpc"){
			artifact = "io.grpc:protoc-gen-grpc-java:1.36.0"
		}
		id("grpckt") {
			artifact = "io.grpc:protoc-gen-grpc-kotlin:1.1.0:jdk7@jar"
		}
	}
	generateProtoTasks {
		all().forEach {
			it.plugins {
				id("grpc")
				id("grpckt")
			}
		}
	}
}