create table sys_menu
(
    menu_id     bigint auto_increment comment '菜单ID' primary key,
    menu_name   varchar(50)            not null comment '菜单名称',
    parent_id   bigint       default 0 null comment '父菜单ID',
    order_num   int          default 0 null comment '显示顺序',
    path        varchar(200) default '' null comment '路由地址',
    component   varchar(255) null comment '组件路径',
    is_frame    tinyint      default 1 null comment '是否为外链（0是 1否）',
    menu_type   char         default '' null comment '菜单类型（M目录 C菜单 F按钮）',
    visible     tinyint      default 0 null comment '菜单状态（0显示 1隐藏）',
    status      tinyint      default 0 null comment '菜单状态（0正常 1停用）',
    perms       varchar(100) null comment '权限标识',
    icon        varchar(100) default '#' null comment '菜单图标',
    remark      varchar(500) default '' null comment '备注',
    create_by   bigint null comment '创建人',
    create_time datetime     default CURRENT_TIMESTAMP null comment '创建时间',
    update_by   bigint null comment '更新人',
    update_time datetime null comment '更新时间',
    del_flag    tinyint      default 0 not null comment '删除标识 0 正常 1 删除'
) comment '菜单权限表';

INSERT INTO openCallHub.sys_menu (menu_name, parent_id, order_num, `path`, component, is_frame, menu_type, visible,
                                  status, perms, icon, remark, create_by, create_time, update_by, update_time, del_flag)
VALUES ('系统管理', 0, 1, '#', NULL, 1, 'M', 0, 0, NULL, '', '系统管理目录', 1, '2024-07-15 10:43:17.0', NULL, NULL, 0),
       ('FS配置管理', 0, 2, '#', NULL, 1, 'M', 0, 0, NULL, '', 'FS配置管理目录', 1, '2024-07-15 10:43:17.0', NULL, NULL,
        0),
       ('呼叫管理', 0, 3, '#', NULL, 1, 'M', 0, 0, NULL, '', '呼叫管理目录', 1, '2024-07-15 10:43:17.0', NULL, NULL, 0),
       ('用户管理', 1, 1, '/system/v1/user', NULL, 1, 'C', 0, 0, NULL, '', '用户管理菜单', 1, '2024-07-15 10:43:17.0',
        NULL, NULL, 0),
       ('角色管理', 1, 2, '/system/v1/role', NULL, 1, 'C', 0, 0, NULL, '', '角色管理菜单', 1, '2024-07-15 10:43:17.0',
        NULL, NULL, 0),
       ('菜单管理', 1, 3, '/system/v1/menu', NULL, 1, 'C', 0, 0, NULL, '', '菜单管理菜单', 1, '2024-07-15 10:43:17.0',
        NULL, NULL, 0),
       ('日志管理', 1, 4, '/system/v1/log', NULL, 1, 'C', 0, 0, NULL, '', '日志管理菜单', 1, '2024-07-15 10:43:17.0',
        NULL, NULL, 0),
       ('FS配置管理', 2, 1, '/system/v1/fs', NULL, 1, 'C', 0, 0, NULL, '', 'fs配置管理菜单', 1, '2024-07-15 10:43:17.0',
        NULL, NULL, 0),
       ('FS模块配置管理', 2, 2, '/system/v1/modules', NULL, 1, 'C', 0, 0, NULL, '', 'FS模块配置管理菜单', 1,
        '2024-07-15 10:43:17.0', NULL, NULL, 0),
       ('FS网关管理', 2, 3, '/system/v1/gateway', NULL, 1, 'C', 0, 0, NULL, '', 'FS网关管理菜单', 1,
        '2024-07-15 10:43:17.0', NULL, NULL, 0);
INSERT INTO openCallHub.sys_menu (menu_name, parent_id, order_num, `path`, component, is_frame, menu_type, visible,
                                  status, perms, icon, remark, create_by, create_time, update_by, update_time, del_flag)
VALUES ('FS访问控制管理', 2, 4, '/system/v1/acl', NULL, 1, 'C', 0, 0, NULL, '', 'FS访问控制管理菜单', 1,
        '2024-07-15 10:43:17.0', NULL, NULL, 0),
       ('FS拨号计划管理', 2, 5, '/system/v1/dialplan', NULL, 1, 'C', 0, 0, NULL, '', 'FS拨号计划管理菜单', 1,
        '2024-07-15 10:43:17.0', NULL, NULL, 0),
       ('坐席管理', 3, 1, '/system/v1/agent', NULL, 1, 'C', 0, 0, NULL, '', '坐席管理菜单', 1, '2024-07-15 10:43:17.0',
        NULL, NULL, 0),
       ('号码路由管理', 3, 2, 'call/v1/rout', NULL, 1, 'C', 0, 0, NULL, '', '号码路由管理', 1, '2024-07-15 10:43:17.0',
        NULL, NULL, 0),
       ('SIP号码管理', 3, 3, 'call/v1/sip', NULL, 1, 'C', 0, 0, NULL, '', 'SIP号码管理管理', 1, '2024-07-15 10:43:17.0',
        NULL, NULL, 0),
       ('新增用户', 100, 1, '#', NULL, 1, 'F', 0, 0, 'system:user:add', '', '新增用户按钮', 1, '2024-07-15 10:43:17.0',
        NULL, NULL, 0),
       ('修改用户', 100, 2, '#', NULL, 1, 'F', 0, 0, 'system:user:edit', '', '修改用户按钮', 1, '2024-07-15 10:43:17.0',
        NULL, NULL, 0),
       ('修改密码', 100, 3, '#', NULL, 1, 'F', 0, 0, 'system:user:editPassWord', '', '修改密码按钮', 1,
        '2024-07-15 10:43:17.0', NULL, NULL, 0),
       ('删除用户', 100, 4, '#', NULL, 1, 'F', 0, 0, 'system:user:delete', '', '删除用户按钮', 1,
        '2024-07-15 10:43:17.0', NULL, NULL, 0),
       ('用户详情', 100, 5, '#', NULL, 1, 'F', 0, 0, 'system:user:get', '', '用户详情按钮', 1, '2024-07-15 10:43:17.0',
        NULL, NULL, 0);
