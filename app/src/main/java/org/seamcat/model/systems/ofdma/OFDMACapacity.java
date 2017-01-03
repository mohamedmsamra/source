package org.seamcat.model.systems.ofdma;

import org.seamcat.model.plugin.Config;

public interface OFDMACapacity {

    @Config(order = 1, name = "OFDMA capacity")
    int usersPerBS();
    int usersPerBS = 20;
}
