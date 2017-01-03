package org.seamcat.model.systems.generic;

import org.seamcat.model.generic.Defaults;
import org.seamcat.model.plugin.Config;
import org.seamcat.model.types.LocalEnvironment;

import java.util.List;

public interface LocalEnvironments {

    @Config(order = 1, name = "receiver")
    List<LocalEnvironment> receiverEnvironments();
    List<LocalEnvironment> receiverEnvironments = Defaults.defaultEnvironment();

    @Config(order = 2, name = "transmitter")
    List<LocalEnvironment> transmitterEnvironments();
    List<LocalEnvironment> transmitterEnvironments = Defaults.defaultEnvironment();
}
