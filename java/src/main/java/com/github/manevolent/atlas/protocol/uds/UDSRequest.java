package com.github.manevolent.atlas.protocol.uds;

import java.lang.reflect.ParameterizedType;

public abstract class UDSRequest<T extends UDSResponse> extends UDSBody {

    public boolean isResponseExpected() {
        return true;
    }

    @SuppressWarnings("unchecked")
    public static Class<? extends UDSResponse> getResponseClass(Class<? extends UDSRequest<?>> clazz) {
        return (Class<? extends UDSResponse>) ((ParameterizedType) clazz
                .getGenericSuperclass()).getActualTypeArguments()[0];
    }
}
