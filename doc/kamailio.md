## CentOS系统 Kamailio 安装(Docker 请看安装官网教程)
1. 安装依赖包
```shell
sudo  yum  install  -y  gcc  gcc-c++  flex bison make  openssl  openssl-devel libxml2  libxml2-devel  zlib-devel  ncurses*  e2fsprogs-devel  uuid-devel  libuuid-devel  readline6  readline-devel curl-devel mysql-devel lua-devel
```
2. 下载Kamailio源码包
```shell
wget http://www.kamailio.org/pub/kamailio/5.7.0/src/kamailio-5.7.0_src.tar.gz
tar -zxvf kamailio-5.7.0_src.tar.gz
cd ./kamailio-5.7.0

make FLAVOUR=kamailio cfg

#需要添加模块
make  FLAVOUR=kamailio include_modules="db_mysql app_lua" cfg

cd ./src

make all
#缺什么包装什么包就行
make install
```

3. 配置Kamailio

4. 启动Kamailio
5. 开启Kamailio日志
````
#日志输出
vim /etc/rsyslog.conf
末尾添加local0.* -/var/log/kamailio.log（可以注释修改人员和日期）
重启服务systemctl restart rsyslog.service
````