INSERT INTO openCallHub.sys_menu (menu_name, parent_id, order_num, `path`, component, is_frame, menu_type, visible,
                                  status, perms, icon, remark, create_by, create_time, update_by, update_time, del_flag)
VALUES ('用户列表', 100, 6, '#', NULL, 1, 'F', 0, 0, 'system:user:list', '', '用户列表按钮', 1, '2024-07-15 10:43:17.0',
        NULL, NULL, 0),
       ('新增角色', 101, 1, '#', NULL, 1, 'F', 0, 0, 'system:role:add', '', '新增角色按钮', 1, '2024-07-15 10:43:17.0',
        NULL, NULL, 0),
       ('修改角色', 101, 2, '#', NULL, 1, 'F', 0, 0, 'system:role:edit', '', '修改角色按钮', 1, '2024-07-15 10:43:17.0',
        NULL, NULL, 0),
       ('角色详情', 101, 3, '#', NULL, 1, 'F', 0, 0, 'system:role:get', '', '角色详情按钮', 1, '2024-07-15 10:43:17.0',
        NULL, NULL, 0),
       ('删除角色', 101, 3, '#', NULL, 1, 'F', 0, 0, 'system:role:delete', '', '删除角色按钮', 1,
        '2024-07-15 10:43:17.0', NULL, NULL, 0),
       ('角色列表', 101, 4, '#', NULL, 1, 'F', 0, 0, 'system:role:list', '', '角色列表按钮', 1, '2024-07-15 10:43:17.0',
        NULL, NULL, 0),
       ('新增菜单', 102, 1, '#', NULL, 1, 'F', 0, 0, 'system:menu:add', '', '新增菜单按钮', 1, '2024-07-15 10:43:17.0',
        NULL, NULL, 0),
       ('修改菜单', 102, 2, '#', NULL, 1, 'F', 0, 0, 'system:menu:edit', '', '修改菜单按钮', 1, '2024-07-15 10:43:17.0',
        NULL, NULL, 0),
       ('菜单详情', 102, 3, '#', NULL, 1, 'F', 0, 0, 'system:menu:get', '', '菜单详情按钮', 1, '2024-07-15 10:43:17.0',
        NULL, NULL, 0),
       ('删除菜单', 102, 4, '#', NULL, 1, 'F', 0, 0, 'system:menu:delete', '', '删除菜单按钮', 1,
        '2024-07-15 10:43:17.0', NULL, NULL, 0);
INSERT INTO openCallHub.sys_menu (menu_name, parent_id, order_num, `path`, component, is_frame, menu_type, visible,
                                  status, perms, icon, remark, create_by, create_time, update_by, update_time, del_flag)
VALUES ('菜单列表', 102, 5, '#', NULL, 1, 'F', 0, 0, 'system:menu:list', '', '菜单列表按钮', 1, '2024-07-15 10:43:17.0',
        NULL, NULL, 0),
       ('日志列表', 103, 1, '#', NULL, 1, 'F', 0, 0, 'system:log:list', '', '日志列表按钮', 1, '2024-07-15 10:43:17.0',
        NULL, NULL, 0),
       ('删除日志', 103, 2, '#', NULL, 1, 'F', 0, 0, 'system:log:delete', '', '删除日志按钮', 1,
        '2024-07-15 10:43:17.0', NULL, NULL, 0),
       ('清空日志', 103, 3, '#', NULL, 1, 'F', 0, 0, 'system:log:empty', '', '清空日志按钮', 1, '2024-07-15 10:43:17.0',
        NULL, NULL, 0),
       ('新增FS配置', 200, 1, '#', NULL, 1, 'F', 0, 0, 'system:fs:add', '', '新增fs配置按钮', 1,
        '2024-07-15 10:43:17.0', NULL, NULL, 0),
       ('修改FS配置', 200, 2, '#', NULL, 1, 'F', 0, 0, 'system:fs:edit', '', '修改fs配置按钮', 1,
        '2024-07-15 10:43:17.0', NULL, NULL, 0),
       ('fs配置详情', 200, 3, '#', NULL, 1, 'F', 0, 0, 'system:fs:get', '', 'fs配置详情按钮', 1,
        '2024-07-15 10:43:17.0', NULL, NULL, 0),
       ('删除fs配置', 200, 4, '#', NULL, 1, 'F', 0, 0, 'system:fs:delete', '', '删除fs配置按钮', 1,
        '2024-07-15 10:43:17.0', NULL, NULL, 0),
       ('fs配置列表(分页)', 200, 5, '#', NULL, 1, 'F', 0, 0, 'system:fs:page:list', '', 'fs配置列表(分页)按钮', 1,
        '2024-07-15 10:43:17.0', NULL, NULL, 0),
       ('fs配置列表(不分页)', 200, 6, '#', NULL, 1, 'F', 0, 0, 'system:fs:list', '', 'fs配置列表(不分页)按钮', 1,
        '2024-07-15 10:43:17.0', NULL, NULL, 0);
INSERT INTO openCallHub.sys_menu (menu_name, parent_id, order_num, `path`, component, is_frame, menu_type, visible,
                                  status, perms, icon, remark, create_by, create_time, update_by, update_time, del_flag)
