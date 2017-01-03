package org.seamcat.interfaces;

import org.seamcat.cdma.CDMALinkLevelData;
import org.seamcat.function.BlockingMaskImpl;
import org.seamcat.function.EmissionMaskImpl;
import org.seamcat.model.systems.SystemModel;
import org.seamcat.model.systems.generic.ReceiverModel;
import org.seamcat.model.systems.generic.T_ReceiverModel;
import org.seamcat.model.systems.generic.TransmitterModel;
import org.seamcat.model.types.LibraryItem;
import org.seamcat.plugin.PluginConfiguration;

public class Dispatcher {

    public static <T> T dispatch( LibraryVisitor<T> visitor, LibraryItem item ) {
        if ( item instanceof ReceiverModel) {
            return visitor.visit((ReceiverModel) item);
            
        } else if ( item instanceof TransmitterModel ) {
            return visitor.visit((ReceiverModel) item);
            
        }else if ( item instanceof T_ReceiverModel ) {
                return visitor.visit((T_ReceiverModel) item);
                
        } else if ( item instanceof EmissionMaskImpl) {
            return visitor.visit((EmissionMaskImpl)item);
        } else if ( item instanceof BlockingMaskImpl) {
            return visitor.visit((BlockingMaskImpl)item);
        } else if ( item instanceof CDMALinkLevelData ) {
            return visitor.visit((CDMALinkLevelData)item);
        } else if ( item instanceof SystemModel) {
            return visitor.visit((SystemModel)item );
        } else if ( item instanceof PluginConfiguration) {
            return visitor.visit((PluginConfiguration)item );
        }

        throw new RuntimeException("Could not dispatch: "+ item);
    }
}
