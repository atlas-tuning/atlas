package com.github.manevolent.atlas.uds;

import com.github.manevolent.atlas.uds.request.*;
import com.github.manevolent.atlas.uds.response.UDSNegativeResponse;

public interface UDSProtocol {

    BasicUDSProtocol STANDARD = new BasicUDSProtocol(
        UDSQuery.from("Diagnostic Session Control", 0x10, UDSDiagSessionControlRequest.class),
            UDSQuery.from("ECU Reset", 0x11, UDSECUResetRequest.class),
            UDSQuery.from("Security Access", 0x27, UDSSecurityAccessRequest.class),
            UDSQuery.from("Communication Control", 0x28,UDSCommunicationControlRequest.class),
            UDSQuery.from("Authentication", 0x29, UDSAuthenticationRequest.class),
            UDSQuery.from("Tester Present", 0x3E, UDSTesterPresentRequest.class),
            UDSQuery.from("Access Timing Parameters", 0x83, UDSAccessTimingParametersRequest.class),
            UDSQuery.from("Read Data by ID", 0x22, UDSReadDataByIDRequest.class),
            UDSQuery.from("Routine Control", 0x31, UDSRoutineControlRequest.class),
            UDSQuery.from("Control DTC Settings", 0x85, UDSCommunicationControlRequest.class),
            UDSQuery.from("Reset DTC Information", 0x14, UDSClearDTCInformationRequest.class),
            UDSQuery.from("Read DTC Information", 0x19, UDSReadDTCInformationRequest.class),
            UDSQuery.from("Download", 0x34, UDSDownloadRequest.class),
            UDSQuery.from("Transfer", 0x36, UDSTransferRequest.class),
            UDSQuery.from("Negative", UDSSide.RESPONSE, 0x7F, UDSNegativeResponse.class)
    );

    UDSQuery getBySid(int sid) throws IllegalArgumentException;

    default Class<? extends UDSBody> getClassBySid(int sid) throws IllegalArgumentException, IllegalStateException {
        UDSQuery query = getBySid(sid);
        UDSMapping<?> mapping = query.getMapping(sid);
        if (mapping == null) {
            throw new IllegalArgumentException("unknown SID " + sid);
        }

        return mapping.getBodyClass();
    }

    default LayeredUDSProtocol layer(UDSProtocol lower) {
        return new LayeredUDSProtocol(this, lower);
    }

    int getSid(Class<? extends UDSBody> clazz);

}
