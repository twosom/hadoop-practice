terraform {
  required_providers {
    docker = {
      source  = "kreuzwerker/docker"
      version = "3.0.2"
    }
  }
}

resource "docker_image" "hadoop_namenode" {
  name = "hadoop-namenode"
  build {
    context = "${path.module}/."
    tag     = ["hadoop-namenode:hadoop-3.3.5-java8-arm"]
  }
}