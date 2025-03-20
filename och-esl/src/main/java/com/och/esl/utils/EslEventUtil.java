package com.och.esl.utils;


import org.freeswitch.esl.client.transport.event.EslEvent;

public class EslEventUtil {

    public static final String CORE_UUID = "Core-UUID";
    public static final String UNIQUE_ID = "Unique-ID";
    public static final String OTHER_UNIQUE_ID = "Other-Leg-Unique-ID";

    private static final String CALL_DIRECTION = "Call-Direction";
    public static final String EVENT_NAME = "Event-Name";
    public static final String EVENT_SEQUENCE = "Event-Sequence";
    public static final String EVENT_DATE_LOCAL = "Event-Date-Local";
    public static final String EVENT_DATE_GMT = "Event-Date-GMT";
    public static final String EVENT_DATE_TIMESTAMP = "Event-Date-Timestamp";

    public static final String EVENT_CALLING_FILE = "Event-Calling-File";
    public static final String EVENT_CALLING_FUNCTION = "Event-Calling-Function";
    public static final String EVENT_CALLING_LINE_NUMBER = "Event-Calling-Line-Number";

    public static final String FREESWITCH_IPV4 = "FreeSWITCH-IPv4";
    public static final String FREESWITCH_IPV6 = "FreeSWITCH-IPv6";
    public static final String FREESWITCH_HOSTNAME = "FreeSWITCH-Hostname";
    public static final String FREESWITCH_SWITCHNAME = "FreeSWITCH-Switchname";

    public static final String CALLER_UNIQUE_ID = "Caller-Unique-ID";
    public static final String CALLER_NETWORK_ADDR = "Caller-Network-address";

    public static final String CALL_CHANNEL_UUID = "Channel-Call-UUID";

    public static final String CALLER_CONTEXT = "Caller-Context";
    public static final String CALLER_DIALPLAN = "Caller-Dialplan";
    public static final String CALLER_DIRECTION = "Caller-Direction";
    public static final String CALLER_LOGICAL_DIRECTION = "Caller-Logical-Direction";
    public static final String CALLER_PROFILE_INDEX = "Caller-Profile-Index";

    public static final String CALLER_ANI = "Caller-ANI";
    public static final String APPLICATION = "Application";
    public static final String APPLICATION_RESPONSE = "Application-Response";



    public static final String CALLER_USERNAME = "Caller-Username";
    public static final String CALLER_DESTINATION_NUMBER = "Caller-Destination-Number";
    public static final String CALLER_CALLER_ID_NAME = "Caller-Caller-ID-Name";
    public static final String CALLER_CALLER_ID_NUMBER = "Caller-Caller-ID-Number";

    public static final String CALLER_ORIG_CALLER_ID_NAME = "Caller-Orig-Caller-ID-Name";
    public static final String CALLER_ORIG_CALLER_ID_NUMBER = "Caller-Orig-Caller-ID-Number";

    public static final String CALLER_PROFILE_CREATED_TIME = "Caller-Profile-Created-Time";

    public static final String CALLER_CHANNEL_CREATED_TIME = "Caller-Channel-Created-Time";
    public static final String CALLER_CHANNEL_PROGRESS_TIME = "Caller-Channel-Progress-Time";
    public static final String CALLER_CHANNEL_PROGRESS_MEDIA_TIME = "Caller-Channel-Progress-Media-Time";
    public static final String CALLER_CHANNEL_ANSWERED_TIME = "Caller-Channel-Answered-Time";
    public static final String CALLER_CHANNEL_HANGUP_TIME = "Caller-Channel-Hangup-Time";
    public static final String HANGUP_CAUSE = "Hangup-Cause";
    public static final String CALLER_CHANNEL_BRIDGED_TIME = "Caller-Channel-Bridged-Time";

    public static final String CALLER_CHANNEL_NAME = "Caller-Channel-Name";
    public static final String CALLER_CHANNEL_HOLD_ACCUM = "Caller-Channel-Hold-Accum";
    public static final String CALLER_CHANNEL_LAST_HOLD = "Caller-Channel-Last-Hold";
    public static final String CALLER_CHANNEL_TRANSFER_TIME = "Caller-Channel-Transfer-Time";
    public static final String CALLER_CHANNEL_RESURRECT_TIME = "Caller-Channel-Resurrect-Time";

    public static final String VARIABLE_SIP_TO_URI = "variable_sip_to_uri";

    public static final String VARIABLE_SIP_TO_HOST = "variable_sip_to_host";

    private static final String VARIABLE_SIP_VIA_PORT = "variable_sip_via_port";
    private static final String VARIABLE_SIP_CONTACT_PORT = "variable_sip_contact_port";

    public static final String VARIABLE_SIP_TO_REAL_USER = "variable_sip_h_X-To-Real-User";

    public static final String VARIABLE_SIP_VOICE_GATEWAY = "variable_sip_h_X-Voice-Gateway";

