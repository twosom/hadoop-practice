terraform {
  required_providers {
    docker = {
      source  = "kreuzwerker/docker"
      version = "3.0.2"
    }
  }
}

provider "docker" {
  host = "unix:///var/run/docker.sock"
}

module "base_image" {
  source = "./base"
}

module "datanode_image" {
  source     = "./datanode"
  depends_on = [module.base_image]
  providers  = {
    docker = docker
  }
}

module "historyserver_image" {
  source     = "./historyserver"
  depends_on = [module.base_image]
  providers  = {
    docker = docker
  }
}

module "namenode_image" {
  source     = "./namenode"
  depends_on = [module.base_image]
  providers  = {
    docker = docker
  }
}

module "nodemanager_image" {
  source     = "./nodemanager"
  depends_on = [module.base_image]
  providers  = {
    docker = docker
  }
}

module "resourcemanager_image" {
  source     = "./resourcemanager"
  depends_on = [module.base_image]
  providers  = {
    docker = docker
  }
}

module "hiveserver_image" {
  source     = "./hive"
  depends_on = [module.base_image]
  providers  = {
    docker = docker
  }
}

resource "docker_network" "default" {
  name = "docker-hadoop-poc"
}

resource "docker_volume" "namenode" {
  name = "namenode"
}
resource "docker_volume" "datanode" {
  name = "datanode"
}
resource "docker_volume" "hadoop_historyserver" {
  name = "hadoop_historyserver"
}

resource "docker_container" "namenode" {
  name  = "namenode"
  image = module.namenode_image.image_name
  volumes {
    volume_name    = docker_volume.namenode.name
    container_path = "/hadoop/dfs/name"
  }
  env = concat(local.default_env, ["CLUSTER_NAME=test"])
  ports {
    internal = 9870
    external = 9870
  }
  ports {
    internal = 8020
    external = 8020
  }

  networks_advanced {
    name = docker_network.default.name
  }
}

resource "docker_container" "datanode" {
  name  = "datanode"
  image = module.datanode_image.image_name
  volumes {
    volume_name    = docker_volume.datanode.name
    container_path = "/hadoop/dfs/data"
  }
  env = concat(local.default_env, ["SERVICE_PRECONDITION=namenode:9870"])
  ports {
    internal = 9864
    external = 9864
  }

  networks_advanced {
    name = docker_network.default.name
  }
}

resource "docker_container" "resourcemanager" {
  name  = "resourcemanager"
  image = module.resourcemanager_image.image_name
  env   = concat(local.default_env, ["SERVICE_PRECONDITION=namenode:9000 namenode:9870 datanode:9864"])
  ports {
    internal = 8032
    external = 8032
  }

  ports {
    internal = 8088
    external = 8088
  }

  networks_advanced {
    name = docker_network.default.name
  }
}

resource "docker_container" "nodemanager" {
  name  = "nodemanager"
  image = module.nodemanager_image.image_name
  env   = concat(local.default_env, [
    "SERVICE_PRECONDITION=namenode:9000 namenode:9870 datanode:9864 resourcemanager:8088"
  ])
  ports {
    internal = 8042
    external = 8042
  }

  networks_advanced {
    name = docker_network.default.name
  }
}

resource "docker_container" "historyserver" {
  name  = "historyserver"
  image = module.historyserver_image.image_name
  env   = concat(local.default_env, [
    "SERVICE_PRECONDITION=namenode:9000 namenode:9870 datanode:9864 resourcemanager:8088"
  ])
  volumes {
    volume_name    = docker_volume.hadoop_historyserver.name
    container_path = "/hadoop/yarn/timeline"
  }

  ports {
    internal = 8188
    external = 8188
  }

  networks_advanced {
    name = docker_network.default.name
  }
}

resource "docker_container" "hive-metastore-mysql" {
  name    = "hive-metastore-mysql"
  image   = "mysql:latest"
  command = ["--default-authentication-plugin=mysql_native_password", "--lower-case-table-names=1"]
  env     = [
    "MYSQL_ROOT_PASSWORD=password",
    "MYSQL_DATABASE=metastore_v1",
    "MYSQL_USER=hive",
    "MYSQL_PASSWORD=hive"
  ]

  ports {
    internal = 3306
    external = 3306
  }
  networks_advanced {
    name = docker_network.default.name
  }
}

resource "docker_container" "hiveserver" {
  name       = "hive-server"
  image      = module.hiveserver_image.image_name
  depends_on = [
    docker_container.resourcemanager,
    docker_container.nodemanager,
    docker_container.historyserver,
    docker_container.hivemetastore
  ]

  volumes {
    host_path      = abspath("./dummy_data")
    container_path = "/opt/dummy_data"
  }
  env = concat(local.default_env, [
    "SERVICE_PRECONDITION=hive-metastore:9083",
    "DUMMY_DATA=1"
  ])
  ports {
    internal = 10000
    external = 10000
  }
  networks_advanced {
    name = docker_network.default.name
  }
}

resource "docker_container" "hivemetastore" {
  name       = "hive-metastore"
  image      = module.hiveserver_image.image_name
  depends_on = [
    docker_container.hive-metastore-mysql
  ]
  env     = concat(local.default_env, ["SERVICE_PRECONDITION=namenode:9870 datanode:9864 hive-metastore-mysql:3306"])
  command = ["startup.sh", "metastore"]
  ports {
    external = 9083
    internal = 9083
  }
  networks_advanced {
    name = docker_network.default.name
  }
}

resource "docker_container" "zookeeper" {
  image   = "zookeeper:latest"
  restart = "always"
  name    = "zookeeper"
  ports {
    internal = 2181
    external = 2181
  }
  networks_advanced {
    name = docker_network.default.name
  }
}