VALUES ('新增FS模块', 201, 1, '#', NULL, 1, 'F', 0, 0, 'system:fs:modules:add', '', '新增FS模块按钮', 1,
        '2024-07-15 10:43:17.0', NULL, NULL, 0),
       ('修改FS模块', 201, 2, '#', NULL, 1, 'F', 0, 0, 'system:fs:modules:edit', '', '修改FS模块按钮', 1,
        '2024-07-15 10:43:17.0', NULL, NULL, 0),
       ('FS模块详情', 201, 3, '#', NULL, 1, 'F', 0, 0, 'system:fs:modules:get', '', 'FS模块详情按钮', 1,
        '2024-07-15 10:43:17.0', NULL, NULL, 0),
       ('删除FS模块', 201, 4, '#', NULL, 1, 'F', 0, 0, 'system:fs:modules:delete', '', '删除FS模块按钮', 1,
        '2024-07-15 10:43:17.0', NULL, NULL, 0),
       ('FS模块配置列表(分页)', 201, 5, '#', NULL, 1, 'F', 0, 0, 'system:fs:modules:page:list', '',
        'FS模块配置列表(分页)按钮', 1, '2024-07-15 10:43:17.0', NULL, NULL, 0),
       ('FS模块配置列表(不分页)', 201, 6, '#', NULL, 1, 'F', 0, 0, 'system:fs:modules:list', '',
        'FS模块配置列表(不分页)按钮', 1, '2024-07-15 10:43:17.0', NULL, NULL, 0),
       ('新增FS网关', 202, 1, '#', NULL, 1, 'F', 0, 0, 'system:fs:gateway:add', '', '新增FS网关按钮', 1,
        '2024-07-15 10:43:17.0', NULL, NULL, 0),
       ('修改FS网关', 202, 2, '#', NULL, 1, 'F', 0, 0, 'system:fs:gateway:edit', '', '修改FS网关按钮', 1,
        '2024-07-15 10:43:17.0', NULL, NULL, 0),
       ('FS网关详情', 202, 3, '#', NULL, 1, 'F', 0, 0, 'system:fs:gateway:get', '', 'FS网关详情按钮', 1,
        '2024-07-15 10:43:17.0', NULL, NULL, 0),
       ('删除FS网关', 202, 4, '#', NULL, 1, 'F', 0, 0, 'system:fs:gateway:delete', '', '删除FS网关按钮', 1,
        '2024-07-15 10:43:17.0', NULL, NULL, 0);
INSERT INTO openCallHub.sys_menu (menu_name, parent_id, order_num, `path`, component, is_frame, menu_type, visible,
                                  status, perms, icon, remark, create_by, create_time, update_by, update_time, del_flag)
VALUES ('FS网关列表(分页)', 202, 5, '#', NULL, 1, 'F', 0, 0, 'system:fs:gateway:page:list', '', 'FS网关列表(分页)按钮',
        1, '2024-07-15 10:43:17.0', NULL, NULL, 0),
       ('FS网关列表(不分页)', 202, 6, '#', NULL, 1, 'F', 0, 0, 'system:fs:gateway:list', '', 'FS网关列表(不分页)按钮',
        1, '2024-07-15 10:43:17.0', NULL, NULL, 0),
       ('新增acl规则表', 203, 1, '#', NULL, 1, 'F', 0, 0, 'system:acl:table:add', '', '新增acl规则表按钮', 1,
        '2024-07-15 10:43:17.0', NULL, NULL, 0),
       ('新增acl规则', 203, 2, '#', NULL, 1, 'F', 0, 0, 'system:acl:node:add', '', '新增acl规则按钮', 1,
        '2024-07-15 10:43:17.0', NULL, NULL, 0),
       ('修改acl规则表', 203, 3, '#', NULL, 1, 'F', 0, 0, 'system:acl:list:edit', '', '修改acl规则表按钮', 1,
        '2024-07-15 10:43:17.0', NULL, NULL, 0),
       ('修改acl规则', 203, 4, '#', NULL, 1, 'F', 0, 0, 'system:acl:node:edit', '', '修改acl规则按钮', 1,
        '2024-07-15 10:43:17.0', NULL, NULL, 0),
       ('acl规则详情', 203, 5, '#', NULL, 1, 'F', 0, 0, 'system:acl:get', '', 'acl规则详情按钮', 1,
        '2024-07-15 10:43:17.0', NULL, NULL, 0),
       ('删除acl', 203, 6, '#', NULL, 1, 'F', 0, 0, 'system:acl:delete', '', '删除acl按钮', 1, '2024-07-15 10:43:17.0',
        NULL, NULL, 0),
       ('acl列表(分页)', 203, 7, '#', NULL, 1, 'F', 0, 0, 'system:acl:page:list', '', 'acl列表(分页)按钮', 1,
        '2024-07-15 10:43:17.0', NULL, NULL, 0),
       ('acl列表不分页)', 203, 8, '#', NULL, 1, 'F', 0, 0, 'system:acl:list', '', 'acl列表(不分页)按钮', 1,
        '2024-07-15 10:43:17.0', NULL, NULL, 0);
INSERT INTO openCallHub.sys_menu (menu_name, parent_id, order_num, `path`, component, is_frame, menu_type, visible,
                                  status, perms, icon, remark, create_by, create_time, update_by, update_time, del_flag)
VALUES ('新增拨号计划', 204, 1, '#', NULL, 1, 'F', 0, 0, 'system:dialplan:add', '', '新增拨号计划按钮', 1,
        '2024-07-15 10:43:17.0', NULL, NULL, 0),
       ('修改拨号计划', 204, 2, '#', NULL, 1, 'F', 0, 0, 'system:dialplan:edit', '', '修改拨号计划按钮', 1,
        '2024-07-15 10:43:17.0', NULL, NULL, 0),
       ('拨号计划详情', 204, 3, '#', NULL, 1, 'F', 0, 0, 'system:dialplan:get', '', '拨号计划详情按钮', 1,
        '2024-07-15 10:43:17.0', NULL, NULL, 0),
       ('删除拨号计划', 204, 4, '#', NULL, 1, 'F', 0, 0, 'system:dialplan:delete', '', '删除拨号计划按钮', 1,
        '2024-07-15 10:43:17.0', NULL, NULL, 0),
       ('拨号计划列表(分页)', 204, 5, '#', NULL, 1, 'F', 0, 0, 'system:dialplan:page:list', '',
        '拨号计划列表(分页)按钮', 1, '2024-07-15 10:43:17.0', NULL, NULL, 0),
       ('拨号计划列表(不分页)', 204, 6, '#', NULL, 1, 'F', 0, 0, 'system:dialplan:list', '', '拨号计划列表(不分页)按钮',
        1, '2024-07-15 10:43:17.0', NULL, NULL, 0),
       ('新增坐席', 300, 1, '#', NULL, 1, 'F', 0, 0, 'system:agent:add', '', '新增坐席按钮', 1, '2024-07-15 10:43:17.0',
        NULL, NULL, 0),
       ('修改坐席', 300, 2, '#', NULL, 1, 'F', 0, 0, 'system:agent:edit', '', '修改坐席按钮', 1,
        '2024-07-15 10:43:17.0', NULL, NULL, 0),
       ('删除坐席', 300, 3, '#', NULL, 1, 'F', 0, 0, 'system:agent:delete', '', '删除坐席按钮', 1,
        '2024-07-15 10:43:17.0', NULL, NULL, 0),
       ('坐席详情', 300, 4, '#', NULL, 1, 'F', 0, 0, 'system:agent:get', '', '坐席详情按钮', 1, '2024-07-15 10:43:17.0',
        NULL, NULL, 0);
