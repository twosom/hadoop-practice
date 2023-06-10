terraform {
  required_providers {
    docker = {
      source  = "kreuzwerker/docker"
      version = "3.0.2"
    }
  }
}

resource "docker_image" "hadoop_base" {
  name = "hadoop-base"
  build {
    context = "${path.module}/."
    tag     = ["hadoop-base:hadoop-3.3.5-java8-arm"]
  }
}