CREATE TABLE `rhea_job`
(
    `id`           bigint(13) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `gmt_create`   bigint(13) unsigned NOT NULL DEFAULT '0' COMMENT '创建时间',
    `gmt_modified` bigint(13) unsigned NOT NULL DEFAULT '0' COMMENT '修改时间',
    `status`       tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除',
    `create_user`  varchar(16) NOT NULL DEFAULT  '' COMMENT '创建用户',
    `modify_user`  varchar(16) NOT NULL DEFAULT  '' COMMENT '创建用户',
    `env` varchar(8) NOT NULL DEFAULT '' COMMENT '区域',
    `job_name`         varchar(128)        NOT NULL DEFAULT '' COMMENT '任务名',
    `job_type`    varchar(16) NOT NULL DEFAULT '' COMMENT '任务类型',
    `job_desc` varchar(512) NOT NULL DEFAULT  '' COMMENT '任务描述',
    `alarm_config` varchar (1024) NOT NULL DEFAULT  '' COMMENT '告警配置',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4 COMMENT ='rhea任务表';

  CREATE TABLE `rhea_job_detail`
(
    `id`           bigint(13) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `gmt_create`   bigint(13) unsigned NOT NULL DEFAULT '0' COMMENT '创建时间',
    `gmt_modified` bigint(13) unsigned NOT NULL DEFAULT '0' COMMENT '修改时间',
    `status`       tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除',
    `create_user`  varchar(16) NOT NULL DEFAULT  '' COMMENT '创建用户',
    `modify_user`  varchar(16) NOT NULL DEFAULT  '' COMMENT '创建用户',
    `env` varchar(8) NOT NULL DEFAULT '' COMMENT '区域',
    `job_id` bigint(13) unsigned NOT NULL DEFAULT  0 COMMENT '任务ID',
    `version` int(11) unsigned NOT NULL DEFAULT  0 COMMENT '版本号',
    `text` text COMMENT '文本',
    `conf` text COMMENT '配置',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4 COMMENT ='rhea任务信息配置表';

  CREATE TABLE `rhea_job_action`
(
    `id`           bigint(13) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `gmt_create`   bigint(13) unsigned NOT NULL DEFAULT '0' COMMENT '创建时间',
    `gmt_modified` bigint(13) unsigned NOT NULL DEFAULT '0' COMMENT '修改时间',
    `status`       tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除',
    `create_user`  varchar(16) NOT NULL DEFAULT  '' COMMENT '创建用户',
    `modify_user`  varchar(16) NOT NULL DEFAULT  '' COMMENT '创建用户',
    `env` varchar(8) NOT NULL DEFAULT '' COMMENT '区域',
    `job_id` bigint(13) unsigned NOT NULL DEFAULT  0 COMMENT '任务ID',
    `version` int(11) unsigned NOT NULL DEFAULT  0 COMMENT '版本号',
    `job_action_result` text COMMENT '结果',
    `publish_desc` varchar (512) NOT NULL DEFAULT  '' COMMENT '描述',
    `job_status` varchar(16) NOT NULL DEFAULT '' COMMENT '状态',
    `cluster_id` bigint(13) unsigned NOT NULL DEFAULT  0 COMMENT '集群ID',
    `component_id` bigint(13) unsigned NOT NULL DEFAULT  0 COMMENT '组件ID',
    `current` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否是当前',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4 COMMENT ='rhea执行记录表';

CREATE TABLE `rhea_cluster_conf`
(
    `id`           bigint(13) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `gmt_create`   bigint(13) unsigned NOT NULL DEFAULT '0' COMMENT '创建时间',
    `gmt_modified` bigint(13) unsigned NOT NULL DEFAULT '0' COMMENT '修改时间',
    `status`       tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除',
    `create_user`  varchar(16) NOT NULL DEFAULT  '' COMMENT '创建用户',
    `modify_user`  varchar(16) NOT NULL DEFAULT  '' COMMENT '创建用户',
    `env` varchar(8) NOT NULL DEFAULT '' COMMENT '区域',
    `name` varchar (32) NOT NULL DEFAULT  '' COMMENT 'name',
    `params` varchar (512) NOT NULL DEFAULT  '' COMMENT '参数配置',
    `valid` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否有效',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4 COMMENT ='集群配置表';

CREATE TABLE `rhea_component_conf`
(
    `id`           bigint(13) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `gmt_create`   bigint(13) unsigned NOT NULL DEFAULT '0' COMMENT '创建时间',
    `gmt_modified` bigint(13) unsigned NOT NULL DEFAULT '0' COMMENT '修改时间',
    `status`       tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除',
    `create_user`  varchar(16) NOT NULL DEFAULT  '' COMMENT '创建用户',
    `modify_user`  varchar(16) NOT NULL DEFAULT  '' COMMENT '创建用户',
    `env` varchar(8) NOT NULL DEFAULT '' COMMENT '区域',
    `name` varchar (32) NOT NULL DEFAULT  '' COMMENT 'name',
    `params` varchar (512) NOT NULL DEFAULT  '' COMMENT '参数配置',
    `cluster_id` bigint(13) NOT NULL DEFAULT 0 COMMENT '集群配置ID',
    `type` varchar (16) NOT NULL DEFAULT  '' COMMENT 'type',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4 COMMENT ='组件配置表';

CREATE TABLE `rhea_job_log`
(
    `id`           bigint(13) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `gmt_create`   bigint(13) unsigned NOT NULL DEFAULT '0' COMMENT '创建时间',
    `gmt_modified` bigint(13) unsigned NOT NULL DEFAULT '0' COMMENT '修改时间',
    `status`       tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除',
    `env` varchar(8) NOT NULL DEFAULT '' COMMENT '区域',
    `key` varchar(64) NOT NULL DEFAULT '' COMMENT 'key',
    `log` mediumblob NOT NULL COMMENT '日志',
    `start_byte` int(10) NOT NULL DEFAULT '0' COMMENT '开始字节点',
    `end_byte` int(10) NOT NULL DEFAULT '0' COMMENT '结束字节点',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4 COMMENT ='日志表';
