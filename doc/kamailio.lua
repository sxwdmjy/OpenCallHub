-- 全局变量，在这里再写一遍，要与主配置文件中相应的配置一样
FLT_ACC=1
FLT_ACCMISSED=2
FLT_ACCFAILED=3
FLT_NATS=5
FLB_NATB=6
FLB_NATSIPPING=7

-- 这个名称固定的，对应原生脚本request_route,收到所有SIP请求都会调用该函数进行处理
function ksr_request_route()
  ksr_route_reqinit();
  -- 初始合法性检查，其实他就是一个一般的Lua函数（function）调用，函数名可以任意
  -- NAT处理
  ksr_route_natdetect();
  --CANCEL处理
  if KSR.is_CANCEL() then 
    if KSR.tm.t_check_trans()>0 then
      ksr_route_relay();
    end
    return 1;
  end
  -- 处理重传
  if not KSR.is_ACK() then
    -- 如果没有对话（仅对于没有To tag的消息）,则继续进行下面的处理
    if KSR.tmx.t_precheck_trans()>0 then
      KSR.tm.t_check_trans();
      return 1;
    end
    if KSR.tm.t_check_trans()==0 then return 1 end
  end
  -- SIP对话内的消息处理
  ksr_route_withindlg();
  --鉴权
  ksr_route_auth();
  -- 去掉原有的Route消息头(如果有的话),并换成我们自己的，以便后于的消息还经过我们
  KSR.hdr.remove("Route");
  --如果method是INVITE或SUBSCRIBE
  if KSR.is_method_in("IS") then 
    KSR.rr.record_route();
  end

  if KSR.is_INVITE() then --仅对INVITE记账
    KSR.setflag(FLT_ACC); --记账
  end

  --将domain不是本地的请求转发到外部服务器
  ksr_route_sipout(); 


  -- 下面是我们自己的domain了

  --处理注册
  ksr_route_registrar(); 

  if KSR.corex.has_ruri_user() < 0 then
    -- RUI中必有user部分，否则回复“484地址不全”的消息
    KSR.sl.sl_send_reply(484,"Address Incomplete");
    return 1;
  end
  --查找并呼叫本地注册用户
  ksr_route_location(); 

  return 1;
end

--接力转发/中继功能
function ksr_route_relay() 
  -- 对于转发的消息执行更多的事件触发路由，如串行Forking、RTP转发处理
  if KSR.is_method_in("IBSU") then 
    if KSR.tm.t_is_set("branch_route")<0 then
      -- 如果设置了branch_route, 则会出发相应的事件路由
      KSR.tm.t_on_branch("ksr_branch_manage");
    end
  end

  if KSR.is_method_in("ISU") then
    if KSR.tm.t_is_set("onreply_route") < 0 then
      KSR.tm.t_on_reply("ksr_onreply_manage");
    end
  end

  if KSR.is_INVITE() then
    if KSR.tm.t_is_set("failure_route") < 0 then
      KSR.tm.t_on_failure("ksr_failure_manage");
    end
  end

  --调用t_relay()进行转发，如果出错则返回储物信息
  --ksr_loadbalance();

  if KSR.tm.t_relay() < 0 then 
     KSR.sl.sl_send_error();
  end

  -- 到此就结束了，因此，如果在任意路由快里调用了该函数，对于本消息而言，脚本就终止，就不进行后续的操作了
  KSR.x.exit();
end

