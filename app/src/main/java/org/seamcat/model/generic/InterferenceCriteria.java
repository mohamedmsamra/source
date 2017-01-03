package org.seamcat.model.generic;

import org.seamcat.model.plugin.Config;
import org.seamcat.model.systems.CalculatedValue;

public interface InterferenceCriteria {
    @Config(order = 1, name = "C / I", unit = "dB")
    double protection_ratio();
    double protection_ratio = 19.0;

    @Config(order = 2, name = "C / (N + I)", unit = "dB")
    double extended_protection_ratio();
    double extended_protection_ratio = 16.0;

    @Config(order = 3, name = "(N + I) / N", unit = "dB")
    double noise_augmentation();
    double noise_augmentation = 3.02;

    @Config(order = 4, name = "I / N", unit ="dB")
    double interference_to_noise_ratio();
    double interference_to_noise_ratio = 0.02;

    @Config(order = 5, name = "Calculate Interference Criteria")
    CalculatedValue calcIC();
}
