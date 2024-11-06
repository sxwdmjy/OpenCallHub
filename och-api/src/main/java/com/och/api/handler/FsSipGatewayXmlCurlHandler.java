package com.och.api.handler;

import cn.hutool.core.collection.CollectionUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.och.api.factory.FsXmlCurlEventStrategy;
import com.och.common.annotation.XmlCurlEventName;
import com.och.common.constant.SectionNames;
import com.och.common.enums.SipGatewayParamEnum;
import com.och.common.enums.SipGatewaySettingParamEnum;
import com.och.common.utils.StringUtils;
import com.och.common.xmlcurl.sofia.GlobalSettings;
import com.och.common.xmlcurl.sofia.Param;
import com.och.common.xmlcurl.sofia.Sofia;
import com.och.common.xmlcurl.sofia.domain.Domain;
import com.och.common.xmlcurl.sofia.domain.Domains;
import com.och.common.xmlcurl.sofia.gateway.Gateway;
import com.och.common.xmlcurl.sofia.gateway.Gateways;
import com.och.common.xmlcurl.sofia.profile.Profile;
import com.och.common.xmlcurl.sofia.profile.Profiles;
import com.och.common.xmlcurl.sofia.setting.Settings;
import com.och.system.domain.entity.FsSipGateway;
import com.och.common.domain.FsXmlCurl;
import com.och.system.domain.query.fssip.FsSipGatewayQuery;
import com.och.system.service.IFsSipGatewayService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * SIP网关处理类
 *
 * @author danmo
 * @date 2023/09/13 22:02
 **/
@XmlCurlEventName(value = SectionNames.SOFIA)
@Slf4j
@Component
public class FsSipGatewayXmlCurlHandler implements FsXmlCurlEventStrategy {

    @Resource
    private IFsSipGatewayService iFsSipGatewayService;

    @Override
    public String eventHandle(FsXmlCurl fsXmlCurl) {
        String xml = null;
        try {
            xml = getConfiguration(fsXmlCurl.getKeyValue());
        } catch (JsonProcessingException e) {
            log.error("SipGateway 配置获取异常 msg:{}",e.getMessage(),e);
        }
        log.info("FsConfigurationSofiaXmlCurlHandler: {}", xml);
        return xml;
    }

    private String getConfiguration(String keyValue) throws JsonProcessingException {
        Sofia configuration = new Sofia();
        configuration.setName(keyValue);
        configuration.setDescription("sofia endpoint");
        configuration.setProfiles(getProfiles());
        configuration.setGlobalSettings(new GlobalSettings().setParam(getGlobalSettingsParamList()));
        return configuration.toXmlString();
    }

    private List<Param> getGlobalSettingsParamList() {
        List<Param> paramList = new ArrayList<>();
        paramList.add(new Param("log_level", "0"));
        paramList.add(new Param("debug_presence", "0"));
        return paramList;
    }


    private Profiles getProfiles() {
        return new Profiles().setProfile(getProfileList());
    }

    private List<Profile> getProfileList() {
        List<Profile> profiles = new ArrayList<>();
        profiles.add(new Profile()
                .setName("internal")
                .setDomains(new Domains().setDomain(getInternalDomains()))
                .setSettings(new Settings().setParam(getInternalSettings())));
        profiles.add(new Profile()
                .setName("external")
                .setDomains(new Domains().setDomain(getExternalDomains()))
                .setSettings(new Settings().setParam(getExternalSettings()))
                .setGateways(new Gateways().setGateway(getGatewayList(2))));
        return profiles;
    }

    private List<Domain> getInternalDomains() {
        List<Domain> domainList = new ArrayList<>();
        domainList.add(new Domain("all", "true", "false"));
        return domainList;
    }

