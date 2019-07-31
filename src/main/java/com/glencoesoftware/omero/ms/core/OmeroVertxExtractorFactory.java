package com.glencoesoftware.omero.ms.core;

import java.util.Map;

import brave.Tracing;
import brave.propagation.TraceContext.Extractor;

public class OmeroVertxExtractorFactory {

    public static Extractor<Map<String, String>> getExtractor(){
        Tracing tracing = Tracing.current();
        if (tracing == null) {
            return null;
        }
        return tracing.propagation()
                .extractor((carrier, key) -> {
                    return carrier.get(key);
                });
    }

}