--对每一个SIP请求执行合法性检查
function ksr_route_reqinit() 
  KSR.dbg("=====SIP请求执行合法性检查 IP - " .. KSR.kx.get_method() .. " from - " .. KSR.kx.get_furi() .. " (IP: " .. KSR.kx.get_srcip() .. ":" .. KSR.kx.get_srcport() .. ") \n");
  if not KSR.is_myself_srcip() then

    -- 检查是否收到大量SIP消息的洪水攻击，将信任的IP地址放到白名单，以防误伤
    local srcip = KSR.kx.get_srcip();

    -- 这个sht 是一个共享内存的哈希表，用作IP地址黑名单
    if KSR.htable.sht_match_name("ipban","eq",srcip) > 0 then
      
      -- 如果能从表中找到，说明这个IP地址已经被防住了，直接退出
      KSR.dbg("requert from blocked IP - " .. KSR.kx.get_method() .. " from " .. KSR.kx.get_furi() .. " (IP: " .. srcip .. ":" .. KSR.kx.get_srcport() .. ") \n");
      
      KSR.x.exit();
    end

    -- 使用pike模块检测从同一个IP地址收到消息的频率（具体频率可设置），如果请求频次超过限制，则将这个IP地址加入黑名单
    if KSR.pike.pike_check_req() < 0 then

      KSR.err("ALERTL pike blocking " .. KSR.kx.get_method() .. " from " .. KSR.kx.get_furi() .. " (IP:" .. srcip .. ":" .. KSR.kx.get_srcport() .. ") \n");
      
      KSR.htable.sht_seti("ipban", srcip, 1);
      
      KSR.x.exit();
    end
  end

  -- 判断User-Agent 是否是已知的扫面工具，如果是，则直接返回200 OK, 不处理
  local ua= KSR.kx.gete_ua();
  if string.find(ua,"friendly") or string.find(ua, "scanner")
      or string.find(ua, "sipcli") or string.find(ua, "sipvicious") then

    KSR.sl.sl_send_reply(200, "OK");

    KSR.x.exit();
  end

  -- 检查Max-Forward字段是否低到10，防止SIP消息又绕回来产生死循环
  if KSR.maxfwd.process_maxfwd(10) < 0 then
    KSR.sl.sl_send_reply(483, "Too Many Hops");
    KSR.x.exit();
  end

  if KSR.is_OPTIONS() --处理OPTIONS请求
      and KSR.is_myself_ruri()
      and KSR.corex.has_ruri_user() < 0 then
    KSR.sl.sl_send_reply(200,"Keepalive");
    KSR.x.exit();
  end

  if KSR.sanity.sanity_check(17895, 7) < 0 then -- 检查SIP消息的合法性
    KSR.err("Malformed SIP message from " .. KSR.kx.get_srcip() .. ":" .. KSR.kx.get_srcport .. "\n");
    KSR.x.exit();
  end

end

-- 处理SIP对话内的请求
function ksr_route_withindlg()
  if KSR.siputils.has_totag() < 0 then return 1; end

  -- 同一对话内后续的请求应该从Record-Routing 头域中选择下一跳
  if KSR.rr.loose_route() > 0 then
    ksr_route_dlguri();
    if KSR.is_BYE() then
      KSR.setflag(FLT_ACC); --记账
      KSR.setflag(FLT_ACCFAILED); --即使呼叫失败也记账
    elseif KSR.is_ACK() then
        ksr_route_natmanage(); -- ACK 需要无状态转发
    elseif KSR.is_NOTIFY() then
      -- 为对话内的NOTIFY增加Record-Route头域，参见RFC 6665
      KSR.rr.record_route();
    end
    ksr_route_relay();
    KSR.x.exit();
  end

  if KSR.is_ACK() then
    if KSR.tm.t_check_trans() > 0 then
      -- 不是松散路由，却是有状态的ACK，该ACK因该是487后的ACK
      -- 或者是上有服务器返回404时的ACK
      ksr_route_relay();
      KSR.x.exit();
    else
      KSR.x.exit(); -- ACK没有对应的事务，忽略并丢弃
    end
  end

  -- 如果执行到这里，就不归我们管了，返回404错误
  KSR.sl.sl_send_reply(404, "Not here");
  KSR.x.exit();
end

 --处理SIP注册请求
function ksr_route_registrar()
  if not KSR.is_REGISTER() then return 1; end --仅处理注册消息，否则直接返回

  if KSR.isflagset(FLT_NATS) then --NAT相关处理，略
    KSR.setflag(FLB_NATB)
    KSR.setflag(FLB_NATSIPPING); -- 执行SIP NAT pinging, 略
  end

  -- 将注册消息中的Contact写到location表中（可以是内存也可以是数据库）并返回200 OK 如果保存失败则返回错误
  if KSR.registrar.save("ko_location", 0) < 0 then
    KSR.sl_send_error();
  end
  KSR.x.exit();
end

--查找并呼叫本地注册用户
function ksr_route_location()
  KSR.info("查找并呼叫本地注册用户\n");
  -- 比如两个用户a和b都注册到Kamailio中，则a呼叫b时会查询location表看b有没有注册
  local rc = KSR.registrar.lookup("ko_location");
  KSR.info("查询本地用户结果 rc:" .. rc  .."\n");
  if rc < 0 then -- 如果找不到则返回值进行出错处理
    KSR.tm.t_newtran();
    if rc==-3 then
      KSR.sl.sl_send_reply(404,"Not Found");
      KSR.x.exit()
    elseif rc==-2 then
      KSR.sl.sl_send_reply(405,"Method Not Allowed");
      KSR.x.exit()
    end
  end

  if KSR.is_INVITE() then -- 当使用usrloc路由时，也对未呼通的呼叫记账
    KSR.setflag(FLT_ACCMISSED);
  end

  if KSR.is_INVITE() and ksr_freeswitch() < 0 then
    ksr_loadbalance()
  end

  -- 如果找到被叫用户的注册地址，则向注册地址转发INVITE消息
  ksr_route_relay();
  KSR.x.exit();
