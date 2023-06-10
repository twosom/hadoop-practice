terraform {
  required_providers {
    docker = {
      source  = "kreuzwerker/docker"
      version = "3.0.2"
    }
  }
}

resource "docker_image" "hadoop_datanode" {
  name = "hadoop-datanode"
  build {
    context = "${path.module}/."
    tag     = ["hadoop-datanode:hadoop-3.3.5-java8-arm"]
  }
}