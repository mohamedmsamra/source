package org.seamcat.interfaces;

import org.seamcat.cdma.CDMALinkLevelData;
import org.seamcat.function.BlockingMaskImpl;
import org.seamcat.function.EmissionMaskImpl;
import org.seamcat.model.factory.Model;
import org.seamcat.model.systems.SystemModel;
import org.seamcat.model.systems.generic.ReceiverModel;
import org.seamcat.model.systems.generic.T_ReceiverModel;
import org.seamcat.model.systems.generic.TransmitterModel;
import org.seamcat.plugin.PluginConfiguration;

public class ImportLibraryVisitor implements LibraryVisitor<Void> {

    @Override
    public Void visit(ReceiverModel receiver) {
        Model.getInstance().getLibrary().addReceiver( receiver );
        return null;
    }

    @Override
    public Void visit(TransmitterModel transmitter) {
        Model.getInstance().getLibrary().addTransmitter( transmitter );
        return null;
    }

    @Override
    public Void visit(CDMALinkLevelData lld) {
        Model.getInstance().getLibrary().addCDMALinkLevelData( lld );
        return null;
    }

    @Override
    public Void visit(EmissionMaskImpl mask) {
        Model.getInstance().getLibrary().addLibraryFunction( mask );
        return null;
    }

    @Override
    public Void visit(BlockingMaskImpl mask) {
        Model.getInstance().getLibrary().addLibraryFunction( mask );
        return null;
    }

    @Override
    public Void visit(PluginConfiguration plugin) {
        Model.getInstance().getLibrary().addPluginConfiguration( plugin );
        return null;
    }

    @Override
    public Void visit(SystemModel system) {
        Model.getInstance().getLibrary().addSystem( system );
        return null;
    }

	@Override
	public Void visit(T_ReceiverModel t_receiver) {
		// TODO Auto-generated method stub
		 Model.getInstance().getLibrary().addTReceiver( t_receiver );
	        
		return null;
	}

	
}
