package com.github.manevolent.atlas.protocol.uds;

import java.util.*;

public class UDSQuery {
    private final String name;
    private final List<UDSMapping<?>> mappings = new ArrayList<>();
    private final Map<Integer, UDSMapping<?>> sidMappings = new HashMap<>();
    private final Map<UDSSide<?>, UDSMapping<?>> sideMappings = new HashMap<>();

    public UDSQuery(String name,
                    int requestSid,
                    Class<? extends UDSRequest<?>> requestClass,
                    int responseSid,
                    Class<? extends UDSResponse> responseClass) {
        this(name,
                new UDSMapping<>(UDSSide.REQUEST, requestSid, requestClass),
                new UDSMapping<>(UDSSide.RESPONSE, responseSid, responseClass));
    }

    public UDSQuery(String name, UDSMapping<?>... mappings) {
        this(name, Arrays.asList(mappings));
    }

    public UDSQuery(String name, List<UDSMapping<?>> mappings) {
        this.name = name;

        for (UDSMapping<?> mapping : mappings) {
            this.mappings.add(mapping);
            this.sidMappings.put(mapping.getSid(), mapping);
            this.sideMappings.put(mapping.getSide(), mapping);
        }
    }

    public String getName() {
        return name;
    }

    public UDSMapping<?> getMapping(int sid) {
        return sidMappings.get(sid);
    }

    public Collection<UDSMapping<?>> getMappings() {
        return Collections.unmodifiableCollection(mappings);
    }

    @SuppressWarnings("unchecked")
    public <T extends UDSBody> UDSMapping<T> getMapping(UDSSide<T> side) {
        return (UDSMapping<T>) sideMappings.get(side);
    }

    @SuppressWarnings("unchecked")
    public Integer getSid(UDSSide<?> side) {
        UDSMapping<?> mapping = getMapping(side);
        if (mapping == null) {
            return null;
        } else {
            return mapping.getSid();
        }
    }

    public Set<UDSSide<?>> getSides() {
        return Collections.unmodifiableSet(sideMappings.keySet());
    }

    public boolean hasSide(UDSSide<?> side) {
        return sideMappings.containsKey(side);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return mappings.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof UDSQuery && equals((UDSQuery) obj);
    }

    public boolean equals(UDSQuery obj) {
        return this == obj;
    }

    /**
     * Constructs a new UDS query pairing between a request and response, presuming that
     * the response SID would have the 2nd most significant bit set to 1 (0x40).
     */
    public static UDSQuery from(String name,
                                int requestSid,
                                Class<? extends UDSRequest<?>> requestClass) {
        return from(name, requestSid, requestSid | 0x40, requestClass);
    }

    public static UDSQuery from(String name,
                                int requestSid, int responseSid,
                                Class<? extends UDSRequest<?>> requestClass) {
        return new UDSQuery(name,
                requestSid, requestClass,
                responseSid, UDSRequest.getResponseClass(requestClass));
    }

    public static <T extends UDSBody> UDSQuery from(String name, UDSSide<T> side, int sid, Class<? extends T> clazz) {
        return new UDSQuery(name, new UDSMapping<>(side, sid, clazz));
    }
}