locals {
  hadoop_version = "3.3.5"
  default_env    = [
    "HIVE_SITE_CONF_javax_jdo_option_ConnectionURL=jdbc:mysql://hive-metastore-mysql/metastore_v1",
    "HIVE_SITE_CONF_javax_jdo_option_ConnectionDriverName=com.mysql.cj.jdbc.Driver",
    "HIVE_SITE_CONF_javax_jdo_option_ConnectionUserName=hive",
    "HIVE_SITE_CONF_javax_jdo_option_ConnectionPassword=hive",
    "HIVE_SITE_CONF_datanucleus_autoCreateSchema=false",
    "HIVE_SITE_CONF_hive_metastore_uris=thrift://hive-metastore:9083",
    "HDFS_CONF_dfs_namenode_datanode_registration_ip___hostname___check=false",
    "CORE_CONF_fs_defaultFS=hdfs://namenode:9000",
    "CORE_CONF_hadoop_http_staticuser_user=root",
    "CORE_CONF_hadoop_proxyuser_hue_hosts=*",
    "CORE_CONF_hadoop_proxyuser_hue_groups=*",
    "CORE_CONF_fs_trash_interval=360",
    "CORE_CONF_fs_trash_checkpoint_interval=60",
    "CORE_CONF_io_compression_codecs=org.apache.hadoop.io.compress.SnappyCodec",
    "HDFS_CONF_dfs_webhdfs_enabled=true",
    "HDFS_CONF_dfs_permissions_enabled=false",
    "HDFS_CONF_dfs_namenode_datanode_registration_ip___hostname___check=false",
    "YARN_CONF_yarn_log___aggregation___enable=true",
    "YARN_CONF_yarn_log_server_url=http://historyserver:8188/applicationhistory/logs/",
    "YARN_CONF_yarn_resourcemanager_recovery_enabled=true",
    "YARN_CONF_yarn_resourcemanager_store_class=org.apache.hadoop.yarn.server.resourcemanager.recovery.FileSystemRMStateStore",
    "YARN_CONF_yarn_resourcemanager_scheduler_class=org.apache.hadoop.yarn.server.resourcemanager.scheduler.capacity.CapacityScheduler",
    "YARN_CONF_yarn_scheduler_capacity_root_default_maximum___allocation___mb=8192",
    "YARN_CONF_yarn_scheduler_capacity_root_default_maximum___allocation___vcores=4",
    "YARN_CONF_yarn_resourcemanager_fs_state___store_uri=/rmstate",
    "YARN_CONF_yarn_resourcemanager_system___metrics___publisher_enabled=true",
    "YARN_CONF_yarn_resourcemanager_hostname=resourcemanager",
    "YARN_CONF_yarn_resourcemanager_address=resourcemanager:8032",
    "YARN_CONF_yarn_resourcemanager_scheduler_address=resourcemanager:8030",
    "YARN_CONF_yarn_resourcemanager_resource__tracker_address=resourcemanager:8031",
    "YARN_CONF_yarn_timeline___service_enabled=true",
    "YARN_CONF_yarn_timeline___service_generic___application___history_enabled=true",
    "YARN_CONF_yarn_timeline___service_hostname=historyserver",
    "YARN_CONF_mapreduce_map_output_compress=true",
    "YARN_CONF_mapred_map_output_compress_codec=org.apache.hadoop.io.compress.SnappyCodec",
    "YARN_CONF_yarn_nodemanager_resource_memory___mb=16384",
    "YARN_CONF_yarn_nodemanager_resource_cpu___vcores=8",
    "YARN_CONF_yarn_nodemanager_disk___health___checker_max___disk___utilization___per___disk___percentage=98.5",
    "YARN_CONF_yarn_nodemanager_remote___app___log___dir=/app-logs",
    "YARN_CONF_yarn_nodemanager_aux___services=mapreduce_shuffle",
    "MAPRED_CONF_mapreduce_framework_name=yarn",
    "MAPRED_CONF_mapred_child_java_opts=-Xmx4096m",
    "MAPRED_CONF_mapreduce_map_memory_mb=4096",
    "MAPRED_CONF_mapreduce_reduce_memory_mb=8192",
    "MAPRED_CONF_mapreduce_map_java_opts=-Xmx3072m",
    "MAPRED_CONF_mapreduce_reduce_java_opts=-Xmx6144m",
    "MAPRED_CONF_yarn_app_mapreduce_am_env=HADOOP_MAPRED_HOME=/opt/hadoop-${local.hadoop_version}/",
    "MAPRED_CONF_mapreduce_map_env=HADOOP_MAPRED_HOME=/opt/hadoop-${local.hadoop_version}/",
    "MAPRED_CONF_mapreduce_reduce_env=HADOOP_MAPRED_HOME=/opt/hadoop-${local.hadoop_version}/",
    "HIVE_SITE_CONF_javax_jdo_option_ConnectionURL=jdbc:mysql://hive-metastore-mysql/metastore",
    "HIVE_SITE_CONF_javax_jdo_option_ConnectionDriverName=com.mysql.cj.jdbc.Driver",
    "HIVE_SITE_CONF_javax_jdo_option_ConnectionUserName=hive",
    "HIVE_SITE_CONF_javax_jdo_option_ConnectionPassword=hive",
    "HIVE_SITE_CONF_datanucleus_autoCreateSchema=false",
    "HIVE_SITE_CONF_hive_metastore_uris=thrift://hive-metastore:9083",
    "HIVE_SITE_CONF_hive_zookeeper_quorum=zookeeper:2181",
    "HIVE_SITE_CONF_hive_server2_zookeeper_namespace=hiveserver2",
    "HIVE_SITE_CONF_hive_server2_support_dynamic_service_discovery=true"
  ]
}