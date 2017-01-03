package org.seamcat.interfaces;

import org.seamcat.cdma.CDMALinkLevelData;
import org.seamcat.function.BlockingMaskImpl;
import org.seamcat.function.EmissionMaskImpl;
import org.seamcat.model.systems.SystemModel;
import org.seamcat.model.systems.generic.ReceiverModel;
import org.seamcat.model.systems.generic.T_ReceiverModel;
import org.seamcat.model.systems.generic.TransmitterModel;
import org.seamcat.plugin.PluginConfiguration;

public interface LibraryVisitor<T> {
    T visit( ReceiverModel receiver );
    T visit( T_ReceiverModel t_receiver );
    T visit( TransmitterModel transmitter );
    T visit( CDMALinkLevelData lld );
    T visit( EmissionMaskImpl mask);
    T visit( BlockingMaskImpl mask );
    T visit( PluginConfiguration plugin );
    T visit( SystemModel system );
}
