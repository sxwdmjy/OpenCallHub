-- openCallHub.sys_category definition

CREATE TABLE `sys_category` (
                                `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
                                `type` int(11) NOT NULL COMMENT '1-拨号计划',
                                `name` varchar(50) DEFAULT NULL COMMENT '分类名称',
                                `parent_id` bigint(100) DEFAULT '0' COMMENT '父分类的id',
                                `flag` tinyint(4) DEFAULT '0' COMMENT '可删除标识 0 可删除 1 不可删除',
                                `create_by` bigint(20) DEFAULT NULL COMMENT '创建人',
                                `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `update_by` bigint(20) DEFAULT NULL COMMENT '更新人',
                                `update_time` datetime DEFAULT NULL COMMENT '修改时间',
                                `del_flag` tinyint(4) NOT NULL DEFAULT '0' COMMENT '删除标识 0 有效 1删除',
                                PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COMMENT='分类配置表';


-- openCallHub.sys_file definition

CREATE TABLE `sys_file` (
                            `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
                            `cos_id` varchar(64) DEFAULT NULL COMMENT '云存储ID',
                            `file_name` varchar(128) NOT NULL COMMENT '文件名称',
                            `file_suffix` varchar(20) NOT NULL COMMENT '文件后缀',
                            `file_type` tinyint(4) NOT NULL COMMENT '文件类型 1-image 2-voice 3-file',
                            `file_path` varchar(128) NOT NULL COMMENT '文件地址',
                            `file_size` varchar(64) NOT NULL COMMENT '文件大小',
                            `create_by` bigint(20) DEFAULT NULL COMMENT '创建人',
                            `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_by` bigint(20) DEFAULT NULL COMMENT '更新人',
                            `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                            `del_flag` tinyint(4) NOT NULL DEFAULT '0' COMMENT '删除标识 0 正常 1 删除',
                            PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件管理';


-- openCallHub.sys_menu definition

CREATE TABLE `sys_menu` (
                            `menu_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
                            `menu_name` varchar(50) NOT NULL COMMENT '菜单名称',
                            `parent_id` bigint(20) DEFAULT '0' COMMENT '父菜单ID',
                            `order_num` int(11) DEFAULT '0' COMMENT '显示顺序',
                            `path` varchar(200) DEFAULT '' COMMENT '路由地址',
                            `component` varchar(255) DEFAULT NULL COMMENT '组件路径',
                            `is_frame` tinyint(4) DEFAULT '1' COMMENT '是否为外链（0是 1否）',
                            `menu_type` char(1) DEFAULT '' COMMENT '菜单类型（M目录 C菜单 F按钮）',
                            `visible` tinyint(4) DEFAULT '0' COMMENT '菜单状态（0显示 1隐藏）',
                            `status` tinyint(4) DEFAULT '0' COMMENT '菜单状态（0正常 1停用）',
                            `perms` varchar(100) DEFAULT NULL COMMENT '权限标识',
                            `icon` varchar(100) DEFAULT '#' COMMENT '菜单图标',
                            `remark` varchar(500) DEFAULT '' COMMENT '备注',
                            `create_by` bigint(20) DEFAULT NULL COMMENT '创建人',
                            `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_by` bigint(20) DEFAULT NULL COMMENT '更新人',
                            `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                            `del_flag` tinyint(4) NOT NULL DEFAULT '0' COMMENT '删除标识 0 正常 1 删除',
                            PRIMARY KEY (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';


-- openCallHub.sys_oper_log definition

CREATE TABLE `sys_oper_log` (
                                `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志主键',
                                `title` varchar(50) DEFAULT '' COMMENT '模块标题',
                                `business_type` tinyint(4) DEFAULT '0' COMMENT '业务类型（0=其它,1=新增,2=修改,3=删除,4=查询,5=导出,6=导入 7-登录 8-登出）',
                                `method` varchar(100) DEFAULT '' COMMENT '方法名称',
                                `request_method` varchar(10) DEFAULT '' COMMENT '请求方式',
                                `operator_type` tinyint(4) DEFAULT '0' COMMENT '操作类别（0其它 1后台用户 2手机端用户）',
                                `oper_name` varchar(50) DEFAULT '' COMMENT '操作人员',
                                `oper_url` varchar(255) DEFAULT '' COMMENT '请求URL',
                                `oper_ip` varchar(128) DEFAULT '' COMMENT '主机地址',
                                `oper_location` varchar(255) DEFAULT '' COMMENT '操作地点',
                                `oper_param` varchar(2000) DEFAULT '' COMMENT '请求参数',
                                `json_result` varchar(2000) DEFAULT '' COMMENT '返回参数',
                                `status` tinyint(4) DEFAULT '0' COMMENT '操作状态（0正常 1异常）',
                                `error_msg` varchar(2000) DEFAULT '' COMMENT '错误消息',
                                `oper_time` datetime DEFAULT NULL COMMENT '操作时间',
                                `cost_time` bigint(20) DEFAULT '0' COMMENT '消耗时间',
                                `create_by` varchar(255) DEFAULT NULL COMMENT '创建人',
                                `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `update_by` varchar(255) DEFAULT NULL COMMENT '更新人',
                                `update_time` datetime DEFAULT NULL COMMENT '修改时间',
                                `del_flag` tinyint(4) NOT NULL DEFAULT '0' COMMENT '删除标识 0正常 1 删除',
                                PRIMARY KEY (`id`),
                                KEY `idx_sys_oper_log_bt` (`business_type`),
                                KEY `idx_sys_oper_log_ot` (`oper_time`),
                                KEY `idx_sys_oper_log_s` (`status`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COMMENT='系统操作日志记录';


-- openCallHub.sys_role definition

CREATE TABLE `sys_role` (
                            `role_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
                            `role_name` varchar(30) NOT NULL COMMENT '角色名称',
                            `role_key` varchar(100) NOT NULL COMMENT '角色权限字符串',
                            `role_sort` int(11) NOT NULL COMMENT '显示顺序',
                            `data_scope` tinyint(4) DEFAULT '1' COMMENT '数据范围（1：全部数据权限 2:本部门及以下数据权限 3：本部门数据权限 4：本人数据权限）',
                            `status` tinyint(4) DEFAULT '0' COMMENT '角色状态（0正常 1停用）',
                            `remark` varchar(500) DEFAULT NULL COMMENT '备注',
                            `create_by` bigint(20) DEFAULT NULL COMMENT '创建人',
                            `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_by` bigint(20) DEFAULT NULL COMMENT '更新人',
                            `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                            `del_flag` tinyint(4) NOT NULL DEFAULT '0' COMMENT '删除标识 0 正常 1 删除',
                            PRIMARY KEY (`role_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COMMENT='角色信息表';


-- openCallHub.sys_role_menu definition

CREATE TABLE `sys_role_menu` (
                                 `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                 `role_id` bigint(20) NOT NULL COMMENT '角色ID',
                                 `menu_id` bigint(20) NOT NULL COMMENT '菜单ID',
                                 `create_by` bigint(20) DEFAULT NULL COMMENT '创建人',
                                 `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `update_by` bigint(20) DEFAULT NULL COMMENT '更新人',
                                 `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                                 `del_flag` tinyint(4) NOT NULL DEFAULT '0' COMMENT '删除标识 0 正常 1 删除',
                                 PRIMARY KEY (`id`),
                                 KEY `role_menu_id_idx` (`menu_id`,`role_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';


-- openCallHub.sys_user definition

CREATE TABLE `sys_user` (
                            `user_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                            `user_name` varchar(50) NOT NULL COMMENT '用户账号',
                            `nick_name` varchar(50) DEFAULT NULL COMMENT '用户昵称',
                            `password` varchar(64) DEFAULT NULL COMMENT '密码',
                            `avatar` varchar(128) DEFAULT NULL COMMENT '用户头像',
                            `sex` tinyint(4) DEFAULT NULL COMMENT '用户性别（0-未知 1-男 2-女）',
                            `phone` varchar(32) DEFAULT NULL COMMENT '手机号',
                            `email` varchar(32) DEFAULT NULL COMMENT '邮箱',
                            `status` tinyint(4) DEFAULT '1' COMMENT '状态 1-启用 2-禁用',
                            `remark` varchar(500) DEFAULT NULL COMMENT '备注',
                            `create_by` bigint(20) DEFAULT '1' COMMENT '创建人',
                            `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_by` bigint(20) DEFAULT NULL COMMENT '更新人',
                            `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                            `del_flag` tinyint(4) NOT NULL DEFAULT '0' COMMENT '删除标识 0 正常 1 删除',
                            PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';


-- openCallHub.sys_user_role definition

CREATE TABLE `sys_user_role` (
                                 `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                 `user_id` bigint(20) NOT NULL COMMENT '用户ID',
                                 `role_id` bigint(20) NOT NULL COMMENT '角色ID',
                                 `create_by` bigint(20) DEFAULT NULL COMMENT '创建人',
                                 `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `update_by` bigint(20) DEFAULT NULL COMMENT '更新人',
                                 `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                                 `del_flag` tinyint(4) NOT NULL DEFAULT '0' COMMENT '删除标识 0 正常 1 删除',
                                 PRIMARY KEY (`id`),
                                 KEY `user_role_id_idx` (`user_id`,`role_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';


-- openCallHub.call_display definition

CREATE TABLE `call_display` (
                                `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
                                `phone` varchar(18) NOT NULL COMMENT '电话号码',
                                `type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '号码类型 1-主叫显号 2-被叫显号',
                                `area` varchar(64) DEFAULT NULL COMMENT '归属地',
                                `create_by` bigint(20) DEFAULT NULL COMMENT '创建人',
                                `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `update_by` bigint(20) DEFAULT NULL COMMENT '更新人',
                                `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                                `del_flag` tinyint(4) NOT NULL DEFAULT '0' COMMENT '删除标识 0 正常 1 删除',
                                PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COMMENT='号码管理';


-- openCallHub.call_display_pool definition

CREATE TABLE `call_display_pool` (
                                     `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
                                     `name` varchar(18) NOT NULL COMMENT '电话池名称',
                                     `type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '类型 1-随机 2-轮询',
                                     `create_by` bigint(20) DEFAULT NULL COMMENT '创建人',
                                     `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     `update_by` bigint(20) DEFAULT NULL COMMENT '更新人',
                                     `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                                     `tenant_id` int(20) NOT NULL DEFAULT '0' COMMENT '租户ID',
                                     `del_flag` tinyint(4) NOT NULL DEFAULT '0' COMMENT '删除标识 0 正常 1 删除',
                                     PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='号码池管理';


-- openCallHub.call_display_pool_rel definition

CREATE TABLE `call_display_pool_rel` (
                                         `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
                                         `pool_id` bigint(20) NOT NULL COMMENT '电话池ID',
                                         `display_id` bigint(20) NOT NULL COMMENT '号码ID',
                                         `create_by` bigint(20) DEFAULT NULL COMMENT '创建人',
                                         `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                         `update_by` bigint(20) DEFAULT NULL COMMENT '更新人',
                                         `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                                         `del_flag` tinyint(4) NOT NULL DEFAULT '0' COMMENT '删除标识 0 正常 1 删除',
                                         PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='号码池号码关联表';



-- openCallHub.call_record definition

CREATE TABLE `call_record` (
                               `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                               `call_id` varchar(64) NOT NULL COMMENT '呼叫唯一ID',
                               `caller_number` varchar(32) NOT NULL COMMENT '主叫号码',
                               `caller_display_number` varchar(32) NOT NULL COMMENT '主叫显号',
                               `callee_number` varchar(32) NOT NULL COMMENT '被叫号码',
                               `callee_display_number` varchar(32) NOT NULL COMMENT '被叫显号',
                               `number_location` varchar(128) DEFAULT NULL COMMENT '号码归属地',
                               `agent_id` bigint(20) NOT NULL COMMENT '坐席ID',
                               `agent_number` varchar(32) NOT NULL COMMENT '坐席号码',
                               `agent_name` varchar(128) NOT NULL COMMENT '坐席名称',
                               `call_state` tinyint(4) NOT NULL DEFAULT '1' COMMENT '呼叫状态 1-成功 2-失败',
                               `direction` tinyint(4) DEFAULT NULL COMMENT '呼叫方式 1-呼出 2-呼入',
                               `call_start_time` datetime DEFAULT NULL COMMENT '呼叫开始时间',
                               `call_end_time` datetime DEFAULT NULL COMMENT '呼叫结束时间',
                               `answer_flag` tinyint(4) NOT NULL COMMENT '应答标识 0-接通 1-坐席未接用户未接 2-坐席接通用户未接通 3-用户接通坐席未接通',
                               `answer_time` datetime NOT NULL COMMENT '呼叫接通时间',
                               `ringing_time` datetime DEFAULT NULL COMMENT '振铃时间',
                               `hangup_dir` tinyint(4) DEFAULT NULL COMMENT '挂机方向 1-主叫挂机 2-被叫挂机 3-系统挂机',
                               `hangup_cause_code` tinyint(4) DEFAULT NULL COMMENT '挂机原因 ',
                               `file_path` varchar(500) DEFAULT NULL COMMENT '录音文件地址',
                               `ringing_path` varchar(500) DEFAULT NULL COMMENT '振铃文件地址',
                               `create_by` bigint(20) DEFAULT NULL COMMENT '创建人',
                               `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                               `update_by` bigint(20) DEFAULT NULL COMMENT '更新人',
                               `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                               `del_flag` tinyint(4) NOT NULL DEFAULT '0' COMMENT '删除标识 0 正常 1 删除',
                               PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COMMENT='呼叫记录表';


-- openCallHub.call_route definition

CREATE TABLE `call_route` (
                              `id` bigint(20) NOT NULL AUTO_INCREMENT,
                              `name` varchar(64) NOT NULL COMMENT '路由名称',
                              `route_num` varchar(32) NOT NULL COMMENT '路由号码',
                              `type` tinyint(4) DEFAULT '1' COMMENT '路由类型 1-呼入 2-呼出',
                              `level` int(11) DEFAULT '0' COMMENT '路由优先级',
                              `status` tinyint(4) DEFAULT '0' COMMENT '状态  0-未启用 1-启用',
                              `schedule_id` bigint(20) DEFAULT NULL COMMENT '日程ID',
                              `route_type` tinyint(4) DEFAULT NULL COMMENT '路由类型 1-坐席 2-外呼 3-sip 4-技能组 5-放音 6-ivr',
                              `route_value` varchar(200) DEFAULT NULL COMMENT '路由类型值',
                              `create_by` bigint(20) DEFAULT NULL COMMENT '创建人',
                              `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              `update_by` bigint(20) DEFAULT NULL COMMENT '更新人',
                              `update_time` datetime DEFAULT NULL COMMENT '修改时间',
                              `del_flag` tinyint(4) NOT NULL DEFAULT '0' COMMENT '删除标识 0 有效 1删除',
                              PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='号码路由表';


-- openCallHub.call_schedule definition

CREATE TABLE `call_schedule` (
                                 `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
                                 `name` varchar(128) NOT NULL COMMENT '日程名称',
                                 `level` int(11) NOT NULL DEFAULT '0' COMMENT '优先级 0-10',
                                 `type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '0-指定时间 1-相对时间',
                                 `start_day` varchar(50) DEFAULT NULL COMMENT '开始日期',
                                 `end_day` varchar(50) DEFAULT NULL COMMENT '结束日期',
                                 `start_time` varchar(50) DEFAULT NULL COMMENT '开始时间',
                                 `end_time` varchar(50) DEFAULT NULL COMMENT '结束时间',
                                 `work_cycle` varchar(20) NOT NULL COMMENT '周期时间',
                                 `create_by` bigint(20) DEFAULT NULL COMMENT '创建人',
                                 `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `update_by` bigint(20) DEFAULT NULL COMMENT '更新人',
                                 `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                                 `del_flag` tinyint(4) NOT NULL DEFAULT '0' COMMENT '删除标识 0 正常 1 删除',
                                 PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COMMENT='日程安排表';


-- openCallHub.call_skill definition

CREATE TABLE `call_skill` (
                              `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                              `group_id` bigint(20) DEFAULT NULL COMMENT '分组ID',
                              `name` varchar(128) DEFAULT NULL COMMENT '技能名称',
                              `priority` int(11) NOT NULL DEFAULT '0' COMMENT '优先级',
                              `describe` varchar(128) DEFAULT NULL COMMENT '描述',
                              `strategy_type` tinyint(4) NOT NULL DEFAULT '0' COMMENT '策略类型 0-随机 1-轮询 2-最长空闲时间 3-当天最少应答次数 4-最长话后时长',
                              `full_busy_type` tinyint(4) DEFAULT '0' COMMENT '全忙 0-排队 1-溢出 2-挂机',
                              `overflow_type` tinyint(4) DEFAULT '0' COMMENT '溢出策略 0-挂机 1-转IVR',
                              `overflow_value` varchar(32) DEFAULT NULL COMMENT '溢出策略值',
                              `time_out` int(11) DEFAULT NULL COMMENT '排队超时时间（秒）',
                              `queue_length` int(11) DEFAULT NULL COMMENT '最大排队人数',
                              `queue_voice` bigint(20) DEFAULT NULL COMMENT '排队音',
                              `agent_voice` bigint(20) DEFAULT NULL COMMENT '转坐席音',
                              `caller_phone_pool` bigint(20) DEFAULT NULL COMMENT '主叫号码池',
                              `callee_phone_pool` bigint(20) DEFAULT NULL COMMENT '被叫号码池',
                              `create_by` bigint(20) DEFAULT NULL COMMENT '创建人',
                              `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              `update_by` bigint(20) DEFAULT NULL COMMENT '更新人',
                              `update_time` datetime DEFAULT NULL COMMENT '修改时间',
                              `del_flag` tinyint(4) NOT NULL DEFAULT '0' COMMENT '删除标识 0 有效 1删除',
                              PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='技能表'


-- openCallHub.call_skill_agent_rel definition

CREATE TABLE `call_skill_agent_rel` (
                                        `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                        `skill_id` bigint(20) DEFAULT NULL COMMENT '技能ID',
                                        `agent_id` varchar(128) DEFAULT NULL COMMENT '坐席ID',
                                        `create_by` bigint(20) DEFAULT NULL COMMENT '创建人',
                                        `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                        `update_by` bigint(20) DEFAULT NULL COMMENT '更新人',
                                        `update_time` datetime DEFAULT NULL COMMENT '修改时间',
                                        `del_flag` tinyint(4) NOT NULL DEFAULT '0' COMMENT '删除标识 0 有效 1删除',
                                        PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='技能坐席关联表';


-- openCallHub.flow_info definition

CREATE TABLE `flow_info` (
                             `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '流程实例唯一标识符',
                             `group_id` bigint(20) NOT NULL COMMENT '分组ID',
                             `name` varchar(255) NOT NULL COMMENT 'ivr名称',
                             `desc` varchar(255) DEFAULT NULL COMMENT '流程描述',
                             `status` tinyint(4) DEFAULT '0' COMMENT '流程状态 0-草稿 1-待发布 2-已发布',
                             `flow_data` json DEFAULT NULL COMMENT '流程数据',
                             `create_by` bigint(20) DEFAULT NULL COMMENT '创建人',
                             `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `update_by` bigint(20) DEFAULT NULL COMMENT '更新人',
                             `update_time` datetime DEFAULT NULL COMMENT '修改时间',
                             `del_flag` tinyint(4) NOT NULL DEFAULT '0' COMMENT '删除标识 0 有效 1删除',
                             PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ivr流程信息';


-- openCallHub.flow_instances definition

CREATE TABLE `flow_instances` (
                                  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '流程实例唯一标识符',
                                  `call_id` bigint(20) NOT NULL COMMENT '通话ID',
                                  `flow_id` bigint(20) NOT NULL COMMENT '流程ID，用于区分不同流程',
                                  `status` tinyint(4) DEFAULT '1' COMMENT '流程实例的状态：1-进行中、2-已完成  3-失败',
                                  `start_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '流程开始时间',
                                  `end_time` datetime DEFAULT NULL COMMENT '流程结束时间',
                                  `current_node_id` bigint(20) DEFAULT NULL COMMENT '当前节点ID',
                                  `variables` json DEFAULT NULL COMMENT '存储流程实例的变量（例如条件判断、数据传递等）',
                                  `create_by` bigint(20) DEFAULT NULL COMMENT '创建人',
                                  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  `update_by` bigint(20) DEFAULT NULL COMMENT '更新人',
                                  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
                                  `del_flag` tinyint(4) NOT NULL DEFAULT '0' COMMENT '删除标识 0 有效 1删除',
                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COMMENT='存储流程实例的基本信息';


-- openCallHub.flow_node_execution_history definition

CREATE TABLE `flow_node_execution_history` (
                                               `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '节点执行历史记录ID',
                                               `instance_id` bigint(20) NOT NULL COMMENT '流程实例ID',
                                               `node_id` bigint(20) NOT NULL COMMENT '节点ID',
                                               `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '节点执行状态：1-进入 2-退出 3-跳过 4-失败',
                                               `start_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '节点开始执行时间',
                                               `end_time` datetime DEFAULT NULL COMMENT '节点结束执行时间',
                                               `duration` int(11) DEFAULT NULL COMMENT '节点执行时长（秒）',
                                               `description` text COMMENT '节点执行描述',
                                               `create_by` bigint(20) DEFAULT NULL COMMENT '创建人',
                                               `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                               `update_by` bigint(20) DEFAULT NULL COMMENT '更新人',
                                               `update_time` datetime DEFAULT NULL COMMENT '修改时间',
                                               `del_flag` tinyint(4) NOT NULL DEFAULT '0' COMMENT '删除标识 0 有效 1删除',
                                               PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='记录每次节点执行的历史记录';


-- openCallHub.fs_acl definition

CREATE TABLE `fs_acl` (
                          `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                          `name` varchar(50) DEFAULT NULL COMMENT '名称',
                          `default_type` varchar(20) DEFAULT 'allow' COMMENT '类型 allow-允许 deny-拒绝',
                          `list_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '列表ID',
                          `node_type` varchar(20) DEFAULT NULL COMMENT '规则类型 allow-允许 deny-拒绝',
                          `cidr` varchar(64) DEFAULT NULL COMMENT 'IP地址',
                          `domain` varchar(64) DEFAULT NULL COMMENT '域地址',
                          `create_by` bigint(20) DEFAULT NULL COMMENT '创建人',
                          `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                          `update_by` bigint(20) DEFAULT NULL COMMENT '更新人',
                          `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                          `del_flag` tinyint(4) NOT NULL DEFAULT '0' COMMENT '删除标识 0 正常 1 删除',
                          PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='fs访问控制表';


-- openCallHub.fs_cdr definition

CREATE TABLE `fs_cdr` (
                          `call_id` bigint(20) NOT NULL COMMENT '通话唯一ID',
                          `uuid` varchar(64) DEFAULT NULL COMMENT 'uuid',
                          `direction` varchar(16) DEFAULT NULL COMMENT '呼叫方向 outbound-呼出 inbound-呼入',
                          `sip_local_network_addr` varchar(32) DEFAULT NULL COMMENT '本地呼叫地址',
                          `sip_network_ip` varchar(32) DEFAULT NULL COMMENT '呼叫IP',
                          `caller_id_number` varchar(64) DEFAULT NULL COMMENT '主叫号码',
                          `caller_display` varchar(64) DEFAULT NULL COMMENT '主叫显号',
                          `destination_number` varchar(64) DEFAULT NULL COMMENT '被叫号码',
                          `destination_display` varchar(64) DEFAULT NULL COMMENT '被叫显号',
                          `start_stamp` datetime DEFAULT NULL COMMENT '开始时间',
                          `answer_stamp` datetime DEFAULT NULL COMMENT '应答时间',
                          `end_stamp` datetime DEFAULT NULL COMMENT '结束时间',
                          `bridge_stamp` datetime DEFAULT NULL COMMENT '桥接时间',
                          `progress_stamp` datetime DEFAULT NULL COMMENT '振铃时间',
                          `duration` int(11) DEFAULT NULL COMMENT '呼叫时长',
                          `answer_sec` int(11) DEFAULT NULL COMMENT '应答时长',
                          `bill_sec` int(11) DEFAULT NULL COMMENT '计费时长',
                          `hangup_cause` varchar(64) DEFAULT NULL COMMENT '挂断原因',
                          `record_start_time` datetime DEFAULT NULL COMMENT '录音开始时间',
                          `record_end_time` datetime DEFAULT NULL COMMENT '录音结束时间',
                          `record_sec` int(11) DEFAULT NULL COMMENT '录音时长',
                          `record` varchar(128) DEFAULT NULL COMMENT '录音地址',
                          `read_codec` varchar(64) DEFAULT NULL COMMENT '读编码类型',
                          `write_codec` varchar(64) DEFAULT NULL COMMENT '写编码类型',
                          `sip_hangup_disposition` varchar(64) DEFAULT NULL COMMENT '挂断意向',
                          `create_by` bigint(20) DEFAULT NULL COMMENT '创建人',
                          `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          `update_by` bigint(20) DEFAULT NULL COMMENT '更新人',
                          `update_time` datetime DEFAULT NULL COMMENT '修改时间',
                          `del_flag` tinyint(4) NOT NULL DEFAULT '0' COMMENT '删除标识 0 有效 1删除',
                          PRIMARY KEY (`call_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='cdr话单表';


-- openCallHub.fs_config definition

CREATE TABLE `fs_config` (
                             `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                             `name` varchar(128) DEFAULT NULL COMMENT '名称',
                             `group` varchar(64) DEFAULT NULL COMMENT '客户端分组',
                             `ip` varchar(64) DEFAULT NULL COMMENT '机器地址IP',
                             `port` int(11) DEFAULT NULL COMMENT '服务器端口',
                             `user_name` varchar(64) DEFAULT NULL COMMENT '用户名',
                             `password` varchar(64) DEFAULT NULL COMMENT '密码',
                             `status` tinyint(4) DEFAULT '0' COMMENT '状态 0-在线 1-下线',
                             `out_time` int(11) DEFAULT NULL COMMENT '超时时间（秒）',
                             `create_by` bigint(20) DEFAULT NULL COMMENT '创建人',
                             `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `update_by` bigint(20) DEFAULT NULL COMMENT '更新人',
                             `update_time` datetime DEFAULT NULL COMMENT '修改时间',
                             `del_flag` tinyint(4) NOT NULL DEFAULT '0' COMMENT '删除标识 0 有效 1删除',
                             PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='fs管理配置表';


-- openCallHub.fs_dialplan definition

CREATE TABLE `fs_dialplan` (
                               `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                               `group_id` bigint(20) NOT NULL COMMENT '分组ID',
                               `name` varchar(128) NOT NULL COMMENT '计划名称',
                               `type` varchar(64) NOT NULL COMMENT '类型 xml格式,json格式',
                               `expression` varchar(100) DEFAULT NULL COMMENT '正则匹配规则',
                               `context_name` varchar(32) NOT NULL COMMENT '内容类型 public、default',
                               `content` longtext NOT NULL COMMENT '内容',
                               `describe` varchar(256) NOT NULL COMMENT '描述',
                               `create_by` bigint(20) DEFAULT NULL COMMENT '创建人',
                               `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                               `update_by` bigint(20) DEFAULT NULL COMMENT '更新人',
                               `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                               `del_flag` tinyint(4) NOT NULL DEFAULT '0' COMMENT '删除标识 0 正常 1 删除',
                               PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='fs拨号计划表';


-- openCallHub.fs_modules definition

CREATE TABLE `fs_modules` (
                              `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                              `fs_name` varchar(128) NOT NULL COMMENT 'FS主机名',
                              `name` varchar(128) NOT NULL COMMENT '模块名称',
                              `type` varchar(64) NOT NULL COMMENT '类型 xml格式,json格式',
                              `content` longtext NOT NULL COMMENT '内容',
                              `describe` varchar(256) NOT NULL COMMENT '描述',
                              `create_by` bigint(20) DEFAULT NULL COMMENT '创建人',
                              `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                              `update_by` bigint(20) DEFAULT NULL COMMENT '更新人',
                              `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                              `del_flag` tinyint(4) NOT NULL DEFAULT '0' COMMENT '删除标识 0 正常 1 删除',
                              PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='fs模块管理表';


-- openCallHub.fs_sip_gateway definition

CREATE TABLE `fs_sip_gateway` (
                                  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                  `name` varchar(50) NOT NULL COMMENT '网关名称',
                                  `user_name` varchar(50) DEFAULT NULL COMMENT '账号',
                                  `password` varchar(64) DEFAULT NULL COMMENT '密码',
                                  `realm` varchar(32) NOT NULL COMMENT '认证地址',
                                  `proxy` varchar(32) DEFAULT NULL COMMENT '代理地址',
                                  `register` tinyint(4) DEFAULT '1' COMMENT '注册类型 0-不注册 1-注册',
                                  `transport` tinyint(4) DEFAULT '1' COMMENT '注册协议 1-udp, 2-tcp',
                                  `caller_id_in_from` tinyint(4) NOT NULL DEFAULT '0' COMMENT '通过此网关出站呼叫时，在from字段中使用入站呼叫的callerid(0-true 1-false)',
                                  `from_domain` varchar(32) DEFAULT '' COMMENT 'from域',
                                  `retry_time` int(11) DEFAULT '30' COMMENT '重试时间（秒）',
                                  `ping_time` int(11) DEFAULT '30' COMMENT '心跳时间（秒）',
                                  `expire_time` int(11) NOT NULL DEFAULT '300' COMMENT '超时时间（秒）',
                                  `type` tinyint(4) NOT NULL DEFAULT '2' COMMENT '网关类型 1-internal 2-external',
                                  `gateway_type` tinyint(4) NOT NULL COMMENT '是否外线 0-否 1-是',
                                  `create_by` bigint(20) DEFAULT NULL COMMENT '创建人',
                                  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  `update_by` bigint(20) DEFAULT NULL COMMENT '更新人',
                                  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                                  `del_flag` tinyint(4) NOT NULL DEFAULT '0' COMMENT '删除标识 0 正常 1 删除',
                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SIP网关表';


-- openCallHub.ko_acc definition

CREATE TABLE `ko_acc` (
                          `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
                          `method` varchar(16) NOT NULL DEFAULT '',
                          `from_tag` varchar(128) NOT NULL DEFAULT '',
                          `to_tag` varchar(128) NOT NULL DEFAULT '',
                          `callid` varchar(255) NOT NULL DEFAULT '',
                          `sip_code` varchar(3) NOT NULL DEFAULT '',
                          `sip_reason` varchar(128) NOT NULL DEFAULT '',
                          `time` datetime NOT NULL,
                          `src_user` varchar(64) NOT NULL DEFAULT '',
                          `src_domain` varchar(128) NOT NULL DEFAULT '',
                          `src_ip` varchar(64) NOT NULL DEFAULT '',
                          `dst_ouser` varchar(64) NOT NULL DEFAULT '',
                          `dst_user` varchar(64) NOT NULL DEFAULT '',
                          `dst_domain` varchar(128) NOT NULL DEFAULT '',
                          PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账单记账表';


-- openCallHub.ko_address definition

CREATE TABLE `ko_address` (
                              `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
                              `grp` int(11) unsigned NOT NULL DEFAULT '1',
                              `ip_addr` varchar(50) NOT NULL,
                              `mask` int(11) NOT NULL DEFAULT '32',
                              `port` smallint(5) unsigned NOT NULL DEFAULT '0',
                              `tag` varchar(64) DEFAULT NULL,
                              PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- openCallHub.ko_dispatcher definition

CREATE TABLE `ko_dispatcher` (
                                 `id` int(10) NOT NULL AUTO_INCREMENT,
                                 `setid` int(11) NOT NULL DEFAULT '0' COMMENT '分组ID',
                                 `destination` varchar(192) NOT NULL DEFAULT '' COMMENT '目标地址',
                                 `flags` int(11) NOT NULL DEFAULT '0' COMMENT '标识',
                                 `priority` int(11) NOT NULL DEFAULT '0' COMMENT '优先级',
                                 `attrs` varchar(128) NOT NULL DEFAULT '' COMMENT '属性',
                                 `description` varchar(64) NOT NULL DEFAULT '' COMMENT '描述',
                                 `status` tinyint(4) DEFAULT '0' COMMENT '状态 0-在线 1-下线',
                                 `create_by` bigint(20) DEFAULT NULL COMMENT '创建人',
                                 `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `update_by` bigint(20) DEFAULT NULL COMMENT '更新人',
                                 `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                                 `del_flag` tinyint(4) NOT NULL DEFAULT '0' COMMENT '删除标识 0 正常 1 删除',
                                 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='KO负载管理表';


-- openCallHub.ko_location definition

CREATE TABLE `ko_location` (
                               `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
                               `ruid` varchar(64) NOT NULL DEFAULT '',
                               `username` varchar(64) NOT NULL DEFAULT '',
                               `domain` varchar(64) DEFAULT NULL,
                               `contact` varchar(512) NOT NULL DEFAULT '',
                               `received` varchar(128) DEFAULT NULL,
                               `path` varchar(512) DEFAULT NULL,
                               `expires` datetime NOT NULL DEFAULT '2030-05-28 21:32:15',
                               `q` float(10,2) NOT NULL DEFAULT '1.00',
  `callid` varchar(255) NOT NULL DEFAULT 'Default-Call-ID',
  `cseq` int(11) NOT NULL DEFAULT '1',
  `last_modified` datetime NOT NULL DEFAULT '2000-01-01 00:00:01',
  `flags` int(11) NOT NULL DEFAULT '0',
  `cflags` int(11) NOT NULL DEFAULT '0',
  `user_agent` varchar(255) NOT NULL DEFAULT '',
  `socket` varchar(64) DEFAULT NULL,
  `methods` int(11) DEFAULT NULL,
  `instance` varchar(255) DEFAULT NULL,
  `reg_id` int(11) NOT NULL DEFAULT '0',
  `server_id` int(11) NOT NULL DEFAULT '0',
  `connection_id` int(11) NOT NULL DEFAULT '0',
  `keepalive` int(11) NOT NULL DEFAULT '0',
  `partition` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `ruid_idx` (`ruid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='kamailio注册信息表';


-- openCallHub.ko_subscriber definition

CREATE TABLE `ko_subscriber` (
                                 `id` int(10) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                 `username` varchar(64) NOT NULL DEFAULT '' COMMENT '用户名称',
                                 `domain` varchar(64) NOT NULL DEFAULT '' COMMENT '域名地址',
                                 `password` varchar(64) NOT NULL DEFAULT '' COMMENT '密码',
                                 `ha1` varchar(128) NOT NULL DEFAULT '' COMMENT '哈希码',
                                 `ha1b` varchar(128) NOT NULL DEFAULT '' COMMENT '哈希码',
                                 `vmpin` varchar(8) NOT NULL DEFAULT '1234' COMMENT '鉴权值',
                                 `status` tinyint(4) DEFAULT '0' COMMENT '状态 0-开启 1-关闭',
                                 `create_by` bigint(20) DEFAULT NULL COMMENT '创建人',
                                 `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `update_by` bigint(20) DEFAULT NULL COMMENT '更新人',
                                 `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `account_idx` (`username`,`domain`),
                                 KEY `username_idx` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户订阅表（SIP 订阅）';


-- openCallHub.sip_agent definition

CREATE TABLE `sip_agent` (
                             `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                             `name` varchar(128) NOT NULL COMMENT '坐席名称',
                             `user_id` bigint(20) NOT NULL COMMENT '主键ID',
                             `agent_number` varchar(64) NOT NULL COMMENT 'sip账号ID',
                             `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '开通状态 0-未开通 1-开通',
                             `online_status` tinyint(4) NOT NULL DEFAULT '3' COMMENT '在线状态 0-空闲  1-忙碌 2-通话中 3-离线',
                             `create_by` bigint(20) DEFAULT NULL COMMENT '创建人',
                             `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                             `update_by` bigint(20) DEFAULT NULL COMMENT '更新人',
                             `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                             `del_flag` tinyint(4) NOT NULL DEFAULT '0' COMMENT '删除标识 0 正常 1 删除',
                             PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='坐席管理表';




-- openCallHub.version definition

CREATE TABLE `version` (
                           `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
                           `table_name` varchar(32) NOT NULL,
                           `table_version` int(10) unsigned NOT NULL DEFAULT '0',
                           PRIMARY KEY (`id`),
                           UNIQUE KEY `table_name_idx` (`table_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='kamailio模块版本表';


-- openCallHub.voice_file definition

CREATE TABLE `voice_file` (
                              `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                              `name` varchar(50) NOT NULL COMMENT '文件名称',
                              `type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '类型 1-手动上传 2-语音合成',
                              `tts` tinyint(4) DEFAULT NULL COMMENT 'tts方式 1-腾讯 2-阿里 3-讯飞(type=2生效)',
                              `speech_text` varchar(500) DEFAULT NULL COMMENT '合成文本',
                              `file_id` bigint(20) DEFAULT NULL COMMENT '文件ID',
                              `create_by` bigint(20) DEFAULT NULL COMMENT '创建人',
                              `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              `update_by` bigint(20) DEFAULT NULL COMMENT '更新人',
                              `update_time` datetime DEFAULT NULL COMMENT '修改时间',
                              `del_flag` tinyint(4) NOT NULL DEFAULT '0' COMMENT '删除标识 0 有效 1删除',
                              PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='语音文件表';


INSERT INTO sys_user
(user_id, user_name, nick_name, password, avatar, sex, phone, email, status, remark, create_by, create_time, update_by, update_time, del_flag)
VALUES(1, 'admin', '系统管理员', '$2a$10$vfKaPSchXa41aZoCJQ1nnu.pUX3qHH.ve5JdNWHVP/EOy5yv7IMvW', NULL, 1, NULL, NULL, 1, '系统管理员', 1, '2024-07-15 10:39:43.0', NULL, NULL, 0);

INSERT INTO sys_user_role
(id, user_id, role_id, create_by, create_time, update_by, update_time, del_flag)
VALUES(1, 1, 1, 1, '2024-07-16 02:49:33.0', NULL, NULL, 0);

INSERT INTO sys_role
(role_id, role_name, role_key, role_sort, data_scope, status, remark, create_by, create_time, update_by, update_time, del_flag)
VALUES(1, '超级管理员', 'ROLE_ADMIN', 0, 1, 0, '超级管理员', NULL, '2024-07-15 10:40:59.0', NULL, NULL, 0);
INSERT INTO sys_role
(role_id, role_name, role_key, role_sort, data_scope, status, remark, create_by, create_time, update_by, update_time, del_flag)
VALUES(2, '演示角色', 'ROLE_MEMBER', 1, 4, 0, '作为演示使用', 1, '2024-11-08 10:06:58.0', 1, '2024-12-30 11:05:21.0', 0);



INSERT INTO sys_menu (menu_name,parent_id,order_num,`path`,component,is_frame,menu_type,visible,status,perms,icon,remark,create_by,create_time,update_by,update_time,del_flag) VALUES
                                                                                                                                                                                   ('系统管理',0,1,'#',NULL,1,'M',0,0,NULL,'AppstoreOutlined','系统管理目录',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('FS配置管理',0,2,'#',NULL,1,'M',0,0,NULL,'ControlOutlined','FS配置管理目录',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('呼叫管理',0,3,'#',NULL,1,'M',0,0,NULL,'PhoneOutlined','呼叫管理目录',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('用户管理',1,1,'/userManagement',NULL,1,'C',0,0,NULL,'','用户管理菜单',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('角色管理',1,2,'/roleManagement',NULL,1,'C',0,0,NULL,'','角色管理菜单',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('菜单管理',1,3,'/menuManagement',NULL,1,'C',0,0,NULL,'','菜单管理菜单',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('日志管理',1,4,'/logManagement',NULL,1,'C',0,0,NULL,'','日志管理菜单',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('企业管理',1,5,'/system/v1/corp',NULL,1,'C',0,0,NULL,'','企业管理菜单',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('FS配置管理',2,1,'/fsConfigureManagement',NULL,1,'C',0,0,NULL,'','fs配置管理菜单',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('FS模块配置管理',2,2,'/fsModuleConfigureManagement',NULL,1,'C',0,0,NULL,'','FS模块配置管理菜单',1,'2024-07-15 10:43:17.0',NULL,NULL,0);
INSERT INTO sys_menu (menu_name,parent_id,order_num,`path`,component,is_frame,menu_type,visible,status,perms,icon,remark,create_by,create_time,update_by,update_time,del_flag) VALUES
                                                                                                                                                                                   ('FS网关管理',2,3,'/gatewayManagement',NULL,1,'C',0,0,NULL,'','FS网关管理菜单',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('FS访问控制管理',2,4,'/accessControl',NULL,1,'C',0,0,NULL,'','FS访问控制管理菜单',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('FS拨号计划管理',2,5,'/dialPlanManagement',NULL,1,'C',0,0,NULL,'','FS拨号计划管理菜单',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('坐席管理',3,1,'/agentManagement',NULL,1,'C',0,0,NULL,'','坐席管理菜单',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('号码路由管理',3,2,'/phoneNumberRoute',NULL,1,'C',0,0,NULL,'','号码路由管理',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('SIP号码管理',3,3,'/sipPhoneNumber',NULL,1,'C',0,0,NULL,'','SIP号码管理管理',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('号码池管理',3,4,'/phonePool',NULL,1,'C',0,0,NULL,'','号码池管理',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('号码管理',3,5,'/phoneNumber',NULL,1,'C',0,0,NULL,'','号码管理',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('语音文件管理',3,6,'/voiceFile',NULL,1,'C',0,0,NULL,'','语音文件管理',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('日程管理',3,7,'/scheduleManagement',NULL,1,'C',0,0,NULL,'','日程管理',1,'2024-07-15 10:43:17.0',NULL,NULL,0);
INSERT INTO sys_menu (menu_name,parent_id,order_num,`path`,component,is_frame,menu_type,visible,status,perms,icon,remark,create_by,create_time,update_by,update_time,del_flag) VALUES
                                                                                                                                                                                   ('技能组管理',3,8,'/skillGroupManagement',NULL,1,'C',0,0,NULL,'','技能组管理',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('新增用户',100,1,'#',NULL,1,'F',0,0,'system:user:add','','新增用户按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('修改用户',100,2,'#',NULL,1,'F',0,0,'system:user:edit','','修改用户按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('修改密码',100,3,'#',NULL,1,'F',0,0,'system:user:editPassWord','','修改密码按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('删除用户',100,4,'#',NULL,1,'F',0,0,'system:user:delete','','删除用户按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('用户详情',100,5,'#',NULL,1,'F',0,0,'system:user:get','','用户详情按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('用户列表',100,6,'#',NULL,1,'F',0,0,'system:user:list','','用户列表按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('新增角色',101,1,'#',NULL,1,'F',0,0,'system:role:add','','新增角色按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('修改角色',101,2,'#',NULL,1,'F',0,0,'system:role:edit','','修改角色按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('角色详情',101,3,'#',NULL,1,'F',0,0,'system:role:get','','角色详情按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0);
INSERT INTO sys_menu (menu_name,parent_id,order_num,`path`,component,is_frame,menu_type,visible,status,perms,icon,remark,create_by,create_time,update_by,update_time,del_flag) VALUES
                                                                                                                                                                                   ('删除角色',101,3,'#',NULL,1,'F',0,0,'system:role:delete','','删除角色按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('角色列表',101,4,'#',NULL,1,'F',0,0,'system:role:list','','角色列表按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('新增菜单',102,1,'#',NULL,1,'F',0,0,'system:menu:add','','新增菜单按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('修改菜单',102,2,'#',NULL,1,'F',0,0,'system:menu:edit','','修改菜单按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('菜单详情',102,3,'#',NULL,1,'F',0,0,'system:menu:get','','菜单详情按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('删除菜单',102,4,'#',NULL,1,'F',0,0,'system:menu:delete','','删除菜单按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('菜单列表',102,5,'#',NULL,1,'F',0,0,'system:menu:list','','菜单列表按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('日志列表',103,1,'#',NULL,1,'F',0,0,'system:log:list','','日志列表按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('删除日志',103,2,'#',NULL,1,'F',0,0,'system:log:delete','','删除日志按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('清空日志',103,3,'#',NULL,1,'F',0,0,'system:log:empty','','清空日志按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0);
INSERT INTO sys_menu (menu_name,parent_id,order_num,`path`,component,is_frame,menu_type,visible,status,perms,icon,remark,create_by,create_time,update_by,update_time,del_flag) VALUES
                                                                                                                                                                                   ('新增FS配置',200,1,'#',NULL,1,'F',0,0,'system:fs:add','','新增fs配置按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('修改FS配置',200,2,'#',NULL,1,'F',0,0,'system:fs:edit','','修改fs配置按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('fs配置详情',200,3,'#',NULL,1,'F',0,0,'system:fs:get','','fs配置详情按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('删除fs配置',200,4,'#',NULL,1,'F',0,0,'system:fs:delete','','删除fs配置按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('fs配置列表(分页)',200,5,'#',NULL,1,'F',0,0,'system:fs:page:list','','fs配置列表(分页)按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('fs配置列表(不分页)',200,6,'#',NULL,1,'F',0,0,'system:fs:list','','fs配置列表(不分页)按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('新增FS模块',201,1,'#',NULL,1,'F',0,0,'system:fs:modules:add','','新增FS模块按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('修改FS模块',201,2,'#',NULL,1,'F',0,0,'system:fs:modules:edit','','修改FS模块按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('FS模块详情',201,3,'#',NULL,1,'F',0,0,'system:fs:modules:get','','FS模块详情按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('删除FS模块',201,4,'#',NULL,1,'F',0,0,'system:fs:modules:delete','','删除FS模块按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0);
INSERT INTO sys_menu (menu_name,parent_id,order_num,`path`,component,is_frame,menu_type,visible,status,perms,icon,remark,create_by,create_time,update_by,update_time,del_flag) VALUES
                                                                                                                                                                                   ('FS模块配置列表(分页)',201,5,'#',NULL,1,'F',0,0,'system:fs:modules:page:list','','FS模块配置列表(分页)按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('FS模块配置列表(不分页)',201,6,'#',NULL,1,'F',0,0,'system:fs:modules:list','','FS模块配置列表(不分页)按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('新增FS网关',202,1,'#',NULL,1,'F',0,0,'system:fs:gateway:add','','新增FS网关按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('修改FS网关',202,2,'#',NULL,1,'F',0,0,'system:fs:gateway:edit','','修改FS网关按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('FS网关详情',202,3,'#',NULL,1,'F',0,0,'system:fs:gateway:get','','FS网关详情按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('删除FS网关',202,4,'#',NULL,1,'F',0,0,'system:fs:gateway:delete','','删除FS网关按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('FS网关列表(分页)',202,5,'#',NULL,1,'F',0,0,'system:fs:gateway:page:list','','FS网关列表(分页)按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('FS网关列表(不分页)',202,6,'#',NULL,1,'F',0,0,'system:fs:gateway:list','','FS网关列表(不分页)按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('新增acl规则表',203,1,'#',NULL,1,'F',0,0,'system:acl:table:add','','新增acl规则表按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('新增acl规则',203,2,'#',NULL,1,'F',0,0,'system:acl:node:add','','新增acl规则按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0);
INSERT INTO sys_menu (menu_name,parent_id,order_num,`path`,component,is_frame,menu_type,visible,status,perms,icon,remark,create_by,create_time,update_by,update_time,del_flag) VALUES
                                                                                                                                                                                   ('修改acl规则表',203,3,'#',NULL,1,'F',0,0,'system:acl:list:edit','','修改acl规则表按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('修改acl规则',203,4,'#',NULL,1,'F',0,0,'system:acl:node:edit','','修改acl规则按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('acl规则详情',203,5,'#',NULL,1,'F',0,0,'system:acl:get','','acl规则详情按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('删除acl',203,6,'#',NULL,1,'F',0,0,'system:acl:delete','','删除acl按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('acl列表(分页)',203,7,'#',NULL,1,'F',0,0,'system:acl:page:list','','acl列表(分页)按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('acl列表不分页)',203,8,'#',NULL,1,'F',0,0,'system:acl:list','','acl列表(不分页)按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('新增拨号计划',204,1,'#',NULL,1,'F',0,0,'system:dialplan:add','','新增拨号计划按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('修改拨号计划',204,2,'#',NULL,1,'F',0,0,'system:dialplan:edit','','修改拨号计划按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('拨号计划详情',204,3,'#',NULL,1,'F',0,0,'system:dialplan:get','','拨号计划详情按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('删除拨号计划',204,4,'#',NULL,1,'F',0,0,'system:dialplan:delete','','删除拨号计划按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0);
INSERT INTO sys_menu (menu_name,parent_id,order_num,`path`,component,is_frame,menu_type,visible,status,perms,icon,remark,create_by,create_time,update_by,update_time,del_flag) VALUES
                                                                                                                                                                                   ('拨号计划列表(分页)',204,5,'#',NULL,1,'F',0,0,'system:dialplan:page:list','','拨号计划列表(分页)按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('拨号计划列表(不分页)',204,6,'#',NULL,1,'F',0,0,'system:dialplan:list','','拨号计划列表(不分页)按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('新增坐席',300,1,'#',NULL,1,'F',0,0,'system:agent:add','','新增坐席按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('修改坐席',300,2,'#',NULL,1,'F',0,0,'system:agent:edit','','修改坐席按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('删除坐席',300,3,'#',NULL,1,'F',0,0,'system:agent:delete','','删除坐席按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('坐席详情',300,4,'#',NULL,1,'F',0,0,'system:agent:get','','坐席详情按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('坐席列表',300,5,'#',NULL,1,'F',0,0,'system:agent:page:list','','坐席列表按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('坐席签入',300,6,'#',NULL,1,'F',0,0,'system:agent:check:in','','坐席签入按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('坐席签出',300,7,'#',NULL,1,'F',0,0,'system:agent:check:out','','坐席签出按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('坐席忙碌',300,8,'#',NULL,1,'F',0,0,'system:agent:check:busy','','坐席忙碌按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0);
INSERT INTO sys_menu (menu_name,parent_id,order_num,`path`,component,is_frame,menu_type,visible,status,perms,icon,remark,create_by,create_time,update_by,update_time,del_flag) VALUES
                                                                                                                                                                                   ('坐席通话中',300,9,'#',NULL,1,'F',0,0,'system:agent:check:calling','','坐席通话中按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                                   ('清空日志',103,3,'#',NULL,1,'F',0,0,'system:log:empty','','清空日志按钮',1,'2024-07-15 10:43:17.0',NULL,NULL,0),
                                                                                                                                                                          ('IVR管理',3,9,'/IVRManagement','',1,'C',0,0,'','#','',1,'2025-01-14 14:12:41.0',1,'2025-01-14 14:12:41.0',0);

INSERT INTO version(id, table_name, table_version)VALUES(1, 'ko_location', 9);
INSERT INTO version(id, table_name, table_version)VALUES(2, 'ko_subscriber', 6);
INSERT INTO version(id, table_name, table_version)VALUES(3, 'version', 1);
INSERT INTO version(id, table_name, table_version)VALUES(4, 'ko_dispatcher', 2);
INSERT INTO version(id, table_name, table_version)VALUES(5, 'aliases', 3);
INSERT INTO version(id, table_name, table_version)VALUES(7, 'ko_address', 6);