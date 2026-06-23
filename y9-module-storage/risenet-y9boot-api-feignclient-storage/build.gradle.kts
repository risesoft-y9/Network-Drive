plugins {
    alias(libs.plugins.y9.conventions.java)
}

dependencies {
    api(platform(libs.y9.digitalbase.bom))
    api(platform(libs.y9.digitalbase.dependencies))

    api("net.risesoft:risenet-y9boot-starter-openfeign")
    api(project(":y9-module-storage:risenet-y9boot-api-interface-storage"))
}

description = "risenet-y9boot-api-feignclient-storage"