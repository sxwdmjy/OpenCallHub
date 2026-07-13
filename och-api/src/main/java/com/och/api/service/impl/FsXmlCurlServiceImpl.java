package com.och.api.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.och.api.service.IFsXmlCurlService;
import com.och.common.domain.FsXmlCurl;
import com.och.security.utils.XmlCurlUtil;
import com.och.system.domain.entity.FsSipGateway;
import com.och.system.domain.query.dialplan.FsDialplanQuery;
import com.och.system.domain.query.fssip.FsSipGatewayQuery;
import com.och.system.domain.query.subsriber.KoSubscriberQuery;
import com.och.system.domain.entity.KoSubscriber;
import com.och.system.domain.vo.dialplan.FsDialplanVo;
import com.och.system.domain.vo.sip.KoSubscriberVo;
import com.och.system.service.IFsDialplanService;
import com.och.system.service.IFsSipGatewayService;
import com.och.system.service.IKoSubscriberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * FreeSWITCH XML Curl 服务实现类
 * <p>处理 FreeSWITCH 的 XML Curl 请求，动态生成拨号计划、目录、网关等配置。</p>
 *
 * @author danmo
 * @date 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FsXmlCurlServiceImpl implements IFsXmlCurlService {

    private final IKoSubscriberService koSubscriberService;
    private final IFsDialplanService fsDialplanService;
    private final IFsSipGatewayService fsSipGatewayService;

    /**
     * 处理 XML Curl 请求
     * <p>根据请求参数动态生成 FreeSWITCH XML 配置（拨号计划、目录、网关等）。</p>
     *
     * @param request HttpServletRequest 请求对象，包含 FreeSWITCH 传递的参数
     * @return FreeSWITCH XML 配置字符串
     */
    @Override
    public String xmlHandle(HttpServletRequest request) {
        FsXmlCurl xmlCurl = XmlCurlUtil.decodeThenParse(request);
        String section = xmlCurl.getSection();
        log.info("[FsXmlCurlService] 收到 XML Curl 请求: section={}, keyValue={}", section, xmlCurl.getKeyValue());

        if (section == null || section.isEmpty()) {
            return notFound();
        }

        String body;
        try {
            body = switch (section.toLowerCase()) {
                case "directory" -> buildDirectory(xmlCurl);
                case "dialplan" -> buildDialplan(xmlCurl);
                case "configuration" -> buildConfiguration(xmlCurl);
                default -> {
                    log.warn("[FsXmlCurlService] 未支持的 section: {}", section);
                    yield notFound();
                }
            };
        } catch (Exception e) {
            log.error("[FsXmlCurlService] 生成 XML 配置异常: section={}", section, e);
            body = "<section name=\"error\"><result status=\"error\" message=\"" + escapeXml(e.getMessage()) + "\" /></section>";
        }

        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<document type=\"freeswitch/xml\">\n"
                + body
                + "</document>";
    }

    // ======================== directory ========================

    /**
     * 构建 directory（用户目录）XML
     * <p>FreeSWITCH 通过 directory section 获取 SIP 用户信息，用于注册认证和呼叫路由。</p>
     */
    private String buildDirectory(FsXmlCurl xmlCurl) {
        StringBuilder sb = new StringBuilder();
        sb.append("  <section name=\"directory\">\n");

        // domain 定义
        sb.append("    <domain name=\"$${domain}\">\n");

        // domain 级别参数
        sb.append("      <params>\n");
        sb.append("        <param name=\"dial-string\" value=\"{^^:sip_invite_domain=${dialed_domain}:presence_id=${dialed_user}@${dialed_domain}}${sofia_contact(*/${dialed_user}@${dialed_domain})},${verto_contact(${dialed_user}@${dialed_domain})}\"/>\n");
        sb.append("        <param name=\"jsonrpc-allowed-methods\" value=\"verto\"/>\n");
        sb.append("      </params>\n");

        // domain 级别变量
        sb.append("      <variables>\n");
        sb.append("        <variable name=\"record_stereo\" value=\"true\"/>\n");
        sb.append("        <variable name=\"default_gateway\" value=\"$${default_provider}\"/>\n");
        sb.append("        <variable name=\"default_areacode\" value=\"$${default_areacode}\"/>\n");
        sb.append("        <variable name=\"transfer_fallback_extension\" value=\"operator\"/>\n");
        sb.append("      </variables>\n");

        // 查询用户
        String reqUser = xmlCurl.getMetadata() != null ? xmlCurl.getMetadata().get("user") : null;
        sb.append("      <groups>\n");
        sb.append("        <group name=\"default\">\n");
        sb.append("          <users>\n");

        if (reqUser != null && !reqUser.isEmpty()) {
            // 查询单个用户
            KoSubscriber subscriber = koSubscriberService.getByUserName(reqUser);
            if (subscriber != null) {
                sb.append(buildUserXmlFromEntity(subscriber));
            }
        } else {
            // 查询所有活跃用户
            KoSubscriberQuery query = new KoSubscriberQuery();
            query.setStatus(0);
            List<KoSubscriberVo> subscribers = koSubscriberService.getList(query);
            if (subscribers != null) {
                for (KoSubscriberVo sub : subscribers) {
                    sb.append(buildUserXml(sub));
                }
            }
        }

        sb.append("          </users>\n");
        sb.append("        </group>\n");
        sb.append("      </groups>\n");
        sb.append("    </domain>\n");
        sb.append("  </section>\n");
        return sb.toString();
    }

    private String buildUserXml(KoSubscriberVo subscriber) {
        return buildUserEntry(subscriber.getUserName(), subscriber.getPassword(), subscriber.getDomain());
    }

    private String buildUserXmlFromEntity(KoSubscriber subscriber) {
        return buildUserEntry(subscriber.getUsername(), subscriber.getPassword(), subscriber.getDomain());
    }

    private String buildUserEntry(String username, String password, String domain) {
        StringBuilder sb = new StringBuilder();
        sb.append("            <user id=\"").append(escapeXml(username)).append("\">\n");
        sb.append("              <params>\n");
        sb.append("                <param name=\"password\" value=\"").append(escapeXml(password)).append("\"/>\n");
        sb.append("              </params>\n");
        sb.append("              <variables>\n");
        sb.append("                <variable name=\"user_context\" value=\"default\"/>\n");
        sb.append("                <variable name=\"effective_caller_id_number\" value=\"").append(escapeXml(username)).append("\"/>\n");
        if (domain != null && !domain.isEmpty()) {
            sb.append("                <variable name=\"effective_caller_id_name\" value=\"").append(escapeXml(domain)).append("\"/>\n");
        }
        sb.append("              </variables>\n");
        sb.append("            </user>\n");
        return sb.toString();
    }

    // ======================== dialplan ========================

    /**
     * 构建 dialplan（拨号计划）XML
     * <p>FreeSWITCH 通过 dialplan section 获取呼叫路由规则。</p>
     */
    private String buildDialplan(FsXmlCurl xmlCurl) {
        StringBuilder sb = new StringBuilder();
        sb.append("  <section name=\"dialplan\">\n");

        // 构建 public context
        sb.append(buildContextXml("public"));

        // 构建 default context
        sb.append(buildContextXml("default"));

        sb.append("  </section>\n");
        return sb.toString();
    }

    private String buildContextXml(String contextName) {
        StringBuilder sb = new StringBuilder();
        sb.append("    <context name=\"").append(contextName).append("\">\n");

        FsDialplanQuery query = new FsDialplanQuery();
        query.setContextName(contextName);
        List<FsDialplanVo> dialplanList = fsDialplanService.getList(query);
        if (dialplanList != null) {
            for (FsDialplanVo fsDialplan : dialplanList) {
                sb.append("      <extension name=\"").append(escapeXml(fsDialplan.getName())).append("\"");
                if (fsDialplan.getContent() != null && !fsDialplan.getContent().isEmpty()) {
                    sb.append(">\n");
                    sb.append(buildExtensionBody(fsDialplan));
                    sb.append("      </extension>\n");
                } else {
                    sb.append("/>\n");
                }
            }
        }

        sb.append("    </context>\n");
        return sb.toString();
    }

    /**
     * 根据 dialplan 内容构建 extension body XML
     * <p>支持 xml 和 json 两种格式的内容。</p>
     */
    private String buildExtensionBody(FsDialplanVo dialplan) {
        if ("xml".equals(dialplan.getType())) {
            // XML 格式内容直接嵌入
            return "        " + dialplan.getContent().trim() + "\n";
        } else if ("json".equals(dialplan.getType())) {
            // JSON 格式内容转换为 XML
            return buildExtensionFromJson(dialplan.getContent());
        }
        // 默认当作空内容
        return "";
    }

    /**
     * 将 JSON 格式的 dialplan 内容转换为 FreeSWITCH XML 格式
     */
    private String buildExtensionFromJson(String jsonContent) {
        StringBuilder sb = new StringBuilder();
        try {
            JSONObject extJson = JSONObject.parseObject(jsonContent);
            JSONArray conditions = extJson.getJSONArray("condition");
            if (conditions != null) {
                for (int i = 0; i < conditions.size(); i++) {
                    JSONObject cond = conditions.getJSONObject(i);
                    String field = cond.getString("field");
                    String expression = cond.getString("expression");
                    sb.append("        <condition field=\"").append(escapeXml(field)).append("\"");
                    sb.append(" expression=\"").append(escapeXml(expression)).append("\">\n");
                    JSONArray actions = cond.getJSONArray("action");
                    if (actions != null) {
                        for (int j = 0; j < actions.size(); j++) {
                            JSONObject action = actions.getJSONObject(j);
                            String app = action.getString("application");
                            String data = action.getString("data");
                            if (data == null) {
                                data = "";
                            }
                            // 反条件动作（anti-action）
                            Boolean isAnti = action.getBoolean("anti");
                            if (isAnti != null && isAnti) {
                                sb.append("          <anti-action application=\"").append(escapeXml(app)).append("\"");
                                sb.append(" data=\"").append(escapeXml(data)).append("\"/>\n");
                            } else {
                                sb.append("          <action application=\"").append(escapeXml(app)).append("\"");
                                sb.append(" data=\"").append(escapeXml(data)).append("\"/>\n");
                            }
                        }
                    }
                    sb.append("        </condition>\n");
                }
            }
        } catch (Exception e) {
            log.error("[FsXmlCurlService] JSON 格式 dialplan 内容解析失败: {}", jsonContent, e);
        }
        return sb.toString();
    }

    // ======================== configuration ========================

    /**
     * 构建 configuration XML
     * <p>根据 key_value 确定配置类型（sofia、modules 等）。</p>
     */
    private String buildConfiguration(FsXmlCurl xmlCurl) {
        String keyValue = xmlCurl.getKeyValue();
        if (keyValue == null) {
            return notFound();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("  <section name=\"configuration\">\n");
        sb.append("    <configuration name=\"").append(escapeXml(keyValue)).append("\"");

        if ("sofia.conf".equals(keyValue) || "sofia".equalsIgnoreCase(keyValue)) {
            sb.append(" description=\"Sofia SIP Endpoint\">\n");
            sb.append(buildSofiaProfiles(xmlCurl));
        } else {
            sb.append(">\n");
            sb.append("    </configuration>\n");
        }

        sb.append("  </section>\n");
        return sb.toString();
    }

    /**
     * 构建 Sofia SIP Profile 配置 XML
     */
    private String buildSofiaProfiles(FsXmlCurl xmlCurl) {
        StringBuilder sb = new StringBuilder();
        // 全局设置
        sb.append("      <global_settings>\n");
        sb.append("        <param name=\"log-level\" value=\"0\"/>\n");
        sb.append("        <param name=\"debug-presence\" value=\"0\"/>\n");
        sb.append("      </global_settings>\n");

        // 综合设置
        sb.append("      <profiles>\n");

        // internal profile
        sb.append("        <profile name=\"internal\">\n");
        sb.append("          <domains>\n");
        sb.append("            <domain name=\"all\" alias=\"true\" parse=\"false\"/>\n");
        sb.append("          </domains>\n");
        sb.append("          <settings>\n");
        sb.append("            <param name=\"debug\" value=\"0\"/>\n");
        sb.append("            <param name=\"sip-trace\" value=\"no\"/>\n");
        sb.append("            <param name=\"context\" value=\"public\"/>\n");
        sb.append("            <param name=\"rfc2833-pt\" value=\"101\"/>\n");
        sb.append("            <param name=\"sip-port\" value=\"$${internal_sip_port}\"/>\n");
        sb.append("            <param name=\"dialplan\" value=\"XML\"/>\n");
        sb.append("            <param name=\"dtmf-duration\" value=\"2000\"/>\n");
        sb.append("            <param name=\"inbound-codec-prefs\" value=\"$${global_codec_prefs}\"/>\n");
        sb.append("            <param name=\"outbound-codec-prefs\" value=\"$${global_codec_prefs}\"/>\n");
        sb.append("            <param name=\"rtp-timer-name\" value=\"soft\"/>\n");
        sb.append("            <param name=\"rtp-ip\" value=\"$${local_ip_v4}\"/>\n");
        sb.append("            <param name=\"sip-ip\" value=\"$${local_ip_v4}\"/>\n");
        sb.append("            <param name=\"hold-music\" value=\"$${hold_music}\"/>\n");
        sb.append("            <param name=\"apply-nat-acl\" value=\"nat.auto\"/>\n");
        sb.append("            <param name=\"apply-inbound-acl\" value=\"domains\"/>\n");
        sb.append("            <param name=\"local-network-acl\" value=\"localnet.auto\"/>\n");
        sb.append("            <param name=\"record-path\" value=\"$${recordings_dir}\"/>\n");
        sb.append("            <param name=\"record-template\" value=\"${caller_id_number}.${target_domain}.${strftime(%Y_%m_%d_%H_%M_%S)}.wav\"/>\n");
        sb.append("            <param name=\"manage-presence\" value=\"true\"/>\n");
        sb.append("            <param name=\"presence-hosts\" value=\"$${domain},$${local_ip_v4}\"/>\n");
        sb.append("            <param name=\"presence-privacy\" value=\"$${presence_privacy}\"/>\n");
        sb.append("            <param name=\"inbound-codec-negotiation\" value=\"generous\"/>\n");
        sb.append("            <param name=\"inbound-late-negotiation\" value=\"true\"/>\n");
        sb.append("            <param name=\"inbound-zrtp-passthru\" value=\"true\"/>\n");
        sb.append("            <param name=\"nonce-ttl\" value=\"60\"/>\n");
        sb.append("            <param name=\"auth-calls\" value=\"$${internal_auth_calls}\"/>\n");
        sb.append("            <param name=\"inbound-reg-force-matching-username\" value=\"true\"/>\n");
        sb.append("            <param name=\"auth-all-packets\" value=\"false\"/>\n");
        sb.append("            <param name=\"rtp-timeout-sec\" value=\"300\"/>\n");
        sb.append("            <param name=\"rtp-hold-timeout-sec\" value=\"1800\"/>\n");
        sb.append("            <param name=\"force-register-domain\" value=\"$${domain}\"/>\n");
        sb.append("            <param name=\"force-subscription-domain\" value=\"$${domain}\"/>\n");
        sb.append("            <param name=\"force-register-db-domain\" value=\"$${domain}\"/>\n");
        sb.append("            <param name=\"ws-binding\" value=\":5066\"/>\n");
        sb.append("            <param name=\"wss-binding\" value=\":7443\"/>\n");
        sb.append("            <param name=\"challenge-realm\" value=\"auto_from\"/>\n");
        sb.append("          </settings>\n");
        sb.append("        </profile>\n");

        // external profile with gateways
        sb.append("        <profile name=\"external\">\n");
        sb.append("          <domains>\n");
        sb.append("            <domain name=\"all\" alias=\"false\" parse=\"true\"/>\n");
        sb.append("          </domains>\n");
        sb.append("          <settings>\n");
        sb.append("            <param name=\"debug\" value=\"0\"/>\n");
        sb.append("            <param name=\"sip-trace\" value=\"no\"/>\n");
        sb.append("            <param name=\"context\" value=\"public\"/>\n");
        sb.append("            <param name=\"rfc2833-pt\" value=\"101\"/>\n");
        sb.append("            <param name=\"sip-port\" value=\"$${external_sip_port}\"/>\n");
        sb.append("            <param name=\"dialplan\" value=\"XML\"/>\n");
        sb.append("            <param name=\"dtmf-duration\" value=\"2000\"/>\n");
        sb.append("            <param name=\"inbound-codec-prefs\" value=\"$${global_codec_prefs}\"/>\n");
        sb.append("            <param name=\"outbound-codec-prefs\" value=\"$${outbound_codec_prefs}\"/>\n");
        sb.append("            <param name=\"hold-music\" value=\"$${hold_music}\"/>\n");
        sb.append("            <param name=\"rtp-timer-name\" value=\"soft\"/>\n");
        sb.append("            <param name=\"local-network-acl\" value=\"localnet.auto\"/>\n");
        sb.append("            <param name=\"manage-presence\" value=\"false\"/>\n");
        sb.append("            <param name=\"inbound-codec-negotiation\" value=\"generous\"/>\n");
        sb.append("            <param name=\"nonce-ttl\" value=\"60\"/>\n");
        sb.append("            <param name=\"auth-calls\" value=\"false\"/>\n");
        sb.append("            <param name=\"inbound-late-negotiation\" value=\"true\"/>\n");
        sb.append("            <param name=\"inbound-zrtp-passthru\" value=\"true\"/>\n");
        sb.append("            <param name=\"rtp-ip\" value=\"$${local_ip_v4}\"/>\n");
        sb.append("            <param name=\"sip-ip\" value=\"$${local_ip_v4}\"/>\n");
        sb.append("            <param name=\"rtp-timeout-sec\" value=\"300\"/>\n");
        sb.append("            <param name=\"rtp-hold-timeout-sec\" value=\"1800\"/>\n");
        sb.append("            <param name=\"ext-rtp-ip\" value=\"$${external_rtp_ip}\"/>\n");
        sb.append("            <param name=\"ext-sip-ip\" value=\"$${external_sip_ip}\"/>\n");
        sb.append("          </settings>\n");
        sb.append("          <gateways>\n");

        // 查询 external 类型网关
        FsSipGatewayQuery gatewayQuery = new FsSipGatewayQuery();
        gatewayQuery.setType(2);
        List<FsSipGateway> gateways = fsSipGatewayService.getList(gatewayQuery);
        if (gateways != null) {
            for (FsSipGateway gw : gateways) {
                sb.append(buildGatewayXml(gw));
            }
        }

        sb.append("          </gateways>\n");
        sb.append("        </profile>\n");
        sb.append("      </profiles>\n");

        return sb.toString();
    }

    private String buildGatewayXml(FsSipGateway gw) {
        StringBuilder sb = new StringBuilder();
        sb.append("            <gateway name=\"").append(escapeXml(gw.getName())).append("\">\n");
        if (gw.getUserName() != null && !gw.getUserName().isEmpty()) {
            sb.append("              <param name=\"username\" value=\"").append(escapeXml(gw.getUserName())).append("\"/>\n");
        }
        if (gw.getPassword() != null && !gw.getPassword().isEmpty()) {
            sb.append("              <param name=\"password\" value=\"").append(escapeXml(gw.getPassword())).append("\"/>\n");
        }
        if (gw.getRealm() != null && !gw.getRealm().isEmpty()) {
            sb.append("              <param name=\"realm\" value=\"").append(escapeXml(gw.getRealm())).append("\"/>\n");
        }
        if (gw.getProxy() != null && !gw.getProxy().isEmpty()) {
            sb.append("              <param name=\"proxy\" value=\"").append(escapeXml(gw.getProxy())).append("\"/>\n");
        }
        if (gw.getFromDomain() != null && !gw.getFromDomain().isEmpty()) {
            sb.append("              <param name=\"from-domain\" value=\"").append(escapeXml(gw.getFromDomain())).append("\"/>\n");
        }
        sb.append("              <param name=\"register\" value=\"").append(gw.getRegister() != null && gw.getRegister() == 1 ? "true" : "false").append("\"/>\n");
        if (gw.getRetryTime() != null) {
            sb.append("              <param name=\"retry-seconds\" value=\"").append(gw.getRetryTime()).append("\"/>\n");
        }
        if (gw.getPingTime() != null) {
            sb.append("              <param name=\"ping\" value=\"").append(gw.getPingTime()).append("\"/>\n");
        }
        if (gw.getExpireTime() != null) {
            sb.append("              <param name=\"expire-seconds\" value=\"").append(gw.getExpireTime()).append("\"/>\n");
        }
        if (gw.getTransport() != null) {
            String transport = gw.getTransport() == 1 ? "udp" : "tcp";
            sb.append("              <param name=\"register-transport\" value=\"").append(transport).append("\"/>\n");
        }
        sb.append("            </gateway>\n");
        return sb.toString();
    }

    // ======================== utilities ========================

    private String notFound() {
        return "  <section name=\"result\">\n"
                + "    <result status=\"not found\" />\n"
                + "  </section>\n";
    }

    /**
     * XML 特殊字符转义
     */
    private String escapeXml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
