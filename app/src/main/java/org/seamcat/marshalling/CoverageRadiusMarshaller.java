package org.seamcat.marshalling;

import org.seamcat.model.types.CoverageRadius;
import org.seamcat.plugin.CoverageRadiusConfiguration;
import org.seamcat.plugin.PluginConfiguration;
import org.seamcat.simulation.coverageradius.UserDefinedCoverageRadius;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CoverageRadiusMarshaller {

    public static CoverageRadiusConfiguration fromElement( Element element ){
        PluginConfiguration configuration = LibraryFunctionMarshaller.fromPluginElement(element);
        if ( configuration instanceof CoverageRadiusConfiguration) {
            return (CoverageRadiusConfiguration) configuration;
        } else {
            return CoverageRadiusConfiguration.coverage(UserDefinedCoverageRadius.class);
        }
    }

    public static Element toElement( Document doc, CoverageRadius model ) {
        if ( model instanceof CoverageRadiusConfiguration ) {
            CoverageRadiusConfiguration configuration = (CoverageRadiusConfiguration) model;
            return LibraryFunctionMarshaller.toElement(configuration, doc);
        } else {
            throw new RuntimeException("Could not serialize propagation model: "+ model);
        }

    }
}
