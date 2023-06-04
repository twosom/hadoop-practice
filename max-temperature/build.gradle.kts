val mainClassName = "com.icloud.MaxTemperature"

application {
    mainClass.set(mainClassName)
}

tasks.jar {
    manifest {
        attributes("Main-Class" to mainClassName)
    }
}
tasks.test {
    useJUnitPlatform()
}