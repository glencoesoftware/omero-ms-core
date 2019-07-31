package com.glencoesoftware.omero.ms.core;

import java.util.Map;

import brave.Tracing;
import brave.propagation.TraceContext.Injector;

public class OmeroVertxInjectorFactory {

    public static Injector<Map<String, String>> getInjector() {
        Tracing tracing = Tracing.current();
        if (tracing == null) {
            return null;
        }
        return tracing.propagation()
                .injector((carrier, key, value) -> {
            carrier.put(key, value);
        }
    );
    }

}
