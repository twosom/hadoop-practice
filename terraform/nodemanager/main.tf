terraform {
  required_providers {
    docker = {
      source  = "kreuzwerker/docker"
      version = "3.0.2"
    }
  }
}

resource "docker_image" "hadoop_nodemanager" {
  name = "hadoop_nodemanager"
  build {
    context = "${path.module}/."
    tag     = ["hadoop_nodemanager:hadoop-3.3.5-java8-arm"]
  }
}