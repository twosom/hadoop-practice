application {
    mainClass.set("com.icloud.MaxTemperature")
}

tasks.jar {
    manifest {
        attributes("Main-Class" to "com.icloud.MaxTemperature")
    }
}
tasks.test {
    useJUnitPlatform()
}