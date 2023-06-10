plugins {
    java
    application
}

/**
 * 기본 하둡 버전 = 3.3.5
 */
val hadoopVersion: String = "3.3.5"


subprojects {
    group = "com.icloud"
    version = "1.0-SNAPSHOT"

    plugins.apply {
        apply("java")
        apply("application")
    }

    repositories {
        mavenCentral()
    }

    /**
     * 하둡 호환성 위해 Java 1.8 버전 사용
     */
    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    dependencies {
        testImplementation(platform("org.junit:junit-bom:5.9.1"))
        testImplementation("org.junit.jupiter:junit-jupiter")
        testImplementation("junit:junit:4.13.1")
        testImplementation("org.mockito:mockito-core:5.3.1")
        testImplementation("org.apache.hadoop:hadoop-minicluster:$hadoopVersion")
        if (project.name != "mapreduce-dev") {
            implementation("org.apache.hadoop:hadoop-client:$hadoopVersion")
            implementation("org.apache.hadoop:hadoop-mapreduce-client-core:$hadoopVersion")
            implementation("org.apache.hadoop:hadoop-mapreduce-client-jobclient:$hadoopVersion")
        }
        implementation("net.bytebuddy:byte-buddy:1.10.22")
        testImplementation("org.objenesis:objenesis:3.2")

    }
    tasks.build {
        val yellow = "\u001B[33m"
        val green = "\u001B[32m"
        val buildStartTime = System.currentTimeMillis()
        doFirst {
            println("${yellow}${project.name} build Started")
        }
        doLast {
            val buildEndTime = System.currentTimeMillis()
            val buildDuration = buildEndTime - buildStartTime

            println("""
                ${green}${this.project.name} build completed
                Build Time : $buildDuration ms
            """.trimIndent())
        }
    }
    tasks.test {
        useJUnitPlatform()
    }
}