    private List<Param> getInternalSettings() {
        List<Param> paramList = new ArrayList<>();
        paramList.add(new Param(SipGatewaySettingParamEnum.DEBUG.key, "0"));
        paramList.add(new Param(SipGatewaySettingParamEnum.SIP_TRACE.key, "no"));
        paramList.add(new Param(SipGatewaySettingParamEnum.SIP_CAPTURE.key, "no"));
        paramList.add(new Param(SipGatewaySettingParamEnum.WATCHDOG_ENABLED.key, "no"));
        paramList.add(new Param(SipGatewaySettingParamEnum.WATCHDOG_STEP_TIMEOUT.key, "30000"));
        paramList.add(new Param(SipGatewaySettingParamEnum.WATCHDOG_EVENT_TIMEOUT.key, "30000"));
        paramList.add(new Param(SipGatewaySettingParamEnum.LOG_AUTH_FAILURES.key, "false"));
        paramList.add(new Param(SipGatewaySettingParamEnum.FORWARD_UNSOLICITED_MWI_NOTIFY.key, "false"));
        paramList.add(new Param(SipGatewaySettingParamEnum.CONTEXT.key, "public"));
        paramList.add(new Param(SipGatewaySettingParamEnum.RFC2833_PT.key, "101"));
        paramList.add(new Param(SipGatewaySettingParamEnum.SIP_PORT.key, "$${internal_sip_port}"));
        paramList.add(new Param(SipGatewaySettingParamEnum.DIALPLAN.key, "XML"));
        paramList.add(new Param(SipGatewaySettingParamEnum.DTMF_DURATION.key, "2000"));
        paramList.add(new Param(SipGatewaySettingParamEnum.INBOUND_CODEC_PREFS.key, "$${global_codec_prefs}"));
        paramList.add(new Param(SipGatewaySettingParamEnum.OUTBOUND_CODEC_PREFS.key, "$${global_codec_prefs}"));
        paramList.add(new Param(SipGatewaySettingParamEnum.RTP_TIMER_NAME.key, "soft"));
        paramList.add(new Param(SipGatewaySettingParamEnum.RTP_IP.key, "$${local_ip_v4}"));
        paramList.add(new Param(SipGatewaySettingParamEnum.SIP_IP.key, "$${local_ip_v4}"));
        paramList.add(new Param(SipGatewaySettingParamEnum.HOLD_MUSIC.key, "$${hold_music}"));
        paramList.add(new Param(SipGatewaySettingParamEnum.APPLY_NAT_ACL.key, "nat.auto"));
        paramList.add(new Param(SipGatewaySettingParamEnum.APPLY_INBOUND_ACL.key, "domains"));
        paramList.add(new Param(SipGatewaySettingParamEnum.LOCAL_NETWORK_ACL.key, "localnet.auto"));
        paramList.add(new Param(SipGatewaySettingParamEnum.RECORD_PATH.key, "$${recordings_dir}"));
        paramList.add(new Param(SipGatewaySettingParamEnum.RECORD_TEMPLATE.key, "${caller_id_number}.${target_domain}.${strftime(%Y_%m_%d_%H_%M_%S)}.wav"));
        paramList.add(new Param(SipGatewaySettingParamEnum.MANAGE_PRESENCE.key, "true"));
        paramList.add(new Param(SipGatewaySettingParamEnum.PRESENCE_HOSTS.key, "$${domain},$${local_ip_v4}"));
        paramList.add(new Param(SipGatewaySettingParamEnum.PRESENCE_PRIVACY.key, "$${presence_privacy}"));
        paramList.add(new Param(SipGatewaySettingParamEnum.INBOUND_CODEC_NEGOTIATION.key, "generous"));
        paramList.add(new Param(SipGatewaySettingParamEnum.TLS.key, "$${internal_ssl_enable}"));
        paramList.add(new Param(SipGatewaySettingParamEnum.TLS_ONLY.key, "false"));
        paramList.add(new Param(SipGatewaySettingParamEnum.TLS_BIND_PARAMS.key, "transport=tls"));
        paramList.add(new Param(SipGatewaySettingParamEnum.TLS_SIP_PORT.key, "$${internal_tls_port}"));
        paramList.add(new Param(SipGatewaySettingParamEnum.TLS_PASSPHRASE.key, ""));
        paramList.add(new Param(SipGatewaySettingParamEnum.TLS_VERIFY_DATE.key, "true"));
        paramList.add(new Param(SipGatewaySettingParamEnum.TLS_VERIFY_POLICY.key, "none"));
        paramList.add(new Param(SipGatewaySettingParamEnum.TLS_VERIFY_DEPTH.key, "2"));
        paramList.add(new Param(SipGatewaySettingParamEnum.TLS_VERIFY_IN_SUBJECTS.key, ""));
        paramList.add(new Param(SipGatewaySettingParamEnum.TLS_VERSION.key, "$${sip_tls_version}"));
        paramList.add(new Param(SipGatewaySettingParamEnum.TLS_CIPHERS.key, "$${sip_tls_ciphers}"));
        paramList.add(new Param(SipGatewaySettingParamEnum.INBOUND_LATE_NEGOTIATION.key, "true"));
        paramList.add(new Param(SipGatewaySettingParamEnum.INBOUND_ZRTP_PASSTHRU.key, "true"));
        paramList.add(new Param(SipGatewaySettingParamEnum.NONCE_TTL.key, "60"));
        paramList.add(new Param(SipGatewaySettingParamEnum.AUTH_CALLS.key, "$${internal_auth_calls}"));
        paramList.add(new Param(SipGatewaySettingParamEnum.INBOUND_REG_FORCE_MATCHING_USERNAME.key, "true"));
        paramList.add(new Param(SipGatewaySettingParamEnum.AUTH_ALL_PACKETS.key, "false"));
        paramList.add(new Param(SipGatewaySettingParamEnum.RTP_TIMEOUT_SEC.key, "300"));
        paramList.add(new Param(SipGatewaySettingParamEnum.RTP_HOLD_TIMEOUT_SEC.key, "1800"));
        paramList.add(new Param(SipGatewaySettingParamEnum.FORCE_REGISTER_DOMAIN.key, "$${domain}"));
        paramList.add(new Param(SipGatewaySettingParamEnum.FORCE_SUBSCRIPTION_DOMAIN.key, "$${domain}"));
        paramList.add(new Param(SipGatewaySettingParamEnum.FORCE_REGISTER_DB_DOMAIN.key, "$${domain}"));
        paramList.add(new Param(SipGatewaySettingParamEnum.WS_BINDING.key, ":5066"));
        paramList.add(new Param(SipGatewaySettingParamEnum.WSS_BINDING.key, ":7443"));
        paramList.add(new Param(SipGatewaySettingParamEnum.CHALLENGE_REALM.key, "auto_from"));
        paramList.add(new Param(SipGatewaySettingParamEnum.APPLY_CANDIDATE_ACL.key, "wan"));
        paramList.add(new Param(SipGatewaySettingParamEnum.APPLY_CANDIDATE_ACL.key, "localnet.auto"));
        paramList.add(new Param(SipGatewaySettingParamEnum.APPLY_CANDIDATE_ACL.key, "rfc1918.auto"));
        return paramList;
    }

