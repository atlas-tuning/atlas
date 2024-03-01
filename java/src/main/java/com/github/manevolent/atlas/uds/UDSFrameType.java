package com.github.manevolent.atlas.uds;

import com.github.manevolent.atlas.uds.request.*;
import com.github.manevolent.atlas.uds.response.*;

public enum UDSFrameType {

    DIAGNOSTIC_SESSION_CONTROL(0x10, UDSDiagSessionControlRequest.class,
                                0x50, UDSDiagSessionControlResponse.class),

    ECU_RESET(0x11,UDSECUResetRequest .class,
             0x51,UDSECUResetResponse .class),

    SECURITY_ACCESS(0x27,UDSSecurityAccessRequest.class,
             0x67,UDSSecurityAccessResponse.class),

    COMMUNICATION_CONTROL(0x28, UDSCommunicationControlRequest.class,
                0x68, UDSCommunicationControlResponse.class),

    AUTHENTICATION(0x29, UDSAuthenticationRequest.class,
            0x69, UDSAuthenticationResponse.class),

    TESTER_PRESENT(0x3E, UDSTesterPresentRequest.class,
            0x7E, UDSTesterPresentResponse.class),

    ACCESS_TIMING_PARAMETERS(0x83, UDSAccessTimingParametersRequest.class,
            0xC3, UDSAccessTimingParametersResponse.class),

    READ_DATA_BY_ID(0x22, UDSReadDataByIDRequest.class,
            0x62, UDSReadDataByIDResponse.class),

    ROUTINE_CONTROL(0x31, UDSRoutineControlRequest.class,
            0x71, UDSRoutineControlResponse.class),

    CONTROL_DTC_SETTINGS(0x85, UDSControlDTCRequest.class,
            0xC5, UDSControlDTCResponse.class),

    DOWNLOAD(0x34, UDSDownloadRequest.class,
            0x74, UDSDownloadResponse.class),

    TRANSFER(0x36, UDSTransferRequest.class,
            0x76, UDSTransferResponse.class),

    NEGATIVE_RESPONSE(0x00, null, 0x7F, UDSNegativeResponse.class);

    ;

    private final byte request_sid, response_sid;
    private final Class<? extends UDSRequest> requestClass;
    private final Class<? extends UDSResponse> responseClass;

    UDSFrameType(int requestSid, Class<? extends UDSRequest> requestClass,
                 int responseSid,Class<? extends UDSResponse> responseClass) {
        this.request_sid = (byte) requestSid;
        this.response_sid = (byte) responseSid;
        this.requestClass = requestClass;
        this.responseClass = responseClass;
    }

    public static UDSFrameType resolveType(byte serviceId) {
        for (UDSFrameType frameType : values()) {
            if (frameType.matches(serviceId)) {
                return frameType;
            }
        }

        throw new UnsupportedOperationException(String.format("Unknown UDS SID: 0x%02X", serviceId));
    }

    public boolean matches(byte sid) {
        return getRequestSid() == sid || getResponseSid() == sid;
    }

    public Class<? extends UDSBody> resolveBodyClass(byte serviceId) {
        if (serviceId == getRequestSid()) {
            return getRequestClass();
        } else if (serviceId == getResponseSid()) {
            return getResponseClass();
        } else {
            throw new IllegalArgumentException("Can't resolve body class: " + serviceId);
        }
    }

    public byte getRequestSid() {
        return request_sid;
    }

    public byte getResponseSid() {
        return response_sid;
    }

    public Class<? extends UDSRequest> getRequestClass() {
        return requestClass;
    }

    public Class<? extends UDSResponse> getResponseClass() {
        return responseClass;
    }
}
