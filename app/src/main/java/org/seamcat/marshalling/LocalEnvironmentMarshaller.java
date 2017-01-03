package org.seamcat.marshalling;

import org.seamcat.model.types.LocalEnvironment;
import org.seamcat.scenario.MutableLocalEnvironment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class LocalEnvironmentMarshaller {

    public static void toElement(Document doc, Element element, List<LocalEnvironment> localEnvironments) {
        Element local = doc.createElement("LocalEnvironments");
        for (LocalEnvironment env : localEnvironments) {
            Element elm = doc.createElement("LocalEnvironment");
            elm.setAttribute("environment", env.getEnvironment().name());
            elm.setAttribute("prop", Double.toString(env.getProbability()));
            elm.setAttribute("wallLoss", Double.toString(env.getWallLoss()));
            elm.setAttribute("stdDev", Double.toString(env.getWallLossStdDev()));
            local.appendChild( elm );
        }
        element.appendChild( local );
    }



    public static List<LocalEnvironment> fromElement( Element element) {
        List<LocalEnvironment> result = new ArrayList<LocalEnvironment>();
        NodeList env = element.getElementsByTagName("LocalEnvironments");
        if (env.getLength() > 0 && env.item(0).getChildNodes().getLength() > 0) {
            Element localEnvironmentsElement = (Element) env.item(0);
            for ( int i=0; i<localEnvironmentsElement.getChildNodes().getLength(); i++) {
                Element local = (Element) localEnvironmentsElement.getChildNodes().item(i);
                MutableLocalEnvironment loc = new MutableLocalEnvironment();
                loc.setEnvironment(LocalEnvironment.Environment.valueOf(local.getAttribute("environment")));
                loc.setProbability(Double.parseDouble(local.getAttribute("prop")));
                loc.setWallLoss(Double.parseDouble(local.getAttribute("wallLoss")));
                loc.setWallLossStdDev(Double.parseDouble(local.getAttribute("stdDev")));
                result.add( loc );
            }
        }
        return result;
    }
}