    private List<Domain> getExternalDomains() {
        List<Domain> domainList = new ArrayList<>();
        domainList.add(new Domain("all", "false", "true"));
        return domainList;
    }


    private List<Param> getExternalSettings() {
        List<Param> paramList = new ArrayList<>();
        paramList.add(new Param(SipGatewaySettingParamEnum.DEBUG.key, "0"));
        paramList.add(new Param(SipGatewaySettingParamEnum.SIP_TRACE.key, "no"));
        paramList.add(new Param(SipGatewaySettingParamEnum.SIP_CAPTURE.key, "no"));
        paramList.add(new Param(SipGatewaySettingParamEnum.RFC2833_PT.key, "101"));
        paramList.add(new Param(SipGatewaySettingParamEnum.SIP_PORT.key, "$${external_sip_port}"));
        paramList.add(new Param(SipGatewaySettingParamEnum.DIALPLAN.key, "XML"));
        paramList.add(new Param(SipGatewaySettingParamEnum.CONTEXT.key, "public"));
        paramList.add(new Param(SipGatewaySettingParamEnum.DTMF_DURATION.key, "2000"));
        paramList.add(new Param(SipGatewaySettingParamEnum.INBOUND_CODEC_PREFS.key, "$${global_codec_prefs}"));
        paramList.add(new Param(SipGatewaySettingParamEnum.OUTBOUND_CODEC_PREFS.key, "$${outbound_codec_prefs}"));
        paramList.add(new Param(SipGatewaySettingParamEnum.HOLD_MUSIC.key, "$${hold_music}"));
        paramList.add(new Param(SipGatewaySettingParamEnum.RTP_TIMER_NAME.key, "soft"));
        paramList.add(new Param(SipGatewaySettingParamEnum.LOCAL_NETWORK_ACL.key, "localnet.auto"));
        paramList.add(new Param(SipGatewaySettingParamEnum.MANAGE_PRESENCE.key, "false"));
        paramList.add(new Param(SipGatewaySettingParamEnum.INBOUND_CODEC_NEGOTIATION.key, "generous"));
        paramList.add(new Param(SipGatewaySettingParamEnum.NONCE_TTL.key, "60"));
        paramList.add(new Param(SipGatewaySettingParamEnum.AUTH_CALLS.key, "false"));
        paramList.add(new Param(SipGatewaySettingParamEnum.INBOUND_LATE_NEGOTIATION.key, "true"));
        paramList.add(new Param(SipGatewaySettingParamEnum.INBOUND_ZRTP_PASSTHRU.key, "true"));
        paramList.add(new Param(SipGatewaySettingParamEnum.RTP_IP.key, "$${local_ip_v4}"));
        paramList.add(new Param(SipGatewaySettingParamEnum.SIP_IP.key, "$${local_ip_v4}"));
        paramList.add(new Param(SipGatewaySettingParamEnum.RTP_TIMEOUT_SEC.key, "300"));
        paramList.add(new Param(SipGatewaySettingParamEnum.RTP_HOLD_TIMEOUT_SEC.key, "1800"));
        paramList.add(new Param(SipGatewaySettingParamEnum.TLS.key, "$${external_ssl_enable}"));
        paramList.add(new Param(SipGatewaySettingParamEnum.TLS_ONLY.key, "false"));
        paramList.add(new Param(SipGatewaySettingParamEnum.TLS_BIND_PARAMS.key, "transport=tls"));
        paramList.add(new Param(SipGatewaySettingParamEnum.TLS_SIP_PORT.key, "$${external_tls_port}"));
        paramList.add(new Param(SipGatewaySettingParamEnum.TLS_PASSPHRASE.key, ""));
        paramList.add(new Param(SipGatewaySettingParamEnum.TLS_VERIFY_DATE.key, "true"));
        paramList.add(new Param(SipGatewaySettingParamEnum.TLS_VERIFY_POLICY.key, "none"));
        paramList.add(new Param(SipGatewaySettingParamEnum.TLS_VERIFY_DEPTH.key, "2"));
        paramList.add(new Param(SipGatewaySettingParamEnum.TLS_VERIFY_IN_SUBJECTS.key, ""));
        paramList.add(new Param(SipGatewaySettingParamEnum.TLS_VERSION.key, "$${sip_tls_version}"));
        paramList.add(new Param(SipGatewaySettingParamEnum.APPLY_CANDIDATE_ACL.key, "localnet.auto"));
        paramList.add(new Param(SipGatewaySettingParamEnum.APPLY_CANDIDATE_ACL.key, "rfc1918.auto"));
        paramList.add(new Param(SipGatewaySettingParamEnum.EXT_RTP_IP.key, "$${external_rtp_ip}"));
        paramList.add(new Param(SipGatewaySettingParamEnum.EXT_SIP_IP.key, "$${external_sip_ip}"));
        return paramList;
    }