INSERT INTO openCallHub.sys_menu (menu_name, parent_id, order_num, `path`, component, is_frame, menu_type, visible,
                                  status, perms, icon, remark, create_by, create_time, update_by, update_time, del_flag)
VALUES ('坐席列表', 300, 5, '#', NULL, 1, 'F', 0, 0, 'system:agent:page:list', '', '坐席列表按钮', 1,
        '2024-07-15 10:43:17.0', NULL, NULL, 0),
       ('坐席签入', 300, 6, '#', NULL, 1, 'F', 0, 0, 'system:agent:check:in', '', '坐席签入按钮', 1,
        '2024-07-15 10:43:17.0', NULL, NULL, 0),
       ('坐席签出', 300, 7, '#', NULL, 1, 'F', 0, 0, 'system:agent:check:out', '', '坐席签出按钮', 1,
        '2024-07-15 10:43:17.0', NULL, NULL, 0),
       ('坐席忙碌', 300, 8, '#', NULL, 1, 'F', 0, 0, 'system:agent:check:busy', '', '坐席忙碌按钮', 1,
        '2024-07-15 10:43:17.0', NULL, NULL, 0),
       ('坐席通话中', 300, 9, '#', NULL, 1, 'F', 0, 0, 'system:agent:check:calling', '', '坐席通话中按钮', 1,
        '2024-07-15 10:43:17.0', NULL, NULL, 0);

create table sys_oper_log
(
    id             bigint auto_increment comment '日志主键'
        primary key,
    title          varchar(50)   default '' null comment '模块标题',
    business_type  tinyint       default 0 null comment '业务类型（0=其它,1=新增,2=修改,3=删除,4=查询,5=导出,6=导入 7-登录 8-登出）',
    method         varchar(100)  default '' null comment '方法名称',
    request_method varchar(10)   default '' null comment '请求方式',
    operator_type  tinyint       default 0 null comment '操作类别（0其它 1后台用户 2手机端用户）',
    oper_name      varchar(50)   default '' null comment '操作人员',
    oper_url       varchar(255)  default '' null comment '请求URL',
    oper_ip        varchar(128)  default '' null comment '主机地址',
    oper_location  varchar(255)  default '' null comment '操作地点',
    oper_param     varchar(2000) default '' null comment '请求参数',
    json_result    varchar(2000) default '' null comment '返回参数',
    status         tinyint       default 0 null comment '操作状态（0正常 1异常）',
    error_msg      varchar(2000) default '' null comment '错误消息',
    oper_time      datetime null comment '操作时间',
    cost_time      bigint        default 0 null comment '消耗时间',
    create_by      varchar(255) null comment '创建人',
    create_time    datetime      default CURRENT_TIMESTAMP not null comment '创建时间',
    update_by      varchar(255) null comment '更新人',
    update_time    datetime null comment '修改时间',
    del_flag       tinyint       default 0                 not null comment '删除标识 0正常 1 删除'
) comment '系统操作日志记录';

create index idx_sys_oper_log_bt
    on sys_oper_log (business_type);

create index idx_sys_oper_log_ot
    on sys_oper_log (oper_time);

create index idx_sys_oper_log_s
    on sys_oper_log (status);

create table sys_role
(
    role_id     bigint auto_increment comment '角色ID'
        primary key,
    role_name   varchar(30)        not null comment '角色名称',
    role_key    varchar(100)       not null comment '角色权限字符串',
    role_sort   int                not null comment '显示顺序',
    data_scope  tinyint  default 1 null comment '数据范围（1：全部数据权限 2:本部门及以下数据权限 3：本部门数据权限 4：本人数据权限）',
    status      tinyint  default 0 null comment '角色状态（0正常 1停用）',
    remark      varchar(500) null comment '备注',
    create_by   bigint null comment '创建人',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_by   bigint null comment '更新人',
    update_time datetime null comment '更新时间',
    del_flag    tinyint  default 0 not null comment '删除标识 0 正常 1 删除'
) comment '角色信息表';

INSERT INTO openCallHub.sys_role (role_name, role_key, role_sort, data_scope, status, remark, create_by, create_time,
                                  update_by, update_time, del_flag)
VALUES ('超级管理员', 'ROLE_ADMIN', 0, 1, 0, '超级管理员', NULL, '2024-07-15 10:40:59.0', NULL, NULL, 0);

create table sys_role_menu
(
    id          bigint auto_increment comment '主键ID'
        primary key,
    role_id     bigint             not null comment '角色ID',
    menu_id     bigint             not null comment '菜单ID',
    create_by   bigint null comment '创建人',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_by   bigint null comment '更新人',
    update_time datetime null comment '更新时间',
    del_flag    tinyint  default 0 not null comment '删除标识 0 正常 1 删除'
) comment '角色菜单关联表';

create index role_menu_id_idx
    on sys_role_menu (menu_id, role_id);

