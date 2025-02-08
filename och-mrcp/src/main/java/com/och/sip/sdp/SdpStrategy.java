package com.och.sip.sdp;

public interface SdpStrategy {
    SdpAnswer negotiate(SdpOffer offer);
}
