package com.github.manevolent.atlas.uds;

import java.lang.reflect.ParameterizedType;

public abstract class UDSRequest<T extends UDSResponse> extends UDSBody {

    @SuppressWarnings("unchecked")
    public static Class<? extends UDSResponse> getResponseClass(Class<? extends UDSRequest<?>> clazz) {
        return (Class<? extends UDSResponse>) ((ParameterizedType) clazz
                .getGenericSuperclass()).getActualTypeArguments()[0];
    }
}
