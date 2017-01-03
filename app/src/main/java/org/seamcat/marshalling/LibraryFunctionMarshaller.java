package org.seamcat.marshalling;

import org.seamcat.function.BlockingMaskImpl;
import org.seamcat.function.DiscreteFunction;
import org.seamcat.function.EmissionMaskImpl;
import org.seamcat.marshalling.types.TypeMarshaller;
import org.seamcat.model.PluginJarFiles;
import org.seamcat.model.types.result.DescriptionImpl;
import org.seamcat.plugin.*;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class LibraryFunctionMarshaller {

    public static Element toElement( EmissionMaskImpl model, Document doc) {
        Element element = doc.createElement("spectrum-emission-mask");
        element.setAttribute( "reference", model.description().name() );
        element.setAttribute( "description", model.description().description() );
        FunctionMarshaller.toElement(element, doc, model);
        return element;
    }

    public static EmissionMaskImpl semFromElement( Element item ) {
        return FunctionMarshaller.fromElement(item);
    }

    public static Element toElement( BlockingMaskImpl model, Document doc) {
        Element element = doc.createElement("blockingMask");
        element.setAttribute( "reference", model.description().name() );
        element.setAttribute( "description", model.description().description() );
        Element bfunctionElement = doc.createElement("function");
        bfunctionElement.appendChild(FunctionMarshaller.toElement(doc, model));
        element.appendChild(bfunctionElement);
        return element;
    }

    public static Element toElement( PluginConfiguration configuration, Document doc) {
        Element plugin = doc.createElement("pluginConfiguration");
        plugin.setAttribute("classname", configuration.getPluginClass().getName());
        plugin.setAttribute("location", configuration.getLocation().getJarId());
        plugin.setAttribute("name", configuration.description().name());
        plugin.setAttribute("description", configuration.getNotes());

        if ( configuration instanceof PropagationModelConfiguration) {
            boolean selected = ((PropagationModelConfiguration) configuration).isVariationSelected();
            plugin.setAttribute("variation", Boolean.toString( selected) );
        } else if ( configuration instanceof AntennaGainConfiguration ) {
            double peakGain = ((AntennaGainConfiguration) configuration).peakGain();
            plugin.setAttribute("peakGain", Double.toString(peakGain));
        }

        TypeMarshaller.toElement(configuration.getModelClass(), doc, plugin, configuration.getModel() );

        return plugin;
    }

    public static PluginConfiguration fromPluginElement(Element element) {
        String classname = element.getAttribute("classname");
        String location = element.getAttribute("location");
        PluginLocation pl = new PluginLocation(location, classname);

        JarConfigurationModel jar = PluginJarFiles.getJarConfiguration(pl.getJarId());
        PluginClass pc = jar.getPluginClass(classname);

        Class<?> modelClass = pc.getModelClass();
        Object model = TypeMarshaller.fromElement(modelClass, element);
        PluginConfiguration configuration = pc.configuration(model);
        if ( configuration instanceof PropagationModelConfiguration ) {
            ((PropagationModelConfiguration) configuration).setVariationSelected( Boolean.valueOf(element.getAttribute("variation")));
        } else if ( configuration instanceof AntennaGainConfiguration) {
            ((AntennaGainConfiguration)configuration).setPeakGain(Double.parseDouble(element.getAttribute("peakGain")));
        }
        configuration.setName(element.getAttribute("name"));
        configuration.setNotes(element.getAttribute("description"));

        return configuration;
    }

    public static Element toElement( JarConfigurationModel model, Document doc ) {
        Element element = doc.createElement("jar");
        element.setAttribute( "name", model.description().name());
        Element jarData = doc.createElement("jarData");
        jarData.appendChild(doc.createCDATASection(model.getJarData()));
        element.appendChild( jarData );
        return element;
    }

    public static JarConfigurationModel fromElement( Element element ) {
        NodeList jarData = element.getElementsByTagName("jarData");
        if ( jarData.getLength() > 0 ) {
            Element jar = (Element) jarData.item(0);
            CharacterData datasection = (CharacterData) jar.getFirstChild();
            return new JarConfigurationModel(datasection.getData(), element.getAttribute("name"));
        }
        throw new RuntimeException("Could not de-serialize jar configuration");
    }


    public static BlockingMaskImpl rbmFromElement(Element item) {

        DiscreteFunction f = FunctionMarshaller.fromFunctionElement( item );
        if (f == null) {
            throw new RuntimeException("Error loading Spectrum Emission Mask");
        }
        BlockingMaskImpl mask;
        if ( f.isConstant() ) {
            mask = new BlockingMaskImpl(f.getConstant());
        } else {
            mask = new BlockingMaskImpl(f.points());
        }
        mask.setDescription(new DescriptionImpl(item.getAttribute("reference"),item.getAttribute("description")));
        return mask;
    }
}
