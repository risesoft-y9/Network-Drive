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
            from("net.risesoft.y9:risenet-gradle-version-catalog:9.7.0-M5")
        }
    }
}

rootProject.name = "y9-storage"
include(":risenet-y9boot-server-storage")

include(":risenet-y9boot-api-auth")
include(":risenet-y9boot-server-dataassets")