    public static final String VARIABLE_SIP_USER_AGENT = "variable_sip_user_agent";
    public static final String VARIABLE_SIP_HANGUP_PHRASE = "variable_sip_hangup_phrase";
    public static final String VARIABLE_SIP_VIA_PROTOCOL = "variable_sip_via_protocol";
    public static final String VARIABLE_SIP_TERM_STATUS = "variable_sip_term_status";
    public static final String VARIABLE_CHANNEL_NAME = "variable_channel_name";
    public static final String VARIABLE_SIP_CONTACT_URI = "variable_sip_contact_uri";
    public static final String VARIABLE_SIP_REQ_PORT = "variable_sip_req_port";

    public static final String VARIABLE_CURRENT_APPLICATION_DATA = "variable_current_application_data";

    public static final String VARIABLE_MENU_DTMF_RETURN_INVALID = "variable_MENU_DTMF_RETURN_invalid";

    private EslEventUtil() {
    }

    public static String getCoreUuid(EslEvent event) {
        return event.getEventHeaders().get(CORE_UUID);
    }

    public static String getUniqueId(EslEvent event) {
        return event.getEventHeaders().get(UNIQUE_ID);
    }

    public static String getOtherUniqueId(EslEvent event) {
        return event.getEventHeaders().get(OTHER_UNIQUE_ID);
    }

    public static String getCallerUniqueId(EslEvent event) {
        return event.getEventHeaders().get(CALLER_UNIQUE_ID);
    }

    public static String getCallChannelUuid(EslEvent event) {
        return event.getEventHeaders().get(CALL_CHANNEL_UUID);
    }

    public static String getEventName(EslEvent event) {
        return event.getEventHeaders().get(EVENT_NAME);
    }

    public static String getEventSequence(EslEvent event) {
        return event.getEventHeaders().get(EVENT_SEQUENCE);
    }

    public static String getEventDateLocal(EslEvent event) {
        return event.getEventHeaders().get(EVENT_DATE_LOCAL);
    }

    public static String getEventDateGmt(EslEvent event) {
        return event.getEventHeaders().get(EVENT_DATE_GMT);
    }

    public static String getEventCallingFile(EslEvent event) {
        return event.getEventHeaders().get(EVENT_CALLING_FILE);
    }

    public static String getEventCallingFunction(EslEvent event) {
        return event.getEventHeaders().get(EVENT_CALLING_FUNCTION);
    }

    public static String getEventCallingLineNumber(EslEvent event) {
        return event.getEventHeaders().get(EVENT_CALLING_LINE_NUMBER);
    }

    public static String getFreeswitchIpv4(EslEvent event) {
        return event.getEventHeaders().get(FREESWITCH_IPV4);
    }

    public static String getFreeswitchIpv6(EslEvent event) {
        return event.getEventHeaders().get(FREESWITCH_IPV6);
    }

    public static String getFreeswitchHostname(EslEvent event) {
        return event.getEventHeaders().get(FREESWITCH_HOSTNAME);
    }

    public static String getFreeswitchSwitchname(EslEvent event) {
        return event.getEventHeaders().get(FREESWITCH_SWITCHNAME);
    }

    public static String getEventDateTimestamp(EslEvent event) {
        return event.getEventHeaders().get(EVENT_DATE_TIMESTAMP);
    }

    public static String getCallerProfileCreatedTime(EslEvent event) {
        return event.getEventHeaders().get(CALLER_PROFILE_CREATED_TIME);
    }

    public static String getCallerChannelCreatedTime(EslEvent event) {
        return event.getEventHeaders().get(CALLER_CHANNEL_CREATED_TIME);
    }

    public static String getCallerChannelProgressTime(EslEvent event) {
        return event.getEventHeaders().get(CALLER_CHANNEL_PROGRESS_TIME);
    }

    public static String getCallerChannelProgressMediaTime(EslEvent event) {
        return event.getEventHeaders().get(CALLER_CHANNEL_PROGRESS_MEDIA_TIME);
    }

    public static String getCallerChannelAnsweredTime(EslEvent event) {
        return event.getEventHeaders().get(CALLER_CHANNEL_ANSWERED_TIME);
    }

    public static String getCallerChannelHangupTime(EslEvent event) {
        return event.getEventHeaders().get(CALLER_CHANNEL_HANGUP_TIME);
    }

    public static String getHangupCause(EslEvent event) {
        return event.getEventHeaders().get(HANGUP_CAUSE);
    }

    public static String getCallerChannelBridgedTime(EslEvent event) {
        return event.getEventHeaders().get(CALLER_CHANNEL_BRIDGED_TIME);
    }

    public static String getCallerNetworkAddr(EslEvent event) {
        return event.getEventHeaders().get(CALLER_NETWORK_ADDR);
    }

    public static String getCallerContext(EslEvent event) {
        return event.getEventHeaders().get(CALLER_CONTEXT);
    }

    public static String getCallerDialplan(EslEvent event) {
        return event.getEventHeaders().get(CALLER_DIALPLAN);
    }