create table sys_user
(
    user_id     bigint auto_increment comment '用户ID'
        primary key,
    user_name   varchar(50)        not null comment '用户账号',
    nick_name   varchar(50) null comment '用户昵称',
    password    varchar(64) null comment '密码',
    avatar      varchar(128) null comment '用户头像',
    sex         tinyint null comment '用户性别（0-未知 1-男 2-女）',
    phone       varchar(32) null comment '手机号',
    email       varchar(32) null comment '邮箱',
    status      tinyint  default 1 null comment '状态 1-启用 2-禁用',
    remark      varchar(500) null comment '备注',
    create_by   bigint   default 1 null comment '创建人',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_by   bigint null comment '更新人',
    update_time datetime null comment '更新时间',
    del_flag    tinyint  default 0 not null comment '删除标识 0 正常 1 删除'
) comment '用户信息表';

INSERT INTO openCallHub.sys_user (user_name, nick_name, password, avatar, sex, phone, email, status, remark, create_by,
                                  create_time, update_by, update_time, del_flag)
VALUES ('admin', '系统管理员', '$2a$10$vfKaPSchXa41aZoCJQ1nnu.pUX3qHH.ve5JdNWHVP/EOy5yv7IMvW', NULL, 1, NULL, NULL, 1,
        '系统管理员', 1, '2024-07-15 10:39:43.0', NULL, NULL, 0);

create table sys_user_role
(
    id          bigint auto_increment comment '主键ID'
        primary key,
    user_id     bigint             not null comment '用户ID',
    role_id     bigint             not null comment '角色ID',
    create_by   bigint null comment '创建人',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_by   bigint null comment '更新人',
    update_time datetime null comment '更新时间',
    del_flag    tinyint  default 0 not null comment '删除标识 0 正常 1 删除'
) comment '用户角色关联表';

create index user_role_id_idx
    on sys_user_role (user_id, role_id);

INSERT INTO openCallHub.sys_user_role (user_id, role_id, create_by, create_time, update_by, update_time, del_flag)
VALUES (1, 1, 1, '2024-07-16 02:49:33.0', NULL, NULL, 0);

create table fs_acl
(
    id           bigint auto_increment comment '主键ID'
        primary key,
    name         varchar(50)           not null comment '名称',
    default_type varchar(20) default 'allow' null comment '类型 allow-允许 deny-拒绝',
    list_id      bigint      default 0 not null comment '列表ID',
    node_type    varchar(20) null comment '规则类型 allow-允许 deny-拒绝',
    cidr         varchar(64) null comment 'IP地址',
    domain       varchar(64) null comment '域地址',
    create_by    bigint null comment '创建人',
    create_time  datetime null comment '创建时间',
    update_by    bigint null comment '更新人',
    update_time  datetime null comment '更新时间',
    del_flag     tinyint     default 0 not null comment '删除标识 0 正常 1 删除'
) comment 'fs访问控制表';

INSERT INTO openCallHub.fs_acl (name, default_type, list_id, node_type, cidr, `domain`, create_by, create_time,
                                update_by, update_time, del_flag)
VALUES ('lan', 'allow', 0, NULL, NULL, NULL, 1, NULL, NULL, NULL, 0),
       ('domains', 'deny', 0, NULL, NULL, NULL, 1, NULL, NULL, NULL, 0),
       ('loopback.auto', 'allow', 0, NULL, NULL, NULL, 1, NULL, NULL, NULL, 0),
       ('lan', 'allow', 1, 'allow', '127.0.0.1/32', NULL, 1, NULL, NULL, NULL, 0),
       ('domains', 'deny', 2, 'allow', '', '$${domain}', 1, NULL, NULL, NULL, 0),
       ('loopback.auto', 'allow', 3, 'allow', '127.0.0.1/32', '', 1, NULL, NULL, NULL, 0);

create table fs_dialplan
(
    id           bigint auto_increment comment '主键ID'
        primary key,
    group_id     bigint            not null comment '分组ID',
    name         varchar(128)      not null comment '计划名称',
    type         varchar(64)       not null comment '类型 xml格式,json格式',
    expression   varchar(100) null comment '正则匹配规则',
    context_name varchar(32)       not null comment '内容类型 public、default',
    content      longtext          not null comment '内容',
    `describe`   varchar(256)      not null comment '描述',
    create_by    bigint null comment '创建人',
    create_time  datetime null comment '创建时间',
    update_by    bigint null comment '更新人',
    update_time  datetime null comment '更新时间',
    del_flag     tinyint default 0 not null comment '删除标识 0 正常 1 删除'
) comment 'fs拨号计划表';

create table fs_config
(
    id          int auto_increment comment '主键ID'
        primary key,
    name        varchar(128) null comment '名称',
    `group`     varchar(64) null comment '客户端分组',
    ip          varchar(64) null comment '机器地址IP',
    port        int null comment '服务器端口',
    user_name   varchar(64) null comment '用户名',
    password    varchar(64) null comment '密码',
    status      tinyint  default 0 null comment '状态 0-在线 1-下线',
    out_time    int null comment '超时时间（秒）',
    create_by   bigint null comment '创建人',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_by   bigint null comment '更新人',
    update_time datetime null comment '修改时间',
    del_flag    tinyint  default 0                 not null comment '删除标识 0 有效 1删除'
) comment 'fs管理配置表';
INSERT INTO openCallHub.fs_config (name, `group`, ip, port, user_name, password, status, out_time, create_by,
                                   create_time, update_by, update_time, del_flag)
VALUES ('FS01', '1', '127.0.0.1', 8021, NULL, 'c2a205fd74bf4c70b9cdec4df34bb930', 1, 60, NULL, '2024-07-18 05:58:36.0',
        1, '2024-07-18 14:52:57.0', 0);

