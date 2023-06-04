/**
 * 기본 하둡 버전 = 3.3.5
 */
val hadoopVersion: String = "3.3.5"

/**
 * Local 에서 하둡 테스트 하기 위한 의존성 추가 설정
 */
dependencies {
    testImplementation("junit:junit:4.13.1")
    testImplementation("org.mockito:mockito-core:5.3.1")
    testImplementation("org.apache.hadoop:hadoop-minicluster:$hadoopVersion")
}

tasks.test {
    useJUnitPlatform()
}