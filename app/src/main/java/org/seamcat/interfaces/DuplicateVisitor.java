package org.seamcat.interfaces;

import org.seamcat.cdma.CDMALinkLevelData;
import org.seamcat.function.BlockingMaskImpl;
import org.seamcat.function.EmissionMaskImpl;
import org.seamcat.model.Library;
import org.seamcat.model.systems.SystemModel;
import org.seamcat.model.systems.generic.ReceiverModel;
import org.seamcat.model.systems.generic.T_ReceiverModel;
import org.seamcat.model.systems.generic.TransmitterModel;
import org.seamcat.model.types.LibraryItem;
import org.seamcat.plugin.PluginConfiguration;

public class DuplicateVisitor implements LibraryVisitor<Boolean> {

    private final Library existing;

    public DuplicateVisitor( Library existing ) {
        this.existing = existing;
    }

    @Override
    public Boolean visit(ReceiverModel receiver) {
        String name = receiver.description().name();
        for (ReceiverModel rec : existing.getReceivers()) {
            if ( name.equals( rec.description().name() )) return true;
        }
        return false;
    }
    
    @Override
    public Boolean visit(T_ReceiverModel t_receiver) {
        String name = t_receiver.description().name();
        for (T_ReceiverModel rec : existing.getTReceivers()) {
            if ( name.equals( rec.description().name() )) return true;
        }
        return false;
    }

    @Override
    public Boolean visit(TransmitterModel transmitter) {
        String name = transmitter.description().name();
        for (LibraryItem rec : existing.getTransmitters()) {
            if ( name.equals( rec.description().name() )) return true;
        }
        return false;
    }

    @Override
    public Boolean visit(CDMALinkLevelData lld) {
        return existing.getCDMALinkLevelData().contains( lld );
    }

    @Override
    public Boolean visit(EmissionMaskImpl mask) {
        return existing.getSpectrumEmissionMasks().contains( mask );
    }

    @Override
    public Boolean visit(BlockingMaskImpl mask) {
        return existing.getReceiverBlockingMasks().contains( mask );
    }

    @Override
    public Boolean visit(PluginConfiguration plugin ) {
        return existing.getPluginConfigurations(plugin.getClass()).contains( plugin );
    }

    @Override
    public Boolean visit(SystemModel system) {
        String name = system.description().name();
        for (SystemModel model : existing.getSystems()) {
            if ( name.equals( model.description().name() )) return true;
        }
        return false;
    }


}