create table fs_cdr
(
    call_id                bigint                             not null comment '通话唯一ID'
        primary key,
    uuid                   varchar(64) null comment 'uuid',
    direction              varchar(16) null comment '呼叫方向 outbound-呼出 inbound-呼入',
    sip_local_network_addr varchar(32) null comment '本地呼叫地址',
    sip_network_ip         varchar(32) null comment '呼叫IP',
    caller_id_number       varchar(64) null comment '主叫号码',
    caller_display         varchar(64) null comment '主叫显号',
    destination_number     varchar(64) null comment '被叫号码',
    destination_display    varchar(64) null comment '被叫显号',
    start_stamp            datetime null comment '开始时间',
    answer_stamp           datetime null comment '应答时间',
    end_stamp              datetime null comment '结束时间',
    bridge_stamp           datetime null comment '桥接时间',
    progress_stamp         datetime null comment '振铃时间',
    duration               int null comment '呼叫时长',
    answer_sec             int null comment '应答时长',
    bill_sec               int null comment '计费时长',
    hangup_cause           varchar(64) null comment '挂断原因',
    record_start_time      datetime null comment '录音开始时间',
    record_end_time        datetime null comment '录音结束时间',
    record_sec             int null comment '录音时长',
    record                 varchar(128) null comment '录音地址',
    read_codec             varchar(64) null comment '读编码类型',
    write_codec            varchar(64) null comment '写编码类型',
    sip_hangup_disposition varchar(64) null comment '挂断意向',
    create_by              bigint null comment '创建人',
    create_time            datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_by              bigint null comment '更新人',
    update_time            datetime null comment '修改时间',
    del_flag               tinyint  default 0                 not null comment '删除标识 0 有效 1删除'
) comment 'cdr话单表';

create table fs_modules
(
    id          bigint auto_increment comment '主键ID'
        primary key,
    fs_name     varchar(128)      not null comment 'FS主机名',
    name        varchar(128)      not null comment '模块名称',
    type        varchar(64)       not null comment '类型 xml格式,json格式',
    content     longtext          not null comment '内容',
    `describe`  varchar(256)      not null comment '描述',
    create_by   bigint null comment '创建人',
    create_time datetime null comment '创建时间',
    update_by   bigint null comment '更新人',
    update_time datetime null comment '更新时间',
    del_flag    tinyint default 0 not null comment '删除标识 0 正常 1 删除'
) comment 'fs模块管理表';

INSERT INTO openCallHub.fs_modules (fs_name, name, `type`, content, `describe`, create_by, create_time, update_by,
                                    update_time, del_flag)
