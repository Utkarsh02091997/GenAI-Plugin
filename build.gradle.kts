// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

plugins {
  id("java")
  id("org.jetbrains.intellij") version "1.15.0"
  id("org.springframework.boot") version "2.7.16"
  id("io.spring.dependency-management") version "1.0.15.RELEASE"

}

group = "org.intellij.sdk"
version = "2.0.0"

repositories {
  mavenCentral()
}

java {
  sourceCompatibility = JavaVersion.VERSION_17
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter")
  implementation("org.springframework:spring-web:6.0.11")
  implementation("org.springframework.boot:spring-boot-configuration-processor:3.1.0")
  // Add other Spring dependencies as needed
}

// See https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
  version.set("2022.3.3")
}

tasks {
  buildSearchableOptions {
    enabled = false
  }

  patchPluginXml {
    version.set("${project.version}")
    sinceBuild.set("223")
    untilBuild.set("232.*")
  }
}
