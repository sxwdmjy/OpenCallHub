## CentOS系统 Freeswitch 安装
1. #### 下载安装包  
``` shell
sudo yum install epel-release vim tcpdump net-tools.x86_64 -y  
sudo yum install gcc-c++ sqlite-devel zlib-devel libcurl-devel pcre-devel speex-devel ldns-devel libedit-devel openssl-devel git -y  
sudo yum install yasm nasm opus-devel -y  
sudo yum groupinstall perl* -y  
sudo yum install python -y  
sudo yum install bzip2 -y  
sudo yum install -y libshout-devel lame-devel libmpg123-devel  
sudo yum install bc -y  
sudo yum install curl -y  
sudo yum install expect telnet -y  
sudo yum install -y unixODBC unixODBC-devel mysql-connector-odbc  
sudo yum install -y yum-plugin-ovl centos-release-scl rpmdevtools yum-utils git wget vim devtoolset-7-gcc* devtoolset-7 libtiff-devel cmake3 libatomic unixODBC unixODBC-devel.x86_64 postgresql-libs postgresql-devel libpqxx-devel  
sudo yum install -y gcc-c++ autoconf automake libtool ncurses-devel zlib-devel libjpeg-devel openssl-devel e2fsprogs-devel sqlite-devel libcurl-devel pcre-devel speex-devel ldns-devel libedit-devel libxml2-devel libyuv-devel libvpx-devel libvpx2* libdb4* libidn-devel unbound-devel libuuid-devel lua-devel libsndfile-devel yasm-devel  
```
2. #### 安装sofia-sip
###### git地址可使用gitee镜像地址
```shell
cd /opt/
git clone https://github.com/freeswitch/sofia-sip
cd sofia-sip
./bootstrap.sh -j
./configure
make
make install
export PKG_CONFIG_PATH=/usr/local/lib/pkgconfig:${PKG_CONFIG_PATH}
ldconfig
```
3. #### 安装spandsp
###### git地址可使用gitee镜像地址
```shell
cd /opt/
git clone https://github.com/freeswitch/spandsp
cd spandsp
./bootstrap.sh -j
./configure
make
make install
ldconfig
```
4. #### 安装freeswitch
###### git地址可使用gitee镜像地址
```shell
cd /opt
git clone --branch v1.10.11 https://github.com/signalwire/freeswitch.git
cd /opt/freeswitch
./bootstrap.sh
sed -i 's/#formats\/mod_shout/formats\/mod_shout/g' /opt/freeswitch/modules.conf
sed -i 's/applications\/mod_signalwire/#applications\/mod_signalwire/g' /opt/freeswitch/modules.conf
sed -i 's/endpoints\/mod_verto/#endpoints\/mod_verto/g' /opt/freeswitch/modules.conf
sed -i 's/applications\/mod_av/#applications\/mod_av/g' /opt/freeswitch/modules.conf
sed -i 's/codecs\/mod_opus/#codecs\/mod_opus/g' /opt/freeswitch/modules.conf
sed -i 's/#applications\/mod_callcenter/applications\/mod_callcenter/g' /opt/freeswitch/modules.conf
sed -i 's/#applications\/mod_httapi/applications\/mod_httapi/g' /opt/freeswitch/modules.conf
sed -i 's/#event_handlers\/mod_odbc_cdr/event_handlers\/mod_odbc_cdr/g' /opt/freeswitch/modules.conf

./configure --enable-portable-binary --prefix=/usr/local/freeswitch --with-gnu-ld --with-python --with-openssl --enable-core-odbc-support --enable-zrtp
make
make install
make -j cd-sounds-install
make -j cd-moh-install
```
5. #### 配置freeswitch 
```
cd /usr/local/freeswitch/conf/autoload_configs/
1、修改modules.conf.xml
放开配置
<load module="mod_xml_curl"/>
2、修改xml_curl.conf.xml，并添加
 <binding name="all configs">
   <param name="gateway-url" value="http://127.0.0.1:8080/fs/curl/api" bindings="dialplan|configuration|phrases"/>
   <param name="timeout" value="10"/>
   <param name="gateway-credentials" value="密钥"/>
   <param name="auth-scheme" value="basic"/>
</binding>
对应项目文件中
fs:
  xml-curl:
    auth-scheme: basic # basic、digest
    secretKey: 1233333 # 密钥

3、修改event_socket.conf.xml
<settings>
    <param name="nat-map" value="false"/>
    <param name="listen-ip" value="0.0.0.0"/>
    <param name="listen-port" value="8021"/>
    <param name="password" value="密码"/>
    <param name="apply-inbound-acl" value="loopback.auto"/>
    <!--<param name="stop-on-bind-error" value="true"/>-->
 </settings>
 loopback.auto 需在acl中配置loopback.auto，可以在项目FS访问控制管理中配置访问IP
 4、如需向外拨打电话修改var.xml
 <X-PRE-PROCESS cmd="set" data="default_password=默认密码"/>
 <X-PRE-PROCESS cmd="set" data="external_rtp_ip=外网IP"/>
 <X-PRE-PROCESS cmd="set" data="external_sip_ip=外网IP"/> 
 <X-PRE-PROCESS cmd="set" data="external_auth_calls=true"/> 
 
```
6. 使用kamailio或者openSips作为SIP网关，请在FS网关管理中配置SIP网关
7. #### 启动freeswitch
```shell
#启动
freeswitch -nc
```