VALUES ('FS01', 'xml_cdr.conf', 'xml', '<settings>
    <param name="url" value="http://127.0.0.1:8080/och-api/fs/cdr/api"/>
    <param name="cred" value="b4c91178-c00e-7e42-6a9b-2c470eb0fb30"/>
    <param name="retries" value="2"/>
    <param name="delay" value="3"/>
    <param name="log-http-and-disk" value="true"/>
    <param name="log-dir" value="/logs/xml_cdr "/>
    <param name="log-b-leg" value="false"/>
    <param name="prefix-a-leg" value="true"/>
    <param name="encode" value="true"/>
</settings>', 'xml_cdr文件配置', 1, NULL, NULL, NULL, 0);

create table fs_sip_gateway
(
    id                bigint auto_increment comment '主键ID'
        primary key,
    name              varchar(50)             not null comment '网关名称',
    uer_name          varchar(50) null comment '账号',
    password          varchar(64) null comment '密码',
    realm             varchar(32)             not null comment '认证地址',
    proxy             varchar(32) null comment '代理地址',
    register          tinyint     default 1 null comment '注册类型 0-不注册 1-注册',
    transport         tinyint     default 1 null comment '注册协议 1-udp, 2-tcp',
    caller_id_in_from tinyint     default 0   not null comment '通过此网关出站呼叫时，在from字段中使用入站呼叫的callerid(0-true 1-false)',
    from_domain       varchar(32) default '' null comment 'from域',
    retry_time        int         default 30 null comment '重试时间（秒）',
    ping_time         int         default 30 null comment '心跳时间（秒）',
    expire_time       int         default 300 not null comment '超时时间（秒）',
    create_by         bigint null comment '创建人',
    create_time       datetime    default CURRENT_TIMESTAMP null comment '创建时间',
    update_by         bigint null comment '更新人',
    update_time       datetime null comment '更新时间',
    del_flag          tinyint     default 0   not null comment '删除标识 0 正常 1 删除'
) comment 'SIP网关表';

INSERT INTO openCallHub.fs_sip_gateway (name, uer_name, password, realm, proxy, register, transport, caller_id_in_from,
                                        from_domain, retry_time, ping_time, expire_time, create_by, create_time,
                                        update_by, update_time, del_flag)
VALUES ('gw_test', NULL, NULL, '127.0.0.1', '127.0.0.1:5060', 0, 1, 0, '127.0.0.1', 30, 25, 60, 1,
        '2024-07-18 02:26:53.0', NULL, NULL, 0),
       ('kamailio', NULL, NULL, '127.0.0.1', '127.0.0.1:2452', 0, 1, 0, '127.0.0.1', 30, 25, 60, 1,
        '2024-07-18 02:26:53.0', NULL, NULL, 0);

create table sys_category
(
    id          bigint                             not null comment '主键id'
        primary key,
    type        int                                not null comment '1-技能 ',
    name        varchar(50) null comment '分类名称',
    parent_id   bigint(100) default 0 null comment '父分类的id',
    flag        tinyint  default 0 null comment '可删除标识 0 可删除 1 不可删除',
    create_by   bigint null comment '创建人',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_by   bigint null comment '更新人',
    update_time datetime null comment '修改时间',
    del_flag    tinyint  default 0                 not null comment '删除标识 0 有效 1删除'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 comment '分类配置表';


CREATE TABLE `ko_dispatcher`
(
    `id`          int(10) NOT NULL AUTO_INCREMENT,
    `setid`       int(11) NOT NULL DEFAULT '0' COMMENT '分组ID',
    `destination` varchar(192) NOT NULL DEFAULT '' COMMENT '目标地址',
    `flags`       int(11) NOT NULL DEFAULT '0' COMMENT '标识',
    `priority`    int(11) NOT NULL DEFAULT '0' COMMENT '优先级',
    `attrs`       varchar(128) NOT NULL DEFAULT '' COMMENT '属性',
    `description` varchar(64)  NOT NULL DEFAULT '' COMMENT '描述',
    `status`      tinyint(4) DEFAULT '0' COMMENT '状态 0-在线 1-下线',
    `create_by`   bigint(20) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   bigint(20) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime              DEFAULT NULL COMMENT '更新时间',
    `del_flag`    tinyint(4) NOT NULL DEFAULT '0' COMMENT '删除标识 0 正常 1 删除',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 comment 'KO负载管理表';


CREATE TABLE `ko_subscriber`
(
    `id`       int(10) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` varchar(64)  NOT NULL DEFAULT '' COMMENT '用户名称',
    `domain`   varchar(64)  NOT NULL DEFAULT '' COMMENT '域名地址',
    `password` varchar(64)  NOT NULL DEFAULT '' COMMENT '密码',
    `ha1`      varchar(128) NOT NULL DEFAULT '' COMMENT '哈希码',
    `ha1b`     varchar(128) NOT NULL DEFAULT '' COMMENT '哈希码',
    `vmpin`    varchar(8)   NOT NULL DEFAULT '1234' COMMENT '鉴权值',
    `status`   tinyint(4) DEFAULT '0' COMMENT '状态 0-开启 1-关闭',
    `create_by`   bigint(20) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   bigint(20) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime              DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `account_idx` (`username`, `domain`),
    KEY        `username_idx` (`username`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 comment '用户订阅表（SIP 订阅）';


create table sip_agent
(
    id            bigint auto_increment comment '主键ID'
        primary key,
    name          varchar(128)      not null comment '坐席名称',
    user_id       bigint            not null comment '主键ID',
    agent_number  varchar(64)       not null comment 'sip账号ID',
    status        tinyint default 0 not null comment '开通状态 0-未开通 1-开通',
    online_status tinyint default 3 not null comment '在线状态 0-空闲  1-忙碌 2-通话中 3-离线',
    create_by     bigint null comment '创建人',
    create_time   datetime null comment '创建时间',
    update_by     bigint null comment '更新人',
    update_time   datetime null comment '更新时间',
    del_flag      tinyint default 0 not null comment '删除标识 0 正常 1 删除'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 comment '坐席管理表';


create table corp_info
(
    id                int(20) auto_increment comment '主键ID'
        primary key,
    corp_name         varchar(50)            not null comment '企业名称',
    corp_code         varchar(64) null comment '企业编码',
    secret_key        varchar(32)            not null comment '密钥',
    status            tinyint      default 1 null comment '企业状态 1-启用 2-禁用',
    seat_num          int          default 0 null comment '坐席数量',
    ivr_num           int          default 0 null comment 'IVR并发数量',
    record_num        int          default 0 null comment '录音保留天数',
    balance           varchar(18)  default '0' null comment '账户余额',
    corp_manage       varchar(50) null comment '企业负责人',
    corp_manage_phone varchar(32)  default '' null comment '企业负责人手机号',
    call_back_path    varchar(200) default '' null comment '回调地址',
    remark            varchar(500) default '' null comment '备注',
    create_by         bigint null comment '创建人',
    create_time       datetime     default CURRENT_TIMESTAMP null comment '创建时间',
    update_by         bigint null comment '更新人',
    update_time       datetime null comment '更新时间',
    del_flag          tinyint      default 0 not null comment '删除标识 0 正常 1 删除'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 comment '企业信息表';


create table call_route
(
    id                 bigint auto_increment
        primary key,
    route_num          varchar(32)                           not null comment '路由号码',
    caller_num         varchar(32) default '*'               not null comment '主叫号码',
    len_min            int         default 4 null comment '号码匹配最小长度',
    len_max            int         default 32 null comment '号码匹配最大长度',
    caller_pattern     varchar(64) null comment '主叫替换规则',
    caller_replace_num varchar(32) null comment '主叫替换号码',
    callee_pattern     varchar(64) null comment '被叫替换规则',
    callee_replace_num varchar(32) null comment '被叫替换号码',
    status             tinyint     default 0 null comment '状态  0-未启用 1-启用',
    create_by          bigint null comment '创建人',
    create_time        datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    update_by          bigint null comment '更新人',
    update_time        datetime null comment '修改时间',
    del_flag           tinyint     default 0                 not null comment '删除标识 0 有效 1删除'
)ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 comment '号码路由表';


create table call_route_rel
(
    id                bigint auto_increment
        primary key,
    order_num         int      default 1                 not null comment '排序字段',
    call_route_id     bigint                             not null comment '号码路由ID',
    gateway_config_id varchar(64)                        not null comment '网关配置ID',
    create_by         bigint                             null comment '创建人',
    create_time       datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_by         bigint                             null comment '更新人',
    update_time       datetime                           null comment '修改时间',
    del_flag          tinyint  default 0                 not null comment '删除标识 0 有效 1删除'
)
    ENGINE = InnoDB DEFAULT CHARSET = utf8mb4  comment '号码路由网关配置关联表';



create table call_display
(
    id          bigint auto_increment comment '主键'
        primary key,
    phone       varchar(18)                        not null comment '电话号码',
    type        tinyint  default 1                 not null comment '号码类型 1-主叫显号 2-被叫显号',
    area        varchar(64)                        null comment '归属地',
    create_by   bigint                             null comment '创建人',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_by   bigint                             null comment '更新人',
    update_time datetime                           null comment '更新时间',
    del_flag    tinyint  default 0                 not null comment '删除标识 0 正常 1 删除'
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4
    comment '号码管理';


create table call_display_pool
(
    id          bigint auto_increment comment '主键'
        primary key,
    name        varchar(18)                        not null comment '电话池名称',
    type        tinyint  default 1                 not null comment '类型 1-随机 2-轮询',
    create_by   bigint                             null comment '创建人',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_by   bigint                             null comment '更新人',
    update_time datetime                           null comment '更新时间',
    tenant_id   int(20)  default 0                 not null comment '租户ID',
    del_flag    tinyint  default 0                 not null comment '删除标识 0 正常 1 删除'
)ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4
    comment '号码池管理';

create table call_display_pool_rel
(
    id          bigint auto_increment comment '主键'
        primary key,
    pool_id       bigint   not null comment '电话池ID',
    display_id        bigint  not null comment '号码ID',
    create_by   bigint                             null comment '创建人',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_by   bigint                             null comment '更新人',
    update_time datetime                           null comment '更新时间',
    del_flag    tinyint  default 0                 not null comment '删除标识 0 正常 1 删除'
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4
    comment '号码池号码关联表';


create table call_skill
(
    id                bigint auto_increment comment '主键ID'
        primary key,
    group_id          bigint                             null comment '分组ID',
    name              varchar(128)                       null comment '技能名称',
    after_time        int      default 0                 null comment '话后空闲时间 1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h',
    priority          int      default 0                 not null comment '优先级',
    `describe`        varchar(128)                       null comment '描述',
    strategy_type     tinyint  default 0                 not null comment '策略类型 0-随机 1-轮询 2-最长空闲时间 3-当天最少应答次数 4-最长话后时长',
    full_busy_type    tinyint  default 0                 null comment '全忙 0-排队 1-溢出 2-挂机',
    overflow_type     tinyint  default 0                 null comment '溢出策略 0-挂机 1-转IVR',
    overflow_value    varchar(32)                        null comment '溢出策略值',
    time_out          int                                null comment '排队超时时间（秒）',
    queue_length      int                                null comment '最大排队人数',
    queue_voice       bigint                             null comment '排队音',
    queue_voice_value varchar(64)                        null comment '排队音',
    agent_voice       bigint                             null comment '转坐席音',
    agent_voice_value varchar(64)                        null comment '转坐席音名称',
    create_by         bigint                             null comment '创建人',
    create_time       datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_by         bigint                             null comment '更新人',
    update_time       datetime                           null comment '修改时间',
    del_flag          tinyint  default 0                 not null comment '删除标识 0 有效 1删除'
)
    ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '技能表';

create table call_skill_agent_rel
(
    id          bigint auto_increment comment '主键ID'
        primary key,
    skill_id    bigint                             null comment '技能ID',
    agent_id    varchar(128)                       null comment '坐席ID',
    level       int                                null comment '级别',
    create_by   bigint                             null comment '创建人',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_by   bigint                             null comment '更新人',
    update_time datetime                           null comment '修改时间',
    del_flag    tinyint  default 0                 not null comment '删除标识 0 有效 1删除'
)
    ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '技能坐席关联表';

CREATE TABLE `voice_file` (
                              `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                              `name` varchar(50) NOT NULL COMMENT '文件名称',
                              `type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '类型 1-手动上传 2-语音合成',
                              `tts` tinyint(4) DEFAULT NULL COMMENT 'tts方式 1-腾讯 2-阿里 3-讯飞(type=2生效)',
                              `speech_text` varchar(500) DEFAULT NULL COMMENT '合成文本',
                              `file_id` varchar(32) DEFAULT NULL COMMENT '文件ID',
                              `file` varchar(128) DEFAULT NULL COMMENT '文件地址',
                              `create_by` bigint(20) DEFAULT NULL COMMENT '创建人',
                              `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              `update_by` bigint(20) DEFAULT NULL COMMENT '更新人',
                              `update_time` datetime DEFAULT NULL COMMENT '修改时间',
                              `del_flag` tinyint(4) NOT NULL DEFAULT '0' COMMENT '删除标识 0 有效 1删除',
                              PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='语音文件表';


CREATE TABLE `call_record` (
                               `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                               `call_id` varchar(64) NOT NULL  COMMENT '呼叫唯一ID',
                               `caller_number` varchar(32) NOT NULL COMMENT '主叫号码',
                               `caller_display_number` varchar(32) NOT NULL COMMENT '主叫显号',
                               `callee_number` varchar(32) NOT NULL COMMENT '被叫号码',
                               `callee_display_number` varchar(32) NOT NULL COMMENT '被叫显号',
                               `number_location` varchar(128) DEFAULT NULL COMMENT '号码归属地',
                               `agent_id` bigint(20) NOT NULL COMMENT '坐席ID',
                               `agent_number` varchar(32) NOT NULL COMMENT '坐席号码',
                               `agent_name` varchar(128) NOT NULL COMMENT '坐席名称',
                               `call_state` tinyint(4) NOT NULL  COMMENT '呼叫状态 1-成功 2-失败',
                               `direction` tinyint(4) DEFAULT NULL COMMENT '呼叫方式 1-呼出 2-呼入',
                               `call_start_time` datetime DEFAULT NULL COMMENT '呼叫开始时间',
                               `call_end_time` datetime DEFAULT NULL COMMENT '呼叫结束时间',
                               `answer_flag` tinyint(4) NOT NULL COMMENT '应答标识 0-接通 1-坐席未接用户未接 2-坐席接通用户未接通 3-用户接通坐席未接通',
                               `answer_time` datetime NOT NULL COMMENT '呼叫接通时间',
                               `ringing_time` datetime DEFAULT NULL COMMENT '振铃时间',
                               `hangup_dir` tinyint(4) NOT NULL COMMENT '挂机方向 1-主叫挂机 2-被叫挂机 3-系统挂机',
                               `hangup_cause_code` tinyint(4) NOT NULL COMMENT '挂机原因 ',
                               `file_path` varchar(500) DEFAULT NULL COMMENT '录音文件地址',
                               `ringing_path` varchar(500) DEFAULT NULL COMMENT '振铃文件地址',
                               `create_by` bigint(20) DEFAULT NULL COMMENT '创建人',
                               `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                               `update_by` bigint(20) DEFAULT NULL COMMENT '更新人',
                               `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                               `del_flag` tinyint(4) NOT NULL DEFAULT '0' COMMENT '删除标识 0 正常 1 删除',
                               PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='呼叫记录表';