end


function ksr_route_auth() --基于IP地址的认证
  if not KSR.auth then
    return 1;
  end

  if KSR.permissions and not KSR.is_REGISTER() then --检查IP是否在白名单
    KSR.info("检查IP是否在白名单 IP - " .. KSR.kx.get_srcip().. " 结果： ".. KSR.permissions.allow_source_address(1) ..") \n");
    if KSR.permissions.allow_source_address(1) > 0 then
      return 1;
    end
  end
  -- 处理注册请求，检查subscriber表中有没有对应的用户名和密码信息
  if KSR.is_REGISTER() or KSR.is_myself_furi() then
    --对请求进行鉴权检查
    if KSR.auth_db.auth_check(KSR.kx.gete_fhost(),"ko_subscriber",1) < 0 then
      -- 如果还没有认证则发起chanllenge认证，返回407
      KSR.auth.auth_challenge(KSR.kx.gete_fhost(),0);
      KSR.x.exit();
    end
  end

  if ksr_freeswitch() > 0 then
    KSR.info("route from freeswitch\n");
    return 1
  end

  -- 如果主叫不是我们本地已知的用户，则只允许呼叫本地注册用户，否则不允许转发，毕竟我们的服务器不是开放的转发服务器
  if (not KSR.is_myself_furi()) and (not KSR.is_myself_ruri()) then
    KSR.sl.sl_send_reply(403,"Not relaying");
    KSR.x.exit();
  end
  return 1;
end

-- 进行主叫NAT相关处理，如修改Contact信息等，略
function ksr_route_natdetect()
  if not KSR.nathelper then
    return 1;
  end
  if KSR.nathelper.nat_uac_test(19) > 0 then
    if KSR.is_REGISTER() then
      KSR.nathelper.fix_nated_register();
    elseif KSR.siputils.is_first_hop() > 0 then
      KSR.nathelper.set_contact_alias();
    end
    KSR.setflag(FLT_NATS);
  end
  return 1;
end

--RTPProxy控制，在NAT环境下将会修改SDP，通过RTPProxy进行媒体转发，略
function ksr_route_natmanage()
  KSR.info("RTPProxy进行媒体转发:" .. tostring(KSR.rtpproxy))
  if not KSR.rtpproxy then
    return 1;
  end
  if KSR.siputils.is_request()>0 then
    if KSR.siputils.has_totag()>0 then
      if KSR.rr.check_route_param("nat=yes")>0 then
        KSR.setbflag(FLB_NATB);
      end
    end
  end
  if (not (KSR.isflagset(FLT_NATS) or KSR.isbflagset(FLB_NATB))) then
    return 1;
  end

  KSR.rtpproxy.rtpproxy_manage("co");

  if KSR.siputils.is_request()>0 then
    if KSR.siputils.has_totag()<0 then
      if KSR.tmx.t_is_branch_route()>0 then
        KSR.rr.add_rr_param(";nat=yes");
      end
    end
  end
  if KSR.siputils.is_reply()>0 then
    if KSR.isbflagset(FLB_NATB) then
      KSR.nathelper.set_contact_alias();
    end
  end
  return 1;
end

--在对话相关请求中可能进行URL更新
function ksr_route_dlguri() 
  if not KSR.nathelper  then return 1; end

  if not KSR.isdsturiset() then
    KSR.nathelper.handle_ruri_alias();
  end
  return 1;
end

--路由到外部的SIP服务器（domain不属于我们）
function ksr_route_sipout()
  if KSR.is_myself_ruri() then return 1; end
  --增加一个SIP头域以便追踪消息
  KSR.hdr.append("P-Hont: outbound\r\n"); 

  --直接转发SIP消息
  ksr_route_relay(); 

  KSR.x.exit();
end

--外呼的分支，相当于原生脚本中的branch_route[...]
function ksr_branch_manage() 
  KSR.dbg("new branch [" .. KSR.pv.get("$T_branch_idx") .. "] to " .. KSR.kx.get_ruri() .. "\n");

  ksr_route_natmanage();

  return 1;
end


--收到回复消息时进行处理
function ksr_onreply_manage()
  KSR.dbg("incoming reply\n");
  local scode = KSR.kx.get_status();
  if scode > 100 and scode < 299 then
    ksr_route_natmanage();
  end
  return 1;
end

