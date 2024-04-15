allprojects {
    group = project.property("GROUP") as String
    version = project.property("VERSION_NAME") as String

    repositories {
        mavenCentral()
        google()
    }
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    google()
}
