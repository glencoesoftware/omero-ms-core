package com.glencoesoftware.omero.ms.core;

import java.util.Map;

import brave.Tracing;
import brave.propagation.TraceContext.Injector;

public class OmeroVertxInjectorFactory {

    static Injector<Map<String, String>> getInjector() {
        return Tracing.current().propagation()
                .injector((carrier, key, value) -> {
            carrier.put(key, value);
        }
    );
    }

}
