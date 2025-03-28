plugins {
    alias(libs.plugins.y9.conventions.java)
    alias(libs.plugins.y9.lombok)
}

dependencies {
    api(platform(libs.y9.digitalbase.bom))
    api(platform(libs.y9.digitalbase.dependencies))

    api("net.risesoft:risenet-y9boot-properties")
    api("net.risesoft:risenet-y9boot-common-model")
    api("net.risesoft:risenet-y9boot-common-util")
    api("net.risesoft:risenet-y9boot-starter-idgenerator")
    api("net.risesoft:risenet-y9boot-starter-jpa-public")

    api("org.springframework.boot:spring-boot-autoconfigure")
    api("org.springframework:spring-webmvc")
    api("com.google.guava:guava")

    compileOnly("jakarta.servlet:jakarta.servlet-api")
}

description = "risenet-y9boot-api-interface-addressbook"
