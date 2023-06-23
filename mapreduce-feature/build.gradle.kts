tasks {
    test {
        doLast {
            val output = File("${workingDir.path}/output") // 테스트 후 남아있는 output 디렉토리 삭제
            output.deleteRecursively()
        }
    }
}