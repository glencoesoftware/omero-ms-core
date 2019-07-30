package com.glencoesoftware.omero.ms.core;

import java.util.Map;

import brave.Tracing;
import brave.propagation.TraceContext.Extractor;

public class OmeroVertxExtractorFactory {

    static Extractor<Map<String, String>> getExtractor(){
        return Tracing.current().propagation()
                .extractor((carrier, key) -> {
                    return carrier.get(key);
                });
    }

}
