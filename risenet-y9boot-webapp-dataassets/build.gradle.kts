plugins {
    alias(libs.plugins.y9.docker)
    alias(libs.plugins.y9.conventions.war)
    alias(libs.plugins.y9.lombok)
}

dependencies {
    api(platform(libs.y9.digitalbase.bom))
    api(platform(libs.y9.digitalbase.dependencies))
    providedRuntime(platform(libs.y9.digitalbase.dependencies))

    api("net.risesoft:risenet-y9boot-starter-sso-oauth2-resource")
    api("net.risesoft:risenet-y9boot-starter-jpa-public")
    api("net.risesoft:risenet-y9boot-starter-jpa-tenant")
    api("net.risesoft:risenet-y9boot-starter-multi-tenant")
    api("net.risesoft:risenet-y9boot-starter-log")
    api("net.risesoft:risenet-y9boot-api-feignclient-platform")
    api("net.risesoft:risenet-y9boot-support-file-service-ftp")
    api("net.risesoft:risenet-y9boot-support-file-service-local")
    api("net.risesoft:risenet-y9boot-idcode")
    api("net.risesoft:risenet-y9boot-common-sqlddl")
    api("net.risesoft:risenet-y9boot-starter-security")
    api("net.risesoft:risenet-y9boot-starter-web")
    api("net.risesoft:risenet-y9boot-common-nacos")
    api("org.springframework.boot:spring-boot-starter-validation")
    api("com.google.guava:guava")

    compileOnly("jakarta.servlet:jakarta.servlet-api")
    providedRuntime("org.springframework.boot:spring-boot-starter-tomcat")
}

description = "risenet-y9boot-webapp-dataassets"

val finalName = "dataAssets"
y9Docker {
    appName = finalName
}

y9War {
    archiveBaseName = finalName
}