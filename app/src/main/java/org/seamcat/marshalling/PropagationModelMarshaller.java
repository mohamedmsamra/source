package org.seamcat.marshalling;

import org.seamcat.model.factory.SeamcatFactory;
import org.seamcat.model.types.PropagationModel;
import org.seamcat.plugin.PluginConfiguration;
import org.seamcat.plugin.PropagationModelConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PropagationModelMarshaller {

    public static PropagationModelConfiguration fromElement( Element element ){
        PluginConfiguration configuration = LibraryFunctionMarshaller.fromPluginElement(element);
        if ( configuration instanceof PropagationModelConfiguration ) {
            return (PropagationModelConfiguration) configuration;
        } else {
            return SeamcatFactory.propagation().getHataSE21();
        }
    }

    public static Element toElement( Document doc, PropagationModel model ) {
        if ( model instanceof PropagationModelConfiguration ) {
            return LibraryFunctionMarshaller.toElement((PropagationModelConfiguration)model, doc);
        } else {
            throw new RuntimeException("Could not serialize propagation model: "+ model);
        }

    }
}