    public static String getCallerDirection(EslEvent event) {
        return event.getEventHeaders().get(CALLER_DIRECTION);
    }

    public static String getCallerLogicalDirection(EslEvent event) {
        return event.getEventHeaders().get(CALLER_LOGICAL_DIRECTION);
    }

    public static String getCallerProfileIndex(EslEvent event) {
        return event.getEventHeaders().get(CALLER_PROFILE_INDEX);
    }

    public static String getCallerChannelName(EslEvent event) {
        return event.getEventHeaders().get(CALLER_CHANNEL_NAME);
    }

    public static String getCallerChannelHoldAccum(EslEvent event) {
        return event.getEventHeaders().get(CALLER_CHANNEL_HOLD_ACCUM);
    }

    public static String getCallerChannelLastHold(EslEvent event) {
        return event.getEventHeaders().get(CALLER_CHANNEL_LAST_HOLD);
    }

    public static String getCallerChannelTransferTime(EslEvent event) {
        return event.getEventHeaders().get(CALLER_CHANNEL_TRANSFER_TIME);
    }

    public static String getCallerChannelResurrectTime(EslEvent event) {
        return event.getEventHeaders().get(CALLER_CHANNEL_RESURRECT_TIME);
    }

    public static String getCallerAni(EslEvent event) {
        return event.getEventHeaders().get(CALLER_ANI);
    }

    public static String getCallerUsername(EslEvent event) {
        return event.getEventHeaders().get(CALLER_USERNAME);
    }

    public static String getCallerDestinationNumber(EslEvent event) {
        return event.getEventHeaders().get(CALLER_DESTINATION_NUMBER);
    }

    public static String getCallerCallerIdName(EslEvent event) {
        return event.getEventHeaders().get(CALLER_CALLER_ID_NAME);
    }

    public static String getCallerCallerIdNumber(EslEvent event) {
        return event.getEventHeaders().get(CALLER_CALLER_ID_NUMBER);
    }

    public static String getCallerOrigCallerIdName(EslEvent event) {
        return event.getEventHeaders().get(CALLER_ORIG_CALLER_ID_NAME);
    }

    public static String getCallerOrigCallerIdNumber(EslEvent event) {
        return event.getEventHeaders().get(CALLER_ORIG_CALLER_ID_NUMBER);
    }

    public static String getSipToUri(EslEvent event) {
        return event.getEventHeaders().get(VARIABLE_SIP_TO_URI);
    }

    public static String getVoiceGateway(EslEvent event) {
        return event.getEventHeaders().get(VARIABLE_SIP_VOICE_GATEWAY);
    }

    public static String getToHost(EslEvent event) {
        return event.getEventHeaders().get(VARIABLE_SIP_TO_HOST);
    }

    public static String getSipViaPort(EslEvent event) {
        return event.getEventHeaders().get(VARIABLE_SIP_VIA_PORT);
    }

    public static String getSipContactPort(EslEvent event) {
        return event.getEventHeaders().get(VARIABLE_SIP_CONTACT_PORT);
    }

    public static String getToRealUser(EslEvent event) {
        return event.getEventHeaders().get(VARIABLE_SIP_TO_REAL_USER);
    }
    public static String getVariableSipUserAgent(EslEvent event) {
        return event.getEventHeaders().get(VARIABLE_SIP_USER_AGENT);
    }

    public static String getSipHangupPhrase(EslEvent event) {
        return event.getEventHeaders().get(VARIABLE_SIP_HANGUP_PHRASE);
    }

    public static String getSipViaProtocol(EslEvent event) {
        return event.getEventHeaders().get(VARIABLE_SIP_VIA_PROTOCOL);
    }

    public static String getSipTermStatus(EslEvent event) {
        return event.getEventHeaders().get(VARIABLE_SIP_TERM_STATUS);
    }

    public static String getChannelName(EslEvent event) {
        return event.getEventHeaders().get(VARIABLE_CHANNEL_NAME);
    }


    public static String getCallDirection(EslEvent event) {
        return event.getEventHeaders().get(CALLER_DIRECTION);
    }

    public static String getSipContactUri(EslEvent event) {
        return event.getEventHeaders().get(VARIABLE_SIP_CONTACT_URI);
    }

    public static String getSipReqPort(EslEvent event) {
        return event.getEventHeaders().get(VARIABLE_SIP_REQ_PORT);
    }

    public static String getApplication(EslEvent event) {
        return event.getEventHeaders().get(APPLICATION);
    }
    public static String getApplicationResponse(EslEvent event) {
        return event.getEventHeaders().get(APPLICATION_RESPONSE);
    }

    public static String getApplicationData(EslEvent event) {
        return event.getEventHeaders().get(VARIABLE_CURRENT_APPLICATION_DATA);
    }

    public static String getMenuDtmfReturnInvalid(EslEvent event) {
        return event.getEventHeaders().get(VARIABLE_MENU_DTMF_RETURN_INVALID);
    }
}