    private List<Gateway> getGatewayList(Integer type) {
        FsSipGatewayQuery query = new FsSipGatewayQuery();
        query.setType(type);
        List<FsSipGateway> gatewayServiceList = iFsSipGatewayService.getList(query);
        if (CollectionUtil.isNotEmpty(gatewayServiceList)) {
            return gatewayServiceList.stream().map(gatewayVo -> {
                Gateway gateway = new Gateway();
                gateway.setName(gatewayVo.getName());
                gateway.setParam(getParamList(gatewayVo));
                return gateway;
            }).collect(Collectors.toList());
        }
        return null;
    }

    private List<Param> getParamList(FsSipGateway gatewayVo) {
        List<Param> paramList = new ArrayList<>();
        paramList.add(new Param(SipGatewayParamEnum.REALM.key, gatewayVo.getRealm()));
        paramList.add(new Param(SipGatewayParamEnum.FROM_DOMAIN.key, gatewayVo.getFromDomain()));
        paramList.add(new Param(SipGatewayParamEnum.REGISTER.key, gatewayVo.getRegister() == 0 ? "false" : "true"));
        paramList.add(new Param(SipGatewayParamEnum.PROXY.key, gatewayVo.getProxy()));
        if (Objects.nonNull(gatewayVo.getRetryTime())) {
            paramList.add(new Param(SipGatewayParamEnum.RETRY_SECONDS.key, String.valueOf(gatewayVo.getRetryTime())));
        }

        if (Objects.nonNull(gatewayVo.getPingTime())) {
            paramList.add(new Param(SipGatewayParamEnum.PING.key, String.valueOf(gatewayVo.getPingTime())));
        }
        if (Objects.nonNull(gatewayVo.getExpireTime())) {
            paramList.add(new Param(SipGatewayParamEnum.EXPIRE_SECONDS.key, String.valueOf(gatewayVo.getExpireTime())));
        }

        if (StringUtils.isNotBlank(gatewayVo.getUserName())) {
            paramList.add(new Param(SipGatewayParamEnum.USERNAME.key, gatewayVo.getUserName()));
        }

        if (StringUtils.isNotBlank(gatewayVo.getPassword())) {
            paramList.add(new Param(SipGatewayParamEnum.PASSWORD.key, gatewayVo.getPassword()));
        }

        if (Objects.nonNull(gatewayVo.getTransport())) {
            String transport = gatewayVo.getTransport() == 1 ? "udp" : "tcp";
            paramList.add(new Param(SipGatewayParamEnum.REGISTER_TRANSPORT.key, transport));
        }
        return paramList;
    }

    public static void main(String[] args) {
        String sda = null + "";
        System.out.println(sda);
    }

}
