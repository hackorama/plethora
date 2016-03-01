package com.hackorama.plethora.channel;

import com.hackorama.plethora.common.data.Metrics;

public interface MetricsReader {
    Metrics getMetricsFromFile(String file, String sysPropertyPrefix);

    Metrics getMetricsFromFile(String file);
}
