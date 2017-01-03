package org.seamcat.marshalling.types;

import org.seamcat.marshalling.CoverageRadiusMarshaller;
import org.seamcat.marshalling.DOMHelper;
import org.seamcat.marshalling.LibraryFunctionMarshaller;
import org.seamcat.marshalling.PropagationModelMarshaller;
import org.seamcat.model.generic.ProxyHelper;
import org.seamcat.model.systems.UIPosition;
import org.seamcat.model.systems.UITab;
import org.seamcat.model.systems.cdma.SystemModelCDMADownLink;
import org.seamcat.model.systems.cdma.SystemModelCDMAUpLink;
import org.seamcat.model.systems.generic.SystemModelGeneric;
import org.seamcat.model.systems.ofdma.SystemModelOFDMADownLink;
import org.seamcat.model.systems.ofdma.SystemModelOFDMAUpLink;
import org.seamcat.model.types.AntennaGain;
import org.seamcat.model.types.CoverageRadius;
import org.seamcat.model.types.PropagationModel;
import org.seamcat.plugin.AntennaGainConfiguration;
import org.seamcat.plugin.PluginConfiguration;
import org.seamcat.presentation.genericgui.panelbuilder.Cache;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

public class CompositeMarshaller {


    public static Element toElementSystem(Object t, Document doc) {
        if ( t instanceof SystemModelGeneric) {
            return toElement(SystemModelGeneric.class, t, doc);
        } 
        
        else if ( t instanceof SystemModelCDMAUpLink) {
            return toElement(SystemModelCDMAUpLink.class, t, doc);
        } else if ( t instanceof SystemModelCDMADownLink) {
            return toElement(SystemModelCDMADownLink.class, t, doc);
        } else if ( t instanceof SystemModelOFDMAUpLink) {
            return toElement(SystemModelOFDMAUpLink.class, t, doc);
        } else if ( t instanceof SystemModelOFDMADownLink) {
            return toElement(SystemModelOFDMADownLink.class, t, doc);
        }

        throw new RuntimeException("unknown system type: " + t );
    }

    public static Element toElement(Class<?> compositeClass, Object composite, Document doc) {
        Element comp = doc.createElement("composite");
        comp.setAttribute("class", compositeClass.getName());

        for (Method method : compositeClass.getDeclaredMethods()) {
            Object invoke;
            try {
                invoke = method.invoke(composite);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("");
            }
            Element methodElm = doc.createElement(method.getName());
            if ( method.getAnnotation(UIPosition.class) != null ) {
                if ( invoke instanceof AntennaGainConfiguration ) {
                    methodElm.appendChild( LibraryFunctionMarshaller.toElement((PluginConfiguration) invoke, doc) );
                } else if ( invoke instanceof PropagationModel) {
                    methodElm.appendChild( PropagationModelMarshaller.toElement( doc, (PropagationModel) invoke) );
                } else if ( invoke instanceof CoverageRadius ) {
                    methodElm.appendChild(CoverageRadiusMarshaller.toElement(doc, (CoverageRadius) invoke));
                } else {
                    TypeMarshaller.toElement((Class<Object>) method.getReturnType(), doc, methodElm, invoke);
                }
            } else if (method.getAnnotation(UITab.class) != null) {
                methodElm.appendChild(toElement(method.getReturnType(), invoke, doc));
            } else {
                throw new RuntimeException("unknown method: " + method);
            }
            comp.appendChild( methodElm );
        }

        return comp;
    }

    public static <T> T fromElement(Class<T> clazz, Element element) {
        String subType = element.getAttribute("class");

        try {
            Map<Method, Object> values = new LinkedHashMap<>();
            Class<?> compositeClass = Class.forName(subType);
            for (Method method : Cache.ordered(compositeClass)) {
                Class<?> type = method.getReturnType();
                // needs to search for first element rooted at this node - otherwise
                // nested elements will be wrong
                Element methodElm = DOMHelper.firstChild( element, method.getName() );
                if ( method.getAnnotation(UIPosition.class) != null ) {
                    if ( AntennaGain.class.isAssignableFrom(type) ) {
                        values.put(method, LibraryFunctionMarshaller.fromPluginElement((Element) methodElm.getFirstChild()));
                    } else if ( PropagationModel.class.isAssignableFrom(type)) {
                        values.put(method, PropagationModelMarshaller.fromElement((Element) methodElm.getFirstChild()));
                    } else if ( CoverageRadius.class.isAssignableFrom(type) ) {
                        values.put(method, CoverageRadiusMarshaller.fromElement((Element) methodElm.getFirstChild()));
                    } else {
                        values.put(method, TypeMarshaller.fromElement(method.getReturnType(), methodElm));
                    }
                } else if (method.getAnnotation(UITab.class) != null) {
                    Element subComposite = DOMHelper.firstChild( methodElm, "composite");
                    values.put(method, fromElement(method.getReturnType(), subComposite));
                } else {
                    throw new RuntimeException("unknown method: " + method);
                }
            }

            return (T) ProxyHelper.proxy( compositeClass, values);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (RuntimeException ee ) {
            ee.printStackTrace();
        }

        return null;
    }
}
