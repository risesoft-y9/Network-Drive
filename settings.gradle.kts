pluginManagement {
    // Include 'plugins build' to define convention plugins.
    //includeBuild("build-logic")

    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}


dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    //引入y9的版本定义
    versionCatalogs {
        create("y9libs") {
            from("net.risesoft.y9:risenet-gradle-version-catalog:9.7.0-RC3")
        }
    }
}

rootProject.name = "y9-storage"
include(":y9-module-storage:risenet-y9boot-api-feignclient-storage")
include(":y9-module-storage:risenet-y9boot-api-interface-storage")
include(":y9-module-storage:risenet-y9boot-server-storage")