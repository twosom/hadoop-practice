val hadoopVersion: String = "2.7.7"
dependencies {
    testImplementation("org.apache.mrunit:mrunit:1.1.0:hadoop2")
    implementation("org.apache.hadoop:hadoop-client:$hadoopVersion")
    implementation("org.apache.hadoop:hadoop-mapreduce-client-core:$hadoopVersion")
    implementation("org.apache.hadoop:hadoop-mapreduce-client-jobclient:$hadoopVersion")
    implementation("org.powermock:powermock-module-junit4:1.4.12")
    testImplementation("org.powermock:powermock-api-mockito2:2.0.9")
}

tasks {
    test {
        doLast {
            val output = File("${workingDir.path}/output") // 테스트 후 남아있는 output 디렉토리 삭제
            output.deleteRecursively()
        }
    }
}