-- 管理故障路由案例
-- 相当于 failure_route[...]{}
function ksr_failure_manage()
  ksr_route_natmanage();

  if KSR.tm.t_is_canceled()>0 then return 1 end

  local status_code = KSR.tm.t_get_status_code();
  if KSR.tm.t_check_status("[4-5][0-9][0-9]") 
    or (KSR.tm.t_branch_timeout() and KSR.tm.t_branch_replied() < 0) then
      --获取下一个可用负载
      local ret = KSR.dispatcher.ds_next_dst();
      if ret > 0 then
        --找到下一个中继，以便打印日志
        dst = KSR.pv.gete('$xavp(_dsdst_[0]=>uri)')
        KSR.notice("负载失败，准备尝试下一个" .. dst)
        KSR.inof("---SCRIPT: going to <" .. KSR.pv.get("$ru") .. "> via <" .. KSR.pv.get("$du") .. "> \n")
        --发送
        KSR.tm.t_relay()
      else
        KSR.notice("负载失败，没有可用于尝试的负载")
      end
  end
  KSR.x.exit()
end

-- SIP 响应处理
-- 相当于reply_route{}
function ksr_reply_route()
  KSR.dbg("response - from kamailio lua script\n");
  if KSR.sanity.sanity_check(17604, 6) < 0 then
    KSR.err("malformed SIP response from " .. KSR.kx.get_srcip() .. ":" .. KSR.kx.get_srcport() .."\n");
    KSR.x.drop();
  end
  return 1;
end

function ksr_xhttp_event(evname)
  -- 打印日志，打印请求IP地址
  KSR.info("==== http request:" .. evname .. " " .. "Ri:" .. KSR.pv.get("$Ri") .. "\n")
  -- 为了安全，我们要求客户端在http头中传递一个token
  -- 为了简单，我们使用硬编码的1234
  if KSR.hdr.get("Authorization") ~= "Bearer 1234" then 
    KSR.hdr.append_to_reply('WWW-Authenticate: Bearer error="invalid_token"\r\n')
    KSR.xhttp.xhttp_reply("401","Unauthorized","",'{"code":401,"message":"invalid_token"}')
  end

  --获取Content-Type头域和请求Body
  local content_tpye = KSR.pv.get("$cT") or ""
  local req_body = KSR.pv.get("$rb")

  -- 我们使用JSON格式传递参数
  if not req_body or content_tpye ~= "application/json" then 
    KSR.xhttp.xhttp_reply(400,"Client Error", "text/plain","invalid content_type or body")
    return
  end

  -- 将参数打印出来
  KSR.notice("request body: " .. req_body .. "\n")
  --如果需要，我们也可以解析JSON参数，根据JSON内容决定返回什么样的值
  -- local jbody = cjson.decode(req_body)

  -- 为了简单，我们固定返回一个路由地址，实际使用时可以根据请求参数决定返回内容
  local body = '{"route":"这是一个路由信息"}'
  KSR.xhttp.xhttp_reply(200,"OK","application/json", body)
  return 1
end


-- evapi模块
function ksr_evapi_event(evname)
  --收到请求消息
  if evname == "evapi:message-receiced" then
    -- 获取消息内容 
    local msg = KSR.pv.gete("$evapi(msg)")
    if msg:find("stats") == 1 then
      local request_body = '{"jsonrpc":"2.0","method":"stats.fetch","params":["all"],"id":1}'
      -- 构造一个JSON-RPC请求以获取内部状态
      KSR.jsonrpcs.exec(request_body) 
      local response_body = KSR.pv.gete("$jsonrpl(body)")
      -- 返回JSON字符串
      KSR.evapi.relay(response_body)
    end
  end
end

function ksr_freeswitch()
  KSR.dbg("开始执行freeswitch校验" .. KSR.kx.get_srcip() .. ":" .. KSR.kx.get_srcport() .."\n")
  --if KSR.dispatcher.ds_is_from_list() then
  --   KSR.info("message sent from freeswitch_1\n");
  --   return 1;
  --end

  if KSR.dispatcher.ds_is_from_list(1) > 0  then
      KSR.info("message sent from freeswitch\n");
      return 1;
  end

  local port = KSR.kx.get_srcport();
  if port == 5080 then
     KSR.info("message sent port from freeswitch\n");
    return 1;
  end

  KSR.info("message sent from other" .. KSR.kx.get_srcip() .. ":" .. KSR.kx.get_srcport() .."\n");
  return -1;
end


function ksr_loadbalance()

  if not KSR.is_INVITE() then 
    return;
  end

  if KSR.is_INVITE() and ksr_freeswitch() > 0 then
      return 1;
  end

  if KSR.dispatcher.ds_select_dst(1,0) < 0  then
      KSR.info("No destination available!\n");
      KSR.sl.sl_send_reply(404, "No destination")
      KSR.x.exit();
  end
 KSR.info("转发消息到服务器 " .. KSR.kx.get_srcip() .. ":" .. KSR.kx.get_srcport() .."\n");
 ksr_route_relay()
end


