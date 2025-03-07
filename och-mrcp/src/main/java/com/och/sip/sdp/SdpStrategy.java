package com.och.sip.sdp;

public interface SdpStrategy {
    SdpAnswer negotiate(String callId, SdpOffer offer);
}
