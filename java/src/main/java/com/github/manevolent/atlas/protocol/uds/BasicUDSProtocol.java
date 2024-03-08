package com.github.manevolent.atlas.protocol.uds;

import java.util.*;

public class BasicUDSProtocol implements UDSProtocol {
    private final Set<UDSQuery> queries = new LinkedHashSet<>();
    private final Map<Integer, UDSQuery> sidMap = new HashMap<>();
    private final Map<Class<? extends UDSBody>, Integer> classMap = new HashMap<>();

    public BasicUDSProtocol() {
        // nothing to do
    }

    public BasicUDSProtocol(UDSQuery... queries) {
        this(Arrays.asList(queries));
    }

    public BasicUDSProtocol(List<UDSQuery> queries) {
        this();

        for (UDSQuery query : queries) {
            registerQuery(query);
        }
    }

    public Set<UDSQuery> getQueries() {
        return Collections.unmodifiableSet(queries);
    }

    protected void registerQuery(UDSQuery query) {
        if (queries.contains(query)) {
            throw new IllegalStateException("query already added");
        }

        queries.add(query);

        for (UDSMapping<?> mapping : query.getMappings()) {
            sidMap.put(mapping.getSid(), query);
            classMap.put(mapping.getBodyClass(), mapping.getSid());
        }
    }

    @Override
    public UDSQuery getBySid(int sid) {
       UDSQuery query = sidMap.get(sid);
       if (query == null) {
           throw new IllegalArgumentException("UDS SID " + sid + " not recognized");
       }

       return query;
    }

    @Override
    public int getSid(Class<? extends UDSBody> clazz) {
        Integer sid = classMap.get(clazz);
        if (sid == null) {
            throw new IllegalArgumentException("UDS class " + clazz + " not recognized");
        }

        return sid;
    }

}
