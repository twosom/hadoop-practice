terraform {
  required_providers {
    docker = {
      source  = "kreuzwerker/docker"
      version = "3.0.2"
    }
  }
}

resource "docker_image" "hadoop_resourcemanager" {
  name = "hadoop_resourcemanager"
  build {
    context = "${path.module}/."
    tag     = ["hadoop_resourcemanager:hadoop-3.3.5-java8-arm"]
  }
}