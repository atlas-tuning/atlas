package com.github.manevolent.atlas.subaru;

import com.github.manevolent.atlas.ssm4.AES;
import com.github.manevolent.atlas.uds.UDSComponent;
import com.github.manevolent.atlas.uds.UDSSession;
import com.github.manevolent.atlas.uds.UDSTransaction;
import com.github.manevolent.atlas.uds.command.UDSSecurityAccessCommand;
import com.github.manevolent.atlas.uds.request.UDSSecurityAccessRequest;
import com.github.manevolent.atlas.uds.response.UDSSecurityAccessResponse;

import java.io.IOException;

import static com.github.manevolent.atlas.subaru.SubaruDITComponent.ENGINE_2;

public class SubaruSecurityAccessCommand extends UDSSecurityAccessCommand {
    private final byte[] aesKey;

    public SubaruSecurityAccessCommand(int seed, UDSComponent component, byte[] aesKey) {
        super(seed, component);

        this.aesKey = aesKey;
    }

    @Override
    protected UDSSecurityAccessRequest answer(UDSSecurityAccessResponse challenge) {
        assert challenge.getSeed() == getSeed();
        return new UDSSecurityAccessRequest(getSeed() + 1, AES.answer(aesKey, challenge.getData()));
    }

    @Override
    protected void handle(UDSSecurityAccessResponse result) throws IOException {
       if (result.getData().length != 0) {
           throw new IOException("Unexpected security access response: " + result.toString());
       }
    }
}
